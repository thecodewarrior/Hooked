package dev.thecodewarrior.hooked.network

import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.network.packet.CustomPayload.Id
import net.minecraft.util.Identifier

data class HookJumpC2SPacket(
    val doubleJump: Boolean,
    val sneaking: Boolean
) : CustomPayload {
    override fun getId(): Id<out CustomPayload> {
        return ID
    }

    companion object {
        val HOOK_JUMP_PACKET_ID = Identifier.of("hooked:hook_jump")
        val ID = Id<HookJumpC2SPacket>(HOOK_JUMP_PACKET_ID)
        val CODEC: PacketCodec<RegistryByteBuf, HookJumpC2SPacket> = PacketCodec.tuple(
            PacketCodecs.BOOL, HookJumpC2SPacket::doubleJump,
            PacketCodecs.BOOL, HookJumpC2SPacket::sneaking,
            ::HookJumpC2SPacket
        )
    }
}
