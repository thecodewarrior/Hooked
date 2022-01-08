package dev.thecodewarrior.hooked.hook

import com.teamwizardry.librarianlib.core.util.block
import com.teamwizardry.librarianlib.core.util.kotlin.inconceivable
import com.teamwizardry.librarianlib.etcetera.Raycaster
import com.teamwizardry.librarianlib.math.*
import dev.thecodewarrior.hooked.Hooked
import dev.thecodewarrior.hooked.util.getWaistPos
import net.minecraft.block.ShapeContext
import kotlin.math.*

/**
 * See [ClientHookProcessor] and [ServerHookProcessor] for information about their netcode.
 *
 * In terms of processing, here's an overview (I'll try to keep it up to date):
 *
 * - Both the client and the server simulate the hook movement and update controllers
 * - The client *doesn't* update the current type or controller based on the equipped hook item, all hook type updates
 * come from on high
 *
 */
abstract class CommonHookProcessor : HookProcessor {
    protected val raycaster: Raycaster = Raycaster()

    /**
     * Ticks the hooks, applying motion, raycasting, etc.
     */
    protected fun applyHookMotion(context: HookProcessorContext) {
        removeNaN(context)
        removeAbsurdLength(context)

        updateHooks(context)
    }

    private fun removeNaN(context: HookProcessorContext) {
        val iter = context.data.hooks.iterator()
        for ((id, hook) in iter) {
            if (!(hook.pos.x.isFinite() && hook.pos.y.isFinite() && hook.pos.z.isFinite())) {
                hook.state = Hook.State.REMOVED
                context.data.syncStatus.addRecentHook(hook)
                iter.remove()
                logger.error("Removing hook $hook that had an infinite or NaN position from player ${context.player.name}")
                context.markDirty(hook)
            }
        }
    }

    private fun updateHooks(context: HookProcessorContext) {
        for (hook in context.hooks) {
            hook.posLastTick = hook.pos
            hook.firstTick = false
        }
        updateRetracting(context)
        updateExtending(context)
        updatePlanted(context)
    }

    private fun updateExtending(context: HookProcessorContext) {
        for (hook in context.hooks) {
            if (hook.state != Hook.State.EXTENDING)
                continue

            val distanceLeft = context.type.range - (hook.pos - context.player.getWaistPos()).length()

            val castDistance = min(context.type.speed, distanceLeft) + context.type.hookLength

            raycaster.cast(
                context.world,
                Raycaster.BlockMode.COLLISION,
                ShapeContext.of(context.player),
                hook.pos.x, hook.pos.y, hook.pos.z,
                hook.pos.x + hook.direction.x * castDistance,
                hook.pos.y + hook.direction.y * castDistance,
                hook.pos.z + hook.direction.z * castDistance
            )

            hook.pos += hook.direction * (castDistance * raycaster.fraction - hook.type.hookLength)

            when (raycaster.hitType) {
                Raycaster.HitType.BLOCK -> {
                    // if we hit a block, plant in it
                    hook.state = Hook.State.PLANTED
                    hook.block = block(raycaster.blockX, raycaster.blockY, raycaster.blockZ)
                    context.markDirty(hook)
                    context.fireEvent(HookEvent(HookEvent.EventType.HIT, hook.id, 0))
                    context.controller.onHookHit(context, hook)
                }
                Raycaster.HitType.NONE -> {
                    // if we reached max extension, transition to the retracting state
                    if (distanceLeft <= context.type.speed) {
                        hook.state = Hook.State.RETRACTING
                        context.markDirty(hook)
                        context.fireEvent(HookEvent(HookEvent.EventType.MISS, hook.id, 0))
                    }
                }
                else -> {
                    raycaster.reset()
                    inconceivable("Raycast only included blocks but returned non-block hit type ${raycaster.hitType}")
                }
            }
            raycaster.reset()
        }
    }

    private fun updatePlanted(context: HookProcessorContext) {
        if(
            context.player.isFallFlying &&
            !context.player.world.gameRules.getBoolean(Hooked.Rules.ALLOW_HOOKS_WHILE_FLYING)
        ) {
            for(hook in context.hooks) {
                if(hook.state != Hook.State.RETRACTING) {
                    hook.state = Hook.State.RETRACTING
                    context.markDirty(hook)
                    context.fireEvent(
                        HookEvent(
                            HookEvent.EventType.DISLODGE,
                            hook.id,
                            HookPlayerController.DislodgeReason.DISALLOWED.ordinal
                        )
                    )
                }
            }
        }

        // a bit of wiggle room before a hook breaks off.
        val breakEpsilon: Double = 1 / 16.0
        val breakRangeSq = (context.type.range + breakEpsilon).pow(2)

        for(hook in context.hooks) {
            if (hook.state != Hook.State.PLANTED) {
                continue
            }

            val reason = when {
                hook.pos.squaredDistanceTo(context.player.getWaistPos()) > breakRangeSq -> {
                    HookPlayerController.DislodgeReason.DISTANCE
                }
                context.world.isAir(hook.block) -> {
                    HookPlayerController.DislodgeReason.BLOCK_BROKEN
                }
                else -> continue
            }

            hook.state = Hook.State.RETRACTING
            context.markDirty(hook)
            context.fireEvent(HookEvent(HookEvent.EventType.DISLODGE, hook.id, reason.ordinal))
        }
        var plantedCount = 0
        // count from the end of the list, retracting everything after the threshold
        for ((_, hook) in context.data.hooks.descendingMap()) {
            if (hook.state == Hook.State.PLANTED) {
                plantedCount++
                if (plantedCount > context.type.count) {
                    hook.state = Hook.State.RETRACTING
                    context.markDirty(hook)
                    context.fireEvent(
                        HookEvent(
                            HookEvent.EventType.DISLODGE,
                            hook.id,
                            HookPlayerController.DislodgeReason.HOOK_COUNT.ordinal
                        )
                    )
                }
            }
        }
    }

    private fun updateRetracting(context: HookProcessorContext) {
        val iterator = context.data.hooks.iterator()
        for ((_, hook) in iterator) {
            if (hook.state != Hook.State.RETRACTING) continue
            val delta = hook.pos - context.player.getWaistPos()
            val distance = delta.length()

            if (distance < max(context.type.speed, 1.0)) {
                context.data.syncStatus.addRecentHook(hook)
                iterator.remove()
                hook.state = Hook.State.REMOVED
                context.markDirty(hook)
            } else {
                val direction = delta / distance
                hook.pos -= direction * min(context.type.speed, distance)
                hook.yaw = -Math.toDegrees(atan2(direction.x, direction.z)).toFloat()
                hook.pitch = -Math.toDegrees(asin(direction.y)).toFloat()
            }
        }
    }

    private fun removeAbsurdLength(context: HookProcessorContext) {
        val threshold = 1024
        val waist = context.player.getWaistPos()
        val iter = context.data.hooks.iterator()
        for ((_, hook) in iter) {
            val distance = waist.distanceTo(hook.pos)
            if (distance > threshold) {
                logger.warn("Hook was an absurd distance ($distance) from player. Removing $hook from ${context.player.name}")
                context.data.syncStatus.addRecentHook(hook)
                iter.remove()
                hook.state = Hook.State.REMOVED
                context.markDirty(hook)
            }
        }
    }

    private val logger = Hooked.logManager.makeLogger<CommonHookProcessor>()
}