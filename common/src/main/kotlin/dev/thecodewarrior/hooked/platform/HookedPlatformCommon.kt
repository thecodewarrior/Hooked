package dev.thecodewarrior.hooked.platform

import com.teamwizardry.librarianlib.core.util.ServiceLoaderHelper
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.item.HookProperties
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.GameRules

interface HookedPlatformCommon {
    /* region == Registries and stuff == */
    fun createHookItem(settings: Item.Settings): Item

    fun <T : GameRules.Rule<T>> registerGameRule(
        name: String,
        category: GameRules.Category,
        type: GameRules.Type<T>
    ): GameRules.Key<T>

    fun createBooleanGameRule(
        defaultValue: Boolean,
        changedCallback: (MinecraftServer, GameRules.BooleanRule) -> Unit
    ): GameRules.Type<GameRules.BooleanRule>
    /* endregion == Registries and stuff == */

    /* region == Runtime stuff == */
    fun getEquippedHook(player: PlayerEntity): HookProperties?

    fun getHookedPlayerData(player: PlayerEntity): HookedPlayerData

    /**
     * Returns the players tracking the given entity. If the target is a player, the returned list will not include
     * that player.
     */
    fun getTrackingPlayers(target: Entity): List<ServerPlayerEntity>
    /* endregion == Runtime stuff == */

    companion object {
        @JvmStatic
        val instance by ServiceLoaderHelper.required<HookedPlatformCommon>()
    }
}