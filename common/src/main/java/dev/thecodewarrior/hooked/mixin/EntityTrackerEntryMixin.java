package dev.thecodewarrior.hooked.mixin;

import dev.thecodewarrior.hooked.hook.ServerHookProcessor;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Based on Cardinal Components' sync code
 */
@Mixin(EntityTrackerEntry.class)
public abstract class EntityTrackerEntryMixin {
    @Shadow
    @Final
    private Entity entity;

    @Inject(method = "startTracking", at = @At("RETURN"))
    private void onStartedTracking(ServerPlayerEntity player, CallbackInfo ci) {
        if (this.entity instanceof ServerPlayerEntity) {
            ServerHookProcessor.INSTANCE.doInitialSync(player, (ServerPlayerEntity) this.entity);
        }
    }
}
