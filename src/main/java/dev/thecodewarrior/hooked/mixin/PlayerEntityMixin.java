package dev.thecodewarrior.hooked.mixin;

import dev.thecodewarrior.hooked.HookedModStats;
import dev.thecodewarrior.hooked.bridge.PlayerMixinBridge;
import dev.thecodewarrior.hooked.capability.HookedPlayerData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements PlayerMixinBridge {
    private HookedPlayerData hookedPlayerData;
    private boolean hookedTravelingByHookFlag = false;

    @NotNull
    @Override
    public HookedPlayerData getHookedPlayerData() {
        return hookedPlayerData;
    }

    @Override
    public void setHookedPlayerData(@NotNull HookedPlayerData hookedPlayerData) {
        this.hookedPlayerData = hookedPlayerData;
    }

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

    @Shadow public abstract void increaseStat(Identifier stat, int amount);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initializeHookedData() {
        //noinspection ConstantConditions
        hookedPlayerData = new HookedPlayerData((PlayerEntity) (Object) this);
    }

    @Inject(method = "increaseTravelMotionStats(DDD)V", at = @At("HEAD"), cancellable = true)
    private void increaseTravelMotionStatsHookedMixin(double dx, double dy, double dz, CallbackInfo ci) {
        if(hookedTravelingByHookFlag) {
            int cm = Math.round(MathHelper.sqrt((float) (dx * dx + dy * dy + dz * dz)) * 100.0F);
            if (cm > 0) {
                this.increaseStat(HookedModStats.INSTANCE.getHookOneCm(), cm);
            }
            ci.cancel();
        }
    }

    @Inject(method = "checkFallFlying", at = @At("HEAD"), cancellable = true)
    private void checkFallFlyingHookedMixin(CallbackInfoReturnable<Boolean> cir) {
        if(this.getHookedShouldAbortElytraFlag()) {
            cir.setReturnValue(false);
        }
    }

}
