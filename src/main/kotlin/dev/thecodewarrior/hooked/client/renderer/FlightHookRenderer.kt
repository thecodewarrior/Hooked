package dev.thecodewarrior.hooked.client.renderer

import com.mojang.blaze3d.systems.RenderSystem
import com.teamwizardry.librarianlib.core.util.Client
import com.teamwizardry.librarianlib.core.util.DefaultRenderStates
import com.teamwizardry.librarianlib.core.util.SimpleRenderTypes
import com.teamwizardry.librarianlib.core.util.kotlin.color
import com.teamwizardry.librarianlib.core.util.kotlin.pos
import com.teamwizardry.librarianlib.math.Matrix4dStack
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.hook.type.BasicHookPlayerController
import dev.thecodewarrior.hooked.hook.type.FlightHookPlayerController
import dev.thecodewarrior.hooked.hook.type.FlightHookType
import dev.thecodewarrior.hooked.util.withAlpha
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.RenderState
import net.minecraft.client.renderer.RenderType
import net.minecraft.entity.player.PlayerEntity
import org.lwjgl.opengl.GL11
import java.awt.Color

class FlightHookRenderer(type: FlightHookType): SimpleHookRenderer<FlightHookPlayerController>(type) {
    override fun render(
        player: PlayerEntity,
        matrix: Matrix4dStack,
        partialTicks: Float,
        data: HookedPlayerData,
        controller: FlightHookPlayerController
    ) {
        renderHooks(player, matrix, partialTicks, data, 2.5)

        if (controller.showHullTimer.value != 0.0) {
            val frontColor = Color(1f, 0f, 0f)
            val backColor = Color(0.5f, 0f, 0f)
            val alpha = controller.showHullTimer.value.toFloat()
            drawWireframe(matrix, controller, wireframeBackRenderType, backColor.withAlpha(alpha))
            drawWireframe(matrix, controller, SimpleRenderTypes.flatLines, frontColor.withAlpha(alpha))
        }
    }

    fun drawWireframe(
        matrix: Matrix4dStack,
        controller: FlightHookPlayerController,
        renderType: RenderType,
        color: Color
    ) {
        RenderSystem.lineWidth(1f)
        RenderSystem.disableTexture()

        val buffer = IRenderTypeBuffer.getImpl(Client.tessellator.buffer)
        val vb = buffer.getBuffer(renderType)

        for(edge in controller.hull.shape.wireframe) {
            vb.pos(matrix, edge.a).color(color).endVertex()
            vb.pos(matrix, edge.b).color(color).endVertex()
        }

        buffer.finish()
        RenderSystem.enableTexture()
    }

    companion object {
        private val wireframeBackRenderType = SimpleRenderTypes.flat(GL11.GL_LINES) {
            it.depthTest(RenderState.DepthTestState(">", GL11.GL_GREATER))
            it.writeMask(DefaultRenderStates.COLOR_WRITE)
        }
    }
}