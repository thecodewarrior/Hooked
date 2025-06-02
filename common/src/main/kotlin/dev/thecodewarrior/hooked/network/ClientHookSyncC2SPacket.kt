package dev.thecodewarrior.hooked.network

import dev.thecodewarrior.hooked.Hooked
import dev.thecodewarrior.hooked.hook.Hook
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.network.packet.CustomPayload.Id
import net.minecraft.util.Identifier

class ClientHookSyncC2SPacket(
    val dirtyHooks: List<Hook>,
) : CustomPayload {
    override fun getId(): Id<out CustomPayload> {
        return ID
    }

    companion object {
        val PACKET_ID = Identifier.of(Hooked.MOD_ID, "client_hook_sync")
        val ID = Id<ClientHookSyncC2SPacket>(PACKET_ID)
        val CODEC: PacketCodec<RegistryByteBuf, ClientHookSyncC2SPacket> = PacketCodec.tuple(
            PacketCodecs.collection(::ArrayList, Hook.PACKET_CODEC), ClientHookSyncC2SPacket::dirtyHooks,
            ::ClientHookSyncC2SPacket
        )
    }
}
