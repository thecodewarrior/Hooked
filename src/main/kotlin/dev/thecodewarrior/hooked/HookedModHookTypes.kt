package dev.thecodewarrior.hooked

import com.teamwizardry.librarianlib.core.util.kotlin.loc
import dev.thecodewarrior.hooked.hook.type.BasicHookType
import dev.thecodewarrior.hooked.hook.type.HookType
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

object HookedModHookTypes {
    val iron = BasicHookType(2, 16.0, 0.8, 0.4, 0.5, 0.05).also {
        it.registryName = loc("hooked:iron_hook")
    }

    @SubscribeEvent
    fun registerTypes(e: RegistryEvent.Register<HookType>) {
        e.registry.register(iron)
    }
}