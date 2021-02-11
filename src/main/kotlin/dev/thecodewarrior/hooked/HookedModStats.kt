package dev.thecodewarrior.hooked

import com.teamwizardry.librarianlib.core.util.mapSrgName
import ll.dev.thecodewarrior.mirror.Mirror
import net.minecraft.stats.IStatFormatter
import net.minecraft.stats.Stats
import net.minecraft.util.ResourceLocation

object HookedModStats {
    private lateinit var _hookOneCm: ResourceLocation
    val hookOneCm: ResourceLocation get() = _hookOneCm
    private lateinit var _hooksFired: ResourceLocation
    val hooksFired: ResourceLocation get() = _hooksFired


    fun register() {
        _hookOneCm = registerCustomStat.call(null, "hooked:hook_one_cm", IStatFormatter.DISTANCE)
        _hooksFired = registerCustomStat.call(null, "hooked:hooks_fired", IStatFormatter.DEFAULT)
    }

    private val registerCustomStat = Mirror.reflectClass<Stats>().getMethod(
        mapSrgName("func_199084_a"),
        Mirror.reflect<String>(), Mirror.reflect<IStatFormatter>()
    )
}