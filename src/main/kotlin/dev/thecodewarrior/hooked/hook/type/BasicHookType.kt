package dev.thecodewarrior.hooked.hook.type

import net.minecraft.entity.player.PlayerEntity

class BasicHookType(
    override val count: Int,
    override val range: Double,
    override val speed: Double,
    override val hookLength: Double,
    val pullStrength: Double,
    val jumpBoost: Double,
): HookType() {
    override fun createController(player: PlayerEntity): HookPlayerController {
        return BasicHookPlayerController(player, this)
    }
}