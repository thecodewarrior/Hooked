package dev.thecodewarrior.hooked.fabric.mixin;

import dev.thecodewarrior.hooked.client.HookRenderManager;
import net.irisshaders.iris.mixin.LevelRendererAccessor;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShadowRenderer.class)
public class IrisShadowRendererMixin {
    @Shadow @Final private BufferBuilderStorage buffers;

    @Inject(method = "renderEntities", at = @At("TAIL"))
    private void hooked$renderEntities(LevelRendererAccessor levelRenderer, EntityRenderDispatcher dispatcher, VertexConsumerProvider.Immediate bufferSource, MatrixStack modelView, float tickDelta, Frustum frustum, double cameraX, double cameraY, double cameraZ, CallbackInfoReturnable<Integer> cir) {
        modelView.translate(-cameraX, -cameraY, -cameraZ);
        HookRenderManager.INSTANCE.renderHooks(modelView, tickDelta, buffers.getEntityVertexConsumers());
        modelView.translate(cameraX, cameraY, cameraZ);
    }

    @Inject(method = "renderPlayerEntity", at = @At("TAIL"))
    private void hooked$renderPlayerEntity(LevelRendererAccessor levelRenderer, EntityRenderDispatcher dispatcher, VertexConsumerProvider.Immediate bufferSource, MatrixStack modelView, float tickDelta, Frustum frustum, double cameraX, double cameraY, double cameraZ, CallbackInfoReturnable<Integer> cir) {
        var player = MinecraftClient.getInstance().player;
        if (player != null) {
            modelView.translate(-cameraX, -cameraY, -cameraZ);
            HookRenderManager.INSTANCE.renderPlayer(player, modelView, tickDelta, buffers.getEntityVertexConsumers());
            modelView.translate(cameraX, cameraY, cameraZ);
        }
    }
}
