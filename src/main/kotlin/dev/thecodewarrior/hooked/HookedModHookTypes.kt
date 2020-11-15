package dev.thecodewarrior.hooked

import com.teamwizardry.librarianlib.core.util.kotlin.loc
import dev.thecodewarrior.hooked.hook.type.BasicHookType
import dev.thecodewarrior.hooked.hook.type.HookType
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

object HookedModHookTypes {
    val types: List<HookType> = listOf(
        BasicHookType(1, 8.0, 0.4, 0.5, 0.2, 0.05).also { it.registryName = loc("hooked:wood_hook") },
        BasicHookType(2, 16.0, 0.8, 0.5, 0.4, 0.05).also { it.registryName = loc("hooked:iron_hook") },
        BasicHookType(4, 24.0, 1.2, 0.5, 1.0, 0.05).also { it.registryName = loc("hooked:diamond_hook") },
        BasicHookType(1, 64.0, 64.0, 0.5, 2.25, 0.05).also { it.registryName = loc("hooked:ender_hook") },
    )

    @SubscribeEvent
    fun registerTypes(e: RegistryEvent.Register<HookType>) {
        e.registry.register(HookType.NONE)
        types.forEach {
            e.registry.register(it)
        }
    }
}