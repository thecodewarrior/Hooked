package dev.thecodewarrior.hooked.mixin;

import dev.thecodewarrior.hooked.bridge.PlayerMixinBridge;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow public abstract ServerPlayerEntity getPlayer();

    @ModifyConstant(method = "onPlayerMove", constant = @Constant(doubleValue = 0.0625))
    double injectMovedWrongly(double constant) {
        if(((PlayerMixinBridge) this.getPlayer()).getHookProcessor().isHookActive(this.getPlayer())) {
            return 5.0;
        } else {
            return constant;
        }
    }
}
