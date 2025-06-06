package dev.thecodewarrior.hooked

import dev.architectury.registry.CreativeTabRegistry
import dev.architectury.registry.registries.RegistrySupplier
import dev.thecodewarrior.hooked.hook.HookBehavior
import dev.thecodewarrior.hooked.hooks.BasicHookBehavior
import dev.thecodewarrior.hooked.hooks.FlightHookBehavior
import dev.thecodewarrior.hooked.item.ChainAppearance
import dev.thecodewarrior.hooked.item.HookModelInfo
import dev.thecodewarrior.hooked.item.ItemComponents
import dev.thecodewarrior.hooked.platform.HookedPlatformCommon
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.joml.Vector3f
import java.util.Optional

object HookItems {
    val itemGroup = HookedRegistries.ITEM_GROUP.register(Identifier.of(Hooked.MOD_ID, "item_group")) {
        CreativeTabRegistry.create(Text.translatable("itemGroup.hooked")) { ItemStack(IRON_HOOK) }
    }

    val WOOD_HOOK = createItem(
        Identifier.of(Hooked.MOD_ID, "wood_hook"),
        count = 1,
        range = 8.0,
        speed = 0.4,
        cooldown = 25,
        behavior = BasicHookBehavior(pullStrength = 0.2)
    )
    val IRON_HOOK = createItem(
        Identifier.of(Hooked.MOD_ID, "iron_hook"),
        count = 2,
        range = 16.0,
        speed = 0.8,
        cooldown = 10,
        behavior = BasicHookBehavior(pullStrength = 0.4)
    )
    val DIAMOND_HOOK = createItem(
        Identifier.of(Hooked.MOD_ID, "diamond_hook"),
        count = 4,
        range = 24.0,
        speed = 1.2,
        cooldown = 8,
        behavior = BasicHookBehavior(pullStrength = 1.0)
    )
    val ENDER_HOOK = createItem(
        Identifier.of(Hooked.MOD_ID, "ender_hook"),
        count = 1,
        range = 64.0,
        speed = 64.0,
        cooldown = 2,
        behavior = BasicHookBehavior(pullStrength = 2.25),
        particleColorMin = Optional.of(Vector3f(0.36f, 0.12f, 0.4f)),
        particleColorMax = Optional.of(Vector3f(0.9f, 0.3f, 1.0f)),
    )
    val REDSTONE_HOOK = createItem(
        Identifier.of(Hooked.MOD_ID, "redstone_hook"),
        count = 8,
        range = 64.0,
        speed = 2.5,
        cooldown = 5,
        behavior = FlightHookBehavior(
            wireframeColor = Vector3f(1f, 0f, 0f),
            breakRangeFactor = FlightHookBehavior.DEFAULT_RANGE_FACTOR
        ),
        playerGap = 2.5,
    )
    val CUSTOM_HOOK = createItem(
        Identifier.of(Hooked.MOD_ID, "custom_hook"),
        count = 4,
        range = 24.0,
        speed = 1.2,
        cooldown = 8,
        behavior = BasicHookBehavior(pullStrength = 1.0),
        addToCreativeTab = false
    )

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
        addToCreativeTab: Boolean = true
    ): RegistrySupplier<Item> {
        val settings = Item.Settings().maxCount(1)

        if (addToCreativeTab) {
            settings.`arch$tab`(itemGroup)
        }

        settings.component(ItemComponents.HOOK_COUNT, count)
        settings.component(ItemComponents.HOOK_RANGE, range)
        settings.component(ItemComponents.HOOK_SPEED, speed)
        settings.component(ItemComponents.FIRE_COOLDOWN, cooldown)
        settings.component(ItemComponents.HOOK_BEHAVIOR, behavior.type)
        behavior.applyToItemSettings(settings)
        settings.component(
            ItemComponents.HOOK_MODEL,
            HookModelInfo.DEFAULT.copy(
                texture = Identifier.of(Hooked.MOD_ID, "textures/hook/${id.path}/hook.png")
            )
        )
        settings.component(ItemComponents.CHAIN_APPEARANCE, ChainAppearance(
            texture1 = Identifier.of(Hooked.MOD_ID, "textures/hook/${id.path}/chain1.png"),
            texture2 = Identifier.of(Hooked.MOD_ID, "textures/hook/${id.path}/chain2.png"),
            playerGap = playerGap,
            particleColorMin = particleColorMin,
            particleColorMax = particleColorMax,
        ))

        return HookedRegistries.ITEM.register(id) {
            HookedPlatformCommon.instance.createHookItem(settings)
        }
    }
}