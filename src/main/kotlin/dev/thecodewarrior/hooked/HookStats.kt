package dev.thecodewarrior.hooked

import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.stat.StatFormatter
import net.minecraft.stat.Stats
import net.minecraft.util.Identifier

object HookStats {
    val HOOK_ONE_CM_KEY = Identifier.of("hooked:hook_one_cm")
    val HOOKS_FIRED_KEY = Identifier.of("hooked:hooks_fired")

    @JvmField
    val HOOK_ONE_CM = Stats.CUSTOM.getOrCreateStat(HOOK_ONE_CM_KEY, StatFormatter.DISTANCE)

    @JvmField
    val HOOKS_FIRED = Stats.CUSTOM.getOrCreateStat(HOOKS_FIRED_KEY, StatFormatter.DEFAULT)

    fun registerStats() {
        Registry.register(Registries.CUSTOM_STAT, HOOK_ONE_CM_KEY, HOOK_ONE_CM_KEY)
        Registry.register(Registries.CUSTOM_STAT, HOOKS_FIRED_KEY, HOOKS_FIRED_KEY)
    }
}