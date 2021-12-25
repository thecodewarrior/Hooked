package dev.thecodewarrior.hooked.mixin;

import dev.thecodewarrior.hooked.bridge.PlayerMixinBridge;
import dev.thecodewarrior.hooked.hook.ClientHookProcessor;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OtherClientPlayerEntity.class)
public abstract class OtherClientPlayerEntityMixin {
    @Inject(method = "tickMovement", at = @At("RETURN"))
    private void tickHooks(CallbackInfo ci) {
        ClientHookProcessor.INSTANCE.tick((AbstractClientPlayerEntity) (Object) this);
    }
}
