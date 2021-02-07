package dev.thecodewarrior.hooked

import com.teamwizardry.librarianlib.core.util.loc
import com.teamwizardry.librarianlib.foundation.registration.RegistrationManager
import com.teamwizardry.librarianlib.foundation.registration.SoundEventSpec
import net.minecraft.util.SoundEvent

object HookedModSounds {
    lateinit var fireHook: SoundEvent
        private set
    lateinit var retractHook: SoundEvent
        private set
    lateinit var hookHit: SoundEvent
        private set
    lateinit var hookMiss: SoundEvent
        private set
    lateinit var hookDislodge: SoundEvent
        private set

    fun registerSounds(registrationManager: RegistrationManager) {
        fireHook = registrationManager.add(
            SoundEventSpec("fire_hook")
                .subtitle("hooked.subtitle.fire_hook")
//                .sound(loc("hooked:hooks/fire"))
                .sound(loc("minecraft:random/bow"))
        )
        retractHook = registrationManager.add(
            SoundEventSpec("retract_hook")
                .subtitle("hooked.subtitle.retract_hook")
//                .sound(loc("hooked:hooks/retract"))
                .sound(loc("minecraft:entity/bobber/retrieve1")) {
                    pitch = 2.4
                }
                .sound(loc("minecraft:entity/bobber/retrieve2")) {
                    pitch = 2.4
                }
        )
        hookHit = registrationManager.add(
            SoundEventSpec("hook_hit")
                .subtitle("hooked.subtitle.hook_hit")
//                .sound(loc("hooked:hooks/hit"))
                .sound(loc("minecraft:random/bowhit1"))
                .sound(loc("minecraft:random/bowhit2"))
                .sound(loc("minecraft:random/bowhit3"))
                .sound(loc("minecraft:random/bowhit4"))
        )
        hookMiss = registrationManager.add(
            SoundEventSpec("hook_miss")
                .subtitle("hooked.subtitle.hook_miss")
//                .sound(loc("hooked:hooks/miss"))
                .sound(loc("minecraft:entity/endereye/dead1"))
                .sound(loc("minecraft:entity/endereye/dead2"))
        )
        hookDislodge = registrationManager.add(
            SoundEventSpec("hook_dislodge")
                .subtitle("hooked.subtitle.hook_dislodge")
//                .sound(loc("hooked:hooks/dislodge"))
                .sound(loc("minecraft:entity/leashknot/break1"))
                .sound(loc("minecraft:entity/leashknot/break2"))
                .sound(loc("minecraft:entity/leashknot/break3"))
        )
    }
}