package dev.thecodewarrior.hooked.hook

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.SoundEvent
import net.minecraft.util.math.vector.Vec3d
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
    fun fireEvent(event: HookEvent)

    fun playFeedbackSound(sound: SoundEvent, volume: Float, pitch: Float)
    fun playWorldSound(sound: SoundEvent, pos: Vec3d, volume: Float, pitch: Float)

    fun retractHook(hook: Hook, silently: Boolean = false) {
        retractHook(hook, HookPlayerController.DislodgeReason.EXPLICIT, silently)
    }

    fun retractHook(hook: Hook, reason: HookPlayerController.DislodgeReason, silently: Boolean = false) {
        if(hook.state == Hook.State.RETRACTING)
            return
        if(hook.state == Hook.State.PLANTED && !silently) {
            fireEvent(HookEvent(HookEvent.EventType.DISLODGE, hook.uuid, reason.ordinal))
        }
        hook.state = Hook.State.RETRACTING
        markDirty(hook)
    }
}
