package dev.thecodewarrior.hooked.mixin;

import com.teamwizardry.librarianlib.core.util.Client;
import dev.thecodewarrior.hooked.client.HookRenderManager;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {
    @Inject(method = "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("TAIL"))
    private void hooked$render(AbstractClientPlayerEntity player, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        var tempModel = new Matrix4f(matrixStack.peek().getModel());
        var tempNormal = new Matrix3f(matrixStack.peek().getNormal());
        matrixStack.pop();

        HookRenderManager.INSTANCE.renderPlayer(player, matrixStack, tickDelta, vertexConsumerProvider);

        matrixStack.push();
        matrixStack.peek().getModel().load(tempModel);
        matrixStack.peek().getNormal().load(tempNormal);
    }
}
