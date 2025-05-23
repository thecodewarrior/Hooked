package dev.thecodewarrior.hooked

import dev.architectury.networking.NetworkManager
import dev.thecodewarrior.hooked.network.FireHookC2SPacket
import dev.thecodewarrior.hooked.network.GameRuleSyncS2CPacket
import dev.thecodewarrior.hooked.network.HookEventsS2CPacket
import dev.thecodewarrior.hooked.network.HookJumpC2SPacket
import dev.thecodewarrior.hooked.network.HookedPlayerDataFullSyncS2CPacket
import dev.thecodewarrior.hooked.network.HookedPlayerDataPartialSyncS2CPacket

object HookServerNetworking {

    fun registerNetworking() {
        NetworkManager.registerS2CPayloadType(GameRuleSyncS2CPacket.ID, GameRuleSyncS2CPacket.CODEC)
        NetworkManager.registerS2CPayloadType(HookEventsS2CPacket.ID, HookEventsS2CPacket.CODEC)
        NetworkManager.registerS2CPayloadType(
            HookedPlayerDataFullSyncS2CPacket.ID,
            HookedPlayerDataFullSyncS2CPacket.CODEC
        )
        NetworkManager.registerS2CPayloadType(
            HookedPlayerDataPartialSyncS2CPacket.ID,
            HookedPlayerDataPartialSyncS2CPacket.CODEC
        )
    }
}