package dev.thecodewarrior.hooked

import dev.thecodewarrior.hooked.hook.HookType
import dev.thecodewarrior.hooked.item.HookItem
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.text.Text
import net.minecraft.util.Identifier

object HookItems {
    val itemGroupKey = RegistryKey.of(Registries.ITEM_GROUP.key, Identifier.of("hooked:item_group"))
    val itemGroup = FabricItemGroup.builder()
        .icon { ItemStack(IRON_ITEM) }
        .displayName(Text.translatable("itemGroup.hooked"))
        .build()

    val WOOD_ITEM = createItem(HookTypes.WOOD_TYPE)
    val IRON_ITEM = createItem(HookTypes.IRON_TYPE)
    val DIAMOND_ITEM = createItem(HookTypes.DIAMOND_TYPE)
    val ENDER_ITEM = createItem(HookTypes.ENDER_TYPE)
    val RED_ITEM = createItem(HookTypes.RED_TYPE)

    val items = listOf(WOOD_ITEM, IRON_ITEM, DIAMOND_ITEM, ENDER_ITEM, RED_ITEM)

    private fun createItem(type: HookType): HookItem {
        return HookItem(Item.Settings().maxCount(1), type)
    }

    fun registerItems() {
        Registry.register(Registries.ITEM, HookTypes.WOOD_ID, WOOD_ITEM)
        Registry.register(Registries.ITEM, HookTypes.IRON_ID, IRON_ITEM)
        Registry.register(Registries.ITEM, HookTypes.DIAMOND_ID, DIAMOND_ITEM)
        Registry.register(Registries.ITEM, HookTypes.ENDER_ID, ENDER_ITEM)
        Registry.register(Registries.ITEM, HookTypes.RED_ID, RED_ITEM)

        Registry.register(Registries.ITEM_GROUP, itemGroupKey, itemGroup)
        ItemGroupEvents.modifyEntriesEvent(itemGroupKey).register { entries ->
            items.forEach(entries::add)
        }
    }
}