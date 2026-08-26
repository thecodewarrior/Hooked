package dev.thecodewarrior.hooked.hook

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.teamwizardry.librarianlib.math.minus
import com.teamwizardry.librarianlib.math.plus
import com.teamwizardry.librarianlib.math.times
import dev.ryanhcode.sable.companion.ClientSubLevelAccess
import dev.ryanhcode.sable.companion.SableCompanion
import dev.ryanhcode.sable.companion.SubLevelAccess
import dev.thecodewarrior.hooked.util.CustomCodecs
import dev.thecodewarrior.hooked.util.CustomPacketCodecs
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.sound.SoundEvent
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.asin
import kotlin.math.atan2

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
    /**
     * The (normalized) direction the hook is pointing
     */
    var direction: Vec3d,
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
     * Used when firing hooks on the client side to prevent them from rendering during the first tick.
     *
     * This is to fix the ender hook flashing in the middle of the screen when firing.
     */
    var firstTick: Boolean = false

    fun moveOutOfSubLevel(sublevel: SubLevelAccess?) {
        if (sublevel == null) return
        pos = sublevel.logicalPose().transformPosition(pos)
        posLastTick = sublevel.logicalPose().transformPosition(posLastTick)
        direction = sublevel.logicalPose().transformNormal(direction)
    }

    fun moveIntoSubLevel(sublevel: SubLevelAccess?) {
        if (sublevel == null) return
        pos = sublevel.logicalPose().transformPositionInverse(pos)
        posLastTick = sublevel.logicalPose().transformPositionInverse(posLastTick)
        direction = sublevel.logicalPose().transformNormalInverse(direction)
    }

    fun renderPose(sublevel: ClientSubLevelAccess?, tickDelta: Float): Pair<Vec3d, Vec3d> {
        var renderPos = posLastTick + (pos - posLastTick) * tickDelta
        var renderDirection = direction
        if (sublevel != null) {
            renderPos = sublevel.renderPose(tickDelta).transformPosition(renderPos)
            renderDirection = sublevel.renderPose(tickDelta).transformNormal(renderDirection)
        }
        return renderPos to renderDirection
    }

    enum class State(val key: String) {
        EXTENDING("Extending"), PLANTED("Planted"), RETRACTING("Retracting"), REMOVED("Removed");

        companion object {
            val CODEC = CustomCodecs.forEnum(entries, State::key)
            val PACKET_CODEC = CustomPacketCodecs.forEnum(entries)
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
                Vec3d.CODEC.fieldOf("Direction").forGetter(Hook::direction),
                State.CODEC.fieldOf("State").forGetter(Hook::state),
                BlockPos.CODEC.fieldOf("Block").forGetter(Hook::block),
                Codec.INT.fieldOf("Tag").forGetter(Hook::tag)
            ).apply(builder, ::Hook)
        }
        val LIST_CODEC = CODEC.listOf()

        val PACKET_CODEC: PacketCodec<RegistryByteBuf, Hook> = PacketCodec.of(
            { value, buffer ->
                PacketCodecs.VAR_INT.encode(buffer, value.id)
                PacketCodecs.FLOAT.encode(buffer, value.hookLength)
                CustomPacketCodecs.VEC3D.encode(buffer, value.pos)
                CustomPacketCodecs.VEC3D.encode(buffer, value.direction)
                State.PACKET_CODEC.encode(buffer, value.state)
                BlockPos.PACKET_CODEC.encode(buffer, value.block)
                PacketCodecs.VAR_INT.encode(buffer, value.tag)
            },
            { buffer ->
                Hook(
                    PacketCodecs.VAR_INT.decode(buffer),
                    PacketCodecs.FLOAT.decode(buffer),
                    CustomPacketCodecs.VEC3D.decode(buffer),
                    CustomPacketCodecs.VEC3D.decode(buffer),
                    State.PACKET_CODEC.decode(buffer),
                    BlockPos.PACKET_CODEC.decode(buffer),
                    PacketCodecs.VAR_INT.decode(buffer),
                )
            }
        )
    }
}