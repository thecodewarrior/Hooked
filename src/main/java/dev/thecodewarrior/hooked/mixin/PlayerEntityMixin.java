package dev.thecodewarrior.hooked.mixin;

import dev.thecodewarrior.hooked.HookedModStats;
import dev.thecodewarrior.hooked.bridge.HookTravelFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements HookTravelFlag {
    private boolean travelingByHookFlag = false;

    @Override
    public boolean getTravelingByHook() {
        return travelingByHookFlag;
    }

    @Override
    public void setTravelingByHook(boolean travelingByHook) {
        travelingByHookFlag = travelingByHook;
    }

    @Shadow public abstract void addStat(ResourceLocation p_195067_1_, int p_195067_2_);

    @Inject(method = "addMovementStat(DDD)V", at = @At("HEAD"), cancellable = true)
    private void addMovementStatMixin(double dx, double dy, double dz, CallbackInfo ci) {
        if(travelingByHookFlag) {
            int cm = Math.round(MathHelper.sqrt(dx * dx + dy * dy + dz * dz) * 100.0F);
            if (cm > 0) {
                this.addStat(HookedModStats.INSTANCE.getHookOneCm(), cm);
            }
            ci.cancel();
        }
    }
}
