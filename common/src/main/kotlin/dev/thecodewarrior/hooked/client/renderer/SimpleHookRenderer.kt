package dev.thecodewarrior.hooked.client.renderer

import com.teamwizardry.librarianlib.math.*
import dev.ryanhcode.sable.companion.SableCompanion
import dev.thecodewarrior.hooked.Hooked
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.hook.Hook
import dev.thecodewarrior.hooked.hook.HookPlayerController
import dev.thecodewarrior.hooked.item.HookProperties
import dev.thecodewarrior.hooked.util.getWaistPos
import dev.thecodewarrior.hooked.util.normal
import dev.thecodewarrior.hooked.util.toAngles
import dev.thecodewarrior.hooked.util.vertex
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkSectionPos
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.asin
import kotlin.math.atan2

abstract class SimpleHookRenderer<C: HookPlayerController>(): HookRenderer<C>() {
    protected fun renderHooks(
        matrices: MatrixStack,
        player: PlayerEntity,
        consumers: VertexConsumerProvider,
        tickDelta: Float,
        data: HookedPlayerData,
    ) {
        data.hooks.forEach { (_, hook) ->
            if(!hook.firstTick)
                renderHook(matrices, player, consumers, tickDelta, data.properties, hook)
        }
        // force it to draw
        consumers.getBuffer(RenderLayer.getEntityCutout(Identifier.of("minecraft:textures/misc/white.png")))
    }

    private fun renderHook(
        matrices: MatrixStack,
        player: PlayerEntity,
        consumers: VertexConsumerProvider,
        tickDelta: Float,
        properties: HookProperties,
        hook: Hook,
    ) {
        val waist = player.getWaistPos(tickDelta)
        val (hookPos, hookDirection) = hook.renderPose(
            SableCompanion.INSTANCE.getContainingClient(hook.pos),
            tickDelta
        )
        val chainLength = waist.distanceTo(hookPos)
        val chainDirection = (hookPos - waist) / chainLength

        matrices.push()
        val (chainPitch, chainYaw) = chainDirection.toAngles()
        // we add 90 to the pitch because the model is based on +y, but pitch/yaw are based on +z
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-chainYaw))
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(chainPitch + 90))
        matrices.translate(0.0, properties.chainAppearance.playerGap, 0.0)

        val actualLength = chainLength - properties.chainAppearance.playerGap

        drawHalfChain(matrices, consumers, properties.chainAppearance.texture1, player.world, waist, chainDirection, actualLength, 0.5, 0.0, 0.0, 1.0)
        drawHalfChain(matrices, consumers, properties.chainAppearance.texture2, player.world, waist, chainDirection, actualLength, 0.0, 0.5, -1.0, 0.0)

        matrices.pop()

        matrices.push()
        matrices.translate(hookPos.x - waist.x, hookPos.y - waist.y, hookPos.z - waist.z)
        val (hookPitch, hookYaw) = hookDirection.toAngles()
        // we add 90 to the pitch because the model is based on +y, but pitch/yaw are based on +z
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-hookYaw))
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(hookPitch + 90))

        val consumer = consumers.getBuffer(RenderLayer.getEntityCutout(properties.hookModel.texture))
        val lightmap = getBrightnessForRender(player.world, hookPos)

        val modelRenderer = HookModelLoader.getModel(properties.hookModel.model)
        modelRenderer.render(matrices, consumer, lightmap)

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
        if (chainLength > 1000) // this can happen with sable sublevels
            return
        val chainSegments = floorInt(chainLength)
        val firstSegmentLength = chainLength - chainSegments

        val consumer = consumers.getBuffer(RenderLayer.getEntityCutout(texture))

        if (firstSegmentLength > chainLengthEpsilon) {
            val lightPos = waist + chainDirection * (firstSegmentLength / 2)
            val lightmap = getBrightnessForRender(world, lightPos)

            val minV = 1 - firstSegmentLength.toFloat()
            val len = firstSegmentLength

            consumer.vertex(matrices, -deltaX, 0, -deltaZ).color(1f, 1f, 1f, 1f).texture(0f, minV)
                .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, normalX, 0, normalZ)
            consumer.vertex(matrices, deltaX, 0, deltaZ).color(1f, 1f, 1f, 1f).texture(1f, minV)
                .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, normalX, 0, normalZ)
            consumer.vertex(matrices, deltaX, len, deltaZ).color(1f, 1f, 1f, 1f).texture(1f, 1f)
                .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, normalX, 0, normalZ)
            consumer.vertex(matrices, -deltaX, len, -deltaZ).color(1f, 1f, 1f, 1f).texture(0f, 1f)
                .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, normalX, 0, normalZ)

            consumer.vertex(matrices, -deltaX, len, -deltaZ).color(1f, 1f, 1f, 1f).texture(0f, 1f)
                .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, -normalX, 0, -normalZ)
            consumer.vertex(matrices, deltaX, len, deltaZ).color(1f, 1f, 1f, 1f).texture(1f, 1f)
                .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, -normalX, 0, -normalZ)
            consumer.vertex(matrices, deltaX, 0, deltaZ).color(1f, 1f, 1f, 1f).texture(1f, minV)
                .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, -normalX, 0, -normalZ)
            consumer.vertex(matrices, -deltaX, 0, -deltaZ).color(1f, 1f, 1f, 1f).texture(0f, minV)
                .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, -normalX, 0, -normalZ)
        }
        for (i in 0 until chainSegments) {
            val yPos = firstSegmentLength + i

            val lightPos = waist + chainDirection * (yPos + 0.5)
            val lightmap = getBrightnessForRender(world, lightPos)

            consumer.vertex(matrices, -deltaX, yPos, -deltaZ).color(1f, 1f, 1f, 1f).texture(0f, 0f)
                .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, normalX, 0, normalZ)
            consumer.vertex(matrices, deltaX, yPos, deltaZ).color(1f, 1f, 1f, 1f).texture(1f, 0f)
                .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, normalX, 0, normalZ)
            consumer.vertex(matrices, deltaX, yPos + 1, deltaZ).color(1f, 1f, 1f, 1f).texture(1f, 1f)
                .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, normalX, 0, normalZ)
            consumer.vertex(matrices, -deltaX, yPos + 1, -deltaZ).color(1f, 1f, 1f, 1f).texture(0f, 1f)
                .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, normalX, 0, normalZ)

            consumer.vertex(matrices, -deltaX, yPos + 1, -deltaZ).color(1f, 1f, 1f, 1f).texture(0f, 1f)
                .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, -normalX, 0, -normalZ)
            consumer.vertex(matrices, deltaX, yPos + 1, deltaZ).color(1f, 1f, 1f, 1f).texture(1f, 1f)
                .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, -normalX, 0, -normalZ)
            consumer.vertex(matrices, deltaX, yPos, deltaZ).color(1f, 1f, 1f, 1f).texture(1f, 0f)
                .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, -normalX, 0, -normalZ)
            consumer.vertex(matrices, -deltaX, yPos, -deltaZ).color(1f, 1f, 1f, 1f).texture(0f, 0f)
                .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, -normalX, 0, -normalZ)
        }
    }

    private fun getBrightnessForRender(world: World, pos: Vec3d): Int {
        val sectionX = ChunkSectionPos.getSectionCoord(pos.x)
        val sectionZ = ChunkSectionPos.getSectionCoord(pos.z)
        return if (world.chunkManager.isChunkLoaded(sectionX, sectionZ)) {
            WorldRenderer.getLightmapCoordinates(world, BlockPos.ofFloored(pos))
        } else {
            0
        }
    }

    companion object {
        val chainLengthEpsilon = 1 / 16.0
        val logger = Hooked.logManager.makeLogger<SimpleHookRenderer<*>>()
    }
}