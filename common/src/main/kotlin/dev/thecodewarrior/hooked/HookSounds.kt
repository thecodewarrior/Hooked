package dev.thecodewarrior.hooked

import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier

object HookSounds {
    val FIRE_HOOK = register(Identifier.of(Hooked.MOD_ID, "fire_hook"))
    val RETRACT_HOOK = register(Identifier.of(Hooked.MOD_ID, "retract_hook"))
    val HOOK_HIT = register(Identifier.of(Hooked.MOD_ID, "hook_hit"))
    val HOOK_MISS = register(Identifier.of(Hooked.MOD_ID, "hook_miss"))
    val HOOK_DISLODGE = register(Identifier.of(Hooked.MOD_ID, "hook_dislodge"))

    private fun register(id: Identifier): SoundEvent {
        val event = SoundEvent.of(id)
        HookedRegistries.SOUND_EVENT.register(id) { event }
        return event
    }
}