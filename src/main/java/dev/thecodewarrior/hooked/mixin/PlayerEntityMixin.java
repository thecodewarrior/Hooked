package dev.thecodewarrior.hooked.mixin;

import dev.thecodewarrior.hooked.HookedModStats;
import dev.thecodewarrior.hooked.bridge.HookPlayerFlags;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements HookPlayerFlags {
    private boolean hookedTravelingByHookFlag = false;

    @Override
    public boolean getHookedTravelingByHookFlag() {
        return hookedTravelingByHookFlag;
    }

    @Override
    public void setHookedTravelingByHookFlag(boolean travelingByHook) {
        hookedTravelingByHookFlag = travelingByHook;
    }

    @Override
    public boolean getHookedShouldAbortElytraFlag() {
        return false; // the flag only applies to the client player
    }

    @Override
    public void setHookedShouldAbortElytraFlag(boolean hookedShouldAbortElytraFlag) {
        // the flag only applies to the client player
    }

    @Shadow public abstract void addStat(ResourceLocation p_195067_1_, int p_195067_2_);

    @Inject(method = "addMovementStat(DDD)V", at = @At("HEAD"), cancellable = true)
    private void addMovementStatMixin(double dx, double dy, double dz, CallbackInfo ci) {
        if(hookedTravelingByHookFlag) {
            int cm = Math.round(MathHelper.sqrt(dx * dx + dy * dy + dz * dz) * 100.0F);
            if (cm > 0) {
                this.addStat(HookedModStats.INSTANCE.getHookOneCm(), cm);
            }
            ci.cancel();
        }
    }

    @Inject(method = "tryToStartFallFlying", at = @At("HEAD"), cancellable = true)
    private void tryToStartFallFlyingMixin(CallbackInfoReturnable<Boolean> cir) {
        if(this.getHookedShouldAbortElytraFlag()) {
            cir.setReturnValue(false);
        }
    }

}
