package dev.thecodewarrior.hooked

import dev.thecodewarrior.hooked.bridge.hookData
import dev.thecodewarrior.hooked.hook.ServerHookProcessor
import dev.thecodewarrior.hooked.network.*
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.network.ServerPlayerEntity
import kotlin.math.sqrt

object HookedCommon: ModInitializer {
    private val logger = Hooked.logManager.makeLogger<HookedCommon>()

    override fun onInitialize() {
        HookTypes.registerTypes()
        HookItems.registerItems()
        HookStats.registerStats()
        HookSounds.registerSounds()
        registerNetworking()
        ServerHookProcessor.registerEvents()
        HookGameRules // static initializer registers the gamerules
        HookTags // static initializer registers tags
    }

    private fun registerNetworking() {
        PayloadTypeRegistry.playS2C().register(GameRuleSyncS2CPacket.ID, GameRuleSyncS2CPacket.CODEC)
        PayloadTypeRegistry.playS2C().register(HookEventsS2CPacket.ID, HookEventsS2CPacket.CODEC)
        PayloadTypeRegistry.playC2S().register(FireHookC2SPacket.ID, FireHookC2SPacket.CODEC)
        PayloadTypeRegistry.playC2S().register(HookJumpC2SPacket.ID, HookJumpC2SPacket.CODEC)

        ServerPlayNetworking.registerGlobalReceiver(FireHookC2SPacket.ID) { payload, context ->
            processFireHookPacket(payload, context.player())
        }
        ServerPlayNetworking.registerGlobalReceiver(HookJumpC2SPacket.ID) { payload, context ->
            processHookJumpPacket(payload, context.player())
        }
    }

    private fun processFireHookPacket(packet: FireHookC2SPacket, player: ServerPlayerEntity) {
        val hookedPlayerData = player.hookData()
        val distanceSq = packet.pos.squaredDistanceTo(player.eyePos)
        val maxDistance = CheatMitigation.fireHookTolerance.getValue(player)
        if (distanceSq > maxDistance * maxDistance) {
            hookedPlayerData.syncStatus.forceFullSyncToClient = true
            logger.error(
                "Player ${player.name} fired a hook from ${sqrt(distanceSq)} blocks away. The tolerance " +
                        "based on their ping of ${player.pingMilliseconds} is $maxDistance"
            )
        } else {
            ServerHookProcessor.fireHook(
                player,
                hookedPlayerData,
                packet.pos,
                packet.pitch,
                packet.yaw,
                packet.sneaking,
                packet.ids
            )
        }
    }

    private fun processHookJumpPacket(packet: HookJumpC2SPacket, player: ServerPlayerEntity) {
        ServerHookProcessor.jump(player.hookData(), packet.doubleJump, packet.sneaking)
    }
}