package dev.thecodewarrior.hooked.mixin;

import dev.thecodewarrior.hooked.HookStats;
import dev.thecodewarrior.hooked.bridge.PlayerMixinBridge;
import dev.thecodewarrior.hooked.hook.HookActiveReason;
import dev.thecodewarrior.hooked.hook.HookProcessor;
import dev.thecodewarrior.hooked.hook.ServerHookProcessor;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stat;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements PlayerMixinBridge {
    @NotNull
    @Override
    public HookProcessor getHookProcessor() {
        return ServerHookProcessor.INSTANCE;
    }

    @Shadow public abstract void increaseStat(Stat<?> stat, int amount);

    @Inject(method = "increaseTravelMotionStats(DDD)V", at = @At("HEAD"), cancellable = true)
    private void hooked$increaseTravelMotionStatsMixin(double dx, double dy, double dz, CallbackInfo ci) {
        if(this.isHookActive(HookActiveReason.TRAVEL_STATS)) {
            int cm = Math.round(MathHelper.sqrt((float) (dx * dx + dy * dy + dz * dz)) * 100.0F);
            if (cm > 0) {
                this.increaseStat(HookStats.INSTANCE.getHookOneCmStat(), cm);
            }
            ci.cancel();
        }
    }

    /**
     * neoforge has no equivalent to {@code ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD}, so we have to mix
     * in ourselves
     */
    @Inject(method = "worldChanged(Lnet/minecraft/server/world/ServerWorld;)V", at = @At("TAIL"))
    private void afterWorldChanged(ServerWorld origin, CallbackInfo ci) {
        ServerHookProcessor.INSTANCE.onPlayerWorldChanged((ServerPlayerEntity) (Object) this);
    }
}
