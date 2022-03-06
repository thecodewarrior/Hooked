package dev.thecodewarrior.hooked.mixin;

import dev.thecodewarrior.hooked.bridge.PlayerMixinBridge;
import dev.thecodewarrior.hooked.hook.HookActiveReason;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin implements PlayerMixinBridge {
    @Inject(method = "isAutoJumpEnabled", at = @At("HEAD"), cancellable = true)
    private void hooked$isAutoJumpEnabledMixin(CallbackInfoReturnable<Boolean> cir) {
        if(this.getHookProcessor().isHookActive((PlayerEntity) (Object) this, HookActiveReason.DISABLE_AUTO_JUMP)) {
            cir.setReturnValue(false);
        }
    }
}
