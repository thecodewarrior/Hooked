package dev.thecodewarrior.hooked.hooks

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.thecodewarrior.hooked.Hooked
import dev.thecodewarrior.hooked.hook.HookBehavior
import dev.thecodewarrior.hooked.hook.HookBehaviorType
import dev.thecodewarrior.hooked.hook.HookPlayerController
import dev.thecodewarrior.hooked.item.HookProperties
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.Registry
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.dynamic.Codecs
import org.joml.Vector3f

object HookBehaviors {
    val BASIC = register(Identifier.of(Hooked.MOD_ID, "basic"), HookBehaviorType(BasicHookBehavior.CODEC))
    val FLIGHT = register(Identifier.of(Hooked.MOD_ID, "flight"), HookBehaviorType(FlightHookBehavior.CODEC))
    val NONE = register(Identifier.of(Hooked.MOD_ID, "none"), HookBehaviorType(NullHookBehavior.CODEC))

    private fun <T : HookBehavior> register(id: Identifier, type: HookBehaviorType<T>): HookBehaviorType<T> {
        return Registry.register(HookBehaviorType.REGISTRY, id, type);
    }
}

data class BasicHookBehavior(
    val pullStrength: Double
) : HookBehavior {
    override val type: HookBehaviorType<*>
        get() = HookBehaviors.BASIC

    override fun controlsHelpText(hookProperties: HookProperties, fireKeyBindText: Text): List<Text> {
        return if(hookProperties.count > 1) {
            listOf(
                Text.translatable("hooked.controller.basic.controls.multi.fire", fireKeyBindText),
                Text.translatable("hooked.controller.basic.controls.multi.fire_extra", fireKeyBindText),
                Text.translatable("hooked.controller.basic.controls.multi.jump"),
            )
        } else {
            listOf(
                Text.translatable("hooked.controller.basic.controls.single.fire", fireKeyBindText),
                Text.translatable("hooked.controller.basic.controls.single.jump"),
            )
        }
    }

    override fun createController(player: PlayerEntity, hookProperties: HookProperties): HookPlayerController {
        return BasicHookPlayerController(player, this)
    }

    companion object {
        val CODEC = RecordCodecBuilder.mapCodec { builder ->
            builder.group(
                Codec.DOUBLE.fieldOf("pullStrength").forGetter(BasicHookBehavior::pullStrength)
            ).apply(builder, ::BasicHookBehavior)
        }
    }
}

data class FlightHookBehavior(
    val wireframeColor: Vector3f
) : HookBehavior {
    override val type: HookBehaviorType<*>
        get() = HookBehaviors.FLIGHT

    override fun controlsHelpText(hookProperties: HookProperties, fireKeyBindText: Text): List<Text> {
        return listOf(
            Text.translatable("hooked.controller.flight.controls.fire", fireKeyBindText),
            Text.translatable("hooked.controller.flight.controls.retract", fireKeyBindText),
            Text.translatable("hooked.controller.flight.controls.jump"),
        )
    }

    override fun createController(player: PlayerEntity, hookProperties: HookProperties): HookPlayerController {
        return FlightHookPlayerController(player, this)
    }

    companion object {
        val CODEC = RecordCodecBuilder.mapCodec { builder ->
            builder.group(
                Codecs.VECTOR_3F.fieldOf("wireframeColor").forGetter(FlightHookBehavior::wireframeColor)
            ).apply(builder, ::FlightHookBehavior)
        }
    }
}


object NullHookBehavior : HookBehavior {
    override val type: HookBehaviorType<*>
        get() = HookBehaviors.NONE

    override fun controlsHelpText(hookProperties: HookProperties, fireKeyBindText: Text): List<Text> {
        return emptyList()
    }

    override fun createController(
        player: PlayerEntity,
        hookProperties: HookProperties
    ): HookPlayerController {
        return HookPlayerController.NONE
    }

    val CODEC = MapCodec.unit(NullHookBehavior)
}