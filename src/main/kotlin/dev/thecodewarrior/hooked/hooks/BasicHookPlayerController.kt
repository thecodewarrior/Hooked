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
import kotlin.math.sqrt

open class BasicHookPlayerController(val player: PlayerEntity, val type: BasicHookType): HookPlayerController() {
    override fun fireHooks(
        delegate: HookControllerDelegate,
        pos: Vec3d,
        pitch: Float,
        yaw: Float,
        sneaking: Boolean,
        addHook: (pos: Vec3d, pitch: Float, yaw: Float) -> Hook
    ): Boolean {
        if(delegate.cooldown == 0) {
            val tag = if(sneaking) {
                0
            } else {
                (delegate.hooks.maxOfOrNull { it.tag } ?: 0) + 1
            }
            val hook = addHook(pos, pitch, yaw)
            hook.tag = tag
            delegate.triggerCooldown()
            return true
        } else {
            return false
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
     * Returns the bounding box of the jump target
     */
    fun computeJumpTargets(
        delegate: HookControllerDelegate
    ): List<Box>? {
        val waist = player.getWaistPos()
        val targetPos = getTargetPoint(delegate.hooks)
        val deltaPos = targetPos - waist
        val deltaNormal = deltaPos.normalize()

        val boostAABB: Box
        when {
            isStuck(delegate) || player.actualMotion == Vec3d.ZERO -> {
                boostAABB = player.boundingBox
            }
            deltaPos.length() < type.pullStrength * 3 -> {
                boostAABB = player.boundingBox.offset(deltaPos) // the player's bounding box centered around the targetPos
            }
            else -> return null
        }

        return boostTestOffsets.map {
            boostAABB.offset(JumpHeightUtil.computeStepTarget(player, boostAABB, it, type.boostHeight))
        }
    }

    private fun performJump(
        delegate: HookControllerDelegate
    ) {
        val jumpTarget = this.computeJumpTargets(delegate)

        if(jumpTarget != null) {
            // the height relative to the player's current position
            val jumpHeight = jumpTarget.maxOf { it.minY } - player.y
            val gravity = 0.08

            player.jump()
            if(jumpHeight > 0) {
                player.velocity = vec(
                    player.velocity.x,
                    sqrt(2 * gravity * jumpHeight),
                    player.velocity.z
                )
            }

            return
        }

        val waist = player.getWaistPos()
        val targetPos = getTargetPoint(delegate.hooks)
        val deltaPos = targetPos - waist
        // if we don't do anything special, just give them a bit of a boost
        player.velocity += deltaPos.normalize() * (type.pullStrength * 0.2)
    }

    override fun update(delegate: HookControllerDelegate) {
        if (delegate.hooks.none { it.state == Hook.State.PLANTED }) {
            return
        }

        player.fallDistance = 0f
        clearFlyingKickTimer(player)

        val targetPlayerPos = player.fromWaistPos(getTargetPoint(delegate.hooks))
        applyRestoringForce(player, targetPlayerPos, type.pullStrength)
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