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

class HookedPlayerDataFullSyncS2CPacket(
    val entityId: Int,
    val initial: Boolean,
    val properties: HookProperties,
    val hooks: List<Hook>,
    val controllerState: ByteArray,
) : CustomPayload {
    override fun getId(): Id<out CustomPayload> {
        return ID
    }

    companion object {
        val PACKET_ID = Identifier.of(Hooked.MOD_ID, "hook_sync_full")
        val ID = Id<HookedPlayerDataFullSyncS2CPacket>(PACKET_ID)
        val CODEC: PacketCodec<RegistryByteBuf, HookedPlayerDataFullSyncS2CPacket> = PacketCodec.tuple(
            PacketCodecs.VAR_INT, HookedPlayerDataFullSyncS2CPacket::entityId,
            PacketCodecs.BOOL, HookedPlayerDataFullSyncS2CPacket::initial,
            HookProperties.PACKET_CODEC, HookedPlayerDataFullSyncS2CPacket::properties,
            PacketCodecs.collection(::ArrayList, Hook.PACKET_CODEC), HookedPlayerDataFullSyncS2CPacket::hooks,
            PacketCodecs.BYTE_ARRAY, HookedPlayerDataFullSyncS2CPacket::controllerState,
            ::HookedPlayerDataFullSyncS2CPacket
        )
    }
}
