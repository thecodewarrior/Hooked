package dev.thecodewarrior.hooked.network

import dev.thecodewarrior.hooked.Hooked
import dev.thecodewarrior.hooked.hook.Hook
import dev.thecodewarrior.hooked.hook.HookEvent
import dev.thecodewarrior.hooked.item.HookProperties
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.network.packet.CustomPayload.Id
import net.minecraft.util.Identifier

class HookedPlayerDataPartialSyncS2CPacket(
    val entityId: Int,
    val dirtyHooks: List<Hook>,
) : CustomPayload {
    override fun getId(): Id<out CustomPayload> {
        return ID
    }

    companion object {
        val PACKET_ID = Identifier.of(Hooked.MOD_ID, "hook_sync_partial")
        val ID = Id<HookedPlayerDataPartialSyncS2CPacket>(PACKET_ID)
        val CODEC: PacketCodec<RegistryByteBuf, HookedPlayerDataPartialSyncS2CPacket> = PacketCodec.tuple(
            PacketCodecs.VAR_INT, HookedPlayerDataPartialSyncS2CPacket::entityId,
            PacketCodecs.collection(::ArrayList, Hook.PACKET_CODEC), HookedPlayerDataPartialSyncS2CPacket::dirtyHooks,
            ::HookedPlayerDataPartialSyncS2CPacket
        )
    }
}
