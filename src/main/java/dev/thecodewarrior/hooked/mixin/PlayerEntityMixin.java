package dev.thecodewarrior.hooked.mixin;

import dev.thecodewarrior.hooked.Hooked;
import dev.thecodewarrior.hooked.bridge.PlayerMixinBridge;
import dev.thecodewarrior.hooked.hook.HookActiveReason;
import dev.thecodewarrior.hooked.hook.HookProcessor;
import dev.thecodewarrior.hooked.hook.NullHookProcessor;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements PlayerMixinBridge {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "checkFallFlying", at = @At("HEAD"), cancellable = true)
    private void hooked$checkFallFlyingMixin(CallbackInfoReturnable<Boolean> cir) {
        if(this.isHookActive(HookActiveReason.CANCEL_ELYTRA)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void hooked$isInvulnerableToMixin(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if(this.isHookActive(HookActiveReason.ELYTRA_DAMAGE)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "clipAtLedge", at = @At("HEAD"), cancellable = true)
    private void hooked$clipAtLedgeHookedMixin(CallbackInfoReturnable<Boolean> cir) {
        if(this.isHookActive(HookActiveReason.DISABLE_CLIP_AT_LEDGE)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getBlockBreakingSpeed", at = @At("RETURN"), cancellable = true)
    private void hooked$fixBreakSpeed(BlockState block, CallbackInfoReturnable<Float> cir) {
        if(this.isHookActive(HookActiveReason.BREAK_SPEED)) {
            var f = cir.getReturnValueF();

            if (this.isSubmergedIn(FluidTags.WATER)) {
                var submergedSpeed = this.getAttributeInstance(EntityAttributes.PLAYER_SUBMERGED_MINING_SPEED).getValue();
                if (submergedSpeed > 0.0) {
                    f /= (float) submergedSpeed;
                }
            }

            if (!this.isOnGround()) {
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
    private void hooked$tickHooks(CallbackInfo ci) {
        getHookProcessor().tick((PlayerEntity) (Object) this);
    }
}
