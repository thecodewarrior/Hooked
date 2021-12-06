package dev.thecodewarrior.hooked

import dev.thecodewarrior.hooked.hook.HookType
import dev.thecodewarrior.hooked.hooks.BasicHookType
import dev.thecodewarrior.hooked.hooks.EnderHookType
import dev.thecodewarrior.hooked.hooks.FlightHookType
import dev.thecodewarrior.hooked.item.HookItem
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

object HookTypes {
    val ITEM_GROUP = FabricItemGroupBuilder.build(Identifier("hooked:item_group")) { ItemStack(IRON_ITEM) }

    val WOOD_ID = Identifier("hooked:wood_hook")
    val WOOD_TYPE = BasicHookType(
        count = 1,
        range = 8.0,
        speed = 0.4,
        hookLength = 0.5,
        cooldown = 25,
        pullStrength = 0.2,
        boostHeight = 1.5
    )
    val WOOD_ITEM = createItem(WOOD_TYPE)

    val IRON_ID = Identifier("hooked:iron_hook")
    val IRON_TYPE = BasicHookType(
        count = 2,
        range = 16.0,
        speed = 0.8,
        hookLength = 0.5,
        cooldown = 10,
        pullStrength = 0.4,
        boostHeight = 2.5
    )
    val IRON_ITEM = createItem(IRON_TYPE)

    val DIAMOND_ID = Identifier("hooked:diamond_hook")
    val DIAMOND_TYPE = BasicHookType(
        count = 4,
        range = 24.0,
        speed = 1.2,
        hookLength = 0.5,
        cooldown = 8,
        pullStrength = 1.0,
        boostHeight = 2.5
    )
    val DIAMOND_ITEM = createItem(DIAMOND_TYPE)

    val ENDER_ID = Identifier("hooked:ender_hook")
    val ENDER_TYPE = EnderHookType(
        count = 1,
        range = 64.0,
        speed = 64.0,
        hookLength = 0.5,
        cooldown = 0,
        pullStrength = 2.25,
        boostHeight = 2.5
    )
    val ENDER_ITEM = createItem(ENDER_TYPE)

    val RED_ID = Identifier("hooked:red_hook")
    val RED_TYPE = FlightHookType(
        count = 8,
        range = 48.0,
        speed = 1.2,
        hookLength = 0.5,
        cooldown = 5,
        pullStrength = 1.0,
    )
    val RED_ITEM = createItem(RED_TYPE)

    fun registerTypes() {
        Registry.register(Hooked.hookRegistry, WOOD_ID, WOOD_TYPE)
        Registry.register(Hooked.hookRegistry, IRON_ID, IRON_TYPE)
        Registry.register(Hooked.hookRegistry, DIAMOND_ID, DIAMOND_TYPE)
        Registry.register(Hooked.hookRegistry, ENDER_ID, ENDER_TYPE)
        Registry.register(Hooked.hookRegistry, RED_ID, RED_TYPE)
        Registry.register(Hooked.hookRegistry, Identifier("hooked:none"), HookType.NONE)
    }

    fun createItem(type: HookType): HookItem {
        return HookItem(Item.Settings().maxCount(1).group(ITEM_GROUP), type)
    }

    fun registerItems() {
        Registry.register(Registry.ITEM, WOOD_ID, WOOD_ITEM)
        Registry.register(Registry.ITEM, IRON_ID, IRON_ITEM)
        Registry.register(Registry.ITEM, DIAMOND_ID, DIAMOND_ITEM)
        Registry.register(Registry.ITEM, ENDER_ID, ENDER_ITEM)
        Registry.register(Registry.ITEM, RED_ID, RED_ITEM)
    }
}