package dev.thecodewarrior.hooked.client

import com.mojang.blaze3d.systems.RenderSystem
import com.teamwizardry.librarianlib.core.util.Client
import com.teamwizardry.librarianlib.core.util.DefaultRenderStates
import com.teamwizardry.librarianlib.core.util.DistinctColors
import com.teamwizardry.librarianlib.core.util.SimpleRenderTypes
import com.teamwizardry.librarianlib.core.util.kotlin.color
import com.teamwizardry.librarianlib.core.util.kotlin.getOrNull
import com.teamwizardry.librarianlib.core.util.kotlin.pos
import com.teamwizardry.librarianlib.core.util.vec
import com.teamwizardry.librarianlib.math.*
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.client.renderer.HookRenderer
import dev.thecodewarrior.hooked.hook.Hook
import dev.thecodewarrior.hooked.hook.HookPlayerController
import dev.thecodewarrior.hooked.hook.HookType
import dev.thecodewarrior.hooked.util.getWaistPos
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.entity.player.PlayerEntity
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.SubscribeEvent
import org.lwjgl.opengl.GL11

object HookRenderManager {
    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    private val registry = mutableMapOf<HookType, HookRenderer<*>>()

    fun register(type: HookType, renderer: HookRenderer<*>) {
        registry[type] = renderer
    }

    @SubscribeEvent
    fun playerRender(e: RenderWorldLastEvent) {

        e.matrixStack.push()
        val viewPos = Client.minecraft.gameRenderer.activeRenderInfo.projectedView
        e.matrixStack.translate(-viewPos.x, -viewPos.y, -viewPos.z)

        val matrix = Matrix4dStack()
        matrix.set(e.matrixStack.last.matrix)

        val world = Client.minecraft.world ?: return
        world.players.forEach { player ->
            player.getCapability(HookedPlayerData.CAPABILITY).getOrNull()?.also { data ->
                if (data.type != HookType.NONE) {
                    if (Client.minecraft.renderManager.isDebugBoundingBox) {
                        drawDebugLines(e, matrix, player, data)
                    }

                    @Suppress("UNCHECKED_CAST")
                    val renderer = registry[data.type] as HookRenderer<HookPlayerController>?
                    matrix.assertEvenDepth {
                        renderer?.render(player, matrix, e.partialTicks, data, data.controller)
                    }
                }
            }
        }

        e.matrixStack.pop()
    }

    fun drawDebugLines(e: RenderWorldLastEvent, matrix: Matrix4dStack, player: PlayerEntity, data: HookedPlayerData) {
        if (data.hooks.isEmpty())
            return

        RenderSystem.lineWidth(1f)
        RenderSystem.disableTexture()

        val buffer = IRenderTypeBuffer.getImpl(Client.tessellator.buffer)
        val vb = buffer.getBuffer(debugRenderType)

        val waistPos = player.getWaistPos(e.partialTicks)
        data.hooks.forEach { hook ->

            vb.pos(matrix, waistPos).color(DistinctColors.white).endVertex()
            vb.pos(matrix, hook.pos).color(DistinctColors.white).endVertex()

            matrix.push()
            matrix.translate(hook.pos)
            matrix.rotate(Quaternion.fromRotationTo(vec(0, 1, 0), hook.direction))

            val color = when (hook.state) {
                Hook.State.EXTENDING -> DistinctColors.green
                Hook.State.PLANTED -> DistinctColors.blue
                Hook.State.RETRACTING -> DistinctColors.red
                Hook.State.REMOVED -> DistinctColors.black
            }
            val length = hook.type.hookLength
            val claw = length / 3

            vb.pos(matrix, vec(0, 0, 0)).color(color).endVertex()
            vb.pos(matrix, vec(0, length, 0)).color(color).endVertex()

            vb.pos(matrix, vec(-claw, length - claw, 0)).color(color).endVertex()
            vb.pos(matrix, vec(0, length, 0)).color(color).endVertex()
            vb.pos(matrix, vec(0, length, 0)).color(color).endVertex()
            vb.pos(matrix, vec(claw, length - claw, 0)).color(color).endVertex()

            vb.pos(matrix, vec(0, length - claw, -claw)).color(color).endVertex()
            vb.pos(matrix, vec(0, length, 0)).color(color).endVertex()
            vb.pos(matrix, vec(0, length, 0)).color(color).endVertex()
            vb.pos(matrix, vec(0, length - claw, claw)).color(color).endVertex()

            matrix.pop()
        }

        buffer.finish()
        RenderSystem.enableTexture()
    }

    private val debugRenderType = SimpleRenderTypes.flat(GL11.GL_LINES) {
        it.depthTest(DefaultRenderStates.DEPTH_ALWAYS)
        it.writeMask(DefaultRenderStates.COLOR_WRITE)
    }
}