package dev.thecodewarrior.hooked.item

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.thecodewarrior.hooked.Hooked
import dev.thecodewarrior.hooked.hook.HookBehavior
import dev.thecodewarrior.hooked.hooks.NullHookBehavior
import net.minecraft.component.ComponentType
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtOps
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.ColorCode
import net.minecraft.util.Identifier
import net.minecraft.util.dynamic.Codecs
import org.joml.Vector3f
import java.util.Optional
import kotlin.jvm.optionals.getOrNull
import kotlin.math.max

object ItemComponents {
    /**
     * The number of simultaneous hooks allowed
     */
    val HOOK_COUNT = register("hooked:hook_count", Codec.INT)

    /**
     * The maximum range from impact point to player
     */
    val HOOK_RANGE = register("hooked:hook_range", Codec.DOUBLE)

    /**
     * The speed of the fired hooks in m/t
     */
    val HOOK_SPEED = register("hooked:hook_speed", Codec.DOUBLE)

    /**
     * The cooldown between firing hooks
     */
    val FIRE_COOLDOWN = register("hooked:fire_cooldown", Codec.INT)

    val HOOK_MODEL = register("hooked:hook_model", HookModelInfo.CODEC)

    val CHAIN_APPEARANCE = register("hooked:chain_appearance", ChainAppearance.CODEC)

    val HOOK_BEHAVIOR = register("hooked:behavior", HookBehavior.CODEC)

    private fun <T> register(id: String, codec: Codec<T>): ComponentType<T> {
        return Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(id),
            ComponentType.builder<T>().codec(codec).build()
        )
    }
}

data class HookProperties(
    val count: Int,
    val range: Double,
    val speed: Double,
    val cooldown: Int,
    val hookModel: HookModelInfo,
    val chainAppearance: ChainAppearance,
    val behavior: HookBehavior,
) {
    /**
     * Returns null on parse error
     */
    fun toNBT(): NbtElement? {
        return CODEC.encodeStart(NbtOps.INSTANCE, this).resultOrPartial { logger.error(it) }.getOrNull()
    }

    companion object {
        /**
         * Returns null on parse error
         */
        fun fromNBT(tag: NbtElement): HookProperties? {
            return CODEC.parse(NbtOps.INSTANCE, tag).resultOrPartial { logger.error(it) }.getOrNull()
        }

        fun fromItemStack(stack: ItemStack): HookProperties? {
            val behavior = stack.get(ItemComponents.HOOK_BEHAVIOR) ?: return null
            return HookProperties(
                count = max(1, stack.get(ItemComponents.HOOK_COUNT) ?: 1),
                range = max(0.0, stack.get(ItemComponents.HOOK_RANGE) ?: 8.0),
                speed = max(0.0, stack.get(ItemComponents.HOOK_SPEED) ?: 0.5),
                cooldown = max(0, stack.get(ItemComponents.FIRE_COOLDOWN) ?: 20),
                hookModel = stack.get(ItemComponents.HOOK_MODEL) ?: HookModelInfo.DEFAULT,
                chainAppearance = stack.get(ItemComponents.CHAIN_APPEARANCE) ?: ChainAppearance.DEFAULT,
                behavior = behavior,
            )
        }

        val NONE = HookProperties(
            count = 0,
            range = 0.0,
            speed = 0.0,
            cooldown = 0,
            hookModel = HookModelInfo.DEFAULT,
            chainAppearance = ChainAppearance.DEFAULT,
            behavior = NullHookBehavior
        )

        val CODEC = RecordCodecBuilder.create { builder ->
            builder.group(
                Codec.INT.fieldOf("count").forGetter(HookProperties::count),
                Codec.DOUBLE.fieldOf("range").forGetter(HookProperties::range),
                Codec.DOUBLE.fieldOf("speed").forGetter(HookProperties::speed),
                Codec.INT.fieldOf("cooldown").forGetter(HookProperties::cooldown),
                HookModelInfo.CODEC.fieldOf("hookModel").forGetter(HookProperties::hookModel),
                ChainAppearance.CODEC.fieldOf("chainAppearance").forGetter(HookProperties::chainAppearance),
                HookBehavior.CODEC.fieldOf("behavior").forGetter(HookProperties::behavior),
            ).apply(builder, ::HookProperties)
        }

        val logger = Hooked.logManager.makeLogger<HookProperties>()
    }
}

data class HookModelInfo(
    /**
     * Currently only an OBJ file (support for json models tbd)
     * Hook should be pointing in the +Y direction, with the base of the hook at the origin
     */
    val model: Identifier,
    /**
     * The texture for the OBJ file (switch to json models in the future)
     */
    val texture: Identifier,
    /**
     * Length of the hook model
     */
    val hookLength: Float,
) {
    companion object {
        val DEFAULT = HookModelInfo(
            Identifier.of("hooked:models/hook/base.obj"),
            Identifier.of("hooked:textures/hook/base/hook.png"),
            0.5f
        )

        val CODEC = RecordCodecBuilder.create { builder ->
            builder.group(
                Identifier.CODEC.optionalFieldOf("model", DEFAULT.model).forGetter(HookModelInfo::model),
                Identifier.CODEC.fieldOf("texture").forGetter(HookModelInfo::texture),
                Codec.FLOAT.optionalFieldOf("hookLength", DEFAULT.hookLength).forGetter(HookModelInfo::hookLength),
            ).apply(builder, ::HookModelInfo)
        }
    }
}

data class ChainAppearance(
    val texture1: Identifier,
    val texture2: Identifier,
    val playerGap: Double,
    val particleColorMin: Optional<Vector3f>,
    val particleColorMax: Optional<Vector3f>,
) {
    companion object {
        val DEFAULT = ChainAppearance(
            Identifier.of("hooked:textures/hook/base/chain1.png"),
            Identifier.of("hooked:textures/hook/base/chain2.png"),
            0.0,
            Optional.empty(),
            Optional.empty(),
        )

        val CODEC = RecordCodecBuilder.create { builder ->
            builder.group(
                Identifier.CODEC.fieldOf("texture1").forGetter(ChainAppearance::texture1),
                Identifier.CODEC.fieldOf("texture2").forGetter(ChainAppearance::texture2),
                Codec.DOUBLE.optionalFieldOf("playerGap", 0.0).forGetter(ChainAppearance::playerGap),
                Codecs.VECTOR_3F.optionalFieldOf("particleColorMin").forGetter(ChainAppearance::particleColorMin),
                Codecs.VECTOR_3F.optionalFieldOf("particleColorMax").forGetter(ChainAppearance::particleColorMax),
            ).apply(builder, ::ChainAppearance)
        }
    }
}