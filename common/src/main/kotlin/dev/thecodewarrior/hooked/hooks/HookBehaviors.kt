package dev.thecodewarrior.hooked.hooks

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.thecodewarrior.hooked.Hooked
import dev.thecodewarrior.hooked.hook.HookBehavior
import dev.thecodewarrior.hooked.hook.HookBehaviorType
import dev.thecodewarrior.hooked.hook.HookPlayerController
import dev.thecodewarrior.hooked.item.HookProperties
import dev.thecodewarrior.hooked.item.ItemComponents
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registry
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.dynamic.Codecs
import org.joml.Vector3f

object HookBehaviors {
    val BASIC_ID = Identifier.of(Hooked.MOD_ID, "basic")
    val BASIC = register(
        BASIC_ID,
        HookBehaviorType(BasicHookBehavior.CODEC, BasicHookBehavior::fromItemStack)
    )
    val FLIGHT_ID = Identifier.of(Hooked.MOD_ID, "flight")
    val FLIGHT = register(
        FLIGHT_ID,
        HookBehaviorType(FlightHookBehavior.CODEC, FlightHookBehavior::fromItemStack)
    )
    val NONE_ID = Identifier.of(Hooked.MOD_ID, "none")
    val NONE = register(
        NONE_ID,
        HookBehaviorType(NullHookBehavior.CODEC, NullHookBehavior::fromItemStack)
    )

    private fun <T : HookBehavior> register(id: Identifier, type: HookBehaviorType<T>): HookBehaviorType<T> {
        return Registry.register(HookBehaviorType.REGISTRY, id, type);
    }
}

data class BasicHookBehavior(
    val pullStrength: Double
) : HookBehavior {
    override val type: HookBehaviorType<*>
        get() = HookBehaviors.BASIC

    override fun controlsHelpText(hookProperties: HookProperties, fireKeyBindText: Text, vararg formatting: Formatting): List<Text> {
        return if(hookProperties.count > 1) {
            listOf(
                Text.translatable("hooked.controller.basic.controls.multi.fire", fireKeyBindText).formatted(*formatting),
                Text.translatable("hooked.controller.basic.controls.multi.fire_extra", fireKeyBindText).formatted(*formatting),
                Text.translatable("hooked.controller.basic.controls.multi.jump").formatted(*formatting),
            )
        } else {
            listOf(
                Text.translatable("hooked.controller.basic.controls.single.fire", fireKeyBindText).formatted(*formatting),
                Text.translatable("hooked.controller.basic.controls.single.jump").formatted(*formatting),
            )
        }
    }

    override fun createController(player: PlayerEntity, hookProperties: HookProperties): HookPlayerController {
        return BasicHookPlayerController(player, this)
    }

    override fun applyToItemSettings(itemSettings: Item.Settings) {
        itemSettings.component(ItemComponents.PULL_STRENGTH, pullStrength)
    }

    companion object {
        val CODEC = RecordCodecBuilder.mapCodec { builder ->
            builder.group(
                Codec.DOUBLE.fieldOf("pullStrength").forGetter(BasicHookBehavior::pullStrength)
            ).apply(builder, ::BasicHookBehavior)
        }

        fun fromItemStack(itemStack: ItemStack): BasicHookBehavior {
            return BasicHookBehavior(
                pullStrength = itemStack.get(ItemComponents.PULL_STRENGTH) ?: 1.0,
            )
        }
    }
}

data class FlightHookBehavior(
    val wireframeColor: Vector3f,
    val breakRangeFactor: Double
) : HookBehavior {
    override val type: HookBehaviorType<*>
        get() = HookBehaviors.FLIGHT

    override fun controlsHelpText(hookProperties: HookProperties, fireKeyBindText: Text, vararg formatting: Formatting): List<Text> {
        return listOf(
            Text.translatable("hooked.controller.flight.controls.tutorial").formatted(*formatting),
            Text.translatable("hooked.controller.flight.controls.fire", fireKeyBindText).formatted(*formatting),
            Text.translatable("hooked.controller.flight.controls.retract", fireKeyBindText).formatted(*formatting),
            Text.translatable("hooked.controller.flight.controls.jump").formatted(*formatting),
        )
    }

    override fun createController(player: PlayerEntity, hookProperties: HookProperties): HookPlayerController {
        return FlightHookPlayerController(player, this)
    }

    override fun applyToItemSettings(itemSettings: Item.Settings) {
        itemSettings.component(ItemComponents.WIREFRAME_COLOR, wireframeColor)
        itemSettings.component(ItemComponents.BREAK_RANGE_FACTOR, breakRangeFactor)
    }

    companion object {
        val DEFAULT_RANGE_FACTOR = 4.0
        val CODEC = RecordCodecBuilder.mapCodec { builder ->
            builder.group(
                Codecs.VECTOR_3F.fieldOf("wireframeColor").forGetter(FlightHookBehavior::wireframeColor),
                Codec.DOUBLE.fieldOf("breakRangeFactor").forGetter(FlightHookBehavior::breakRangeFactor)
            ).apply(builder, ::FlightHookBehavior)
        }

        fun fromItemStack(itemStack: ItemStack): FlightHookBehavior {
            return FlightHookBehavior(
                wireframeColor = itemStack.get(ItemComponents.WIREFRAME_COLOR) ?: Vector3f(1f, 0f, 0f),
                breakRangeFactor = itemStack.get(ItemComponents.BREAK_RANGE_FACTOR) ?: DEFAULT_RANGE_FACTOR,
            )
        }
    }
}


object NullHookBehavior : HookBehavior {
    override val type: HookBehaviorType<*>
        get() = HookBehaviors.NONE

    override fun controlsHelpText(hookProperties: HookProperties, fireKeyBindText: Text, vararg formatting: Formatting): List<Text> {
        return emptyList()
    }

    override fun createController(
        player: PlayerEntity,
        hookProperties: HookProperties
    ): HookPlayerController {
        return HookPlayerController.NONE
    }

    val CODEC = MapCodec.unit(NullHookBehavior)

    override fun applyToItemSettings(itemSettings: Item.Settings) {
        // nop
    }

    fun fromItemStack(itemStack: ItemStack): NullHookBehavior {
        return NullHookBehavior
    }
}