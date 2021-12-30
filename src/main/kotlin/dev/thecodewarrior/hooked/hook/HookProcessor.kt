package dev.thecodewarrior.hooked.hook

import net.minecraft.entity.player.PlayerEntity

interface HookProcessor {
    fun tick(player: PlayerEntity)

    /**
     * Returns true when the hook is "active". i.e. when a hook is planted
     */
    fun isHookActive(player: PlayerEntity): Boolean
}

object NullHookProcessor: HookProcessor {
    override fun tick(player: PlayerEntity) {
    }

    override fun isHookActive(player: PlayerEntity): Boolean = false
}
