package dev.thecodewarrior.hooked.hooks

import com.teamwizardry.librarianlib.core.util.kotlin.color
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
import java.awt.Color

open class FlightHookRenderer(): SimpleHookRenderer<FlightHookPlayerController>() {
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
        renderHooks(matrices, player, consumers, tickDelta, data)
        matrices.pop()

        if (controller.showHullTimer.value != 0.0) {
            val alpha = controller.showHullTimer.value.toFloat()
            val wireframeColor = controller.behavior.wireframeColor

            drawWireframe(
                matrices,
                consumers,
                controller,
                Color(wireframeColor.x, wireframeColor.y, wireframeColor.z, alpha)
            )
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
            consumer.vertex(matrices, edge.a).color(color).normal(matrices, normal)
            consumer.vertex(matrices, edge.b).color(color).normal(matrices, normal)
        }
    }
}