package dev.thecodewarrior.hooked

import dev.thecodewarrior.hooked.network.GameRuleSyncS2CPacket
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.MinecraftServer
import net.minecraft.world.GameRules

object HookGameRules {
    @JvmField
    val ALLOW_HOOKS_WHILE_FLYING = GameRuleRegistry.register(
        "allowHooksWhileFlying",
        GameRules.Category.PLAYER,
        GameRuleFactory.createBooleanRule(true, ::syncGameRules)
    )

    /**
     * This is split into a separate function to avoid recursive type checking issues with
     * `GameRuleSyncS2CPacket.from`
     */
    private fun syncGameRules(server: MinecraftServer, gameRule: GameRules.Rule<*>) {
        server.playerManager.playerList.forEach { player ->
            ServerPlayNetworking.send(player, GameRuleSyncS2CPacket.from(server.gameRules))
        }
    }
}