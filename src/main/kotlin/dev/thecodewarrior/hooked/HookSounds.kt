package dev.thecodewarrior.hooked

import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier

object HookSounds {
    val FIRE_HOOK_ID = Identifier.of("hooked:fire_hook")
    val FIRE_HOOK_EVENT = SoundEvent.of(FIRE_HOOK_ID)
    val RETRACT_HOOK_ID = Identifier.of("hooked:retract_hook")
    val RETRACT_HOOK_EVENT = SoundEvent.of(RETRACT_HOOK_ID)
    val HOOK_HIT_ID = Identifier.of("hooked:hook_hit")
    val HOOK_HIT_EVENT = SoundEvent.of(HOOK_HIT_ID)
    val HOOK_MISS_ID = Identifier.of("hooked:hook_miss")
    val HOOK_MISS_EVENT = SoundEvent.of(HOOK_MISS_ID)
    val HOOK_DISLODGE_ID = Identifier.of("hooked:hook_dislodge")
    val HOOK_DISLODGE_EVENT = SoundEvent.of(HOOK_DISLODGE_ID)

    fun registerSounds() {
        Registry.register(Registries.SOUND_EVENT, FIRE_HOOK_ID, FIRE_HOOK_EVENT)
        Registry.register(Registries.SOUND_EVENT, RETRACT_HOOK_ID, RETRACT_HOOK_EVENT)
        Registry.register(Registries.SOUND_EVENT, HOOK_HIT_ID, HOOK_HIT_EVENT)
        Registry.register(Registries.SOUND_EVENT, HOOK_MISS_ID, HOOK_MISS_EVENT)
        Registry.register(Registries.SOUND_EVENT, HOOK_DISLODGE_ID, HOOK_DISLODGE_EVENT)
    }
}