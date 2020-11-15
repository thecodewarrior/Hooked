package dev.thecodewarrior.hooked.client.renderer

import com.mojang.blaze3d.systems.RenderSystem
import com.teamwizardry.librarianlib.core.bridge.IRenderTypeState
import com.teamwizardry.librarianlib.core.util.*
import com.teamwizardry.librarianlib.core.util.kotlin.color
import com.teamwizardry.librarianlib.core.util.kotlin.loc
import com.teamwizardry.librarianlib.core.util.kotlin.pos
import com.teamwizardry.librarianlib.math.*
import de.javagl.obj.*
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.hook.processor.Hook
import dev.thecodewarrior.hooked.hook.type.BasicHookPlayerController
import dev.thecodewarrior.hooked.util.getWaistPos
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.RenderState
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.profiler.IProfiler
import net.minecraft.resources.IResourceManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.io.IOException

class BasicHookRenderer(val id: ResourceLocation):
    HookRenderer<BasicHookPlayerController>(), ISimpleReloadListener<BasicHookRenderer.ReloadData> {

    var model: Obj = Objs.create()
    var modelVertexIndices: IntArray = IntArray(0)
    val modelLocation = loc(id.namespace, "models/hook/${id.path}.obj")
    val hookRenderType =
        SimpleRenderTypes.flat(loc(id.namespace, "textures/hook/${id.path}/hook.png"), GL11.GL_TRIANGLES)
    val chain1RenderType = createChainType(loc(id.namespace, "textures/hook/${id.path}/chain1.png"))
    val chain2RenderType = createChainType(loc(id.namespace, "textures/hook/${id.path}/chain2.png"))

    init {
        Client.resourceReloadHandler.register(this)
        apply(
            prepare(Client.resourceManager, Client.minecraft.profiler),
            Client.resourceManager, Client.minecraft.profiler
        )
    }

    override fun render(
        player: PlayerEntity,
        matrix: Matrix4dStack,
        partialTicks: Float,
        data: HookedPlayerData,
        controller: BasicHookPlayerController
    ) {
        data.hooks.forEach {
            renderHook(player, matrix, partialTicks, it)
        }
    }

    private fun renderHook(player: PlayerEntity, matrix: Matrix4dStack, partialTicks: Float, hook: Hook) {
        val waist = player.getWaistPos(partialTicks)
        val hookPos = hook.posLastTick + (hook.pos - hook.posLastTick) * partialTicks
        val chainLength = waist.distanceTo(hookPos)

        matrix.push()
        matrix.translate(waist)
        matrix.rotate(Quaternion.fromRotationTo(vec(0, 1, 0), hookPos - waist))

        drawHalfChain(matrix, chain1RenderType, chainLength, 0.5, 0.0)
        drawHalfChain(matrix, chain2RenderType, chainLength, 0.0, 0.5)

        matrix.pop()

        matrix.push()

        matrix.translate(hook.pos)
        matrix.rotate(Quaternion.fromRotationTo(vec(0, 1, 0), hook.direction))

        val buffer = IRenderTypeBuffer.getImpl(Client.tessellator.buffer)
        val vb = buffer.getBuffer(hookRenderType)
        modelVertexIndices.forEach { index ->
            val vertex = model.getVertex(index)
            val tex = model.getTexCoord(index)
//            val normal = model.getNormal(index)
            vb.pos(matrix, vertex.x, vertex.y, vertex.z).color(Color.WHITE).tex(tex.x, 1-tex.y).endVertex()
        }
        buffer.finish()

        matrix.pop()
    }

    /**
     * Draws half of a chain. The deltaX and deltaZ parameters dictate whether to draw the ±X or ±Z part of the chain.
     */
    fun drawHalfChain(matrix: Matrix4d, renderType: RenderType, chainLength: Double, deltaX: Double, deltaZ: Double) {
        val chainSegments = floorInt(chainLength)
        val firstSegmentLength = chainLength - chainSegments

        val buffer = IRenderTypeBuffer.getImpl(Client.tessellator.buffer)

        val vb = buffer.getBuffer(renderType)
        if (firstSegmentLength > chainLengthEpsilon) {
            val minV = 1 - firstSegmentLength.toFloat()
            vb.pos(matrix, -deltaX, 0, -deltaZ).color(Color.WHITE).tex(0f, minV).endVertex()
            vb.pos(matrix, deltaX, 0, deltaZ).color(Color.WHITE).tex(1f, minV).endVertex()
            vb.pos(matrix, deltaX, firstSegmentLength, deltaZ).color(Color.WHITE).tex(1f, 1f).endVertex()
            vb.pos(matrix, -deltaX, firstSegmentLength, -deltaZ).color(Color.WHITE).tex(0f, 1f).endVertex()
        }
        for (i in 0 until chainSegments) {
            val yPos = firstSegmentLength + i

            vb.pos(matrix, -deltaX, yPos, -deltaZ).color(Color.WHITE).tex(0f, 0f).endVertex()
            vb.pos(matrix, deltaX, yPos, deltaZ).color(Color.WHITE).tex(1f, 0f).endVertex()
            vb.pos(matrix, deltaX, yPos + 1, deltaZ).color(Color.WHITE).tex(1f, 1f).endVertex()
            vb.pos(matrix, -deltaX, yPos + 1, -deltaZ).color(Color.WHITE).tex(0f, 1f).endVertex()
        }
        buffer.finish()
    }

    private fun createChainType(texture: ResourceLocation): RenderType {
        val stateBuilder = RenderType.State.getBuilder()
            .texture(RenderState.TextureState(texture, false, false))
            .alpha(DefaultRenderStates.DEFAULT_ALPHA)
            .depthTest(DefaultRenderStates.DEPTH_LEQUAL)
            .transparency(DefaultRenderStates.TRANSLUCENT_TRANSPARENCY)
            .cull(DefaultRenderStates.CULL_DISABLED)
        val state = stateBuilder.build(true)

        // CULL_DISABLED doesn't actually work, so we have to do it ourself
        @Suppress("CAST_NEVER_SUCCEEDS")
        (state as IRenderTypeState).addState("working_cull", {
            RenderSystem.disableCull()
        }, {
            RenderSystem.enableCull()
        })

        return SimpleRenderTypes.makeType("flat_texture",
            DefaultVertexFormats.POSITION_COLOR_TEX, GL11.GL_QUADS, 256, false, false, state
        )
    }

    data class ReloadData(val model: Obj)

    override fun apply(result: ReloadData, resourceManager: IResourceManager, profiler: IProfiler) {
        this.model = result.model
        this.modelVertexIndices = ObjData.getFaceVertexIndicesArray(model)
    }

    override fun prepare(resourceManager: IResourceManager, profiler: IProfiler): ReloadData {
        var model = try {
            resourceManager.getResource(modelLocation).use {
                ObjReader.read(it.inputStream)
            }
        } catch (e: IOException) {
            Objs.create()
        }

        model = ObjUtils.convertToRenderable(model)

        return ReloadData(model)
    }

    companion object {
        val chainLengthEpsilon = 1 / 16.0
    }
}