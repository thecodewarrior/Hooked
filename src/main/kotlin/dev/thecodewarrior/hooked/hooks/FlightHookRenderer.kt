package dev.thecodewarrior.hooked.hooks

import com.mojang.blaze3d.systems.RenderSystem
import com.teamwizardry.librarianlib.albedo.base.buffer.FlatLinesRenderBuffer
import com.teamwizardry.librarianlib.albedo.buffer.Primitive
import com.teamwizardry.librarianlib.math.Matrix4dStack
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.client.renderer.SimpleHookRenderer
import dev.thecodewarrior.hooked.util.getWaistPos
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
        player: PlayerEntity,
        ghost: Boolean,
        tickDelta: Float,
        data: HookedPlayerData,
        controller: FlightHookPlayerController
    ) {
        val waist = player.getWaistPos(tickDelta)
        matrices.translate(waist.x, waist.y, waist.z)
        renderHooks(matrices, player, ghost, tickDelta, data, 2.5)

        if (controller.showHullTimer.value != 0.0) {
            val alpha = controller.showHullTimer.value.toFloat()
            val frontColor = Color(1f, 0f, 0f, alpha)
            val backColor = Color(0.5f, 0f, 0f, alpha)

            matrices.translate(-waist.x, -waist.y, -waist.z)
            RenderSystem.enableBlend()
            RenderSystem.depthFunc(GL11.GL_GREATER)
            RenderSystem.depthMask(false)
            drawWireframe(matrices, controller, backColor)

            RenderSystem.depthFunc(GL11.GL_LEQUAL)
            RenderSystem.depthMask(true)
            drawWireframe(matrices, controller, frontColor)
            RenderSystem.disableBlend()
        }
    }

    fun drawWireframe(
        matrices: MatrixStack,
        controller: FlightHookPlayerController,
        color: Color
    ) {
        val vb = FlatLinesRenderBuffer.SHARED

        for(edge in controller.hull.shape.wireframe) {
            vb.pos(matrices, edge.a.x, edge.a.y, edge.a.z).width(2f).color(color).endVertex()
            vb.pos(matrices, edge.a.x, edge.a.y, edge.a.z).width(2f).color(color).endVertex()
            vb.pos(matrices, edge.b.x, edge.b.y, edge.b.z).width(2f).color(color).endVertex()
            vb.pos(matrices, edge.b.x, edge.b.y, edge.b.z).width(2f).color(color).endVertex()
        }

        vb.draw(Primitive.LINES_ADJACENCY)
    }
}