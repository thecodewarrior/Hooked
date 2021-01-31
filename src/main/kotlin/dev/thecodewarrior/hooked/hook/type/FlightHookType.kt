package dev.thecodewarrior.hooked.hook.type

import net.minecraft.entity.player.PlayerEntity

open class FlightHookType(
    count: Int,
    range: Double,
    speed: Double,
    hookLength: Double,
    pullStrength: Double,
    jumpBoost: Double,
    chainMargin: Double,
): BasicHookType(count, range, speed, hookLength, pullStrength, jumpBoost, chainMargin) {
    override fun createController(player: PlayerEntity): HookPlayerController {
        return FlightHookPlayerController(player, this)
    }
}