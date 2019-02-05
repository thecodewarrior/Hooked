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

class FlightHookController(
    /**
     * The type that created this controller
     */
    type: HookType,
    /**
     * The player this controller is bound to
     */
    player: EntityPlayer,
    /**
     * The number of simultaneous hooks allowed
     */
    fullCount: Int,
    /**
     * The maximum range from impact point to player
     */
    range: Double,
    /**
     * The speed of the fired hooks in m/t
     */
    speed: Double,
    /**
     * The distance from the impact point to where the chain should attach
     */
    hookLength: Double
): BasicHookController(type, player, fullCount, range, speed, 0.0, hookLength) {
    @Save
    var tetherLength = -1.0

    val volume = DynamicHull()

    var jumpTimer = 0

    override fun moveBy(offset: Vec3d) {
        if(plantedHooks.isEmpty()) return
        if(plantedHooks.size == 1) {
            tetherLength = (tetherLength - offset.y).clamp(0.0, range-0.5)
            return
        }
        val newOffset = constrainPos(getWaistPos(player), offset)
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

        return (pos - waist).clampLength(1.0)
    }

    override fun tick() {
        jumpTimer++
        super.tick()
        player.setNoGravity(plantedHooks.size > 1)

        if(plantedHooks.size > 1) {
            val offset = constrainPos(getWaistPos(player), vec(player.motionX, player.motionY, player.motionZ))
            player.motionX = offset.x
            player.motionY = offset.y
            player.motionZ = offset.z
        }

        if(plantedHooks.size != 1) {
            tetherLength = -1.0
        } else if(tetherLength < 0) {
            tetherLength = plantedHooks[0].pos.distanceTo(getWaistPos(player))
        }
        if(tetherLength >= 0)
            tetherPlayer()
    }

    fun tetherPlayer() {
        val waist = getWaistPos(player)
        var pos = waist + vec(player.motionX, player.motionY, player.motionZ)

        val hook = plantedHooks[0]
        if((pos-hook.pos).length() < tetherLength)
            return
        if(tetherLength == 0.0) {
            pos = hook.pos
        } else {
            pos += (hook.pos - waist) * (raySphereIntersection(pos, hook.pos - waist, hook.pos, tetherLength) ?: 0.0)
        }

        var delta = (pos - waist).clampLength(1.0)
        delta = vec(
            if(!delta.x.isFinite()) 0.0 else delta.x,
            if(!delta.y.isFinite()) 0.0 else delta.y,
            if(!delta.z.isFinite()) 0.0 else delta.z
        )

        player.motionX = delta.x
        player.motionY = delta.y
        player.motionZ = delta.z
    }

    override fun playerJump() {
        if(jumpTimer < 7)
            super.playerJump()
        else
            jumpTimer = 0
    }

}