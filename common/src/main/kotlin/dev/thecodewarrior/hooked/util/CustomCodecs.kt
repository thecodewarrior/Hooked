package dev.thecodewarrior.hooked.util

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.util.math.Vec3d
import kotlin.enums.EnumEntries

object CustomPacketCodecs {
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

    fun <T : Enum<T>> forEnum(entries: EnumEntries<T>): PacketCodec<PacketByteBuf, T> {
        return PacketCodec.of<PacketByteBuf, T>(
            { value, buffer -> buffer.writeVarInt(value.ordinal) },
            { buffer -> entries[buffer.readVarInt()] }
        )
    }
}

object CustomCodecs {
    fun <T : Enum<T>> forEnum(entries: EnumEntries<T>, getKey: (T) -> String): Codec<T> {
        val keyMap = entries.associateBy(getKey)
        return Codec.STRING.comapFlatMap(
            { key ->
                keyMap[key]?.let { DataResult.success(it) }
                    ?: DataResult.error { "Invalid enum entry '$key'" }
            },
            getKey
        )
    }
}