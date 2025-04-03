package dev.thecodewarrior.hooked.mixin;

import com.teamwizardry.librarianlib.core.util.Client;
import dev.thecodewarrior.hooked.client.HookRenderManager;
import net.coderbot.iris.mixin.WorldRendererAccessor;
import net.coderbot.iris.pipeline.ShadowRenderer;
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

@Mixin(ShadowRenderer.class)
public class IrisShadowRendererMixin {
    @Shadow @Final private BufferBuilderStorage buffers;

    @Inject(
            method = "renderShadows",
            at = @At(
                    value = "INVOKE_STRING",
                    target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
                    args = {"ldc=build geometry"},
                    shift = At.Shift.AFTER
            )
    )
    private void hooked$renderEntities(WorldRendererAccessor worldRenderer, Camera playerCamera, CallbackInfo ci) {
        var cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
        var modelView = new MatrixStack();
        modelView.method_34425(ShadowRenderer.MODELVIEW);
        modelView.translate(-cameraPos.getX(), -cameraPos.getY(), -cameraPos.getZ());
        HookRenderManager.INSTANCE.renderHooks(modelView, Client.getMinecraft().getTickDelta(), buffers.getEntityVertexConsumers());
    }
}
