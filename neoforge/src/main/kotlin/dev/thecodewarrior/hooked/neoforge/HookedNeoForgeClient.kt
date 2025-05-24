package dev.thecodewarrior.hooked.neoforge

import dev.thecodewarrior.hooked.Hooked
import dev.thecodewarrior.hooked.HookedClient
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.Camera
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.client.event.RenderLevelStageEvent
import net.neoforged.neoforge.common.NeoForge

@Mod(Hooked.MOD_ID, dist = [Dist.CLIENT])
class HookedNeoForgeClient {
    init {
        HookedClient.init()
        NeoForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun onRenderWorldEvent(event: RenderLevelStageEvent) {
        if (event.stage == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            // we don't get the renderer's buffer builders like we can in the fabric event,
            // but in practice this is what is used
            val vertexConsumers = MinecraftClient.getInstance().bufferBuilders.entityVertexConsumers
            renderEventHooks.forEach {
                it(event.poseStack, event.camera, vertexConsumers)
            }
        }
    }

    @SubscribeEvent
    fun onClientTick(event: ClientTickEvent.Post) {
        clientTickHooks.forEach { it() }
    }

    companion object {
        val renderEventHooks = mutableListOf<(MatrixStack, Camera, VertexConsumerProvider) -> Unit>()
        val clientTickHooks = mutableListOf<() -> Unit>()
    }
}