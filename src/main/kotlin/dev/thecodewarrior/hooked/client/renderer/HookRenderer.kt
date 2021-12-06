package dev.thecodewarrior.hooked.client.renderer

import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.hook.HookPlayerController
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.resource.ResourceReloader
import net.minecraft.util.Identifier

abstract class HookRenderer<C: HookPlayerController> : ResourceReloader {
    abstract fun render(
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        player: PlayerEntity,
        style: RenderStyle,
        partialTicks: Float,
        data: HookedPlayerData,
        controller: C
    )

    enum class RenderStyle(val alpha: Float) {
        /**
         * Translucent, e.g. invisible players on your team
         */
        TRANSLUCENT(0.15f),

        /**
         * Normal
         */
        NORMAL(1f),

        /**
         * Outline. Use [RenderLayer.getOutline] for this
         */
        OUTLINE(1f);

        fun getLayer(texture: Identifier): RenderLayer = when (this) {
            TRANSLUCENT -> RenderLayer.getItemEntityTranslucentCull(texture)
            NORMAL -> RenderLayer.getEntityCutout(texture)
            OUTLINE -> RenderLayer.getOutline(texture)
        }
    }
}