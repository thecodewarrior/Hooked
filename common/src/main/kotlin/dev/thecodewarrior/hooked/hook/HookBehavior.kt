package dev.thecodewarrior.hooked.hook

import com.mojang.serialization.DataResult
import com.mojang.serialization.Lifecycle
import com.mojang.serialization.MapCodec
import dev.thecodewarrior.hooked.Hooked
import dev.thecodewarrior.hooked.hooks.HookBehaviors
import dev.thecodewarrior.hooked.item.HookProperties
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.SimpleRegistry
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import kotlin.jvm.optionals.getOrNull

interface HookBehavior {
    val type: HookBehaviorType<*>

    /**
     * The language keys to add to the item tooltip
     */
    fun controlsHelpText(hookProperties: HookProperties, fireKeyBindText: Text, vararg formatting: Formatting): List<Text>

    /**
     * Create a new player controller
     */
    fun createController(player: PlayerEntity, hookProperties: HookProperties): HookPlayerController

    /**
     * Applies this behavior as a set of default item components
     */
    fun applyToItemSettings(itemSettings: Item.Settings)

    companion object {
        val CODEC = HookBehaviorType.REGISTRY.codec
            .dispatch("type", HookBehavior::type, HookBehaviorType<*>::codec)
    }
}

data class HookBehaviorType<T : HookBehavior>(val codec: MapCodec<T>, val fromItemStack: (ItemStack) -> T) {
    companion object {
        val REGISTRY = SimpleRegistry<HookBehaviorType<*>>(
            RegistryKey.ofRegistry(Identifier.of(Hooked.MOD_ID, "hook_behavior_types")),
            Lifecycle.stable()
        )
        val CODEC = Identifier.CODEC.comapFlatMap<HookBehaviorType<*>>(
            { key ->
                REGISTRY.get(key)?.let { DataResult.success(it) }
                    ?: DataResult.error { "Unknown behavior type '$key'" }
            },
            { type ->
                REGISTRY.getKey(type).getOrNull()?.value
                    ?: HookBehaviors.NONE_ID
            }
        )
    }
}
