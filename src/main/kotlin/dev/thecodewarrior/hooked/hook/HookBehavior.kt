package dev.thecodewarrior.hooked.hook

import com.mojang.serialization.Lifecycle
import com.mojang.serialization.MapCodec
import dev.thecodewarrior.hooked.item.HookProperties
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.SimpleRegistry
import net.minecraft.text.Text
import net.minecraft.util.Identifier

interface HookBehavior {
    val type: HookBehaviorType<*>

    /**
     * The language keys to add to the item tooltip
     */
    fun controlsHelpText(hookProperties: HookProperties, fireKeyBindText: Text): List<Text>

    /**
     * Create a new player controller
     */
    fun createController(player: PlayerEntity, hookProperties: HookProperties): HookPlayerController

    companion object {
        val CODEC = HookBehaviorType.REGISTRY.codec
            .dispatch("type", HookBehavior::type, HookBehaviorType<*>::codec)
    }
}

data class HookBehaviorType<T : HookBehavior>(val codec: MapCodec<T>) {
    companion object {
        val REGISTRY = SimpleRegistry<HookBehaviorType<*>>(
            RegistryKey.ofRegistry(Identifier.of("hooked:hook_behavior_types")),
            Lifecycle.stable()
        )
    }
}
