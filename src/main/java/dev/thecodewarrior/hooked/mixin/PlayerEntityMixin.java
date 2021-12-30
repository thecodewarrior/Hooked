package dev.thecodewarrior.hooked.mixin;

import com.mojang.authlib.GameProfile;
import dev.thecodewarrior.hooked.Hooked;
import dev.thecodewarrior.hooked.bridge.PlayerMixinBridge;
import dev.thecodewarrior.hooked.capability.HookedPlayerData;
import dev.thecodewarrior.hooked.hook.Hook;
import dev.thecodewarrior.hooked.hook.HookActiveReason;
import dev.thecodewarrior.hooked.hook.HookProcessor;
import dev.thecodewarrior.hooked.hook.NullHookProcessor;
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
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow public abstract void increaseStat(Identifier stat, int amount);

    @Inject(method = "increaseTravelMotionStats(DDD)V", at = @At("HEAD"), cancellable = true)
    private void increaseTravelMotionStatsHookedMixin(double dx, double dy, double dz, CallbackInfo ci) {
        if(this.getHookProcessor().isHookActive((PlayerEntity) (Object) this, HookActiveReason.TRAVEL_STATS)) {
            int cm = Math.round(MathHelper.sqrt((float) (dx * dx + dy * dy + dz * dz)) * 100.0F);
            if (cm > 0) {
                this.increaseStat(Hooked.HookStats.HOOK_ONE_CM, cm);
            }
            ci.cancel();
        }
    }

    @Inject(method = "checkFallFlying", at = @At("HEAD"), cancellable = true)
    private void checkFallFlyingHookedMixin(CallbackInfoReturnable<Boolean> cir) {
        if(this.getHookProcessor().isHookActive((PlayerEntity) (Object) this, HookActiveReason.CANCEL_ELYTRA)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "clipAtLedge", at = @At("HEAD"), cancellable = true)
    private void clipAtLedgeHookedMixin(CallbackInfoReturnable<Boolean> cir) {
        if(this.getHookProcessor().isHookActive((PlayerEntity) (Object) this, HookActiveReason.DISABLE_CLIP_AT_LEDGE)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getBlockBreakingSpeed", at = @At("RETURN"), cancellable = true)
    private void fixBreakSpeed(BlockState block, CallbackInfoReturnable<Float> cir) {
        if(this.getHookProcessor().isHookActive((PlayerEntity) (Object) this, HookActiveReason.BREAK_SPEED)) {
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

    @NotNull
    @Override
    public HookProcessor getHookProcessor() {
        return NullHookProcessor.INSTANCE;
    }

    @Inject(method = "tickMovement", at = @At("RETURN"))
    private void tickHooks(CallbackInfo ci) {
        getHookProcessor().tick((PlayerEntity) (Object) this);
    }
}
