package dev.thecodewarrior.hooked.hook.type

import com.teamwizardry.librarianlib.core.util.vec
import com.teamwizardry.librarianlib.math.*
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.hook.processor.Hook
import dev.thecodewarrior.hooked.util.fromWaistPos
import dev.thecodewarrior.hooked.util.getWaistPos
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.vector.Vector3d
import kotlin.math.max

open class BasicHookPlayerController(val player: PlayerEntity, val type: BasicHookType): HookPlayerController() {
    override val allowIndividualRetraction: Boolean = false

    override fun remove() {
        enableGravity(player)
    }

    override fun update(player: PlayerEntity, hooks: List<Hook>, jumpState: HookedPlayerData.JumpState?) {
        var plantedCount = 0
        var targetPoint = Vector3d.ZERO
        hooks.forEach { hook ->
            if (hook.state == Hook.State.PLANTED) {
                targetPoint += hook.pos
                plantedCount++
            }
        }
        if (plantedCount == 0) {
            enableGravity(player)
            if (jumpState != null) { // even if none are planted, retract any that are extending
                hooks.forEach {
                    it.state = Hook.State.RETRACTING
                }
            }
            return
        }

        // we have at least one planted hook

        disableGravity(player)
        targetPoint /= plantedCount
        val waist = player.getWaistPos()
        val deltaPos = targetPoint - waist
        val deltaLen = deltaPos.length()
        val deltaNormal = deltaPos / deltaLen

        if (jumpState != null) {
            if (hooks.any { it.state == Hook.State.PLANTED }) {
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
            }
            hooks.forEach {
                it.state = Hook.State.RETRACTING
            }
            return
        }

        player.fallDistance = 0f

        clearFlyingKickTimer(player)

        applyRestoringForce(player, player.fromWaistPos(targetPoint), type.pullStrength)
    }

    companion object {
    }
}