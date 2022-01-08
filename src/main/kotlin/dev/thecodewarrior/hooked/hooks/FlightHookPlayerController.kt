package dev.thecodewarrior.hooked.hooks

import com.teamwizardry.librarianlib.math.dot
import com.teamwizardry.librarianlib.math.minus
import com.teamwizardry.librarianlib.math.times
import dev.thecodewarrior.hooked.hook.Hook
import dev.thecodewarrior.hooked.hook.HookActiveReason
import dev.thecodewarrior.hooked.hook.HookControllerDelegate
import dev.thecodewarrior.hooked.hook.HookPlayerController
import dev.thecodewarrior.hooked.util.DynamicHull
import dev.thecodewarrior.hooked.util.FadeTimer
import dev.thecodewarrior.hooked.util.fromWaistPos
import dev.thecodewarrior.hooked.util.getWaistPos
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.Vec3d
import kotlin.math.cos

open class FlightHookPlayerController(val player: PlayerEntity, val type: FlightHookType): HookPlayerController() {
    val hull: DynamicHull = DynamicHull()
    var hasExternalFlight: Boolean = false
    var isFlightActive: Boolean = false
    var stopFallDamage: Boolean = false
    var isInsideHull: Boolean = false

    /**
     * Used on the client to control rendering the wireframe hull.
     */
    var showHullTimer: FadeTimer = FadeTimer()

    private val allowedFlightRange = 0.5

    override fun saveState(tag: NbtCompound) {
        tag.putBoolean("HasExternalFlight", hasExternalFlight)
        tag.putBoolean("IsFlightActive", isFlightActive)
        tag.putBoolean("StopFallDamage", stopFallDamage)
    }

    override fun loadState(tag: NbtCompound) {
        hasExternalFlight = tag.getBoolean("HasExternalFlight")
        isFlightActive = tag.getBoolean("IsFlightActive")
        stopFallDamage = tag.getBoolean("StopFallDamage")
    }

    override fun writeSyncState(buf: PacketByteBuf) {
        buf.writeBoolean(hasExternalFlight)
        buf.writeBoolean(stopFallDamage)
    }

    override fun readSyncState(buf: PacketByteBuf) {
        hasExternalFlight = buf.readBoolean()
        stopFallDamage = buf.readBoolean()
    }

    override fun remove() {
        disableFlight()
    }

    private fun isPointingAtHook(pos: Vec3d, direction: Vec3d, cosThreshold: Double, hook: Hook): Boolean {
        return direction dot (hook.pos - pos).normalize() > cosThreshold
    }

    private val retractThreshold: Double = cos(Math.toRadians(15.0))
    override fun fireHooks(
        delegate: HookControllerDelegate,
        pos: Vec3d,
        pitch: Float,
        yaw: Float,
        sneaking: Boolean,
        addHook: (pos: Vec3d, pitch: Float, yaw: Float) -> Hook
    ): Boolean {
        if(sneaking) {
            val direction = Vec3d.fromPolar(pitch, yaw)
            val closestHook = delegate.hooks
                .map { hook -> hook to (direction dot (hook.pos - pos).normalize()) }
                .filter { it.second > retractThreshold }
                .maxByOrNull { it.second }
                ?.first
            if(closestHook != null) {
                delegate.retractHook(closestHook)
            }
            return true
        } else if(delegate.cooldown == 0) {
            addHook(pos, pitch, yaw)
            delegate.triggerCooldown()
            return true
        } else {
            return false
        }
    }

    override fun jump(
        delegate: HookControllerDelegate,
        doubleJump: Boolean,
        sneaking: Boolean
    ) {
        if(doubleJump && sneaking) {
            for(hook in delegate.hooks) {
                delegate.retractHook(hook)
            }
            stopFallDamage = true
        }

        if(doubleJump && delegate.hooks.any { it.state == Hook.State.PLANTED }) {
            val waist = player.getWaistPos()
            val constrained = hull.constrain(waist)
            val allowFlight = waist.squaredDistanceTo(constrained.position) < allowedFlightRange * allowedFlightRange

            if(!allowFlight) {
                showHullTimer.start(20)
            }
        }
    }

    override fun update(
        delegate: HookControllerDelegate
    ) {
        showHullTimer.tick()

        fixExternalFlight()

        if(stopFallDamage) {
            player.fallDistance = 0f
        }
        if(player.isOnGround) {
            stopFallDamage = false
        }

        val planted = delegate.hooks.filter { it.state == Hook.State.PLANTED }

        // When only one hook is planted we don't do any creative flight and fall back to the basic behavior
        if (planted.size < 2) {
            disableFlight()
            isInsideHull = false

            return
        }

        if(hull.update(planted.map { it.pos })) {
            showHullTimer.start(20)
        }

        val waist = player.getWaistPos()
        val constrained = hull.constrain(waist)
        isInsideHull = waist.squaredDistanceTo(constrained.position) < allowedFlightRange * allowedFlightRange

        // if the player isn't flying, allow or disallow flight based upon whether they're inside the hull
        if(!player.abilities.flying) {
            if(isInsideHull != isFlightActive) {
                showHullTimer.start(10)
                if(!isInsideHull) {
                    stopFallDamage = true
                }
            }

            if(isInsideHull) {
                enableFlight()
            } else {
                disableFlight()
            }
        }

        // when flying, keep the player's position in check
        if(player.abilities.flying && waist != constrained.position) {
            applyRestoringForce(
                player,
                target = player.fromWaistPos(constrained.position),
                pullForce = type.pullStrength,
                enforcementForce = 2.0,
                lockPlayer = false
            )
            if (constrained.normal != Vec3d.ZERO && (constrained.position - player.pos).length() < 2.0) {
                player.velocity -= constrained.normal * (player.velocity dot constrained.normal)
            }
            showHullTimer.start(10)
        }
    }

    override fun isActive(delegate: HookControllerDelegate, reason: HookActiveReason): Boolean {
        return when(reason) {
            // we should still be able to sneak to avoid ledges
            HookActiveReason.DISABLE_CLIP_AT_LEDGE -> false
            // the player should still be able to auto jump
            HookActiveReason.DISABLE_AUTO_JUMP -> false
            // the hook doesn't pull the player in, no reason to give them too much freedom here
            HookActiveReason.MOVED_WRONGLY -> false
            else -> isInsideHull
        }
    }

    protected fun enableFlight() {
        if(!isFlightActive) {
            hasExternalFlight = player.abilities.allowFlying
        }

        player.abilities.allowFlying = true
        isFlightActive = true
    }

    /**
     * Disable flight, restoring it to its previous state
     */
    protected fun disableFlight() {
        if(!isFlightActive) return

        player.abilities.allowFlying = hasExternalFlight
        player.abilities.flying = false
        isFlightActive = false
    }

    /**
     * If the player takes off a flight item while using a flight hook it would drop them out of the air.
     * This fixes that.
     */
    protected fun fixExternalFlight() {
        if(!isFlightActive) return

        // something external disallowed flight. Set it back to true, making sure we know to reset it to false after
        // we're done
        if(!player.abilities.allowFlying) {
            hasExternalFlight = false
            player.abilities.allowFlying = true
            player.abilities.flying = true
        }
    }
}