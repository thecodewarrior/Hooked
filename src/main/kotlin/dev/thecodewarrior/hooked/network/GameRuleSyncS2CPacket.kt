package dev.thecodewarrior.hooked.network

import dev.thecodewarrior.hooked.HookGameRules
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.network.packet.CustomPayload.Id
import net.minecraft.util.Identifier
import net.minecraft.world.GameRules


data class GameRuleSyncS2CPacket(
    val allowHooksWhileFlying: Boolean,
): CustomPayload {
    override fun getId(): Id<out CustomPayload> {
        return ID
    }

    fun applyTo(gameRules: GameRules) {
        gameRules.get(HookGameRules.ALLOW_HOOKS_WHILE_FLYING).set(this.allowHooksWhileFlying, null)
    }

    companion object {
        val GAME_RULE_SYNC_PACKET_ID = Identifier.of("hooked:gamerule_sync")
        val ID = Id<GameRuleSyncS2CPacket>(GAME_RULE_SYNC_PACKET_ID)
        val CODEC: PacketCodec<RegistryByteBuf, GameRuleSyncS2CPacket> = PacketCodec.tuple(
            PacketCodecs.BOOL, GameRuleSyncS2CPacket::allowHooksWhileFlying,
            ::GameRuleSyncS2CPacket
        )

        @JvmStatic
        fun from(gameRules: GameRules) = GameRuleSyncS2CPacket(
            gameRules.getBoolean(HookGameRules.ALLOW_HOOKS_WHILE_FLYING)
        )
    }
}