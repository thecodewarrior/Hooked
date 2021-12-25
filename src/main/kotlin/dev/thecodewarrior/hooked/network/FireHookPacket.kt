package dev.thecodewarrior.hooked.network

import com.teamwizardry.librarianlib.core.util.vec
import com.teamwizardry.librarianlib.courier.CourierBuffer
import com.teamwizardry.librarianlib.courier.PacketType
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d

data class FireHookPacket(
    val pos: Vec3d,
    val direction: Vec3d,
    val sneaking: Boolean,
    val ids: List<Int>
)

class FireHookPacketType(
    identifier: Identifier,
) : PacketType<FireHookPacket>(identifier, FireHookPacket::class.java) {
    override fun encode(packet: FireHookPacket, buffer: CourierBuffer) {
        buffer.writeDouble(packet.pos.x)
        buffer.writeDouble(packet.pos.y)
        buffer.writeDouble(packet.pos.z)
        buffer.writeDouble(packet.direction.x)
        buffer.writeDouble(packet.direction.y)
        buffer.writeDouble(packet.direction.z)
        buffer.writeBoolean(packet.sneaking)
        buffer.writeCollection(packet.ids, PacketByteBuf::writeVarInt)
    }

    override fun decode(buffer: CourierBuffer): FireHookPacket {
        return FireHookPacket(
            vec(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
            vec(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
            buffer.readBoolean(),
            buffer.readCollection({ mutableListOf() }, PacketByteBuf::readVarInt)
        )
    }
}
