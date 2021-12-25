package dev.thecodewarrior.hooked.hooks

import com.teamwizardry.librarianlib.core.util.mixinCast
import com.teamwizardry.librarianlib.math.dot
import com.teamwizardry.librarianlib.math.minus
import com.teamwizardry.librarianlib.math.times
import dev.thecodewarrior.hooked.bridge.PlayerMixinBridge
import dev.thecodewarrior.hooked.hook.Hook
import dev.thecodewarrior.hooked.hook.HookControllerDelegate
import dev.thecodewarrior.hooked.hook.HookPlayerController
import dev.thecodewarrior.hooked.util.DynamicHull
import dev.thecodewarrior.hooked.util.FadeTimer
import dev.thecodewarrior.hooked.util.fromWaistPos
import dev.thecodewarrior.hooked.util.getWaistPos
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Vec3d
import kotlin.math.cos

open class FlightHookPlayerController(val player: PlayerEntity, val type: FlightHookType): HookPlayerController() {
    val hull: DynamicHull = DynamicHull()
    var hasExternalFlight: Boolean = true
    var isFlightActive: Boolean = false
    var stopFallDamage: Boolean = false

    /**
     * Used on the client to control rendering the wireframe hull.
     */
    var showHullTimer: FadeTimer = FadeTimer()

    private val allowedFlightRange = 0.5

    override fun remove() {
        disableFlight()
    }

    private val retractThreshold: Double = cos(Math.toRadians(15.0))
    private fun isPointingAtHook(pos: Vec3d, direction: Vec3d, cosThreshold: Double, hook: Hook): Boolean {
        return direction dot (hook.pos - pos).normalize() > cosThreshold
    }

    override fun fireHooks(
        delegate: HookControllerDelegate,
        pos: Vec3d,
        direction: Vec3d,
        sneaking: Boolean,
        addHook: (pos: Vec3d, direction: Vec3d) -> Hook
    ): Boolean {
        if(sneaking) {
            for (hook in delegate.hooks) {
                if (isPointingAtHook(pos, direction, retractThreshold, hook)) {
                    delegate.retractHook(hook)
                }
            }
            return true
        } else if(delegate.cooldown == 0) {
            addHook(pos, direction)
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
        if (delegate.hooks.any { it.state == Hook.State.PLANTED }) {
            mixinCast<PlayerMixinBridge>(player).hookedShouldAbortElytraFlag = true
        }

        if(doubleJump && sneaking) {
            for(hook in delegate.hooks) {
                delegate.retractHook(hook)
            }
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

            return
        }

        if(hull.update(planted.map { it.pos })) {
            showHullTimer.start(20)
        }

        val waist = player.getWaistPos()
        val constrained = hull.constrain(waist)

        // if the player isn't flying, allow or disallow flight based upon whether they're inside the hull
        if(!player.abilities.flying) {
            val allowFlight = waist.squaredDistanceTo(constrained.position) < allowedFlightRange * allowedFlightRange

            if(allowFlight != isFlightActive) {
                showHullTimer.start(10)
                if(!allowFlight) {
                    stopFallDamage = true
                }
            }

            if(allowFlight) {
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