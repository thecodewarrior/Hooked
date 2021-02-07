package dev.thecodewarrior.hooked.hook

import com.teamwizardry.librarianlib.core.util.block
import com.teamwizardry.librarianlib.core.util.kotlin.getOrNull
import com.teamwizardry.librarianlib.core.util.kotlin.inconceivable
import com.teamwizardry.librarianlib.etcetera.Raycaster
import com.teamwizardry.librarianlib.math.*
import dev.thecodewarrior.hooked.HookedMod
import dev.thecodewarrior.hooked.HookedModSounds
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.util.getWaistPos
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.SoundEvent
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
//        PlayerEntity.getDigSpeed:
//        if (!this.onGround) {
//            f /= 5.0f
//        }

        if (!e.entity.isOnGround) {
            e.entity.getCapability(HookedPlayerData.CAPABILITY).getOrNull()?.also { data ->
                if(data.hooks.any { it.state == Hook.State.PLANTED }) {
                    e.newSpeed *= 5
                }
            }
        }
    }

    abstract fun enqueueSound(sound: SoundEvent)
    abstract fun onHookStateChange(player: PlayerEntity, data: HookedPlayerData, hook: Hook)

    protected fun getHookData(player: PlayerEntity): HookedPlayerData? {
        return player.getCapability(HookedPlayerData.CAPABILITY).getOrNull()
    }

    protected fun isPointingAtHook(pos: Vector3d, direction: Vector3d, cosThreshold: Double, hook: Hook): Boolean {
        return direction dot (hook.pos - pos).normalize() > cosThreshold
    }

    /**
     * Ticks the hooks, applying motion, raycasting, etc.
     */
    protected fun applyHookMotion(player: PlayerEntity, data: HookedPlayerData) {
        removeNaN(player, data)
        removeAbsurdLength(player, data)

        updateHooks(player, data)
    }

    private fun removeNaN(player: PlayerEntity, data: HookedPlayerData) {
        val iter = data.hooks.iterator()
        for (hook in iter) {
            if (!(hook.pos.x.isFinite() && hook.pos.y.isFinite() && hook.pos.z.isFinite())) {
                hook.state = Hook.State.REMOVED
                iter.remove()
                logger.error("Removing hook $hook that had an infinite or NaN position from player ${player.uniqueID}")
                onHookStateChange(player, data, hook)
            }
        }
    }

    private fun updateHooks(player: PlayerEntity, data: HookedPlayerData) {
        data.hooks.forEach { hook ->
            hook.posLastTick = hook.pos
        }
        updateExtending(player, data)
        updatePlanted(player, data)
        updateRetracting(player, data)
    }

    private fun updateExtending(player: PlayerEntity, data: HookedPlayerData) {
        data.hooks.forEach { hook ->
            if(hook.state != Hook.State.EXTENDING) return@forEach

            val distanceLeft = data.type.range - (hook.pos - player.getWaistPos()).length()

            val tip = hook.tipPos
            val castDistance = min(data.type.speed, distanceLeft) + data.type.hookLength

            raycaster.cast(
                player.world,
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
                    onHookStateChange(player, data, hook)
                    enqueueSound(HookedModSounds.hookHit)
                }
                Raycaster.HitType.NONE -> {
                    // if we reached max extension, transition to the retracting state
                    if (distanceLeft <= data.type.speed) {
                        hook.state = Hook.State.RETRACTING
                        onHookStateChange(player, data, hook)
                        enqueueSound(HookedModSounds.hookMiss)
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

    private fun updatePlanted(player: PlayerEntity, data: HookedPlayerData) {
        // a bit of wiggle room before a hook breaks off.
        val breakEpsilon: Double = 1 / 16.0

        data.hooks.forEach { hook ->
            if(hook.state != Hook.State.PLANTED) return@forEach

            if (
                hook.pos.squareDistanceTo(player.getWaistPos()) > (data.type.range + breakEpsilon).pow(2) ||
                player.world.isAirBlock(hook.block)
            ) {
                hook.state = Hook.State.RETRACTING
                onHookStateChange(player, data, hook)
                enqueueSound(HookedModSounds.hookDislodge)
            }
        }
        var plantedCount = 0
        // count from the end of the list, retracting everything after the threshold
        for(hook in data.hooks.asReversed()) {
            if (hook.state == Hook.State.PLANTED) {
                plantedCount++
                if(plantedCount > data.type.count) {
                    hook.state = Hook.State.RETRACTING
                    onHookStateChange(player, data, hook)
                    enqueueSound(HookedModSounds.hookDislodge)
                }
            }
        }
    }

    private fun updateRetracting(player: PlayerEntity, data: HookedPlayerData) {
        val iterator = data.hooks.iterator()
        for (hook in iterator) {
            if(hook.state != Hook.State.RETRACTING) continue
            val delta = hook.pos - player.getWaistPos()
            val distance = delta.length()

            if (distance < max(data.type.speed, 1.0)) {
                iterator.remove()
                hook.state = Hook.State.REMOVED
                onHookStateChange(player, data, hook)
            } else {
                val direction = delta / distance
                hook.pos -= direction * min(data.type.speed, distance)
                hook.direction = direction
            }
        }
    }

    private fun removeAbsurdLength(player: PlayerEntity, data: HookedPlayerData) {
        val threshold = 1024
        val waist = player.getWaistPos()
        val iter = data.hooks.iterator()
        for(hook in iter) {
            val distance = waist.distanceTo(hook.pos)
            if(distance > threshold) {
                logger.warn("Hook was an absurd distance ($distance) from player. Removing $hook from $player")
                iter.remove()
                hook.state = Hook.State.REMOVED
                onHookStateChange(player, data, hook)
            }
        }
    }

    private val logger = HookedMod.makeLogger<CommonHookProcessor>()
}