package dev.thecodewarrior.hooked.hook

import com.teamwizardry.librarianlib.core.util.block
import com.teamwizardry.librarianlib.core.util.kotlin.getOrNull
import com.teamwizardry.librarianlib.core.util.kotlin.inconceivable
import com.teamwizardry.librarianlib.etcetera.Raycaster
import com.teamwizardry.librarianlib.math.*
import dev.thecodewarrior.hooked.HookedMod
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.util.getWaistPos
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.vector.Vector3d
import net.minecraftforge.event.entity.player.PlayerEvent
import java.util.*
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

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
abstract class CommonHookProcessor {
    protected val raycaster: Raycaster = Raycaster()
    protected val retractThreshold: Double = cos(Math.toRadians(15.0))

    fun fixSpeed(e: PlayerEvent.BreakSpeed) {
//        From `PlayerEntity.getDigSpeed`:
//        if (!this.onGround) {
//            f /= 5.0f
//        }

        if (!e.entity.isOnGround) {
            e.entity.getCapability(HookedPlayerData.CAPABILITY).getOrNull()?.also { data ->
                if (data.hooks.any { it.state == Hook.State.PLANTED }) {
                    e.newSpeed *= 5
                }
            }
        }
    }

    protected fun getHookData(player: PlayerEntity): HookedPlayerData? {
        return player.getCapability(HookedPlayerData.CAPABILITY).getOrNull()
    }

    protected fun isPointingAtHook(pos: Vector3d, direction: Vector3d, cosThreshold: Double, hook: Hook): Boolean {
        return direction dot (hook.pos - pos).normalize() > cosThreshold
    }

    /**
     * Ticks the hooks, applying motion, raycasting, etc.
     */
    protected fun applyHookMotion(context: HookProcessorContext) {
        removeNaN(context)
        removeAbsurdLength(context)

        updateHooks(context)
    }

    private fun removeNaN(context: HookProcessorContext) {
        val iter = context.hooks.iterator()
        for (hook in iter) {
            if (!(hook.pos.x.isFinite() && hook.pos.y.isFinite() && hook.pos.z.isFinite())) {
                hook.state = Hook.State.REMOVED
                context.data.syncStatus.recentHooks.add(hook)
                iter.remove()
                logger.error("Removing hook $hook that had an infinite or NaN position from player ${context.player.name}")
                context.markDirty(hook)
            }
        }
    }

    private fun updateHooks(context: HookProcessorContext) {
        for (hook in context.hooks) {
            hook.posLastTick = hook.pos
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
                    context.fireEvent(HookEvent(HookEvent.EventType.HIT, hook.uuid, 0))
                    context.controller.onHookHit(context, hook)
                }
                Raycaster.HitType.NONE -> {
                    // if we reached max extension, transition to the retracting state
                    if (distanceLeft <= context.type.speed) {
                        hook.state = Hook.State.RETRACTING
                        context.markDirty(hook)
                        context.fireEvent(HookEvent(HookEvent.EventType.MISS, hook.uuid, 0))
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
        // a bit of wiggle room before a hook breaks off.
        val breakEpsilon: Double = 1 / 16.0
        val breakRangeSq = (context.type.range + breakEpsilon).pow(2)

        context.hooks.forEach { hook ->
            if (hook.state != Hook.State.PLANTED) return@forEach

            if (hook.pos.squareDistanceTo(context.player.getWaistPos()) > breakRangeSq) {
                hook.state = Hook.State.RETRACTING
                context.markDirty(hook)
                context.fireEvent(
                    HookEvent(
                        HookEvent.EventType.DISLODGE,
                        hook.uuid,
                        HookPlayerController.DislodgeReason.DISTANCE.ordinal
                    )
                )
            } else if (context.world.isAirBlock(hook.block)) {
                hook.state = Hook.State.RETRACTING
                context.markDirty(hook)
                context.fireEvent(
                    HookEvent(
                        HookEvent.EventType.DISLODGE,
                        hook.uuid,
                        HookPlayerController.DislodgeReason.BLOCK_BROKEN.ordinal
                    )
                )
            }

        }
        var plantedCount = 0
        // count from the end of the list, retracting everything after the threshold
        for (hook in context.hooks.asReversed()) {
            if (hook.state == Hook.State.PLANTED) {
                plantedCount++
                if (plantedCount > context.type.count) {
                    hook.state = Hook.State.RETRACTING
                    context.markDirty(hook)
                    context.fireEvent(
                        HookEvent(
                            HookEvent.EventType.DISLODGE,
                            hook.uuid,
                            HookPlayerController.DislodgeReason.HOOK_COUNT.ordinal
                        )
                    )
                }
            }
        }
    }

    private fun updateRetracting(context: HookProcessorContext) {
        val iterator = context.hooks.iterator()
        for (hook in iterator) {
            if (hook.state != Hook.State.RETRACTING) continue
            val delta = hook.pos - context.player.getWaistPos()
            val distance = delta.length()

            if (distance < max(context.type.speed, 1.0)) {
                context.data.syncStatus.recentHooks.add(hook)
                iterator.remove()
                hook.state = Hook.State.REMOVED
                context.markDirty(hook)
            } else {
                val direction = delta / distance
                hook.pos -= direction * min(context.type.speed, distance)
                hook.direction = direction
            }
        }
    }

    private fun removeAbsurdLength(context: HookProcessorContext) {
        val threshold = 1024
        val waist = context.player.getWaistPos()
        val iter = context.hooks.iterator()
        for (hook in iter) {
            val distance = waist.distanceTo(hook.pos)
            if (distance > threshold) {
                logger.warn("Hook was an absurd distance ($distance) from player. Removing $hook from ${context.player.name}")
                context.data.syncStatus.recentHooks.add(hook)
                iter.remove()
                hook.state = Hook.State.REMOVED
                context.markDirty(hook)
            }
        }
    }

    private val logger = HookedMod.makeLogger<CommonHookProcessor>()
}