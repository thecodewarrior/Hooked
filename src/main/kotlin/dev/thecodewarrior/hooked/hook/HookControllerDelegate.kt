package dev.thecodewarrior.hooked.hook

import dev.thecodewarrior.hooked.HookedModSounds
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

    fun retractHook(hook: Hook, silently: Boolean = false) {
        if(hook.state == Hook.State.RETRACTING)
            return
        if(hook.state == Hook.State.PLANTED && !silently) {
            playWorldSound(Hook.hitSound(world, hook.block), hook.pos, 1f, 1f)
            playFeedbackSound(HookedModSounds.retractHook, 1f, 1f)
        }
        hook.state = Hook.State.RETRACTING
        markDirty(hook)
    }
}
