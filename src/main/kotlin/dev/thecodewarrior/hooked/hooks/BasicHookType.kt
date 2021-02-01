package dev.thecodewarrior.hooked.hooks

import dev.thecodewarrior.hooked.hook.HookPlayerController
import dev.thecodewarrior.hooked.hook.HookType
import net.minecraft.entity.player.PlayerEntity

open class BasicHookType(
    count: Int,
    range: Double,
    speed: Double,
    hookLength: Double,
    allowIndividualRetraction: Boolean,
    val pullStrength: Double,
    val jumpBoost: Double
): HookType(count, range, speed, hookLength, allowIndividualRetraction) {
    override fun createController(player: PlayerEntity): HookPlayerController {
        return BasicHookPlayerController(player, this)
    }
}