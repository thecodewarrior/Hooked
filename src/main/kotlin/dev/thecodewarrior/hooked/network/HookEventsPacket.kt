package dev.thecodewarrior.hooked.network

import dev.thecodewarrior.hooked.hook.HookEvent
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.network.PacketByteBuf

/**
 * Triggers hook events on the client. If the client has recently triggered an identical event, the event is ignored.
 */
data class HookEventsPacket(
    val entityId: Int,
    val events: List<HookEvent>,
) {
    fun encode(): PacketByteBuf {
        val buffer = PacketByteBufs.create()
        buffer.writeVarInt(this.entityId)
        buffer.writeCollection(this.events, ::writeEvent)
        return buffer
    }

    companion object {
        fun decode(buffer: PacketByteBuf): HookEventsPacket {
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
}
