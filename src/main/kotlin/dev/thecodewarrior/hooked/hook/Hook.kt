package dev.thecodewarrior.hooked.hook

import com.teamwizardry.librarianlib.math.plus
import com.teamwizardry.librarianlib.math.times
import ll.dev.thecodewarrior.prism.annotation.Refract
import ll.dev.thecodewarrior.prism.annotation.RefractClass
import ll.dev.thecodewarrior.prism.annotation.RefractConstructor
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3d
import java.util.*

@RefractClass
data class Hook @RefractConstructor constructor(
    /**
     * The hook's unique ID
     */
    @Refract val uuid: UUID,
    /**
     * The type of the hook
     */
    @Refract val type: HookType,
    /**
     * The position of the tail of the hook
     */
    @Refract var pos: Vector3d,
    /**
     * The current state. It's an ordinal int for now, since Prism doesn't have enum support yet. Use [state] instead.
     */
    @Refract var _state: Int,
    /**
     * The (normalized) direction the hook is pointing
     */
    @Refract var direction: Vector3d,
    /**
     * The block the hook is attached to. Should be (0,0,0) unless [state] is [State.PLANTED]
     */
    @Refract var block: BlockPos,
) {
    var state: State
        get() = State.values()[_state]
        set(value) {
            _state = value.ordinal
        }
    /**
     * The position of the tail of the hook last tick
     */
    var posLastTick: Vector3d = pos

    /**
     * The position of the tip of the hook, as computed from the pos and direction
     */
    val tipPos: Vector3d
        get() = pos + direction * type.hookLength

    enum class State {
        EXTENDING, PLANTED, RETRACTING, REMOVED
    }
}