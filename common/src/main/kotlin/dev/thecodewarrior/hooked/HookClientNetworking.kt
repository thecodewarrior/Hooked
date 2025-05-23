package dev.thecodewarrior.hooked

import dev.architectury.networking.NetworkManager
import dev.thecodewarrior.hooked.bridge.hookData
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.hook.ClientHookProcessor
import dev.thecodewarrior.hooked.hook.Hook
import dev.thecodewarrior.hooked.item.HookProperties
import dev.thecodewarrior.hooked.network.GameRuleSyncS2CPacket
import dev.thecodewarrior.hooked.network.HookEventsS2CPacket
import dev.thecodewarrior.hooked.network.HookedPlayerDataFullSyncS2CPacket
import dev.thecodewarrior.hooked.network.HookedPlayerDataPartialSyncS2CPacket
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.world.World
import java.util.TreeMap
import kotlin.collections.set

object HookClientNetworking {
    private val logger = Hooked.logManager.makeLogger<HookClientNetworking>()

    fun registerNetworking() {
        NetworkManager.registerReceiver(
            NetworkManager.Side.S2C,
            GameRuleSyncS2CPacket.ID,
            GameRuleSyncS2CPacket.CODEC
        ) { payload, context ->
            payload.applyTo(MinecraftClient.getInstance().world!!.gameRules)
        }

        NetworkManager.registerReceiver(
            NetworkManager.Side.S2C,
            HookEventsS2CPacket.ID,
            HookEventsS2CPacket.CODEC,
            ::processHookEventsPacket
        )

        NetworkManager.registerReceiver(
            NetworkManager.Side.S2C,
            HookedPlayerDataFullSyncS2CPacket.ID,
            HookedPlayerDataFullSyncS2CPacket.CODEC,
            ::processFullSyncPacket
        )

        NetworkManager.registerReceiver(
            NetworkManager.Side.S2C,
            HookedPlayerDataPartialSyncS2CPacket.ID,
            HookedPlayerDataPartialSyncS2CPacket.CODEC,
            ::processPartialSyncPacket
        )
    }

    private fun getHookDataByEntityId(entityId: Int): HookedPlayerData? {
        val world = MinecraftClient.getInstance().world!!
        val entity = world.getEntityById(entityId) ?: return null
        if (entity !is PlayerEntity) {
            logger.warn("Entity $entityId is not a player, so it has no HookedPlayerData")
            return null
        }
        return entity.hookData()
    }

    private fun processHookEventsPacket(packet: HookEventsS2CPacket, context: NetworkManager.PacketContext) {
        val data = getHookDataByEntityId(packet.entityId) ?: return
        packet.events.forEach {
            ClientHookProcessor.triggerServerEvent(data, it)
        }
    }

    private fun processFullSyncPacket(packet: HookedPlayerDataFullSyncS2CPacket, context: NetworkManager.PacketContext) {
        val data = getHookDataByEntityId(packet.entityId) ?: return

        data.properties = packet.properties

        val newHooks = packet.hooks.associateByTo(TreeMap()) { it.id }
        data.syncStatus.recentHooks.putAll(data.hooks.filterKeys { it !in newHooks })
        data.hooks = newHooks

        data.controller.readSyncState(packet.controllerState, packet.initial)
    }

    private fun processPartialSyncPacket(packet: HookedPlayerDataPartialSyncS2CPacket, context: NetworkManager.PacketContext) {
        val data = getHookDataByEntityId(packet.entityId) ?: return

        for(hook in packet.dirtyHooks) {
            if (hook.state == Hook.State.REMOVED) {
                data.hooks.remove(hook.id)
                data.syncStatus.addRecentHook(hook)
            } else {
                data.hooks[hook.id] = hook
            }
        }
    }
}