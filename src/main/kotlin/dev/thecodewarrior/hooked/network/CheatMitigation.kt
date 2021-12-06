package dev.thecodewarrior.hooked.network

import com.teamwizardry.librarianlib.math.clamp
import net.minecraft.server.network.ServerPlayerEntity

/**
 * Various constants for use in mitigating cheats. Because of the way Hooked is designed it has to give the client a lot
 * of leeway, but even that has limits, and here's where they are.
 */
object CheatMitigation {
    /**
     * The tolerance for the starting position of hooks being fired
     */
    val fireHookTolerance = Tolerance(2.0, 16.0)

    /**
     * Pings at or below this will use the minimum tolerance
     */
    val minimumPing = 50

    /**
     * Pings at or above this will use the maximum tolerance
     */
    val maximumPing = 2000

    /**
     * Creates a 0-1 value based on the player's current ping.
     */
    fun pingFactor(player: ServerPlayerEntity): Double {
        val ping = player.pingMilliseconds.clamp(minimumPing, maximumPing)
        return (ping.toDouble() - minimumPing) / (maximumPing - minimumPing)
    }

    data class Tolerance(
        /**
         * The tolerance to use at [minimumPing] or below
         */
        val min: Double,
        /**
         * The tolerance to use at [maximumPing] or above
         */
        val max: Double
    ) {
        fun getValue(player: ServerPlayerEntity): Double {
            return min + pingFactor(player) * (max - min)
        }
    }
}