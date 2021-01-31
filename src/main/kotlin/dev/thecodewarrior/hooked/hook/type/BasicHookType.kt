package dev.thecodewarrior.hooked.hook.type

import net.minecraft.entity.player.PlayerEntity

open class BasicHookType(
    override val count: Int,
    override val range: Double,
    override val speed: Double,
    override val hookLength: Double,
    val pullStrength: Double,
    val jumpBoost: Double,

    /**
     * This value adds a gap between player and the start of the chain when rendering the hooks. This only affects the
     * clientside rendering
     */
    val chainMargin: Double = 0.0
): HookType() {
    override fun createController(player: PlayerEntity): HookPlayerController {
        return BasicHookPlayerController(player, this)
    }
}