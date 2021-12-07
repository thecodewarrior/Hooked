package dev.thecodewarrior.hooked.mixin;

import dev.thecodewarrior.hooked.bridge.PlayerMixinBridge;
import dev.thecodewarrior.hooked.hook.ClientHookProcessor;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin implements PlayerMixinBridge {
    private boolean hookedShouldAbortElytraFlag = false;

    @Override
    public boolean getHookedShouldAbortElytraFlag() {
        return hookedShouldAbortElytraFlag;
    }

    @Override
    public void setHookedShouldAbortElytraFlag(boolean hookedShouldAbortElytraFlag) {
        this.hookedShouldAbortElytraFlag = hookedShouldAbortElytraFlag;
    }

    @Inject(method = "tickMovement", at = @At("RETURN"))
    private void tickHooks(CallbackInfo ci) {
        hookedShouldAbortElytraFlag = false;
        ClientHookProcessor.INSTANCE.tick((ClientPlayerEntity) (Object) this);
    }
}
