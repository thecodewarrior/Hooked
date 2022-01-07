package dev.thecodewarrior.hooked.client.renderer

import com.teamwizardry.librarianlib.math.*
import dev.thecodewarrior.hooked.Hooked
import dev.thecodewarrior.hooked.shade.obj.*
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.hook.Hook
import dev.thecodewarrior.hooked.hook.HookPlayerController
import dev.thecodewarrior.hooked.hook.HookType
import dev.thecodewarrior.hooked.util.getWaistPos
import dev.thecodewarrior.hooked.util.normal
import dev.thecodewarrior.hooked.util.toMc
import dev.thecodewarrior.hooked.util.vertex
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener
import net.minecraft.client.render.OverlayTexture
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
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import kotlin.math.asin
import kotlin.math.atan2

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
        consumers: VertexConsumerProvider,
        tickDelta: Float,
        data: HookedPlayerData,
        chainMargin: Double
    ) {
        data.hooks.forEach { (_, hook) ->
            renderHook(matrices, player, consumers, tickDelta, hook, chainMargin)
        }
        // force it to draw
        consumers.getBuffer(RenderLayer.getEntityCutout(Identifier("minecraft:textures/misc/white.png")))
    }

    private fun renderHook(
        matrices: MatrixStack,
        player: PlayerEntity,
        consumers: VertexConsumerProvider,
        tickDelta: Float,
        hook: Hook,
        chainMargin: Double
    ) {
        val waist = player.getWaistPos(tickDelta)
        val hookPos = hook.posLastTick + (hook.pos - hook.posLastTick) * tickDelta
        val chainLength = waist.distanceTo(hookPos)
        val chainDirection = (hookPos - waist) / chainLength

        matrices.push()
        val yaw = -Math.toDegrees(atan2(chainDirection.x, chainDirection.z)).toFloat()
        val pitch = -Math.toDegrees(asin(chainDirection.y)).toFloat()
        // we add 90 to the pitch because the model is based on +y, but pitch/yaw are based on +z
        matrices.multiply(Quaternion.fromAxesAnglesDeg(pitch + 90, -yaw, 0f).toMc())
        matrices.translate(0.0, chainMargin, 0.0)

        val actualLength = chainLength - chainMargin

        drawHalfChain(matrices, consumers, chain1Texture, player.world, waist, chainDirection, actualLength, 0.5, 0.0, 0.0, 1.0)
        drawHalfChain(matrices, consumers, chain2Texture, player.world, waist, chainDirection, actualLength, 0.0, 0.5, -1.0, 0.0)

        matrices.pop()

        matrices.push()
        matrices.translate(hookPos.x - waist.x, hookPos.y - waist.y, hookPos.z - waist.z)
        // we add 90 to the pitch because the model is based on +y, but pitch/yaw are based on +z
        matrices.multiply(Quaternion.fromAxesAnglesDeg(hook.pitch + 90, -hook.yaw, 0f).toMc())

        val consumer = consumers.getBuffer(RenderLayer.getEntityCutout(hookTexture))
        val lightmap = getBrightnessForRender(player.world, BlockPos(hookPos))

        modelVertexIndices.forEachIndexed { i, vertexIndex ->
            val vertex = model.getVertex(vertexIndex)
            val tex = model.getTexCoord(vertexIndex)
            val normal = model.getNormal(vertexIndex)
            consumer.vertex(matrices, vertex.x, vertex.y, vertex.z).color(1f, 1f, 1f, 1f).texture(tex.x, 1 - tex.y)
                .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, normal.x, normal.y, normal.z).next()
            if(i % 3 == 2) {
                // see RenderSystem.sharedSequentialQuad
                // 1-2
                // |/|
                // 0-3
                // the game operates in quads, so we need to get rid of the 2-3-0 triangle
                consumer.vertex(matrices, vertex.x, vertex.y, vertex.z).color(1f, 1f, 1f, 1f).texture(tex.x, 1 - tex.y)
                    .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, normal.x, normal.y, normal.z).next()
            }
        }

        matrices.pop()
    }

    /**
     * Draws half of a chain. The deltaX and deltaZ parameters dictate whether to draw the ±X or ±Z part of the chain.
     */
    fun drawHalfChain(
        matrices: MatrixStack,
        consumers: VertexConsumerProvider,
        texture: Identifier,
        world: World,
        waist: Vec3d,
        chainDirection: Vec3d,
        chainLength: Double,
        deltaX: Double,
        deltaZ: Double,
        normalX: Double,
        normalZ: Double
    ) {
        if (chainLength < 0) // this can happen when the chain is shorter than the chain margin
            return
        val chainSegments = floorInt(chainLength)
        val firstSegmentLength = chainLength - chainSegments

        val consumer = consumers.getBuffer(RenderLayer.getEntityCutout(texture))

        if (firstSegmentLength > chainLengthEpsilon) {
            val lightPos = BlockPos(waist + chainDirection * (firstSegmentLength / 2))
            val lightmap = getBrightnessForRender(world, lightPos)

            val minV = 1 - firstSegmentLength.toFloat()
            val len = firstSegmentLength

            consumer.vertex(matrices, -deltaX, 0, -deltaZ).color(1f, 1f, 1f, 1f).texture(0f, minV)
                .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, normalX, 0, normalZ).next()
            consumer.vertex(matrices, deltaX, 0, deltaZ).color(1f, 1f, 1f, 1f).texture(1f, minV)
                .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, normalX, 0, normalZ).next()
            consumer.vertex(matrices, deltaX, len, deltaZ).color(1f, 1f, 1f, 1f).texture(1f, 1f)
                .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, normalX, 0, normalZ).next()
            consumer.vertex(matrices, -deltaX, len, -deltaZ).color(1f, 1f, 1f, 1f).texture(0f, 1f)
                .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, normalX, 0, normalZ).next()

            consumer.vertex(matrices, -deltaX, len, -deltaZ).color(1f, 1f, 1f, 1f).texture(0f, 1f)
                .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, -normalX, 0, -normalZ).next()
            consumer.vertex(matrices, deltaX, len, deltaZ).color(1f, 1f, 1f, 1f).texture(1f, 1f)
                .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, -normalX, 0, -normalZ).next()
            consumer.vertex(matrices, deltaX, 0, deltaZ).color(1f, 1f, 1f, 1f).texture(1f, minV)
                .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, -normalX, 0, -normalZ).next()
            consumer.vertex(matrices, -deltaX, 0, -deltaZ).color(1f, 1f, 1f, 1f).texture(0f, minV)
                .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, -normalX, 0, -normalZ).next()
        }
        for (i in 0 until chainSegments) {
            val yPos = firstSegmentLength + i

            val lightPos = BlockPos(waist + chainDirection * (yPos + 0.5))
            val lightmap = getBrightnessForRender(world, lightPos)

            consumer.vertex(matrices, -deltaX, yPos, -deltaZ).color(1f, 1f, 1f, 1f).texture(0f, 0f)
                .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, normalX, 0, normalZ).next()
            consumer.vertex(matrices, deltaX, yPos, deltaZ).color(1f, 1f, 1f, 1f).texture(1f, 0f)
                .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, normalX, 0, normalZ).next()
            consumer.vertex(matrices, deltaX, yPos + 1, deltaZ).color(1f, 1f, 1f, 1f).texture(1f, 1f)
                .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, normalX, 0, normalZ).next()
            consumer.vertex(matrices, -deltaX, yPos + 1, -deltaZ).color(1f, 1f, 1f, 1f).texture(0f, 1f)
                .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, normalX, 0, normalZ).next()

            consumer.vertex(matrices, -deltaX, yPos + 1, -deltaZ).color(1f, 1f, 1f, 1f).texture(0f, 1f)
                .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, -normalX, 0, -normalZ).next()
            consumer.vertex(matrices, deltaX, yPos + 1, deltaZ).color(1f, 1f, 1f, 1f).texture(1f, 1f)
                .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, -normalX, 0, -normalZ).next()
            consumer.vertex(matrices, deltaX, yPos, deltaZ).color(1f, 1f, 1f, 1f).texture(1f, 0f)
                .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, -normalX, 0, -normalZ).next()
            consumer.vertex(matrices, -deltaX, yPos, -deltaZ).color(1f, 1f, 1f, 1f).texture(0f, 0f)
                .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, -normalX, 0, -normalZ).next()
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