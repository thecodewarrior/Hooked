package dev.thecodewarrior.hooked.mixin;

import dev.thecodewarrior.hooked.bridge.HookPlayerFlags;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin implements HookPlayerFlags {
    private boolean hookedShouldAbortElytraFlag = false;

    @Override
    public boolean getHookedShouldAbortElytraFlag() {
        return hookedShouldAbortElytraFlag;
    }

    @Override
    public void setHookedShouldAbortElytraFlag(boolean hookedShouldAbortElytraFlag) {
        this.hookedShouldAbortElytraFlag = hookedShouldAbortElytraFlag;
    }

    @Inject(method = "livingTick", at = @At("RETURN"))
    private void livingTickMixin(CallbackInfo ci) {
        hookedShouldAbortElytraFlag = false;
    }
}
