package dev.thecodewarrior.hooked.hooks

import dev.thecodewarrior.hooked.hook.HookPlayerController
import dev.thecodewarrior.hooked.hook.HookType
import net.minecraft.entity.player.PlayerEntity

open class FlightHookType(
    count: Int,
    range: Double,
    speed: Double,
    hookLength: Double,
    allowIndividualRetraction: Boolean,
    val pullStrength: Double,
): HookType(count, range, speed, hookLength, allowIndividualRetraction) {
    override val translationBase: String = "hooked.controller.flight"

    override fun createController(player: PlayerEntity): HookPlayerController {
        return FlightHookPlayerController(player, this)
    }
}