package dev.thecodewarrior.hooked.network

import dev.thecodewarrior.hooked.Hooked
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.network.packet.CustomPayload.Id
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d

data class FireHookC2SPacket(
    val pos: Vec3d,
    val pitch: Float,
    val yaw: Float,
    val sneaking: Boolean,
    val ids: List<Int>
) : CustomPayload {
    override fun getId(): Id<out CustomPayload> {
        return ID
    }

    companion object {
        val PACKET_ID = Identifier.of(Hooked.MOD_ID, "fire_hook")
        val ID = Id<FireHookC2SPacket>(PACKET_ID)
        val CODEC: PacketCodec<RegistryByteBuf, FireHookC2SPacket> = PacketCodec.tuple(
            CustomCodecs.VEC3D, FireHookC2SPacket::pos,
            PacketCodecs.FLOAT, FireHookC2SPacket::pitch,
            PacketCodecs.FLOAT, FireHookC2SPacket::yaw,
            PacketCodecs.BOOL, FireHookC2SPacket::sneaking,
            PacketCodecs.collection(::ArrayList, PacketCodecs.VAR_INT), FireHookC2SPacket::ids,
            ::FireHookC2SPacket
        )
    }
}
