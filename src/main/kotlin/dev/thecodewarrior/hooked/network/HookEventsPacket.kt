package dev.thecodewarrior.hooked.network

import com.teamwizardry.librarianlib.courier.CourierBuffer
import com.teamwizardry.librarianlib.courier.CourierPacket
import com.teamwizardry.librarianlib.courier.PacketType
import dev.thecodewarrior.hooked.hook.HookEvent
import ll.dev.thecodewarrior.prism.annotation.Refract
import ll.dev.thecodewarrior.prism.annotation.RefractClass
import ll.dev.thecodewarrior.prism.annotation.RefractConstructor
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import java.util.ArrayList

/**
 * Triggers hook events on the client. If the client has recently triggered an identical event, the event is ignored.
 */
data class HookEventsPacket(
    val entityId: Int,
    val events: List<HookEvent>,
)

class HookEventsPacketType(
    identifier: Identifier,
) : PacketType<HookEventsPacket>(identifier, HookEventsPacket::class.java) {
    override fun encode(packet: HookEventsPacket, buffer: CourierBuffer) {
        buffer.writeVarInt(packet.entityId)
        buffer.writeCollection(packet.events, ::writeEvent)
    }

    override fun decode(buffer: CourierBuffer): HookEventsPacket {
        return HookEventsPacket(buffer.readVarInt(), buffer.readCollection({ mutableListOf() }, ::readEvent))
    }

    private fun writeEvent(buf: PacketByteBuf, event: HookEvent) {
        buf.writeVarInt(event.type.ordinal)
        buf.writeVarInt(event.id)
        buf.writeVarInt(event.data)
    }
    private fun readEvent(buf: PacketByteBuf): HookEvent {
        return HookEvent(
            HookEvent.EventType.values()[buf.readVarInt()],
            buf.readVarInt(),
            buf.readVarInt()
        )
    }
}
