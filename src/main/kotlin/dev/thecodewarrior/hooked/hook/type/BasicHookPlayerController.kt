package dev.thecodewarrior.hooked.hook.type

import com.teamwizardry.librarianlib.core.util.mapSrgName
import com.teamwizardry.librarianlib.core.util.vec
import com.teamwizardry.librarianlib.math.*
import dev.thecodewarrior.hooked.hook.processor.Hook
import dev.thecodewarrior.hooked.util.fromWaistPos
import dev.thecodewarrior.hooked.util.getWaistPos
import ll.dev.thecodewarrior.mirror.Mirror
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.attributes.AttributeModifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.network.play.ServerPlayNetHandler
import net.minecraft.util.math.vector.Vector3d
import net.minecraftforge.common.ForgeMod.ENTITY_GRAVITY
import net.minecraftforge.common.MinecraftForge
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign

open class BasicHookPlayerController(val player: PlayerEntity, val type: BasicHookType): HookPlayerController() {
    override fun remove() {
        enableGravity(player)
    }

    override fun update(player: PlayerEntity, hooks: List<Hook>, jumping: Boolean) {
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
            if (jumping) { // even if none are planted, retract any that are extending
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

        if (jumping) {
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