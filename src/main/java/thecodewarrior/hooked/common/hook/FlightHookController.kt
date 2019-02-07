package thecodewarrior.hooked.common.hook

import com.teamwizardry.librarianlib.features.helpers.vec
import com.teamwizardry.librarianlib.features.kotlin.clamp
import com.teamwizardry.librarianlib.features.kotlin.minus
import com.teamwizardry.librarianlib.features.kotlin.plus
import com.teamwizardry.librarianlib.features.kotlin.times
import com.teamwizardry.librarianlib.features.saving.Save
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.Vec3d
import thecodewarrior.hooked.common.util.DynamicHull
import thecodewarrior.hooked.common.util.clampLength
import thecodewarrior.hooked.common.util.raySphereIntersection
import kotlin.math.min

class FlightHookController(
    type: HookType,
    player: EntityPlayer,
    fullCount: Int,
    range: Double,
    speed: Double,
    pullStrength: Double,
    hookLength: Double,
    jumpBoost: Double
): HookController(type, player, fullCount, range, speed, pullStrength, hookLength, jumpBoost) {
    @Save
    var tetherLength = -1.0
    var tetherHookPos = Vec3d.ZERO

    val volume = DynamicHull()
    override var targetPoint: Vec3d?
        get() = if(tetherLength == 0.0) tetherHookPos else null
        set(value) {}

    override fun moveBy(offset: Vec3d) {
        if(plantedHooks.size <= 1) return
        val newOffset = constrainPos(getWaistPos(player), offset)
        if(player.positionVector + newOffset == Vec3d.ZERO)
            return
        player.motionX = newOffset.x
        player.motionY = newOffset.y
        player.motionZ = newOffset.z
    }

    fun constrainPos(waist: Vec3d, offset: Vec3d): Vec3d {
        var pos = waist + offset

        volume.update(plantedHooks.map { it.pos })
        pos = volume.constrain(pos)
        plantedHooks.forEach { hook ->
            pos = hook.pos + (pos - hook.pos).clampLength(this.range-0.5)
        }

        return (pos - waist).clampLength(pullStrength)
    }

    override fun updateTargetPoint() {
    }

    override fun modifyBreakSpeed(speed: Float): Float {
        if (plantedHooks.isEmpty()) {
            return speed
        } else {
            return speed * 5
        }
    }

    override fun tick() {
        super.tick()
        player.setNoGravity(plantedHooks.size > 1)

        if(plantedHooks.size > 1) {
            val offset = constrainPos(getWaistPos(player), vec(player.motionX, player.motionY, player.motionZ))
            player.motionX = offset.x
            player.motionY = offset.y
            player.motionZ = offset.z
        } else {
            volume.update(emptyList())
        }

        if(plantedHooks.size != 1) {
            tetherLength = -1.0
        } else if(tetherLength < 0 || tetherHookPos != plantedHooks[0].pos) {
            tetherHookPos = plantedHooks[0].pos
            tetherLength = plantedHooks[0].pos.distanceTo(getWaistPos(player))
        }
        if(tetherLength > 0)
            tetherPlayer()
    }

    override fun remove() {
        super.remove()
        player.setNoGravity(false)
    }

    override fun playerJump(count: Int) {
        if(count == 1 && tetherLength > 0)
            performSimpleJump()
        else if(count == 2 && tetherLength > 0)
            tetherLength = 0.0
        else
            super.playerJump(count - 1)
    }

    fun tetherPlayer() {
        val waist = getWaistPos(player)
        var pos = waist + vec(player.motionX, player.motionY, player.motionZ)

        val hook = plantedHooks[0]
        val length = (pos-hook.pos).length()
        if(length > 1.0 && length < tetherLength)
            return
        if(tetherLength == 0.0) {
            pos = hook.pos
        } else {
            pos = hook.pos + (pos - hook.pos).clampLength(tetherLength)
        }

        var delta = (pos - waist).clampLength(pullStrength)
        delta = vec(
            if(!delta.x.isFinite()) 0.0 else delta.x,
            if(!delta.y.isFinite()) 0.0 else delta.y,
            if(!delta.z.isFinite()) 0.0 else delta.z
        )

        player.motionX = delta.x
        player.motionY = delta.y
        player.motionZ = delta.z
    }
}