package dev.thecodewarrior.hooked.fabric.mixin;

import dev.thecodewarrior.hooked.bridge.PlayerMixinBridge;
import dev.thecodewarrior.hooked.hook.HookActiveReason;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements PlayerMixinBridge {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
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
}
