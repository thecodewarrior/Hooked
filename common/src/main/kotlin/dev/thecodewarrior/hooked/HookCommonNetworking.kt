package dev.thecodewarrior.hooked

import dev.architectury.networking.NetworkManager
import dev.thecodewarrior.hooked.bridge.hookData
import dev.thecodewarrior.hooked.hook.Hook
import dev.thecodewarrior.hooked.hook.ServerHookProcessor
import dev.thecodewarrior.hooked.network.ClientHookSyncC2SPacket
import dev.thecodewarrior.hooked.network.HookJumpC2SPacket
import net.minecraft.server.network.ServerPlayerEntity

object HookCommonNetworking {
    private val logger = Hooked.logManager.makeLogger<HookCommonNetworking>()

    fun registerNetworking() {
        NetworkManager.registerReceiver(
            NetworkManager.Side.C2S,
            ClientHookSyncC2SPacket.ID,
            ClientHookSyncC2SPacket.CODEC
        ) { payload, context ->
            processClientHookSyncPacket(payload, context.player as ServerPlayerEntity)
        }

        NetworkManager.registerReceiver(
            NetworkManager.Side.C2S,
            HookJumpC2SPacket.ID,
            HookJumpC2SPacket.CODEC
        ) { payload, context ->
            processHookJumpPacket(payload, context.player as ServerPlayerEntity)
        }
    }

    private fun processClientHookSyncPacket(packet: ClientHookSyncC2SPacket, player: ServerPlayerEntity) {
        val hookedPlayerData = player.hookData()

        for(hook in packet.dirtyHooks) {
            hookedPlayerData.syncStatus.syncToOthers(hook)
            if (hook.state == Hook.State.REMOVED) {
                hookedPlayerData.hooks.remove(hook.id)
            } else {
                hookedPlayerData.hooks[hook.id] = hook
            }
        }
    }

    private fun processHookJumpPacket(packet: HookJumpC2SPacket, player: ServerPlayerEntity) {
        ServerHookProcessor.jump(player.hookData(), packet.doubleJump, packet.sneaking)
    }
}