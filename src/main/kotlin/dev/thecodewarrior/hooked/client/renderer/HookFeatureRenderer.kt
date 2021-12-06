package dev.thecodewarrior.hooked.client.renderer

import com.teamwizardry.librarianlib.core.util.Client
import dev.thecodewarrior.hooked.bridge.hookData
import dev.thecodewarrior.hooked.client.HookRenderManager
import dev.thecodewarrior.hooked.hook.HookType
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.feature.FeatureRenderer
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity

class HookFeatureRenderer<T: PlayerEntity, M: EntityModel<T>>(context: FeatureRendererContext<T, M>):
    FeatureRenderer<T, M>(context) {

    override fun render(
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        entity: T,
        limbAngle: Float,
        limbDistance: Float,
        tickDelta: Float,
        animationProgress: Float,
        headYaw: Float,
        headPitch: Float
    ) {
        val showBody = !entity.isInvisible
        val translucent = !showBody && !entity.isInvisibleTo(Client.player);
        val showOutline = Client.minecraft.hasOutline(entity)
        val style = when {
            translucent -> HookRenderer.RenderStyle.TRANSLUCENT
            showBody -> HookRenderer.RenderStyle.NORMAL
            showOutline -> HookRenderer.RenderStyle.OUTLINE
            else -> return
        }

        val data = entity.hookData()
        if(data.type != HookType.NONE) {
            val renderer = HookRenderManager.getRenderer(data.type) ?: return
            renderer.render(matrices, vertexConsumers, entity, style, tickDelta, data, data.controller)
        }
    }

}
