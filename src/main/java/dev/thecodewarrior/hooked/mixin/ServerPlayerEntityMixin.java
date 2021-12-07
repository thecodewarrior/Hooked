package dev.thecodewarrior.hooked.mixin;

import dev.thecodewarrior.hooked.hook.ServerHookProcessor;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Inject(method = "tick", at = @At("RETURN"))
    private void tickHooks(CallbackInfo ci) {
        ServerHookProcessor.INSTANCE.tick((ServerPlayerEntity) (Object) this);
    }
}
