package dev.thecodewarrior.hooked

import net.minecraft.entity.player.PlayerEntity

interface HookedProxy {
    /**
     * If [disable] is true, overrides the current auto jump setting.
     */
    fun disableAutoJump(player: PlayerEntity, disable: Boolean)
}