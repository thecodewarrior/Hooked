package dev.thecodewarrior.hooked.hook

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.teamwizardry.librarianlib.math.plus
import com.teamwizardry.librarianlib.math.times
import dev.thecodewarrior.hooked.hook.HookEvent.EventType
import dev.thecodewarrior.hooked.item.ChainAppearance
import dev.thecodewarrior.hooked.item.HookModelInfo
import dev.thecodewarrior.hooked.item.HookProperties
import dev.thecodewarrior.hooked.network.CustomCodecs
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.sound.SoundEvent
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

data class Hook(
    /**
     * The id of the hook. Assigned by the client.
     */
    val id: Int,
    /**
     * The length of the hook
     */
    val hookLength: Float,
    /**
     * The position of the tail of the hook
     */
    var pos: Vec3d,
    var pitch: Float,
    var yaw: Float,
    /**
     * The current state.
     */
    var state: State,
    /**
     * The block the hook is attached to. Should be (0,0,0) unless [state] is [State.PLANTED]
     */
    var block: BlockPos,
    /**
     * A controller-defined tag value
     */
    var tag: Int
) {
    /**
     * The position of the tail of the hook last tick
     */
    var posLastTick: Vec3d = pos

    /**
     * The (normalized) direction the hook is pointing
     */
    val direction: Vec3d
        get() = Vec3d.fromPolar(pitch, yaw)

    /**
     * The position of the tip of the hook, as computed from the pos and direction
     */
    val tipPos: Vec3d
        get() = pos + direction * hookLength

    /**
     * Used when firing hooks on the client side to prevent them from rendering during the first tick.
     *
     * This is to fix the ender hook flashing in the middle of the screen when firing.
     */
    var firstTick: Boolean = false

    enum class State(val key: String) {
        EXTENDING("Extending"), PLANTED("Planted"), RETRACTING("Retracting"), REMOVED("Removed");

        companion object {
            fun fromKey(key: String): State? {
                return State.entries.find { it.key == key }
            }

            val CODEC = Codec.STRING.comapFlatMap<State>(
                { key ->
                    fromKey(key)?.let { DataResult.success(it) }
                        ?: DataResult.error { "Invalid state '$key'" }
                },
                State::key
            )
            val PACKET_CODEC = PacketCodec.of<RegistryByteBuf, State>(
                { value, buffer -> buffer.writeVarInt(value.ordinal) },
                { buffer -> State.entries[buffer.readVarInt()] }
            )
        }
    }

    companion object {
        fun hitSound(world: World, pos: BlockPos): SoundEvent {
            return world.getBlockState(pos).soundGroup.hitSound
        }

        val CODEC = RecordCodecBuilder.create { builder ->
            builder.group(
                Codec.INT.fieldOf("Id").forGetter(Hook::id),
                Codec.FLOAT.fieldOf("HookLength").forGetter(Hook::hookLength),
                Vec3d.CODEC.fieldOf("Pos").forGetter(Hook::pos),
                Codec.FLOAT.fieldOf("Pitch").forGetter(Hook::pitch),
                Codec.FLOAT.fieldOf("Yaw").forGetter(Hook::yaw),
                State.CODEC.fieldOf("State").forGetter(Hook::state),
                BlockPos.CODEC.fieldOf("Block").forGetter(Hook::block),
                Codec.INT.fieldOf("Tag").forGetter(Hook::tag)
            ).apply(builder, ::Hook)
        }
        val LIST_CODEC = CODEC.listOf()

        val PACKET_CODEC: PacketCodec<RegistryByteBuf, Hook> = PacketCodec.of<RegistryByteBuf, Hook>(
            { value, buffer ->
                PacketCodecs.VAR_INT.encode(buffer, value.id)
                PacketCodecs.FLOAT.encode(buffer, value.hookLength)
                CustomCodecs.VEC3D.encode(buffer, value.pos)
                PacketCodecs.FLOAT.encode(buffer, value.pitch)
                PacketCodecs.FLOAT.encode(buffer, value.yaw)
                State.PACKET_CODEC.encode(buffer, value.state)
                BlockPos.PACKET_CODEC.encode(buffer, value.block)
                PacketCodecs.VAR_INT.encode(buffer, value.tag)
            },
            { buffer ->
                Hook(
                    PacketCodecs.VAR_INT.decode(buffer),
                    PacketCodecs.FLOAT.decode(buffer),
                    CustomCodecs.VEC3D.decode(buffer),
                    PacketCodecs.FLOAT.decode(buffer),
                    PacketCodecs.FLOAT.decode(buffer),
                    State.PACKET_CODEC.decode(buffer),
                    BlockPos.PACKET_CODEC.decode(buffer),
                    PacketCodecs.VAR_INT.decode(buffer),
                )
            }
        )
//        tuple(
//            ::Hook
//        )
    }
}