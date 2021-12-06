package dev.thecodewarrior.hooked.hook

import com.teamwizardry.librarianlib.math.plus
import com.teamwizardry.librarianlib.math.times
import ll.dev.thecodewarrior.prism.annotation.Refract
import ll.dev.thecodewarrior.prism.annotation.RefractClass
import ll.dev.thecodewarrior.prism.annotation.RefractConstructor
import net.minecraft.sound.SoundEvent
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.*

@RefractClass
data class Hook @RefractConstructor constructor(
    /**
     * The hook's unique ID
     */
    @Refract("uuid") val uuid: UUID,
    /**
     * The type of the hook
     */
    @Refract("type") val type: HookType,
    /**
     * The position of the tail of the hook
     */
    @Refract("pos") var pos: Vec3d,
    /**
     * The current state.
     */
    @Refract("state") var state: State,
    /**
     * The (normalized) direction the hook is pointing
     */
    @Refract("direction") var direction: Vec3d,
    /**
     * The block the hook is attached to. Should be (0,0,0) unless [state] is [State.PLANTED]
     */
    @Refract("block") var block: BlockPos,
    /**
     * A controller-defined tag value
     */
    @Refract("tag") var tag: Int
) {
    /**
     * The position of the tail of the hook last tick
     */
    var posLastTick: Vec3d = pos

    /**
     * The position of the tip of the hook, as computed from the pos and direction
     */
    val tipPos: Vec3d
        get() = pos + direction * type.hookLength

    enum class State {
        EXTENDING, PLANTED, RETRACTING, REMOVED
    }

    companion object {
        fun hitSound(world: World, pos: BlockPos): SoundEvent {
            return world.getBlockState(pos).soundGroup.hitSound
        }
    }
}