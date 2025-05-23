package dev.thecodewarrior.hooked.client.renderer

import com.teamwizardry.librarianlib.core.util.Client
import dev.thecodewarrior.hooked.Hooked
import dev.thecodewarrior.hooked.shade.obj.Obj
import dev.thecodewarrior.hooked.shade.obj.ObjData
import dev.thecodewarrior.hooked.shade.obj.ObjReader
import dev.thecodewarrior.hooked.shade.obj.ObjUtils
import dev.thecodewarrior.hooked.shade.obj.Objs
import dev.thecodewarrior.hooked.util.normal
import dev.thecodewarrior.hooked.util.vertex
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceReloader
import net.minecraft.util.Identifier
import net.minecraft.util.profiler.Profiler
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

object HookModelLoader : ResourceReloader {
    val models = mutableMapOf<Identifier, HookModelRenderer>()

    fun getModel(id: Identifier): HookModelRenderer {
        return models.getOrPut(id) {
            HookModelRenderer(id).also {
                it.loadModel(Client.resourceManager)
            }
        }
    }

    override fun reload(
        synchronizer: ResourceReloader.Synchronizer,
        manager: ResourceManager,
        prepareProfiler: Profiler,
        applyProfiler: Profiler,
        prepareExecutor: Executor,
        applyExecutor: Executor
    ): CompletableFuture<Void> {
        return synchronizer.whenPrepared(null).thenAccept {
            models.clear()
        }
    }
}

class HookModelRenderer(val modelLocation: Identifier) {
    private var model: Obj = Objs.create()
    private var modelVertexIndices: IntArray = IntArray(0)

    fun loadModel(manager: ResourceManager) {
        var rawModel = try {
            manager.open(modelLocation).use { ObjReader.read(it) }
        } catch (e: IOException) {
            logger.error("Failed to load model", e)
            Objs.create()
        }

        model = ObjUtils.convertToRenderable(rawModel)
        modelVertexIndices = ObjData.getFaceVertexIndicesArray(model)
    }

    fun render(
        matrices: MatrixStack,
        consumer: VertexConsumer,
        lightmap: Int,
    ) {
        modelVertexIndices.forEachIndexed { i, vertexIndex ->
            val vertex = model.getVertex(vertexIndex)
            val tex = model.getTexCoord(vertexIndex)
            val normal = model.getNormal(vertexIndex)
            consumer.vertex(matrices, vertex.x, vertex.y, vertex.z).color(1f, 1f, 1f, 1f).texture(tex.x, 1 - tex.y)
                .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, normal.x, normal.y, normal.z)
            if(i % 3 == 2) {
                // see RenderSystem.sharedSequentialQuad
                // 1-2
                // |/|
                // 0-3
                // the game operates in quads, so we need to get rid of the 2-3-0 triangle
                consumer.vertex(matrices, vertex.x, vertex.y, vertex.z).color(1f, 1f, 1f, 1f).texture(tex.x, 1 - tex.y)
                    .overlay(OverlayTexture.DEFAULT_UV).light(lightmap).normal(matrices, normal.x, normal.y, normal.z)
            }
        }

    }

    companion object {
        val logger = Hooked.logManager.makeLogger<HookModelRenderer>()
    }
}