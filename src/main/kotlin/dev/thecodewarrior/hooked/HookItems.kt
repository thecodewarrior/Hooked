package dev.thecodewarrior.hooked

import dev.thecodewarrior.hooked.hook.HookBehavior
import dev.thecodewarrior.hooked.hooks.BasicHookBehavior
import dev.thecodewarrior.hooked.hooks.FlightHookBehavior
import dev.thecodewarrior.hooked.item.ChainAppearance
import dev.thecodewarrior.hooked.item.HookItem
import dev.thecodewarrior.hooked.item.HookModelInfo
import dev.thecodewarrior.hooked.item.ItemComponents
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.joml.Vector3f
import java.util.Optional

object HookItems {
    val itemGroupKey = RegistryKey.of(Registries.ITEM_GROUP.key, Identifier.of("hooked:item_group"))
    val itemGroup = FabricItemGroup.builder()
        .icon { ItemStack(IRON_ITEM) }
        .displayName(Text.translatable("itemGroup.hooked"))
        .build()

    val WOOD_ID = Identifier.of("hooked:wood_hook")
    val WOOD_ITEM = createItem(
        WOOD_ID,
        count = 1,
        range = 8.0,
        speed = 0.4,
        cooldown = 25,
        behavior = BasicHookBehavior(pullStrength = 0.2)
    )
    val IRON_ID = Identifier.of("hooked:iron_hook")
    val IRON_ITEM = createItem(
        IRON_ID,
        count = 2,
        range = 16.0,
        speed = 0.8,
        cooldown = 10,
        behavior = BasicHookBehavior(pullStrength = 0.4)
    )
    val DIAMOND_ID = Identifier.of("hooked:diamond_hook")
    val DIAMOND_ITEM = createItem(
        DIAMOND_ID,
        count = 4,
        range = 24.0,
        speed = 1.2,
        cooldown = 8,
        behavior = BasicHookBehavior(pullStrength = 1.0)
    )
    val ENDER_ID = Identifier.of("hooked:ender_hook")
    val ENDER_ITEM = createItem(
        ENDER_ID,
        count = 1,
        range = 64.0,
        speed = 64.0,
        cooldown = 2,
        behavior = BasicHookBehavior(pullStrength = 2.25),
        particleColorMin = Optional.of(Vector3f(0.36f, 0.12f, 0.4f)),
        particleColorMax = Optional.of(Vector3f(0.9f, 0.3f, 1.0f)),
    )
    val RED_ID = Identifier.of("hooked:red_hook")
    val RED_ITEM = createItem(
        RED_ID,
        count = 8,
        range = 48.0,
        speed = 1.2,
        cooldown = 5,
        behavior = FlightHookBehavior(wireframeColor = Vector3f(1f, 0f, 0f)),
        playerGap = 2.5,
    )

    val items = listOf(WOOD_ITEM, IRON_ITEM, DIAMOND_ITEM, ENDER_ITEM, RED_ITEM)

    private fun createItem(
        id: Identifier,
        count: Int,
        range: Double,
        speed: Double,
        cooldown: Int,
        behavior: HookBehavior,
        playerGap: Double = 0.0,
        particleColorMin: Optional<Vector3f> = Optional.empty(),
        particleColorMax: Optional<Vector3f> = Optional.empty(),
    ): HookItem {
        val settings =Item.Settings().maxCount(1)

        settings.component(ItemComponents.HOOK_COUNT, count)
        settings.component(ItemComponents.HOOK_RANGE, range)
        settings.component(ItemComponents.HOOK_SPEED, speed)
        settings.component(ItemComponents.FIRE_COOLDOWN, cooldown)
        settings.component(ItemComponents.HOOK_BEHAVIOR, behavior)
        settings.component(
            ItemComponents.HOOK_MODEL,
            HookModelInfo.DEFAULT.copy(texture = Identifier.of("hooked:textures/hook/${id.path}/hook.png"))
        )
        settings.component(ItemComponents.CHAIN_APPEARANCE, ChainAppearance(
            texture1 = Identifier.of("hooked:textures/hook/${id.path}/chain1.png"),
            texture2 = Identifier.of("hooked:textures/hook/${id.path}/chain2.png"),
            playerGap = playerGap,
            particleColorMin = particleColorMin,
            particleColorMax = particleColorMax,
        ))
        return HookItem(settings)
    }

    fun registerItems() {
        Registry.register(Registries.ITEM, WOOD_ID, WOOD_ITEM)
        Registry.register(Registries.ITEM, IRON_ID, IRON_ITEM)
        Registry.register(Registries.ITEM, DIAMOND_ID, DIAMOND_ITEM)
        Registry.register(Registries.ITEM, ENDER_ID, ENDER_ITEM)
        Registry.register(Registries.ITEM, RED_ID, RED_ITEM)

        Registry.register(Registries.ITEM_GROUP, itemGroupKey, itemGroup)
        ItemGroupEvents.modifyEntriesEvent(itemGroupKey).register { entries ->
            items.forEach(entries::add)
        }
    }
}