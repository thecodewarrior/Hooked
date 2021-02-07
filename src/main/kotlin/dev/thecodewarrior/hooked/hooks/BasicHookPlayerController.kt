package dev.thecodewarrior.hooked.hooks

import com.teamwizardry.librarianlib.core.util.vec
import com.teamwizardry.librarianlib.math.*
import dev.thecodewarrior.hooked.HookedModSounds
import dev.thecodewarrior.hooked.hook.Hook
import dev.thecodewarrior.hooked.hook.HookControllerDelegate
import dev.thecodewarrior.hooked.hook.HookPlayerController
import dev.thecodewarrior.hooked.util.fromWaistPos
import dev.thecodewarrior.hooked.util.getWaistPos
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.vector.Vector3d
import kotlin.math.max

open class BasicHookPlayerController(val player: PlayerEntity, val type: BasicHookType): HookPlayerController() {
    override fun remove() {
        enableGravity(player)
    }

    override fun jump(
        delegate: HookControllerDelegate,
        doubleJump: Boolean,
        sneaking: Boolean
    ) {
        val waist = player.getWaistPos()
        val deltaPos = getTargetPoint(delegate.hooks) - waist
        val deltaLen = deltaPos.length()
        val deltaNormal = deltaPos / deltaLen

        if (delegate.hooks.any { it.state == Hook.State.PLANTED }) {
            val movementTowardPos = if (deltaPos == Vector3d.ZERO) 0.0 else player.motion dot deltaNormal

            // slow enough that we are likely to be stuck, or close enough to warrant a premature jump
            if (movementTowardPos in 0.0..2 / 20.0 || deltaLen < type.pullStrength * 4) {
                player.motion *= 1.25
                player.jump()
                player.motion = vec(
                    player.motion.x,
                    max(player.motion.y, 0.42 + type.jumpBoost), // 0.42 == vanilla jump speed
                    player.motion.z
                )
            } else {
                // todo: this can feel pretty bad. make it apply a jump force if the hook is already pulling up?
                // give the player a boost
                player.motion += deltaNormal * (type.pullStrength * 0.2)
            }
            delegate.enqueueSound(HookedModSounds.retractHook)
        }

        delegate.hooks.forEach {
            it.state = Hook.State.RETRACTING
            delegate.markDirty(it)
        }
    }

    override fun update(delegate: HookControllerDelegate) {
        if (delegate.hooks.none { it.state == Hook.State.PLANTED }) {
            enableGravity(player)
            return
        }

        // we have at least one planted hook
        disableGravity(player)
        player.fallDistance = 0f
        clearFlyingKickTimer(player)

        applyRestoringForce(player, player.fromWaistPos(getTargetPoint(delegate.hooks)), type.pullStrength)
    }

    private fun getTargetPoint(hooks: List<Hook>): Vector3d {
        var plantedCount = 0
        var targetPoint = Vector3d.ZERO
        hooks.forEach { hook ->
            if (hook.state == Hook.State.PLANTED) {
                targetPoint += hook.pos
                plantedCount++
            }
        }
        targetPoint /= plantedCount
        return targetPoint
    }
}