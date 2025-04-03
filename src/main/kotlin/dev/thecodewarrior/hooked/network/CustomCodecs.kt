package dev.thecodewarrior.hooked.network

import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.util.math.Vec3d

object CustomCodecs {
    val VEC3D = PacketCodec.of<PacketByteBuf, Vec3d>(
        { value, buffer ->
            buffer.writeDouble(value.x)
            buffer.writeDouble(value.y)
            buffer.writeDouble(value.z)
        },
        { buffer ->
            Vec3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble())
        }
    )
}