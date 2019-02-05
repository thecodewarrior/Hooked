package thecodewarrior.hooked.common.hook

import com.teamwizardry.librarianlib.features.helpers.vec
import com.teamwizardry.librarianlib.features.kotlin.clamp
import com.teamwizardry.librarianlib.features.kotlin.div
import com.teamwizardry.librarianlib.features.kotlin.dot
import com.teamwizardry.librarianlib.features.kotlin.minus
import com.teamwizardry.librarianlib.features.kotlin.plus
import com.teamwizardry.librarianlib.features.kotlin.times
import com.teamwizardry.librarianlib.features.saving.Save
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.Vec3d
import thecodewarrior.hooked.common.util.clampLength
import thecodewarrior.hooked.common.util.collinear
import thecodewarrior.hooked.common.util.raySphereIntersection
import kotlin.math.max
import kotlin.math.min

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
    var jumpTimer = 0
    override fun moveBy(offset: Vec3d) {
        if(plantedHooks.isEmpty()) return
        if(plantedHooks.size == 1) {
            tetherLength = (tetherLength - offset.y).clamp(0.0, range-0.5)
            return
        }
//        player.setPosition(player.posX + offset.x, player.posY + offset.y, player.posZ + offset.z)
        val newOffset = constrainPos(getWaistPos(player), offset)
        player.motionX = newOffset.x
        player.motionY = newOffset.y
        player.motionZ = newOffset.z
//        if(offset.x == 0.0 && offset.z == 0.0) {
//            player.motionX = 0.0
//            player.motionZ = 0.0
//        }
//        if(offset.y == 0.0) {
//            player.motionY = 0.0
//        }
    }

    fun constrainPos(waist: Vec3d, offset: Vec3d): Vec3d {
        var pos = waist + offset

        val points = plantedHooks.map { it.pos }.toSet().toList()
        val collinear = points.collinear(0.50)
        if(collinear != null) pos = constrainLine(pos, collinear.first, collinear.second)

        plantedHooks.forEach { hook ->
            pos = hook.pos + (pos - hook.pos).clampLength(this.range-0.5)
        }

        return (pos - waist).clampLength(1.0)
    }

    fun constrainLine(pos: Vec3d, a: Vec3d, b: Vec3d): Vec3d {
        val aToB = (b - a)
        val aToBLen = aToB.length()
        val aToBNorm = aToB / aToBLen
        val aToPos = pos - a

        val dot = aToPos dot aToBNorm
        if(dot < 0) return a
        if(dot > aToBLen) return b

        return a + aToBNorm * dot
    }

    fun constrainTri(pos: Vec3d): Vec3d {
        return pos
    }

    override fun tick() {
        jumpTimer++
        super.tick()
        player.setNoGravity(plantedHooks.size > 1)
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
//        pos = hook.pos + (pos - hook.pos).clampLength(tetherLength)
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