package dev.thecodewarrior.hooked.mixin;

import com.teamwizardry.librarianlib.core.util.Client;
import dev.thecodewarrior.hooked.client.HookRenderManager;
import net.coderbot.iris.mixin.WorldRendererAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(targets = "net.coderbot.iris.pipeline.ShadowRenderer")
public class IrisShadowRendererMixin {
    @Shadow @Final private BufferBuilderStorage buffers;

    @Inject(
            method = "renderShadows",
            at = @At(
                    value = "INVOKE_STRING",
                    target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
                    args = {"ldc=build blockentities"}
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void hooked$renderEntities(
            WorldRendererAccessor worldRenderer, Camera playerCamera,
            CallbackInfo ci,
            MinecraftClient client, MatrixStack modelView
    ) {
        var cameraPos = client.gameRenderer.getCamera().getPos();
        modelView.translate(-cameraPos.getX(), -cameraPos.getY(), -cameraPos.getZ());
        HookRenderManager.INSTANCE.renderHooks(modelView, Client.getMinecraft().getTickDelta(), buffers.getEntityVertexConsumers());
        modelView.translate(cameraPos.getX(), cameraPos.getY(), cameraPos.getZ());
    }
}
