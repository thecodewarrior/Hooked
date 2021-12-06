package dev.thecodewarrior.hooked

import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.config.ModConfig
import thedarkcolour.kotlinforforge.KotlinModLoadingContext

object HookedModConfig {
    val woodHook = BasicHookConfig(
        count = 1,
        range = 8.0,
        speed = 0.4,
        hookLength = 0.5,
        cooldown = 25,
        pullStrength = 0.2,
        boostHeight = 1.5
    )

    val types: List<HookType> = listOf(
        .also { it.registryName = loc("hooked:wood_hook") },
        BasicHookParameters(
            count = 2,
            range = 16.0,
            speed = 0.8,
            hookLength = 0.5,
            cooldown = 10,
            pullStrength = 0.4,
            boostHeight = 2.5
        ).also { it.registryName = loc("hooked:iron_hook") },
        BasicHookParameters(
            count = 4,
            range = 24.0,
            speed = 1.2,
            hookLength = 0.5,
            cooldown = 8,
            pullStrength = 1.0,
            boostHeight = 2.5
        ).also { it.registryName = loc("hooked:diamond_hook") },
        EnderHookParameters(
            count = 1,
            range = 64.0,
            speed = 64.0,
            hookLength = 0.5,
            cooldown = 0,
            pullStrength = 2.25,
            boostHeight = 2.5
        ).also { it.registryName = loc("hooked:ender_hook") },
        FlightHookParameters(
            count = 8,
            range = 48.0,
            speed = 1.2,
            hookLength = 0.5,
            cooldown = 5,
            pullStrength = 1.0,
        ).also { it.registryName = loc("hooked:red_hook") },
    )

    init {
        KotlinModLoadingContext.get().getKEventBus().register(this)
        ModLoadingContext.get().registerConfig(
            ModConfig.Type.COMMON,
            ForgeConfigSpec.Builder().also { spec ->
                spec.define("", "")
            }.build()
        )
    }

    @SubscribeEvent
    fun loadConfig
}