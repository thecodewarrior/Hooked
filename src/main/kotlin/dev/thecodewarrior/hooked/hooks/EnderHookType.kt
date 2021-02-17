package dev.thecodewarrior.hooked.hooks

import dev.thecodewarrior.hooked.hook.HookPlayerController
import net.minecraft.entity.player.PlayerEntity

class EnderHookType(
    count: Int,
    range: Double,
    speed: Double,
    hookLength: Double,
    cooldown: Int,
    pullStrength: Double,
    boostHeight: Double
): BasicHookType(count, range, speed, hookLength, cooldown, pullStrength, boostHeight) {
    override fun createController(player: PlayerEntity): HookPlayerController {
        return EnderHookPlayerController(player, this)
    }
}