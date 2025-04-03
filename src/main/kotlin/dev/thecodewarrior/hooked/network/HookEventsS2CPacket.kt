package dev.thecodewarrior.hooked.network

import dev.thecodewarrior.hooked.hook.HookEvent
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.network.packet.CustomPayload.Id
import net.minecraft.util.Identifier

/**
 * Triggers hook events on the client. If the client has recently triggered an identical event, the event is ignored.
 */
data class HookEventsS2CPacket(
    val entityId: Int,
    val events: List<HookEvent>,
) : CustomPayload {
    override fun getId(): Id<out CustomPayload> {
        return ID
    }

    companion object {
        val HOOK_EVENTS_PACKET_ID = Identifier.of("hooked:hook_events")
        val ID = Id<HookEventsS2CPacket>(HOOK_EVENTS_PACKET_ID)
        val CODEC: PacketCodec<RegistryByteBuf, HookEventsS2CPacket> = PacketCodec.tuple(
            PacketCodecs.VAR_INT, HookEventsS2CPacket::entityId,
            PacketCodecs.collection(::ArrayList, HookEvent.CODEC), HookEventsS2CPacket::events,
            ::HookEventsS2CPacket
        )
    }
}
