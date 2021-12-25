package dev.thecodewarrior.hooked.mixin;

import com.mojang.authlib.GameProfile;
import dev.thecodewarrior.hooked.Hooked;
import dev.thecodewarrior.hooked.bridge.PlayerMixinBridge;
import dev.thecodewarrior.hooked.capability.HookedPlayerData;
import dev.thecodewarrior.hooked.hook.Hook;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements PlayerMixinBridge {
    private boolean hookedTravelingByHookFlag = false;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
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

    @Inject(method = "increaseTravelMotionStats(DDD)V", at = @At("HEAD"), cancellable = true)
    private void increaseTravelMotionStatsHookedMixin(double dx, double dy, double dz, CallbackInfo ci) {
        if(hookedTravelingByHookFlag) {
            int cm = Math.round(MathHelper.sqrt((float) (dx * dx + dy * dy + dz * dz)) * 100.0F);
            if (cm > 0) {
                this.increaseStat(Hooked.HookStats.HOOK_ONE_CM, cm);
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

    @Inject(method = "getBlockBreakingSpeed", at = @At("RETURN"), cancellable = true)
    private void fixBreakSpeed(BlockState block, CallbackInfoReturnable<Float> cir) {
        var hookData = Hooked.Components.HOOK_DATA.get(this);
        if(hookData.getHooks().values().stream().anyMatch(hook -> hook.getState() == Hook.State.PLANTED)) {
            var f = cir.getReturnValueF();

            if (this.isSubmergedIn(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(this)) {
                f *= 5.0F;
            }

            if (!this.onGround) {
                f *= 5.0F;
            }

            cir.setReturnValue(f);
        }
    }
}
