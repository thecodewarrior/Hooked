package dev.thecodewarrior.hooked.hooks

import dev.thecodewarrior.hooked.hook.HookPlayerController
import dev.thecodewarrior.hooked.hook.HookType
import net.minecraft.entity.player.PlayerEntity

class BasicHookType(
    count: Int,
    range: Double,
    speed: Double,
    hookLength: Double,
    /**
     *
     */
    val pullStrength: Double,
    val boostHeight: Double
): HookType(count, range, speed, hookLength) {
    override val translationBase: String = "hooked.controller.basic"
    override val controlLangKeys: List<String> = if(count > 1)
        listOf(
            "$translationBase.controls.multi.fire",
            "$translationBase.controls.multi.fire_extra",
            "$translationBase.controls.multi.jump",
        )
    else
        listOf(
            "$translationBase.controls.single.fire",
            "$translationBase.controls.single.jump",
        )

    override fun createController(player: PlayerEntity): HookPlayerController {
        return BasicHookPlayerController(player, this)
    }
}