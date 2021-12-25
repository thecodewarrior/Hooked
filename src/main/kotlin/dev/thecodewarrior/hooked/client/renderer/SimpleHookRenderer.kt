package dev.thecodewarrior.hooked.client.renderer

import com.teamwizardry.librarianlib.albedo.base.buffer.ShadedTextureRenderBuffer
import com.teamwizardry.librarianlib.albedo.buffer.Primitive
import com.teamwizardry.librarianlib.core.util.*
import com.teamwizardry.librarianlib.math.*
import dev.thecodewarrior.hooked.Hooked
import dev.thecodewarrior.hooked.shade.obj.*
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.hook.Hook
import dev.thecodewarrior.hooked.hook.HookPlayerController
import dev.thecodewarrior.hooked.hook.HookType
import dev.thecodewarrior.hooked.util.getWaistPos
import dev.thecodewarrior.hooked.util.toMc
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.profiler.Profiler
import net.minecraft.world.World
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

abstract class SimpleHookRenderer<C: HookPlayerController>(val type: HookType): HookRenderer<C>(),
    SimpleResourceReloadListener<SimpleHookRenderer.ReloadData> {
    private val id: Identifier = Hooked.hookRegistry.getId(type)

    private var model: Obj = Objs.create()
    private var modelVertexIndices: IntArray = IntArray(0)
    private val modelLocation = Identifier(id.namespace, "models/hook/${id.path}.obj")
    private val hookTexture = Identifier(id.namespace, "textures/hook/${id.path}/hook.png")
    private val chain1Texture = Identifier(id.namespace, "textures/hook/${id.path}/chain1.png")
    private val chain2Texture = Identifier(id.namespace, "textures/hook/${id.path}/chain2.png")

    protected fun renderHooks(
        matrices: MatrixStack,
        player: PlayerEntity,
        ghost: Boolean,
        tickDelta: Float,
        data: HookedPlayerData,
        chainMargin: Double
    ) {
        data.hooks.forEach { (_, hook) ->
            renderHook(matrices, player, ghost, tickDelta, hook, chainMargin)
        }
    }

    private fun renderHook(
        matrices: MatrixStack,
        player: PlayerEntity,
        ghost: Boolean,
        tickDelta: Float,
        hook: Hook,
        chainMargin: Double
    ) {
        val ghostAlpha = if(ghost) 0.15f else 1.0f
        val waist = player.getWaistPos(tickDelta)
        val hookPos = hook.posLastTick + (hook.pos - hook.posLastTick) * tickDelta
        val chainLength = waist.distanceTo(hookPos)
        val chainDirection = (hookPos - waist) / chainLength

        matrices.push()
        matrices.multiply(Quaternion.fromRotationTo(vec(0, 1, 0), hookPos - waist).toMc())
        matrices.translate(0.0, chainMargin, 0.0)

        val actualLength = chainLength - chainMargin

        drawHalfChain(matrices, chain1Texture, player.world, waist, chainDirection, actualLength, 0.5, 0.0, 0.0, 1.0, ghostAlpha)
        drawHalfChain(matrices, chain2Texture, player.world, waist, chainDirection, actualLength, 0.0, 0.5, -1.0, 0.0, ghostAlpha)

        matrices.pop()

        matrices.push()
        matrices.translate(hookPos.x - waist.x, hookPos.y - waist.y, hookPos.z - waist.z)
        if(hook.direction.x == 0.0 && hook.direction.z == 0.0 && hook.direction.y < 0.0) {
            matrices.multiply(Quaternion.fromAngleDegAxis(180.0, 1.0, 0.0, 0.0).toMc())
        } else {
            matrices.multiply(Quaternion.fromRotationTo(vec(0, 1, 0), hook.direction).toMc())
        }

        val vb = ShadedTextureRenderBuffer.SHARED
        vb.texture.set(hookTexture)
        val lightmap = getBrightnessForRender(player.world, BlockPos(hookPos))
        modelVertexIndices.forEach { vertexIndex ->
            val vertex = model.getVertex(vertexIndex)
            val tex = model.getTexCoord(vertexIndex)
            val normal = model.getNormal(vertexIndex)
            vb.pos(matrices.peek().model, vertex.x, vertex.y, vertex.z)
                .color(1f, 1f, 1f, ghostAlpha)
                .tex(tex.x, 1 - tex.y)
                .light(lightmap)
                .normal(matrices.peek().normal, normal.x, normal.y, normal.z)
                .endVertex()
        }
        vb.draw(Primitive.TRIANGLES)

        matrices.pop()
    }

    /**
     * Draws half of a chain. The deltaX and deltaZ parameters dictate whether to draw the ±X or ±Z part of the chain.
     */
    fun drawHalfChain(
        matrices: MatrixStack,
        texture: Identifier,
        world: World,
        waist: Vec3d,
        chainDirection: Vec3d,
        chainLength: Double,
        deltaX: Double,
        deltaZ: Double,
        normalX: Double,
        normalZ: Double,
        ghostAlpha: Float
    ) {
        if (chainLength < 0) // this can happen when the chain is shorter than the chain margin
            return
        val chainSegments = floorInt(chainLength)
        val firstSegmentLength = chainLength - chainSegments

        val normalMatrix = Matrix3d(matrices.peek().normal)
        val normal = normalMatrix.transform(vec(normalX, 0, normalZ))
        val rnormal = -normal

        val vb = ShadedTextureRenderBuffer.SHARED
        vb.texture.set(texture)

        if (firstSegmentLength > chainLengthEpsilon) {
            val lightPos = BlockPos(waist + chainDirection * (firstSegmentLength / 2))
            val lightmap = getBrightnessForRender(world, lightPos)

            val minV = 1 - firstSegmentLength.toFloat()
            val len = firstSegmentLength

            vb.pos(matrices.peek().model, -deltaX, 0, -deltaZ).color(1f, 1f, 1f, ghostAlpha).tex(0f, minV)
                .light(lightmap).normal(matrices.peek().normal, normal.x.toFloat(), normal.y.toFloat(), normal.z.toFloat()).endVertex()
            vb.pos(matrices.peek().model, deltaX, 0, deltaZ).color(1f, 1f, 1f, ghostAlpha).tex(1f, minV)
                .light(lightmap).normal(matrices.peek().normal, normal.x.toFloat(), normal.y.toFloat(), normal.z.toFloat()).endVertex()
            vb.pos(matrices.peek().model, deltaX, len, deltaZ).color(1f, 1f, 1f, ghostAlpha).tex(1f, 1f)
                .light(lightmap).normal(matrices.peek().normal, normal.x.toFloat(), normal.y.toFloat(), normal.z.toFloat()).endVertex()
            vb.pos(matrices.peek().model, -deltaX, len, -deltaZ).color(1f, 1f, 1f, ghostAlpha).tex(0f, 1f)
                .light(lightmap).normal(matrices.peek().normal, normal.x.toFloat(), normal.y.toFloat(), normal.z.toFloat()).endVertex()

            vb.pos(matrices.peek().model, -deltaX, len, -deltaZ).color(1f, 1f, 1f, ghostAlpha).tex(0f, 1f)
                .light(lightmap).normal(matrices.peek().normal, rnormal.x.toFloat(), rnormal.y.toFloat(), rnormal.z.toFloat()).endVertex()
            vb.pos(matrices.peek().model, deltaX, len, deltaZ).color(1f, 1f, 1f, ghostAlpha).tex(1f, 1f)
                .light(lightmap).normal(matrices.peek().normal, rnormal.x.toFloat(), rnormal.y.toFloat(), rnormal.z.toFloat()).endVertex()
            vb.pos(matrices.peek().model, deltaX, 0, deltaZ).color(1f, 1f, 1f, ghostAlpha).tex(1f, minV)
                .light(lightmap).normal(matrices.peek().normal, rnormal.x.toFloat(), rnormal.y.toFloat(), rnormal.z.toFloat()).endVertex()
            vb.pos(matrices.peek().model, -deltaX, 0, -deltaZ).color(1f, 1f, 1f, ghostAlpha).tex(0f, minV)
                .light(lightmap).normal(matrices.peek().normal, rnormal.x.toFloat(), rnormal.y.toFloat(), rnormal.z.toFloat()).endVertex()
        }
        for (i in 0 until chainSegments) {
            val yPos = firstSegmentLength + i

            val lightPos = BlockPos(waist + chainDirection * (yPos + 0.5))
            val lightmap = getBrightnessForRender(world, lightPos)

            vb.pos(matrices.peek().model, -deltaX, yPos, -deltaZ).color(1f, 1f, 1f, ghostAlpha).tex(0f, 0f)
                .light(lightmap).normal(matrices.peek().normal, normal.x.toFloat(), normal.y.toFloat(), normal.z.toFloat()).endVertex()
            vb.pos(matrices.peek().model, deltaX, yPos, deltaZ).color(1f, 1f, 1f, ghostAlpha).tex(1f, 0f)
                .light(lightmap).normal(matrices.peek().normal, normal.x.toFloat(), normal.y.toFloat(), normal.z.toFloat()).endVertex()
            vb.pos(matrices.peek().model, deltaX, yPos + 1, deltaZ).color(1f, 1f, 1f, ghostAlpha).tex(1f, 1f)
                .light(lightmap).normal(matrices.peek().normal, normal.x.toFloat(), normal.y.toFloat(), normal.z.toFloat()).endVertex()
            vb.pos(matrices.peek().model, -deltaX, yPos + 1, -deltaZ).color(1f, 1f, 1f, ghostAlpha).tex(0f, 1f)
                .light(lightmap).normal(matrices.peek().normal, normal.x.toFloat(), normal.y.toFloat(), normal.z.toFloat()).endVertex()

            vb.pos(matrices.peek().model, -deltaX, yPos + 1, -deltaZ).color(1f, 1f, 1f, ghostAlpha).tex(0f, 1f)
                .light(lightmap).normal(matrices.peek().normal, rnormal.x.toFloat(), rnormal.y.toFloat(), rnormal.z.toFloat()).endVertex()
            vb.pos(matrices.peek().model, deltaX, yPos + 1, deltaZ).color(1f, 1f, 1f, ghostAlpha).tex(1f, 1f)
                .light(lightmap).normal(matrices.peek().normal, rnormal.x.toFloat(), rnormal.y.toFloat(), rnormal.z.toFloat()).endVertex()
            vb.pos(matrices.peek().model, deltaX, yPos, deltaZ).color(1f, 1f, 1f, ghostAlpha).tex(1f, 0f)
                .light(lightmap).normal(matrices.peek().normal, rnormal.x.toFloat(), rnormal.y.toFloat(), rnormal.z.toFloat()).endVertex()
            vb.pos(matrices.peek().model, -deltaX, yPos, -deltaZ).color(1f, 1f, 1f, ghostAlpha).tex(0f, 0f)
                .light(lightmap).normal(matrices.peek().normal, rnormal.x.toFloat(), rnormal.y.toFloat(), rnormal.z.toFloat()).endVertex()
        }

        vb.draw(Primitive.QUADS)
    }

    private fun getBrightnessForRender(world: World, pos: BlockPos): Int {
        return if (world.chunkManager.isChunkLoaded(pos.x / 16, pos.y / 16))
            WorldRenderer.getLightmapCoordinates(world, pos) else 0
    }

    data class ReloadData(val model: Obj)

    override fun apply(
        data: ReloadData,
        manager: ResourceManager,
        profiler: Profiler,
        executor: Executor
    ): CompletableFuture<Void> {
        applyData(data)
        return CompletableFuture.completedFuture(null)
    }

    override fun getFabricId(): Identifier {
        return Identifier(id.namespace, "${id.path}/hook_renderer")
    }

    override fun load(
        manager: ResourceManager,
        profiler: Profiler,
        executor: Executor
    ): CompletableFuture<ReloadData> {
        return CompletableFuture.supplyAsync {
            loadModel(manager)
        }
    }

    private fun loadModel(manager: ResourceManager): ReloadData {
        var model = try {
            manager.getResource(modelLocation).use {
                ObjReader.read(it.inputStream)
            }
        } catch (e: IOException) {
            Objs.create()
        }

        model = ObjUtils.convertToRenderable(model)

        return ReloadData(model)
    }

    private fun applyData(data: ReloadData) {
        this.model = data.model
        this.modelVertexIndices = ObjData.getFaceVertexIndicesArray(model)
    }

    companion object {
        val chainLengthEpsilon = 1 / 16.0
    }

}