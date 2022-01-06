package dev.thecodewarrior.hooked.client

import com.mojang.blaze3d.systems.RenderSystem
import com.teamwizardry.librarianlib.albedo.base.buffer.FlatLinesRenderBuffer
import com.teamwizardry.librarianlib.albedo.buffer.Primitive
import com.teamwizardry.librarianlib.core.util.Client
import com.teamwizardry.librarianlib.core.util.DistinctColors
import com.teamwizardry.librarianlib.core.util.kotlin.color
import com.teamwizardry.librarianlib.core.util.kotlin.getOrNull
import com.teamwizardry.librarianlib.core.util.kotlin.vertex
import com.teamwizardry.librarianlib.core.util.vec
import com.teamwizardry.librarianlib.math.*
import dev.thecodewarrior.hooked.bridge.hookData
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.client.renderer.HookRenderer
import dev.thecodewarrior.hooked.hook.ClientHookProcessor
import dev.thecodewarrior.hooked.hook.Hook
import dev.thecodewarrior.hooked.hook.HookPlayerController
import dev.thecodewarrior.hooked.hook.HookType
import dev.thecodewarrior.hooked.util.getWaistPos
import dev.thecodewarrior.hooked.util.toMc
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.VertexConsumers
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceReloader
import net.minecraft.util.Identifier
import net.minecraft.util.math.Box
import net.minecraft.util.profiler.Profiler
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

object HookRenderManager: IdentifiableResourceReloadListener, WorldRenderEvents.Last {
    private val registry = mutableMapOf<HookType, HookRenderer<*>>()

    fun register(type: HookType, renderer: HookRenderer<*>) {
        registry[type] = renderer
    }
    fun getRenderer(type: HookType): HookRenderer<in HookPlayerController>? {
        @Suppress("UNCHECKED_CAST")
        return registry[type] as HookRenderer<in HookPlayerController>?
    }

    override fun getFabricId(): Identifier {
        return Identifier("hooked:hook_render_manager")
    }

    fun registerEvents() {
        WorldRenderEvents.LAST.register(this)
    }

    override fun reload(
        synchronizer: ResourceReloader.Synchronizer,
        manager: ResourceManager,
        prepareProfiler: Profiler,
        applyProfiler: Profiler,
        prepareExecutor: Executor,
        applyExecutor: Executor
    ): CompletableFuture<Void> {
        return CompletableFuture.allOf(
            *registry.map { (_, renderer) ->
                renderer.reload(synchronizer, manager, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor)
            }.toTypedArray()
        )
    }

    override fun onLast(context: WorldRenderContext) {
        val stack = MatrixStack()
        val viewPos = Client.minecraft.gameRenderer.camera.pos
        stack.translate(-viewPos.x, -viewPos.y, -viewPos.z)
        stack.peek().normal.load(context.matrixStack().peek().normal)

        val world = Client.minecraft.world ?: return
        world.players.forEach { player ->
            val data = player.hookData()
            if (data.type != HookType.NONE) {
                val visibleToTeam = !player.isInvisibleTo(Client.player)
                if(!player.isInvisible || visibleToTeam) {
                    stack.push()
                    getRenderer(data.type)?.render(
                        stack,
                        player,
                        player.isInvisible && visibleToTeam,
                        context.tickDelta(),
                        data,
                        data.controller
                    )
                    stack.pop()
                }
                if (Client.minecraft.entityRenderDispatcher.shouldRenderHitboxes()) {
                    drawDebugLines(stack, player, context.tickDelta(), data)
                }
            }
        }

        if(Client.minecraft.entityRenderDispatcher.shouldRenderHitboxes()) {
            val jumpPreview = ClientHookProcessor.previewJumpTarget(Client.minecraft.player!!)?.maxByOrNull { it.minY }

            if(jumpPreview != null) {
                stack.push()
                drawBoundingBox(stack, jumpPreview)
                stack.pop()
            }
        }
    }

    fun drawDebugLines(matrices: MatrixStack, player: PlayerEntity, tickDelta: Float, data: HookedPlayerData) {
        if (data.hooks.isEmpty())
            return

        RenderSystem.disableDepthTest()
        val vb = FlatLinesRenderBuffer.SHARED

        val waist = player.getWaistPos(tickDelta)

        data.hooks.forEach { (_, hook) ->
            val color = when(hook.state) {
                Hook.State.EXTENDING -> DistinctColors.green
                Hook.State.PLANTED -> DistinctColors.navy
                Hook.State.RETRACTING -> DistinctColors.red
                Hook.State.REMOVED -> DistinctColors.black
            }

            val hookPos = hook.posLastTick + (hook.pos - hook.posLastTick) * tickDelta

            vb.pos(matrices, waist).color(color).width(3f).endVertex()
            vb.dupVertex()
            vb.pos(matrices, hookPos).color(color).width(3f).endVertex()

            matrices.push()
            matrices.translate(hookPos.x, hookPos.y, hookPos.z)
            matrices.multiply(Quaternion.fromAxesAnglesDeg(hook.pitch, -hook.yaw, 0f).toMc())

            val length = hook.type.hookLength
            val claw = length / 3

            vb.pos(matrices, 0, 0, length).color(color).width(3f).endVertex()
            vb.dupVertex()

            vb.draw(Primitive.LINE_STRIP_ADJACENCY)

            vb.pos(matrices, 0, 0, 0).color(color).width(3f).endVertex() // curl in ends because why not
            vb.pos(matrices, -claw, 0, length - claw).color(color).width(3f).endVertex()
            vb.pos(matrices, 0, 0, length).color(color).width(3f).endVertex()
            vb.pos(matrices, claw, 0, length - claw).color(color).width(3f).endVertex()
            vb.pos(matrices, 0, 0, 0).color(color).width(3f).endVertex() // curl in ends because why not

            vb.draw(Primitive.LINE_STRIP_ADJACENCY)

            vb.pos(matrices, 0, 0, 0).color(color).width(3f).endVertex() // curl in ends because why not
            vb.pos(matrices, 0, -claw, length - claw).color(color).width(3f).endVertex()
            vb.pos(matrices, 0, 0, length).color(color).width(3f).endVertex()
            vb.pos(matrices, 0, claw, length - claw).color(color).width(3f).endVertex()
            vb.pos(matrices, 0, 0, 0).color(color).width(3f).endVertex() // curl in ends because why not

            vb.draw(Primitive.LINE_STRIP_ADJACENCY)

            matrices.pop()
        }

        RenderSystem.enableDepthTest()
    }


    fun drawBoundingBox(matrices: MatrixStack, box: Box) {
        val color = DistinctColors.yellow
        val width = 3f

        val vb = FlatLinesRenderBuffer.SHARED

        vb.pos(matrices, box.maxX, box.minY, box.maxZ).color(color).width(width).endVertex()
        vb.pos(matrices, box.maxX, box.minY, box.minZ).color(color).width(width).endVertex()
        vb.pos(matrices, box.minX, box.minY, box.minZ).color(color).width(width).endVertex()
        vb.pos(matrices, box.minX, box.maxY, box.minZ).color(color).width(width).endVertex()
        vb.pos(matrices, box.maxX, box.maxY, box.minZ).color(color).width(width).endVertex()
        vb.pos(matrices, box.maxX, box.maxY, box.maxZ).color(color).width(width).endVertex()
        vb.draw(Primitive.LINE_STRIP_ADJACENCY)

        vb.pos(matrices, box.minX, box.minY, box.maxZ).color(color).width(width).endVertex()
        vb.pos(matrices, box.maxX, box.minY, box.maxZ).color(color).width(width).endVertex()
        vb.pos(matrices, box.maxX, box.minY, box.minZ).color(color).width(width).endVertex()
        vb.pos(matrices, box.maxX, box.maxY, box.minZ).color(color).width(width).endVertex()
        vb.pos(matrices, box.maxX, box.maxY, box.maxZ).color(color).width(width).endVertex()
        vb.pos(matrices, box.minX, box.maxY, box.maxZ).color(color).width(width).endVertex()
        vb.draw(Primitive.LINE_STRIP_ADJACENCY)

        vb.pos(matrices, box.minX, box.minY, box.minZ).color(color).width(width).endVertex()
        vb.pos(matrices, box.minX, box.minY, box.maxZ).color(color).width(width).endVertex()
        vb.pos(matrices, box.maxX, box.minY, box.maxZ).color(color).width(width).endVertex()
        vb.pos(matrices, box.maxX, box.maxY, box.maxZ).color(color).width(width).endVertex()
        vb.pos(matrices, box.minX, box.maxY, box.maxZ).color(color).width(width).endVertex()
        vb.pos(matrices, box.minX, box.maxY, box.minZ).color(color).width(width).endVertex()
        vb.draw(Primitive.LINE_STRIP_ADJACENCY)

        vb.pos(matrices, box.maxX, box.minY, box.minZ).color(color).width(width).endVertex()
        vb.pos(matrices, box.minX, box.minY, box.minZ).color(color).width(width).endVertex()
        vb.pos(matrices, box.minX, box.minY, box.maxZ).color(color).width(width).endVertex()
        vb.pos(matrices, box.minX, box.maxY, box.maxZ).color(color).width(width).endVertex()
        vb.pos(matrices, box.minX, box.maxY, box.minZ).color(color).width(width).endVertex()
        vb.pos(matrices, box.maxX, box.maxY, box.minZ).color(color).width(width).endVertex()
        vb.draw(Primitive.LINE_STRIP_ADJACENCY)
    }

}