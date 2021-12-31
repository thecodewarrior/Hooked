package dev.thecodewarrior.hooked.network

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.network.PacketByteBuf

data class HookJumpPacket(
    val doubleJump: Boolean,
    val sneaking: Boolean
) {
    fun encode(): PacketByteBuf {
        val buffer = PacketByteBufs.create()
        buffer.writeBoolean(this.doubleJump)
        buffer.writeBoolean(this.sneaking)
        return buffer
    }

    companion object {
        fun decode(buffer: PacketByteBuf): HookJumpPacket {
            return HookJumpPacket(buffer.readBoolean(), buffer.readBoolean())
        }
    }
}
