package dev.thecodewarrior.hooked.hook

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.SoundEvent
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.world.World

/**
 * Provides the hook controller with access to information or functionality from the hook data or processor
 */
interface HookControllerDelegate {
    val player: PlayerEntity
    val world: World
    val hooks: List<Hook>

    val cooldown: Int
    fun triggerCooldown()

    fun markDirty(hook: Hook)
    fun forceFullSyncToClient()
    fun forceFullSyncToOthers()

    fun playFeedbackSound(sound: SoundEvent, volume: Float, pitch: Float)
    fun playWorldSound(sound: SoundEvent, pos: Vector3d, volume: Float, pitch: Float)
}
