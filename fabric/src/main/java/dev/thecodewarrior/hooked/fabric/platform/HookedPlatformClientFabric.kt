package dev.thecodewarrior.hooked.fabric.platform

import com.google.auto.service.AutoService
import dev.thecodewarrior.hooked.platform.HookedPlatformClient
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.render.Camera
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.InputUtil
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceReloader
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier
import net.minecraft.util.profiler.Profiler
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

@AutoService(HookedPlatformClient::class)
class HookedPlatformClientFabric : HookedPlatformClient {
    override fun createInGameKeybind(
        translationKey: String,
        type: InputUtil.Type,
        code: Int,
        category: String
    ): KeyBinding {
        return KeyBinding(translationKey, type, code, category)
    }

    override fun registerKeybindTickEvent(hook: () -> Unit) {
        ClientTickEvents.END_CLIENT_TICK.register { hook() }
    }

    override fun registerRenderWorldEvent(hook: (MatrixStack, Camera, VertexConsumerProvider) -> Unit) {
        WorldRenderEvents.AFTER_ENTITIES.register { context ->
            hook(
                context.matrixStack()!!,
                context.gameRenderer().camera,
                context.consumers()!!
            )
        }
    }

    override fun registerClientResourceReloader(loader: ResourceReloader, identifier: Identifier) {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(object : IdentifiableResourceReloadListener {
            override fun getFabricId(): Identifier = identifier

            override fun reload(
                synchronizer: ResourceReloader.Synchronizer,
                manager: ResourceManager,
                prepareProfiler: Profiler,
                applyProfiler: Profiler,
                prepareExecutor: Executor,
                applyExecutor: Executor
            ): CompletableFuture<Void> {
                return loader.reload(synchronizer, manager, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor)
            }
        });
    }
}