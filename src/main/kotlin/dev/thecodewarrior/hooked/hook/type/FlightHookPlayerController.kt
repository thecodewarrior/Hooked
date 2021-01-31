package dev.thecodewarrior.hooked.hook.type

import com.teamwizardry.librarianlib.math.dot
import com.teamwizardry.librarianlib.math.minus
import com.teamwizardry.librarianlib.math.times
import dev.thecodewarrior.hooked.hook.processor.Hook
import dev.thecodewarrior.hooked.util.DynamicHull
import dev.thecodewarrior.hooked.util.FadeTimer
import dev.thecodewarrior.hooked.util.fromWaistPos
import dev.thecodewarrior.hooked.util.getWaistPos
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.vector.Vector3d

open class FlightHookPlayerController(player: PlayerEntity, type: FlightHookType): BasicHookPlayerController(player, type) {
    val hull: DynamicHull = DynamicHull()
    var hasExternalFlight: Boolean = true
    var isFlightActive: Boolean = false

    var shouldRetract: Boolean = false

    /**
     * Used on the client to control rendering the wireframe hull.
     */
    var showHullTimer: FadeTimer = FadeTimer()

    override fun remove() {
        disableFlight()
    }

    override fun update(player: PlayerEntity, hooks: List<Hook>, jumping: Boolean) {
        showHullTimer.tick()

        fixExternalFlight()

        if(shouldRetract) {
            hooks.forEach {
                it.state = Hook.State.RETRACTING
            }
        }
        shouldRetract = false

        val planted = hooks.filter { it.state == Hook.State.PLANTED }

        // When only one hook is planted we don't do any creative flight and fall back to the basic behavior
        if (planted.size < 2) {
            disableFlight()

            return
        }

        enableFlight()
        if(hull.update(planted.map { it.pos })) {
            showHullTimer.start(20)
        }

        val waist = player.getWaistPos()
        val constrained = hull.constrain(waist)

        // keep the player's position in check
        if(waist != constrained.position) {
            applyRestoringForce(
                player,
                target = player.fromWaistPos(constrained.position),
                pullForce = type.pullStrength,
                enforcementForce = 2.0,
                lockPlayer = false
            )
            if(constrained.normal != Vector3d.ZERO && (constrained.position - player.positionVec).length() < 2.0) {
                player.motion -= constrained.normal * (player.motion dot constrained.normal)
            }
            showHullTimer.start(10)
        }
    }

    protected fun enableFlight() {
        if(!isFlightActive) {
            hasExternalFlight = player.abilities.allowFlying
            player.abilities.isFlying = true
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
        player.abilities.isFlying = false
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
            player.abilities.isFlying = true
        }
    }
}