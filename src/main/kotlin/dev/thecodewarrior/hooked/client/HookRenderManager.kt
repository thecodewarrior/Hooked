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
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceReloader
import net.minecraft.util.Identifier
import net.minecraft.util.profiler.Profiler
import org.lwjgl.opengl.GL11
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

object HookRenderManager: IdentifiableResourceReloadListener, WorldRenderEvents.DebugRender {
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

    override fun beforeDebugRender(context: WorldRenderContext) {

        context.matrixStack().push()
        val viewPos = Client.minecraft.gameRenderer.camera.pos
        context.matrixStack().translate(-viewPos.x, -viewPos.y, -viewPos.z)

        val world = Client.minecraft.world ?: return
        world.players.forEach { player ->
            val data = player.hookData()
            if (data.type != HookType.NONE) {
                if (Client.minecraft.wireFrame) {
                    drawDebugLines(context, player, data)
                }
            }
        }
    }

    fun drawDebugLines(context: WorldRenderContext, player: PlayerEntity, data: HookedPlayerData) {
        if (data.hooks.isEmpty())
            return

        RenderSystem.lineWidth(1f)
        RenderSystem.disableTexture()

        val vb = FlatLinesRenderBuffer.SHARED

        val waistPos = player.getWaistPos(Client.worldTime.tickDelta)
        val matrix = Matrix4dStack()
        matrix.set(context.matrixStack().peek().model)

        data.hooks.forEach { hook ->

            vb.pos(matrix, waistPos).color(DistinctColors.white).endVertex()
            vb.dupVertex()
            vb.pos(matrix, hook.pos).color(DistinctColors.white).endVertex()
            vb.dupVertex()

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

            vb.pos(matrix, 0, 0, 0).color(color).endVertex()
            vb.dupVertex()
            vb.pos(matrix, 0, length, 0).color(color).endVertex()
            vb.dupVertex()

            vb.pos(matrix, -claw, length - claw, 0).color(color).endVertex()
            vb.dupVertex()
            vb.pos(matrix, 0, length, 0).color(color).endVertex()
            vb.dupVertex()

            vb.dupVertex()
            vb.dupVertex()
            vb.pos(matrix, claw, length - claw, 0).color(color).endVertex()
            vb.dupVertex()

            vb.pos(matrix, 0, length - claw, -claw).color(color).endVertex()
            vb.dupVertex()
            vb.pos(matrix, 0, length, 0).color(color).endVertex()
            vb.dupVertex()

            vb.dupVertex()
            vb.dupVertex()
            vb.pos(matrix, 0, length - claw, claw).color(color).endVertex()
            vb.dupVertex()

            matrix.pop()
        }

        vb.draw(Primitive.LINES_ADJACENCY)
        RenderSystem.enableTexture()
    }

}