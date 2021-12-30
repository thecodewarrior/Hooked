package dev.thecodewarrior.hooked.mixin;

import dev.thecodewarrior.hooked.bridge.PlayerMixinBridge;
import dev.thecodewarrior.hooked.hook.HookProcessor;
import dev.thecodewarrior.hooked.hook.ServerHookProcessor;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
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
}
