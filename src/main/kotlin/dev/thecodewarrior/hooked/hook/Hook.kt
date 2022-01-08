package dev.thecodewarrior.hooked.hook

import com.teamwizardry.librarianlib.math.plus
import com.teamwizardry.librarianlib.math.times
import net.minecraft.sound.SoundEvent
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

data class Hook(
    /**
     * The id of the hook. Assigned by the server. Temporary client-side hooks will have negative ids
     */
    val id: Int,
    /**
     * The type of the hook
     */
    val type: HookType,
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
        get() = pos + direction * type.hookLength

    /**
     * Used when firing hooks on the client side to prevent them from rendering during the first tick.
     *
     * This is to fix the ender hook flashing in the middle of the screen when firing.
     */
    var firstTick: Boolean = false

    enum class State {
        EXTENDING, PLANTED, RETRACTING, REMOVED
    }

    companion object {
        fun hitSound(world: World, pos: BlockPos): SoundEvent {
            return world.getBlockState(pos).soundGroup.hitSound
        }
    }
}