package dev.thecodewarrior.hooked.hook

import net.minecraft.entity.player.PlayerEntity

abstract class HookType(
    /**
     * The number of simultaneous hooks allowed
     */
    val count: Int,
    /**
     * The maximum range from impact point to player
     */
    val range: Double,
    /**
     * The speed of the fired hooks in m/t
     */
    val speed: Double,
    /**
     * The distance from the impact point to where the chain should attach
     */
    val hookLength: Double,
    val cooldown: Int,
) {

    abstract val translationBase: String

    /**
     * The language keys to add to the item tooltip
     */
    abstract val controlLangKeys: List<String>

    /**
     * Create a new player controller
     */
    abstract fun createController(player: PlayerEntity): HookPlayerController

    companion object {
        val NONE: HookType = object: HookType(0, 0.0, 0.0, 0.0, 0) {
            override val translationBase: String = "hooked.controller.none"
            override val controlLangKeys: List<String> = emptyList()

            override fun createController(player: PlayerEntity): HookPlayerController = HookPlayerController.NONE
        }
    }
}