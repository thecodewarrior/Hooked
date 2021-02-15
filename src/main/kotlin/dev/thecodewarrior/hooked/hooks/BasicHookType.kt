package dev.thecodewarrior.hooked.hooks

import dev.thecodewarrior.hooked.hook.HookPlayerController
import dev.thecodewarrior.hooked.hook.HookType
import net.minecraft.entity.player.PlayerEntity

class BasicHookType(
    count: Int,
    range: Double,
    speed: Double,
    hookLength: Double,
    val pullStrength: Double,
    val boostHeight: Double
): HookType(count, range, speed, hookLength) {
    override val translationBase: String = "hooked.controller.basic"
    override val controlLangKeys: List<String> = listOfNotNull(
        "$translationBase.controls.fire",
        if(count > 1) "$translationBase.controls.fire_extra" else null,
        "$translationBase.controls.jump",
    )

    override fun createController(player: PlayerEntity): HookPlayerController {
        return BasicHookPlayerController(player, this)
    }
}