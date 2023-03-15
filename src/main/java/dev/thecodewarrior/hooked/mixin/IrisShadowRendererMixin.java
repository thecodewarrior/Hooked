package dev.thecodewarrior.hooked.mixin;

import dev.thecodewarrior.hooked.client.HookRenderManager;
import net.coderbot.iris.pipeline.ShadowRenderer;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ShadowRenderer.class)
public class IrisShadowRendererMixin {
    @Shadow @Final private BufferBuilderStorage buffers;

    @Inject(
            method = "renderBlockEntities",
            at = @At(
                    value = "INVOKE_STRING",
                    target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V",
                    args = {"ldc=build blockentities"}
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void hooked$renderEntities(
            VertexConsumerProvider.Immediate par1, MatrixStack modelView, double cameraX, double cameraY, double cameraZ, float tickDelta, boolean hasEntityFrustum, CallbackInfoReturnable<Integer> cir
    ) {
        modelView.translate(-cameraX, -cameraY, -cameraZ);
        HookRenderManager.INSTANCE.renderHooks(modelView, tickDelta, buffers.getEntityVertexConsumers());
        modelView.translate(cameraX, cameraY, cameraZ);
    }
}
