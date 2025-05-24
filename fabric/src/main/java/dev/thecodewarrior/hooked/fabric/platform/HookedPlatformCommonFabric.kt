package dev.thecodewarrior.hooked.fabric.platform

import com.google.auto.service.AutoService
import dev.architectury.networking.NetworkManager
import dev.emi.trinkets.api.TrinketsApi
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.fabric.HookItemFabric
import dev.thecodewarrior.hooked.fabric.HookedCardinalComponents
import dev.thecodewarrior.hooked.item.HookProperties
import dev.thecodewarrior.hooked.platform.HookedPlatformCommon
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.network.packet.CustomPayload
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.GameRules
import kotlin.jvm.optionals.getOrNull

@AutoService(HookedPlatformCommon::class)
class HookedPlatformCommonFabric : HookedPlatformCommon {
    override fun createHookItem(settings: Item.Settings): Item {
        return HookItemFabric(settings)
    }

    override fun <T : GameRules.Rule<T>> registerGameRule(
        name: String,
        category: GameRules.Category,
        type: GameRules.Type<T>
    ): GameRules.Key<T> {
        return GameRuleRegistry.register(name, category, type)
    }

    override fun createBooleanGameRule(
        defaultValue: Boolean,
        changedCallback: (MinecraftServer, GameRules.BooleanRule) -> Unit
    ): GameRules.Type<GameRules.BooleanRule> {
        return GameRuleFactory.createBooleanRule(defaultValue, changedCallback)
    }

    override fun getEquippedHook(player: PlayerEntity): HookProperties? {
        val component = TrinketsApi.getTrinketComponent(player).getOrNull() ?: return null
        return component.allEquipped.firstNotNullOfOrNull { slotPair ->
            slotPair.right?.let { HookProperties.fromItemStack(it) }
        }
    }

    override fun getHookedPlayerData(player: PlayerEntity): HookedPlayerData {
        return HookedCardinalComponents.HOOK_DATA.get(player).data
    }

    override fun sendToPlayersTrackingEntity(target: Entity, packets: List<CustomPayload>) {
        for (player in PlayerLookup.tracking(target)) {
            if (player != target) {
                for (packet in packets) {
                    NetworkManager.sendToPlayer(player, packet)
                }
            }
        }
    }
}