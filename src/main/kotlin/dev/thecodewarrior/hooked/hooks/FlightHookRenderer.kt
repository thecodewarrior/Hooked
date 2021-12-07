package dev.thecodewarrior.hooked.hooks

import com.mojang.blaze3d.systems.RenderSystem
import com.teamwizardry.librarianlib.albedo.base.buffer.FlatLinesRenderBuffer
import com.teamwizardry.librarianlib.albedo.buffer.Primitive
import com.teamwizardry.librarianlib.math.Matrix4dStack
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.client.renderer.SimpleHookRenderer
import dev.thecodewarrior.hooked.util.withAlpha
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Identifier
import org.lwjgl.opengl.GL11
import java.awt.Color

open class FlightHookRenderer(type: FlightHookType): SimpleHookRenderer<FlightHookPlayerController>(type) {
    override fun render(
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        player: PlayerEntity,
        style: RenderStyle,
        partialTicks: Float,
        data: HookedPlayerData,
        controller: FlightHookPlayerController
    ) {
        renderHooks(matrices, vertexConsumers, player, style, partialTicks, data, 0.0)

        if (controller.showHullTimer.value != 0.0) {
            val frontColor = Color(1f, 0f, 0f)
            val backColor = Color(0.5f, 0f, 0f)
            val alpha = controller.showHullTimer.value.toFloat()
            RenderSystem.depthFunc(GL11.GL_GREATER)
            RenderSystem.depthMask(false)
            drawWireframe(matrices, vertexConsumers, controller, backColor.withAlpha(alpha))

            RenderSystem.depthFunc(GL11.GL_LEQUAL)
            RenderSystem.depthMask(true)
            drawWireframe(matrices, vertexConsumers, controller, frontColor.withAlpha(alpha))
        }
    }

    fun drawWireframe(
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        controller: FlightHookPlayerController,
        color: Color
    ) {
//        val vb = FlatLinesRenderBuffer.SHARED
//        vb.width(1f)
//
//        for(edge in controller.hull.shape.wireframe) {
//            vb.pos(matrix, edge.a.x, edge.a.y, edge.a.z).color(color).endVertex()
//            vb.dupVertex()
//            vb.pos(matrix, edge.b.x, edge.b.y, edge.b.z).color(color).endVertex()
//            vb.dupVertex()
//        }
//
//        vb.draw(Primitive.LINES_ADJACENCY)
    }
}