package thecodewarrior.hooked.common.hook

import net.minecraft.util.math.Vec3d
import thecodewarrior.hooked.common.util.isFinite
import java.util.*

interface IHook {
    var pos: Vec3d
    var direction: Vec3d
    val uuid: UUID
    fun verify() {
        if(!pos.isFinite()) {
            throw IllegalStateException("Non-finite value not permitted. ($pos)")
        }
        if(!direction.isFinite()) {
            throw IllegalStateException("Non-finite value not permitted. ($pos)")
        }
    }
}