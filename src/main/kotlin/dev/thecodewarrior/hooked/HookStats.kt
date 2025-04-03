package dev.thecodewarrior.hooked

import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.stat.Stat
import net.minecraft.stat.StatFormatter
import net.minecraft.stat.Stats
import net.minecraft.util.Identifier

object HookStats {
    @JvmField
    val HOOK_ONE_CM = register(Identifier.of("hooked:hook_one_cm"), StatFormatter.DISTANCE)

    @JvmField
    val HOOKS_FIRED = register(Identifier.of("hooked:hooks_fired"), StatFormatter.DEFAULT)

    private fun register(id: Identifier, formatter: StatFormatter): Stat<Identifier> {
        Registry.register(Registries.CUSTOM_STAT, id, id)
        return Stats.CUSTOM.getOrCreateStat(id, formatter)
    }
}