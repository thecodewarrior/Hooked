package dev.thecodewarrior.hooked.network

import com.teamwizardry.librarianlib.courier.CourierBuffer
import com.teamwizardry.librarianlib.courier.PacketType
import net.minecraft.util.Identifier

data class HookJumpPacket(
    val doubleJump: Boolean,
    val sneaking: Boolean
)

class HookJumpPacketType(
    identifier: Identifier,
) : PacketType<HookJumpPacket>(identifier, HookJumpPacket::class.java) {
    override fun encode(packet: HookJumpPacket, buffer: CourierBuffer) {
        buffer.writeBoolean(packet.doubleJump)
        buffer.writeBoolean(packet.sneaking)
    }

    override fun decode(buffer: CourierBuffer): HookJumpPacket {
        return HookJumpPacket(buffer.readBoolean(), buffer.readBoolean())
    }
}
