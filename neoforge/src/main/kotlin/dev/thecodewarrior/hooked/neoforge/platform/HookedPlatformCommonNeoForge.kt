package dev.thecodewarrior.hooked.neoforge.platform

import com.google.auto.service.AutoService
import dev.architectury.networking.NetworkManager
import dev.architectury.networking.transformers.PacketSink
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.item.HookProperties
import dev.thecodewarrior.hooked.neoforge.HookItemNeoForge
import dev.thecodewarrior.hooked.neoforge.HookedNeoForgeCommon
import dev.thecodewarrior.hooked.platform.HookedPlatformCommon
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.network.packet.CustomPayload
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerChunkManager
import net.minecraft.world.GameRules
import top.theillusivec4.curios.api.CuriosApi
import kotlin.jvm.optionals.getOrNull


@AutoService(HookedPlatformCommon::class)
class HookedPlatformCommonNeoForge : HookedPlatformCommon {
    override fun createHookItem(settings: Item.Settings): Item {
        return HookItemNeoForge(settings)
    }

    override fun <T : GameRules.Rule<T>> registerGameRule(
        name: String,
        category: GameRules.Category,
        type: GameRules.Type<T>
    ): GameRules.Key<T> {
        return GameRules.register(name, category, type)
    }

    override fun createBooleanGameRule(
        defaultValue: Boolean,
        changedCallback: (MinecraftServer, GameRules.BooleanRule) -> Unit
    ): GameRules.Type<GameRules.BooleanRule> {
        return GameRules.BooleanRule.create(defaultValue, changedCallback)
    }

    override fun getEquippedHook(player: PlayerEntity): HookProperties? {
        val curiosInventory = CuriosApi.getCuriosInventory(player).getOrNull() ?: return null
        val slotHandler = curiosInventory.getStacksHandler("hooked_hook").getOrNull() ?: return null
        return (0 until slotHandler.stacks.slots).firstNotNullOfOrNull { i ->
            HookProperties.Companion.fromItemStack(slotHandler.stacks.getStackInSlot(i))
        }
    }

    override fun getHookedPlayerData(player: PlayerEntity): HookedPlayerData {
        return player.getData(HookedNeoForgeCommon.HOOK_DATA).data
    }

    override fun sendToPlayersTrackingEntity(target: Entity, packets: List<CustomPayload>) {
        val chunkManager = target.world.chunkManager as? ServerChunkManager ?: return
        for (packet in packets) {
            // Architectury does its own packet wrapping, so we can't use
            // `PacketDistributor.sendToPlayersTrackingEntity` directly
            NetworkManager.collectPackets(
                { chunkManager.sendToOtherNearbyPlayers(target, it) },
                NetworkManager.Side.S2C,
                packet,
                target.world.registryManager
            )
        }
    }
}