package dev.thecodewarrior.hooked

import dev.thecodewarrior.hooked.hook.HookType
import dev.thecodewarrior.hooked.hooks.BasicHookType
import dev.thecodewarrior.hooked.hooks.EnderHookType
import dev.thecodewarrior.hooked.hooks.FlightHookType
import dev.thecodewarrior.hooked.item.HookItem
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.fabricmc.fabric.api.event.registry.RegistryAttribute
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.text.Text
import net.minecraft.util.Identifier

object HookTypes {
    val HOOK_TYPE_REGISTRY_KEY = RegistryKey.ofRegistry<HookType>(Identifier.of("hooked:hook_type"))

    val HOOK_TYPE_REGISTRY = FabricRegistryBuilder.createDefaulted(HOOK_TYPE_REGISTRY_KEY, Identifier.of("hooked:none"))
        .attribute(RegistryAttribute.SYNCED)
        .buildAndRegister()

    val WOOD_ID = Identifier.of("hooked:wood_hook")
    val WOOD_TYPE = BasicHookType(
        count = 1,
        range = 8.0,
        speed = 0.4,
        hookLength = 0.5,
        cooldown = 25,
        pullStrength = 0.2,
        boostHeight = 2.5
    )

    val IRON_ID = Identifier.of("hooked:iron_hook")
    val IRON_TYPE = BasicHookType(
        count = 2,
        range = 16.0,
        speed = 0.8,
        hookLength = 0.5,
        cooldown = 10,
        pullStrength = 0.4,
        boostHeight = 2.5
    )

    val DIAMOND_ID = Identifier.of("hooked:diamond_hook")
    val DIAMOND_TYPE = BasicHookType(
        count = 4,
        range = 24.0,
        speed = 1.2,
        hookLength = 0.5,
        cooldown = 8,
        pullStrength = 1.0,
        boostHeight = 2.5
    )

    val ENDER_ID = Identifier.of("hooked:ender_hook")
    val ENDER_TYPE = EnderHookType(
        count = 1,
        range = 64.0,
        speed = 64.0,
        hookLength = 0.5,
        cooldown = 2,
        pullStrength = 2.25,
        boostHeight = 2.5
    )

    val RED_ID = Identifier.of("hooked:red_hook")
    val RED_TYPE = FlightHookType(
        count = 8,
        range = 48.0,
        speed = 1.2,
        hookLength = 0.5,
        cooldown = 5,
        pullStrength = 1.0,
    )

    val types = listOf(WOOD_TYPE, IRON_TYPE, DIAMOND_TYPE, ENDER_TYPE, RED_TYPE)

    fun registerTypes() {
        Registry.register(HOOK_TYPE_REGISTRY, WOOD_ID, WOOD_TYPE)
        Registry.register(HOOK_TYPE_REGISTRY, IRON_ID, IRON_TYPE)
        Registry.register(HOOK_TYPE_REGISTRY, DIAMOND_ID, DIAMOND_TYPE)
        Registry.register(HOOK_TYPE_REGISTRY, ENDER_ID, ENDER_TYPE)
        Registry.register(HOOK_TYPE_REGISTRY, RED_ID, RED_TYPE)
        Registry.register(HOOK_TYPE_REGISTRY, Identifier.of("hooked:none"), HookType.NONE)
    }
}