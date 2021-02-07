package dev.thecodewarrior.hooked.hook

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.SoundEvent

/**
 * Provides the hook controller with access to information or functionality from the hook data or processor
 */
interface HookControllerDelegate {
    val player: PlayerEntity
    val hooks: List<Hook>

    fun markDirty(hook: Hook)
    fun enqueueSound(sound: SoundEvent)
}