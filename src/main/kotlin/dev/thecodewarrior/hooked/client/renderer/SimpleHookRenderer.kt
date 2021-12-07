package dev.thecodewarrior.hooked.client.renderer

import com.teamwizardry.librarianlib.core.util.*
import com.teamwizardry.librarianlib.core.util.kotlin.vertex
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
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.profiler.Profiler
import net.minecraft.world.World
import java.awt.Color
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
        vertexConsumers: VertexConsumerProvider,
        player: PlayerEntity,
        style: RenderStyle,
        partialTicks: Float,
        data: HookedPlayerData,
        chainMargin: Double
    ) {
        data.hooks.forEach {
            renderHook(player, vertexConsumers, matrices, style, partialTicks, it, chainMargin)
        }
    }

    private fun renderHook(
        player: PlayerEntity,
        vertexConsumers: VertexConsumerProvider,
        matrices: MatrixStack,
        style: RenderStyle,
        partialTicks: Float,
        hook: Hook,
        chainMargin: Double
    ) {
        val waist = player.getWaistPos(partialTicks)
        val hookPos = hook.posLastTick + (hook.pos - hook.posLastTick) * partialTicks
        val chainLength = waist.distanceTo(hookPos)
        val chainDirection = (hookPos - waist) / chainLength

        matrices.push()
        matrices.multiply(Quaternion.fromRotationTo(vec(0, 1, 0), hookPos - waist).toMc())
        matrices.translate(0.0, chainMargin, 0.0)


        val actualLength = chainLength - chainMargin


        drawHalfChain(vertexConsumers, matrices, style.getLayer(chain1Texture), player.world, waist, chainDirection, actualLength, 0.5, 0.0, 0.0, 1.0, style.alpha)
        drawHalfChain(vertexConsumers, matrices, style.getLayer(chain2Texture), player.world, waist, chainDirection, actualLength, 0.0, 0.5, -1.0, 0.0, style.alpha)

        matrices.pop()

        matrices.push()

        matrices.multiply(Quaternion.fromRotationTo(vec(0, 1, 0), hook.direction).toMc())

        val vb = vertexConsumers.getBuffer(style.getLayer(hookTexture))
        val lightmap = getBrightnessForRender(player.world, BlockPos(hookPos))
        modelVertexIndices.forEachIndexed { i, vertexIndex ->
            val vertex = model.getVertex(vertexIndex)
            val tex = model.getTexCoord(vertexIndex)
            val normal = model.getNormal(vertexIndex)
            repeat(if(i % 3 == 0) 2 else 1) {
                vb.vertex(matrices.peek().model, vertex.x, vertex.y, vertex.z)
                    .color(1f, 1f, 1f, style.alpha)
                    .texture(tex.x, 1 - tex.y)
                    .overlay(0, 0)
                    .light(lightmap)
                    .normal(matrices.peek().normal, normal.x, normal.y, normal.z)
                    .next()
            }
        }

        matrices.pop()
    }

    /**
     * Draws half of a chain. The deltaX and deltaZ parameters dictate whether to draw the ±X or ±Z part of the chain.
     */
    fun drawHalfChain(
        vertexConsumers: VertexConsumerProvider,
        matrices: MatrixStack,
        renderLayer: RenderLayer,
        world: World,
        waist: Vec3d,
        chainDirection: Vec3d,
        chainLength: Double,
        deltaX: Double,
        deltaZ: Double,
        normalX: Double,
        normalZ: Double,
        alpha: Float
    ) {
        if (chainLength < 0) // this can happen when the chain is shorter than the chain margin
            return
        val chainSegments = floorInt(chainLength)
        val firstSegmentLength = chainLength - chainSegments

        val normalMatrix = Matrix3d(matrices.peek().normal)
        val normal = normalMatrix.transform(vec(normalX, 0, normalZ))
        val rnormal = -normal

        val vb = vertexConsumers.getBuffer(renderLayer)
        if (firstSegmentLength > chainLengthEpsilon) {
            val lightPos = BlockPos(waist + chainDirection * (firstSegmentLength / 2))
            val lightmap = getBrightnessForRender(world, lightPos)

            val minV = 1 - firstSegmentLength.toFloat()
            val len = firstSegmentLength

            vb.vertex(matrices.peek().model, -deltaX, 0, -deltaZ).color(1f, 1f, 1f, alpha).texture(0f, minV)
                .light(lightmap).normal(matrices.peek().normal, normal.x.toFloat(), normal.y.toFloat(), normal.z.toFloat()).next()
            vb.vertex(matrices.peek().model, deltaX, 0, deltaZ).color(1f, 1f, 1f, alpha).texture(1f, minV)
                .light(lightmap).normal(matrices.peek().normal, normal.x.toFloat(), normal.y.toFloat(), normal.z.toFloat()).next()
            vb.vertex(matrices.peek().model, deltaX, len, deltaZ).color(1f, 1f, 1f, alpha).texture(1f, 1f)
                .light(lightmap).normal(matrices.peek().normal, normal.x.toFloat(), normal.y.toFloat(), normal.z.toFloat()).next()
            vb.vertex(matrices.peek().model, -deltaX, len, -deltaZ).color(1f, 1f, 1f, alpha).texture(0f, 1f)
                .light(lightmap).normal(matrices.peek().normal, normal.x.toFloat(), normal.y.toFloat(), normal.z.toFloat()).next()

            vb.vertex(matrices.peek().model, -deltaX, len, -deltaZ).color(1f, 1f, 1f, alpha).texture(0f, 1f)
                .light(lightmap).normal(matrices.peek().normal, rnormal.x.toFloat(), rnormal.y.toFloat(), rnormal.z.toFloat()).next()
            vb.vertex(matrices.peek().model, deltaX, len, deltaZ).color(1f, 1f, 1f, alpha).texture(1f, 1f)
                .light(lightmap).normal(matrices.peek().normal, rnormal.x.toFloat(), rnormal.y.toFloat(), rnormal.z.toFloat()).next()
            vb.vertex(matrices.peek().model, deltaX, 0, deltaZ).color(1f, 1f, 1f, alpha).texture(1f, minV)
                .light(lightmap).normal(matrices.peek().normal, rnormal.x.toFloat(), rnormal.y.toFloat(), rnormal.z.toFloat()).next()
            vb.vertex(matrices.peek().model, -deltaX, 0, -deltaZ).color(1f, 1f, 1f, alpha).texture(0f, minV)
                .light(lightmap).normal(matrices.peek().normal, rnormal.x.toFloat(), rnormal.y.toFloat(), rnormal.z.toFloat()).next()
        }
        for (i in 0 until chainSegments) {
            val yPos = firstSegmentLength + i

            val lightPos = BlockPos(waist + chainDirection * (yPos + 0.5))
            val lightmap = getBrightnessForRender(world, lightPos)

            vb.vertex(matrices.peek().model, -deltaX, yPos, -deltaZ).color(1f, 1f, 1f, alpha).texture(0f, 0f)
                .light(lightmap).normal(matrices.peek().normal, normal.x.toFloat(), normal.y.toFloat(), normal.z.toFloat()).next()
            vb.vertex(matrices.peek().model, deltaX, yPos, deltaZ).color(1f, 1f, 1f, alpha).texture(1f, 0f)
                .light(lightmap).normal(matrices.peek().normal, normal.x.toFloat(), normal.y.toFloat(), normal.z.toFloat()).next()
            vb.vertex(matrices.peek().model, deltaX, yPos + 1, deltaZ).color(1f, 1f, 1f, alpha).texture(1f, 1f)
                .light(lightmap).normal(matrices.peek().normal, normal.x.toFloat(), normal.y.toFloat(), normal.z.toFloat()).next()
            vb.vertex(matrices.peek().model, -deltaX, yPos + 1, -deltaZ).color(1f, 1f, 1f, alpha).texture(0f, 1f)
                .light(lightmap).normal(matrices.peek().normal, normal.x.toFloat(), normal.y.toFloat(), normal.z.toFloat()).next()

            vb.vertex(matrices.peek().model, -deltaX, yPos + 1, -deltaZ).color(1f, 1f, 1f, alpha).texture(0f, 1f)
                .light(lightmap).normal(matrices.peek().normal, rnormal.x.toFloat(), rnormal.y.toFloat(), rnormal.z.toFloat()).next()
            vb.vertex(matrices.peek().model, deltaX, yPos + 1, deltaZ).color(1f, 1f, 1f, alpha).texture(1f, 1f)
                .light(lightmap).normal(matrices.peek().normal, rnormal.x.toFloat(), rnormal.y.toFloat(), rnormal.z.toFloat()).next()
            vb.vertex(matrices.peek().model, deltaX, yPos, deltaZ).color(1f, 1f, 1f, alpha).texture(1f, 0f)
                .light(lightmap).normal(matrices.peek().normal, rnormal.x.toFloat(), rnormal.y.toFloat(), rnormal.z.toFloat()).next()
            vb.vertex(matrices.peek().model, -deltaX, yPos, -deltaZ).color(1f, 1f, 1f, alpha).texture(0f, 0f)
                .light(lightmap).normal(matrices.peek().normal, rnormal.x.toFloat(), rnormal.y.toFloat(), rnormal.z.toFloat()).next()
        }
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