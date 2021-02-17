package dev.thecodewarrior.hooked.hooks

import dev.thecodewarrior.hooked.hook.HookPlayerController
import dev.thecodewarrior.hooked.hook.HookType
import net.minecraft.entity.player.PlayerEntity

open class FlightHookType(
    count: Int,
    range: Double,
    speed: Double,
    hookLength: Double,
    cooldown: Int,
    val pullStrength: Double,
): HookType(count, range, speed, hookLength, cooldown) {
    override val translationBase: String = "hooked.controller.flight"
    override val controlLangKeys: List<String> = listOf(
        "$translationBase.controls.fire",
        "$translationBase.controls.retract",
        "$translationBase.controls.jump",
    )

    override fun createController(player: PlayerEntity): HookPlayerController {
        return FlightHookPlayerController(player, this)
    }
}