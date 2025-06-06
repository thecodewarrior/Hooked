package dev.thecodewarrior.hooked.hook

import dev.thecodewarrior.hooked.util.CustomPacketCodecs
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs

data class HookEvent(
    val type: EventType,
    val id: Int,
    val data: Int,
) {

    enum class EventType {
        HIT, MISS, DISLODGE;

        companion object {
            val CODEC = CustomPacketCodecs.forEnum(entries)
        }
    }

    companion object {
        val CODEC: PacketCodec<RegistryByteBuf, HookEvent> = PacketCodec.tuple(
            EventType.CODEC, HookEvent::type,
            PacketCodecs.VAR_INT, HookEvent::id,
            PacketCodecs.VAR_INT, HookEvent::data,
            ::HookEvent
        )
    }
}