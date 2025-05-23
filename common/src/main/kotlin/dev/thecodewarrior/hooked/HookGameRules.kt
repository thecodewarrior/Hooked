package dev.thecodewarrior.hooked

import dev.architectury.networking.NetworkManager
import dev.thecodewarrior.hooked.network.GameRuleSyncS2CPacket
import dev.thecodewarrior.hooked.platform.HookedPlatformCommon
import net.minecraft.server.MinecraftServer
import net.minecraft.world.GameRules

object HookGameRules {
    @JvmField
    val ALLOW_HOOKS_WHILE_FLYING = HookedPlatformCommon.instance.registerGameRule(
        "allowHooksWhileFlying",
        GameRules.Category.PLAYER,
        HookedPlatformCommon.instance.createBooleanGameRule(true, ::syncGameRules)
    )

    /**
     * This is split into a separate function to avoid recursive type checking issues with
     * `GameRuleSyncS2CPacket.from`
     */
    private fun syncGameRules(server: MinecraftServer, gameRule: GameRules.Rule<*>) {
        NetworkManager.sendToPlayers(server.playerManager.playerList, GameRuleSyncS2CPacket.from(server.gameRules))
    }
}