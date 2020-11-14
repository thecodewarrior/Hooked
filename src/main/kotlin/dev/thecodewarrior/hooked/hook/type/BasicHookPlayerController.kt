package dev.thecodewarrior.hooked.hook.type

import com.teamwizardry.librarianlib.core.util.mapSrgName
import com.teamwizardry.librarianlib.math.*
import dev.thecodewarrior.hooked.HookedMod
import dev.thecodewarrior.hooked.hook.processor.Hook
import dev.thecodewarrior.hooked.util.getWaistPos
import ll.dev.thecodewarrior.mirror.Mirror
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.attributes.AttributeModifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Vec3d
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign

class BasicHookPlayerController(val player: PlayerEntity, val type: BasicHookType): HookPlayerController() {
    override fun remove() {
        val gravityAttribute = player.getAttribute(LivingEntity.ENTITY_GRAVITY)
        if(gravityAttribute.hasModifier(HOOK_GRAVITY))
            gravityAttribute.removeModifier(HOOK_GRAVITY)
    }

    override fun update(player: PlayerEntity, hooks: List<Hook>) {
        var plantedCount = 0
        var targetPoint = Vec3d.ZERO
        hooks.forEach { hook ->
            if (hook.state == Hook.State.PLANTED) {
                targetPoint += hook.pos
                plantedCount++
            }
        }
        val gravityAttribute = player.getAttribute(LivingEntity.ENTITY_GRAVITY)
        if (plantedCount == 0) {
            if(gravityAttribute.hasModifier(HOOK_GRAVITY))
                gravityAttribute.removeModifier(HOOK_GRAVITY)
            return
        } else {
            if(!gravityAttribute.hasModifier(HOOK_GRAVITY))
                gravityAttribute.applyModifier(HOOK_GRAVITY)
        }
        targetPoint /= plantedCount

        val waist = player.getWaistPos()
        val deltaPos = targetPoint - waist
        val deltaLen = deltaPos.length()
        val deltaNormal = deltaPos / deltaLen

        if (isJumping.getFast(player)) {
            if (hooks.any { it.state == Hook.State.PLANTED }) {
                val movementTowardPos = if (deltaPos == Vec3d.ZERO) 0.0 else player.motion dot deltaNormal

                // slow enough that we are likely to be stuck, or close enough to warrant a premature jump
                if (movementTowardPos in 0.0..2 / 20.0 || deltaLen < type.pullStrength * 4) {
                    player.motion *= 1.25
                    player.jump()
                    player.motion = vec(
                        player.motion.x,
                        max(player.motion.y, 0.42 + type.jumpBoost), // 0.42 == vanilla jump speed
                        player.motion.z
                    )
                }
//            markDirty()
            }
            hooks.forEach {
                it.state = Hook.State.RETRACTING
            }
        }

        HookedMod.proxy.disableAutoJump(player, true)
        player.fallDistance = 0f
//        player.jumpTicks = 10

//        (player as? ServerPlayerEntity)?.connection?.floatingTickCount = 0

        if (deltaLen <= type.pullStrength) { // close enough that we should set to avoid oscillations
            player.motion = deltaPos
        } else {
            val pull = deltaPos * (type.pullStrength / deltaLen)

            player.motion = vec(
                applyPull(player.motion.x, pull.x),
                applyPull(player.motion.y, pull.y),
                applyPull(player.motion.z, pull.z)
            )
        }
    }

    private fun applyPull(entityMotion: Double, pull: Double): Double {
        val forceMultiplier = 0.5

        if (abs(entityMotion) < abs(pull) || sign(entityMotion) != sign(pull)) {
            val adjusted = entityMotion + pull * forceMultiplier
            if (abs(adjusted) > abs(pull) && sign(adjusted) == sign(pull))
                return pull
            else
                return adjusted
        }

        return entityMotion
    }

    companion object {
        private val isJumping = Mirror.reflectClass<LivingEntity>().getDeclaredField(mapSrgName("field_70703_bu"))

        private val HOOK_GRAVITY_ID: UUID = UUID.fromString("654bd58d-a1c6-40e7-9d2b-09699b9558fe")
        private val HOOK_GRAVITY: AttributeModifier = AttributeModifier(HOOK_GRAVITY_ID, "Hook gravity modifier", -0.05, AttributeModifier.Operation.ADDITION).setSaved(false)
    }
}