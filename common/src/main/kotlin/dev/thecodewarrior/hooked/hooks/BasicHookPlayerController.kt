package dev.thecodewarrior.hooked.hooks

import com.teamwizardry.librarianlib.core.util.vec
import com.teamwizardry.librarianlib.math.*
import dev.thecodewarrior.hooked.hook.Hook
import dev.thecodewarrior.hooked.hook.HookControllerDelegate
import dev.thecodewarrior.hooked.hook.HookPlayerController
import dev.thecodewarrior.hooked.util.JumpHeightUtil
import dev.thecodewarrior.hooked.util.actualMotion
import dev.thecodewarrior.hooked.util.fromWaistPos
import dev.thecodewarrior.hooked.util.getWaistPos
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import kotlin.math.acos
import kotlin.math.max
import kotlin.math.sqrt

open class BasicHookPlayerController(val player: PlayerEntity, val behavior: BasicHookBehavior): HookPlayerController() {
    override fun fireHooks(
        delegate: HookControllerDelegate,
        pos: Vec3d,
        pitch: Float,
        yaw: Float,
        sneaking: Boolean
    ) {
        if(delegate.cooldown <= 0) {
            val tag = if(sneaking) {
                0
            } else {
                (delegate.hooks.maxOfOrNull { it.tag } ?: 0) + 1
            }
            delegate.fireHook(pos, pitch, yaw) { it.tag = tag }
            delegate.triggerCooldown()
        }
    }

    override fun onHookHit(delegate: HookControllerDelegate, hook: Hook) {
        super.onHookHit(delegate, hook)
        if(hook.tag > 0) {
            if(delegate.hooks.any { it.tag > hook.tag }) { // a more recent "solo" hook exists, immediately retract
                delegate.retractHook(hook, true)
            } else {
                for(other in delegate.hooks) {
                    if(other !== hook && other.state == Hook.State.PLANTED && other.tag < hook.tag) {
                        delegate.retractHook(other)
                    }
                }
            }
        }
    }

    override fun jump(
        delegate: HookControllerDelegate,
        doubleJump: Boolean,
        sneaking: Boolean
    ) {
        if (delegate.hooks.any { it.state == Hook.State.PLANTED }) {
            performJump(delegate)
        }

        for(hook in delegate.hooks) {
            delegate.retractHook(hook)
        }
    }

    fun isStuck(delegate: HookControllerDelegate): Boolean {
        val delta = getTargetPoint(delegate.hooks) - player.getWaistPos()

        return delta == Vec3d.ZERO || acos(player.actualMotion.normalize() dot delta.normalize()) > Math.toRadians(80.0)
    }

    /**
     * Returns the bounding boxes of the jump targets. The returned list is sorted from highest to lowest
     * priority. That is, the first element should be used as the final jump target. Null if the player is in a state
     * where they aren't allowed to jump.
     */
    fun computeJumpTargets(
        delegate: HookControllerDelegate
    ): List<Box>? {
        val waist = player.getWaistPos()
        val targetPos = getTargetPoint(delegate.hooks)
        val deltaPos = targetPos - waist

        val boostAABB: Box
        when {
            isStuck(delegate) || player.actualMotion == Vec3d.ZERO -> {
                boostAABB = player.boundingBox
            }
            deltaPos.length() < behavior.pullStrength * 3 -> {
                boostAABB = player.boundingBox.offset(deltaPos) // the player's bounding box centered around the targetPos
            }
            else -> return null // not allowed to jump
        }

        return JumpHeightUtil.computeJumpTargetOffsets(player).map {
            boostAABB.offset(JumpHeightUtil.computeStepTarget(player, boostAABB, it, 2.5))
        }.sortedByDescending { it.minY }.filter { it.minY > player.y }
    }

    private fun performJump(
        delegate: HookControllerDelegate
    ) {
        val jumpTargets = this.computeJumpTargets(delegate)

        if(jumpTargets != null) {
            player.jump()

            if (jumpTargets.isNotEmpty()) {
                // the height relative to the player's current position
                val jumpHeight = jumpTargets.first().minY - player.y
                val gravity = max(0.0, player.finalGravity)
                val jumpVelocity = sqrt(2 * gravity * jumpHeight)

                if(jumpVelocity > player.velocity.y) {
                    player.velocity = vec(
                        player.velocity.x,
                        jumpVelocity,
                        player.velocity.z
                    )
                }
            }

            return
        }

        val waist = player.getWaistPos()
        val targetPos = getTargetPoint(delegate.hooks)
        val deltaPos = targetPos - waist
        // if we don't do anything special, just give them a bit of a boost
        player.velocity += deltaPos.normalize() * (behavior.pullStrength * 0.2)
    }

    override fun update(delegate: HookControllerDelegate) {
        if (delegate.hooks.none { it.state == Hook.State.PLANTED }) {
            return
        }

        player.fallDistance = 0f
        clearFlyingKickTimer(player)

        val targetPlayerPos = player.fromWaistPos(getTargetPoint(delegate.hooks))
        applyRestoringForce(player, targetPlayerPos, behavior.pullStrength)
        if(isStuck(delegate) || player.actualMotion == Vec3d.ZERO) {
            player.stopFallFlying()
        }
    }

    protected fun getTargetPoint(hooks: Collection<Hook>): Vec3d {
        var plantedCount = 0
        var targetPoint = Vec3d.ZERO
        hooks.forEach { hook ->
            if (hook.state == Hook.State.PLANTED) {
                targetPoint += hook.pos
                plantedCount++
            }
        }
        targetPoint /= plantedCount
        return targetPoint
    }

    companion object {

        private val boostTestRange = 1.0
        private val boostTestOffsets = listOf(
            vec(boostTestRange, 0, 0),
            vec(boostTestRange, 0, boostTestRange),
            vec(0, 0, boostTestRange),
            vec(-boostTestRange, 0, boostTestRange),
            vec(-boostTestRange, 0, 0),
            vec(-boostTestRange, 0, -boostTestRange),
            vec(0, 0, -boostTestRange),
            vec(boostTestRange, 0, -boostTestRange),
        )
    }
}