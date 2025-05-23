package dev.thecodewarrior.hooked

import net.minecraft.stat.Stat
import net.minecraft.stat.StatFormatter
import net.minecraft.stat.Stats
import net.minecraft.util.Identifier

object HookStats {
    val hookOneCmStat by register(Identifier.of(Hooked.MOD_ID, "hook_one_cm"), StatFormatter.DISTANCE)

    val hooksFiredStat by register(Identifier.of(Hooked.MOD_ID, "hooks_fired"), StatFormatter.DEFAULT)

    private fun register(id: Identifier, formatter: StatFormatter): Lazy<Stat<Identifier>> {
        HookedRegistries.CUSTOM_STAT.register(id) { id }
        return lazy {
            Stats.CUSTOM.getOrCreateStat(id, formatter)
        }
    }
}