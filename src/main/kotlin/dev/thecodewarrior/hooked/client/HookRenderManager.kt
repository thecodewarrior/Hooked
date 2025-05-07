package dev.thecodewarrior.hooked.client

import com.teamwizardry.librarianlib.core.util.Client
import com.teamwizardry.librarianlib.core.util.DistinctColors
import com.teamwizardry.librarianlib.core.util.kotlin.color
import com.teamwizardry.librarianlib.core.util.vec
import com.teamwizardry.librarianlib.math.minus
import com.teamwizardry.librarianlib.math.plus
import com.teamwizardry.librarianlib.math.times
import dev.thecodewarrior.hooked.bridge.hookData
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.client.renderer.HookRenderer
import dev.thecodewarrior.hooked.hook.ClientHookProcessor
import dev.thecodewarrior.hooked.hook.Hook
import dev.thecodewarrior.hooked.hook.HookBehaviorType
import dev.thecodewarrior.hooked.hook.HookPlayerController
import dev.thecodewarrior.hooked.util.getWaistPos
import dev.thecodewarrior.hooked.util.normal
import dev.thecodewarrior.hooked.util.vertex
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.render.*
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceReloader
import net.minecraft.util.Identifier
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.profiler.Profiler
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.math.sqrt

object HookRenderManager: WorldRenderEvents.AfterEntities {
    private val registry = mutableMapOf<HookBehaviorType<*>, HookRenderer<*>>()

    fun register(type: HookBehaviorType<*>, renderer: HookRenderer<*>) {
        registry[type] = renderer
    }

    fun getRenderer(type: HookBehaviorType<*>): HookRenderer<in HookPlayerController>? {
        @Suppress("UNCHECKED_CAST")
        return registry[type] as HookRenderer<in HookPlayerController>?
    }

    fun registerEvents() {
        WorldRenderEvents.AFTER_ENTITIES.register(this)
    }

    fun renderPlayer(player: AbstractClientPlayerEntity, matrices: MatrixStack, tickDelta: Float, consumers: VertexConsumerProvider) {
        val data = player.hookData()
        if (data.maxHooks > 0) {
            val visibleToTeam = !player.isInvisibleTo(Client.player)
            if(!player.isInvisible || visibleToTeam) {
                matrices.push()
                getRenderer(data.properties.behavior.type)?.render(
                    matrices,
                    player,
                    consumers,
                    tickDelta,
                    data,
                    data.controller
                )
                matrices.pop()
            }
            if (Client.minecraft.entityRenderDispatcher.shouldRenderHitboxes()) {
                drawDebugLines(matrices, consumers, player, tickDelta, data)
            }
        }
    }

    fun renderHooks(stack: MatrixStack, tickDelta: Float, consumers: VertexConsumerProvider) {
        Client.minecraft.world?.players?.forEach { player ->
            renderPlayer(player, stack, tickDelta, consumers)
        }
    }

    override fun afterEntities(context: WorldRenderContext) {
        val matrixStack = context.matrixStack()!!
        matrixStack.push()
        val viewPos = context.gameRenderer().camera.pos
        matrixStack.translate(-viewPos.x, -viewPos.y, -viewPos.z)

        renderHooks(matrixStack, context.tickCounter().getTickDelta(true), context.consumers()!!)

        if(Client.minecraft.entityRenderDispatcher.shouldRenderHitboxes()) {
            val player = Client.minecraft.player!!
            val playerBox = player.boundingBox
            val playerBoxMin = vec(playerBox.minX, playerBox.minY, playerBox.minZ)
            val targets = ClientHookProcessor.previewJumpTarget(player)
            if(!targets.isNullOrEmpty()) {
                val targetY = targets.maxOf { it.minY }
                val jumpPreview = targets.filter { it.minY == targetY }.minByOrNull {
                    vec(it.minX, it.minY, it.minZ).squaredDistanceTo(playerBoxMin)
                }

                if (jumpPreview != null && targetY > player.y) {
                    val color = DistinctColors.yellow
                    WorldRenderer.drawBox(
                        context.matrixStack(),
                        context.consumers()!!.getBuffer(RenderLayer.getLines()),
                        jumpPreview,
                        color.red / 255f,
                        color.green / 255f,
                        color.blue / 255f,
                        1f
                    )
                }
            }
        }

        matrixStack.pop()
        (context.consumers() as? VertexConsumerProvider.Immediate)?.draw()
    }

    fun drawDebugLines(matrices: MatrixStack, consumers: VertexConsumerProvider, player: PlayerEntity, tickDelta: Float, data: HookedPlayerData) {
        if (data.hooks.isEmpty())
            return

        val consumer = consumers.getBuffer(RenderLayer.getLines())

        val waist = player.getWaistPos(tickDelta)

        data.hooks.forEach { (_, hook) ->
            val color = when(hook.state) {
                Hook.State.EXTENDING -> DistinctColors.green
                Hook.State.PLANTED -> DistinctColors.navy
                Hook.State.RETRACTING -> DistinctColors.red
                Hook.State.REMOVED -> DistinctColors.black
            }

            val hookPos = hook.posLastTick + (hook.pos - hook.posLastTick) * tickDelta

            val normal = (hookPos - waist).normalize()
            consumer.vertex(matrices, waist).color(color).normal(matrices, normal)
            consumer.vertex(matrices, hookPos).color(color).normal(matrices, normal)

            matrices.push()
            matrices.translate(hookPos.x, hookPos.y, hookPos.z)
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-hook.yaw))
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(hook.pitch))

            val length = hook.hookLength
            val claw = length / 3

            val rt2 = sqrt(2.0)

            consumer.vertex(matrices, 0, 0, 0).color(color).normal(matrices, 0, 0, 1)
            consumer.vertex(matrices, 0, 0, length).color(color).normal(matrices, 0, 0, 1)

            consumer.vertex(matrices, -claw, 0, length - claw).color(color).normal(matrices, rt2, 0, rt2)
            consumer.vertex(matrices, 0, 0, length).color(color).normal(matrices, rt2, 0, rt2)

            consumer.vertex(matrices, 0, 0, length).color(color).normal(matrices, rt2, 0, rt2)
            consumer.vertex(matrices, claw, 0, length - claw).color(color).normal(matrices, rt2, 0, rt2)

            consumer.vertex(matrices, 0, -claw, length - claw).color(color).normal(matrices, 0, rt2, rt2)
            consumer.vertex(matrices, 0, 0, length).color(color).normal(matrices, 0, rt2, rt2)

            consumer.vertex(matrices, 0, 0, length).color(color).normal(matrices, 0, rt2, rt2)
            consumer.vertex(matrices, 0, claw, length - claw).color(color).normal(matrices, 0, rt2, rt2)

            matrices.pop()
        }

    }
}