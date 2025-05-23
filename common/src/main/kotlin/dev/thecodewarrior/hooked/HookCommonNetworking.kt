package dev.thecodewarrior.hooked

import dev.architectury.networking.NetworkManager
import dev.thecodewarrior.hooked.bridge.hookData
import dev.thecodewarrior.hooked.hook.ServerHookProcessor
import dev.thecodewarrior.hooked.network.FireHookC2SPacket
import dev.thecodewarrior.hooked.network.GameRuleSyncS2CPacket
import dev.thecodewarrior.hooked.network.HookEventsS2CPacket
import dev.thecodewarrior.hooked.network.HookJumpC2SPacket
import net.minecraft.server.network.ServerPlayerEntity
import kotlin.math.sqrt

object HookCommonNetworking {
    private val logger = Hooked.logManager.makeLogger<HookCommonNetworking>()

    fun registerNetworking() {
        NetworkManager.registerReceiver(
            NetworkManager.Side.C2S,
            FireHookC2SPacket.ID,
            FireHookC2SPacket.CODEC
        ) { payload, context ->
            processFireHookPacket(payload, context.player as ServerPlayerEntity)
        }

        NetworkManager.registerReceiver(
            NetworkManager.Side.C2S,
            HookJumpC2SPacket.ID,
            HookJumpC2SPacket.CODEC
        ) { payload, context ->
            processHookJumpPacket(payload, context.player as ServerPlayerEntity)
        }
    }

    /**
     * The tolerance for the starting position of hooks being fired. Because of the way Hooked is designed it has to
     * give the client a lot of leeway, but even that has limits.
     */
    val fireHookCheatLimit = 16

    private fun processFireHookPacket(packet: FireHookC2SPacket, player: ServerPlayerEntity) {
        val hookedPlayerData = player.hookData()
        val distanceSq = packet.pos.squaredDistanceTo(player.eyePos)
        if (distanceSq > fireHookCheatLimit * fireHookCheatLimit) {
            hookedPlayerData.syncStatus.forceFullSyncToClient = true
            logger.error("Player ${player.name} fired a hook from ${sqrt(distanceSq)} blocks away. The tolerance is $fireHookCheatLimit")
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