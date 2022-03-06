package dev.thecodewarrior.hooked.hooks

import com.teamwizardry.librarianlib.core.bridge.IMatrix4f
import com.teamwizardry.librarianlib.core.util.Client
import com.teamwizardry.librarianlib.core.util.kotlin.color
import com.teamwizardry.librarianlib.core.util.mixinCast
import com.teamwizardry.librarianlib.core.util.vec
import com.teamwizardry.librarianlib.math.cross
import com.teamwizardry.librarianlib.math.minus
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.client.renderer.SimpleHookRenderer
import dev.thecodewarrior.hooked.util.getWaistPos
import dev.thecodewarrior.hooked.util.normal
import dev.thecodewarrior.hooked.util.vertex
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Vec3d
import java.awt.Color

open class FlightHookRenderer(type: FlightHookType): SimpleHookRenderer<FlightHookPlayerController>(type) {
    override fun render(
        matrices: MatrixStack,
        player: PlayerEntity,
        consumers: VertexConsumerProvider,
        tickDelta: Float,
        data: HookedPlayerData,
        controller: FlightHookPlayerController
    ) {
        val waist = player.getWaistPos(tickDelta)
        matrices.push()
        matrices.translate(waist.x, waist.y, waist.z)
        renderHooks(matrices, player, consumers, tickDelta, data, 2.5)
        matrices.pop()

        if (controller.showHullTimer.value != 0.0) {
            val alpha = controller.showHullTimer.value.toFloat()
            val frontColor = Color(1f, 0f, 0f, alpha)
            val backColor = Color(0.5f, 0f, 0f, alpha)

//            RenderSystem.depthFunc(GL11.GL_GREATER)
//            RenderSystem.depthMask(false)
//            drawWireframe(matrices, consumers, controller, backColor)

            drawWireframe(matrices, consumers, controller, frontColor)
        }
    }

    fun drawWireframe(
        matrices: MatrixStack,
        consumers: VertexConsumerProvider,
        controller: FlightHookPlayerController,
        color: Color
    ) {
        val consumer = consumers.getBuffer(RenderLayer.getLines())

        for(edge in controller.hull.shape.wireframe) {
            val normal = (edge.b - edge.a).normalize()
            consumer.vertex(matrices, edge.a).color(color).normal(matrices, normal).next()
            consumer.vertex(matrices, edge.b).color(color).normal(matrices, normal).next()
        }
    }
}