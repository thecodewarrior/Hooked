package dev.thecodewarrior.hooked

import com.teamwizardry.librarianlib.core.util.loc
import dev.thecodewarrior.hooked.hook.type.BasicHookType
import dev.thecodewarrior.hooked.hook.type.FlightHookType
import dev.thecodewarrior.hooked.hook.type.HookType
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

object HookedModHookTypes {
    val types: List<HookType> = listOf(
        BasicHookType(
            count = 1,
            range = 8.0,
            speed = 0.4,
            hookLength = 0.5,
            pullStrength = 0.2,
            jumpBoost = 0.05
        ).also { it.registryName = loc("hooked:wood_hook") },
        BasicHookType(
            count = 2,
            range = 16.0,
            speed = 0.8,
            hookLength = 0.5,
            pullStrength = 0.4,
            jumpBoost = 0.05
        ).also { it.registryName = loc("hooked:iron_hook") },
        BasicHookType(
            count = 4,
            range = 24.0,
            speed = 1.2,
            hookLength = 0.5,
            pullStrength = 1.0,
            jumpBoost = 0.05
        ).also { it.registryName = loc("hooked:diamond_hook") },
        BasicHookType(
            count = 1,
            range = 64.0,
            speed = 64.0,
            hookLength = 0.5,
            pullStrength = 2.25,
            jumpBoost = 0.05
        ).also { it.registryName = loc("hooked:ender_hook") },
        FlightHookType(
            count = 8,
            range = 48.0,
            speed = 1.2,
            hookLength = 0.5,
            pullStrength = 1.0,
            jumpBoost = 0.05,
        ).also { it.registryName = loc("hooked:red_hook") },
    )

    @SubscribeEvent
    fun registerTypes(e: RegistryEvent.Register<HookType>) {
        e.registry.register(HookType.NONE)
        types.forEach {
            e.registry.register(it)
        }
    }
}