package dev.thecodewarrior.hooked.mixin;

import dev.thecodewarrior.hooked.bridge.PlayerMixinBridge;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OtherClientPlayerEntity.class)
public abstract class OtherClientPlayerEntityMixin implements PlayerMixinBridge {
    @Inject(method = "tickMovement", at = @At("RETURN"))
    private void tickHooks(CallbackInfo ci) {
        getHookProcessor().tick((PlayerEntity) (Object) this);
    }
}
