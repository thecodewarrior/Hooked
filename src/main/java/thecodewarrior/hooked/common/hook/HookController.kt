package thecodewarrior.hooked.common.hook

import com.teamwizardry.librarianlib.core.client.ClientTickHandler
import com.teamwizardry.librarianlib.features.helpers.vec
import com.teamwizardry.librarianlib.features.kotlin.*
import com.teamwizardry.librarianlib.features.methodhandles.MethodHandleHelper
import com.teamwizardry.librarianlib.features.saving.*
import com.teamwizardry.librarianlib.features.utilities.RaycastUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import thecodewarrior.hooked.HookedMod
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * A type of hook
 */
@SaveInPlace
abstract class HookController(
        /**
         * The type that created this controller
         */
        val type: HookType,
        /**
         * The player this controller is bound to
         */
        val player: EntityPlayer,
        /**
         * The number of simultaneous hooks allowed
         */
        var count: Int,
        /**
         * The maximum range from impact point to player
         */
        var range: Double,
        /**
         * The speed of the fired hooks in m/t
         */
        var speed: Double,
        /**
         * The speed the player is pulled toward the target point in m/t
         */
        var pullStrength: Double,
        /**
         * The distance from the impact point to where the chain should attach
         */
        var hookLength: Double
) {
    private val retractDistSq = (range+1/16.0).pow(2)
    var dirty: Boolean = true
        internal set
    fun markDirty() {
        dirty = true
    }

    /**
     * The hooks currently in flight away from the player. Custom controllers don't generally have to worry about these
     */
    @Save
    @NonPersistent
    val extendingHooks: MutableList<HookInFlight> = mutableListOf()

    /**
     * The hooks currently being retracted toward the player. Custom controllers don't generally have to worry about these
     */
    @Save
    @NonPersistent
    val retractingHooks: MutableList<HookInFlight> = mutableListOf()

    /**
     * The hooks currently planted in blocks.
     */
    @Save
    val plantedHooks: LinkedList<Hook> = LinkedList()

    /**
     * The point the player will move toward or null if the player's motion should be unaffected
     */
    var targetPoint: Vec3d? = null

    /**
     * Updates [targetPoint] based on the current state
     */
    abstract fun updateTargetPoint()

    /**
     * Adjusts the break speed of the player
     */
    abstract fun modifyBreakSpeed(speed: Float): Float

    open fun preTick() {}
    open fun postTick() {}

    open fun getSpecificHookToRetract(): Hook? {
        val look = player.getLook(ClientTickHandler.partialTicks)
        val eye = player.getPositionEyes(ClientTickHandler.partialTicks)
        // the dot of two normalized vectors is cos(theta) where theta is the angle between them
        // I find the max because cos(theta) increases as theta approaches 0
        // and instead of a bunch of inverse cosines, I can check the cosine itself.
        val found = plantedHooks.map {
            it to Math.max(
                    (it.pos - eye).normalize() dot look,
                    ((it.pos + it.direction * hookLength) - eye).normalize() dot look
            )
        }.maxBy { it.second } ?: return null
        if(found.second < Math.cos(Math.toRadians(10.0))) return null
        return found.first
    }

    open fun fireHook(startPos: Vec3d, normal: Vec3d, uuid: UUID) {
        extendingHooks.add(HookInFlight(startPos, normal, uuid))
    }

    open fun releaseSpecificHook(uuid: UUID) {

        val extendIterator = extendingHooks.iterator()
        for (hook in extendIterator) {
            if (hook.uuid == uuid) {
                extendIterator.remove()
                retractingHooks.add(hook)
            }
        }

        val plantedIterator = plantedHooks.iterator()
        for(hook in plantedIterator) {
            if (hook.uuid == uuid) {
                plantedIterator.remove()
                retractingHooks.add(HookInFlight(hook.pos, hook.direction, hook.uuid))
            }
        }
    }

    open fun playerJump() {
        if(plantedHooks.isNotEmpty()) {
            performSimpleJump()
        }
    }

    protected fun performSimpleJump() {
        player.motionX *= 1.25
        player.motionY *= 1.25
        player.motionZ *= 1.25
        player.jump()
        player.motionY = max(player.motionY, 0.42 + 0.1) // 0.42 == vanilla jump speed
    }

    open fun tick() {
        preTick()

        updateInFlight()
        updatePlanted()
        updateRetracting()

        updateTargetPoint()
        updatePlayer()

        postTick()
    }

    private fun updateInFlight() {
        val iterator = extendingHooks.iterator()
        for (hook in iterator) {
            val tip = hook.pos + hook.direction*hookLength
            val distanceLeft = range - (hook.pos - getWaistPos(player)).lengthVector()
            if(distanceLeft < 1/16.0) {
                iterator.remove()
                markDirty()
                continue
            }

            val trace = RaycastUtils.raycast(
                    player.world,
                    tip,
                    tip + hook.direction * min(speed, distanceLeft)
            )
            if (trace == null || trace.typeOfHit == RayTraceResult.Type.MISS) {
                hook.pos += hook.direction * min(speed, distanceLeft)
            } else {
                iterator.remove()
                val planted = Hook(
                        trace.hitVec - hook.direction * hookLength,
                        hook.direction, trace.blockPos, trace.sideHit,
                        hook.uuid
                )
                plantedHooks.addFirst(planted)
                markDirty()
            }
        }
    }

    private fun updatePlanted() {
        val iterator = plantedHooks.iterator()
        for(hook in iterator) {
            if (
                    hook.pos.squareDistanceTo(getWaistPos(player)) > retractDistSq ||
                    player.world.isAirBlock(hook.block)
            ) {
                iterator.remove()
                val retracting = HookInFlight(hook.pos, hook.direction, hook.uuid)
                retractingHooks.add(retracting)
                markDirty()
            }
        }

        while(plantedHooks.size > count) {
            val hook = plantedHooks.removeLast()
            val retracting = HookInFlight(hook.pos, hook.direction, hook.uuid)
            retractingHooks.add(retracting)
            markDirty()
        }
    }

    private fun updateRetracting() {
        val iterator = retractingHooks.iterator()
        for (hook in iterator) {
            val hookBack = hook.pos - hook.direction * hookLength
            val direction = hookBack - getWaistPos(player)
            val distance = direction.lengthVector()

            if(distance < 1) {
                iterator.remove()
                markDirty()
                continue
            }

            hook.pos -= direction * min(speed, distance)
        }
    }

    private fun updatePlayer() {
        HookedMod.PROXY.setAutoJump(player, true) //TODO put in cap
        val targetPoint = targetPoint ?: return

        player.fallDistance = 0f
//            entity.onGround = true
        player.jumpTicks = 10
        HookedMod.PROXY.setAutoJump(player, false)
        val waist = getWaistPos(player)
        val deltaPos = targetPoint - waist
        val deltaLen = deltaPos.lengthVector()

        if (deltaLen < pullStrength*3) { // close enough that we should set to avoid oscillations
            player.motionX = deltaPos.x
            player.motionY = deltaPos.y
            player.motionZ = deltaPos.z
        } else {
            val pull = deltaPos * pullStrength / deltaLen

            player.motionX = applyPull(player.motionX, pull.x)
            player.motionY = applyPull(player.motionY, pull.y)
            player.motionZ = applyPull(player.motionZ, pull.z)
        }
    }

    private fun applyPull(entityMotion: Double, pull: Double): Double {
        val forceMultiplier = 0.5

        if (abs(entityMotion) < abs(pull)) {
            val adjusted = entityMotion + pull * forceMultiplier
            if (abs(adjusted) > abs(pull))
                return pull
            else
                return adjusted
        }

        return entityMotion
    }

    companion object {
        @JvmStatic
        fun getWaistPos(e: Entity): Vec3d {
            return e.positionVector + vec(0, e.eyeHeight / 2, 0)
        }

        private var EntityLivingBase.jumpTicks by MethodHandleHelper.delegateForReadWrite<EntityLivingBase, Int>(
                EntityLivingBase::class.java, "jumpTicks", "field_70773_bE")
    }
}
