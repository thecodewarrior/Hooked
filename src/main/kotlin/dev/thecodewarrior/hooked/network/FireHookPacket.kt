package dev.thecodewarrior.hooked.network

import com.teamwizardry.librarianlib.core.util.vec
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.Vec3d

data class FireHookPacket(
    val pos: Vec3d,
    val pitch: Float,
    val yaw: Float,
    val sneaking: Boolean,
    val ids: List<Int>
) {
    fun encode(): PacketByteBuf {
        val buffer = PacketByteBufs.create()
        buffer.writeDouble(this.pos.x)
        buffer.writeDouble(this.pos.y)
        buffer.writeDouble(this.pos.z)
        buffer.writeFloat(this.pitch)
        buffer.writeFloat(this.yaw)
        buffer.writeBoolean(this.sneaking)
        buffer.writeCollection(this.ids, PacketByteBuf::writeVarInt)
        return buffer
    }

    companion object {
        fun decode(buffer: PacketByteBuf): FireHookPacket {
            return FireHookPacket(
                vec(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                buffer.readFloat(), buffer.readFloat(),
                buffer.readBoolean(),
                buffer.readCollection({ mutableListOf() }, PacketByteBuf::readVarInt)
            )
        }
    }
}
