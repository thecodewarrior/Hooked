package dev.thecodewarrior.hooked.mixin;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPlayNetworkHandler.class)
public interface FloatingTicksAccess {
    @Accessor
    int getFloatingTicks();
    @Accessor
    void setFloatingTicks(int floatingTicks);
}
