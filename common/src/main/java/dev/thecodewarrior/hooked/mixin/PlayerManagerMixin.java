package dev.thecodewarrior.hooked.mixin;

import dev.architectury.networking.NetworkManager;
import dev.thecodewarrior.hooked.hook.ServerHookProcessor;
import dev.thecodewarrior.hooked.network.GameRuleSyncS2CPacket;
import net.minecraft.entity.Entity;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Based on Cardinal Components' sync code
 */
@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(
            method = "onPlayerConnect",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;sendStatusEffects(Lnet/minecraft/server/network/ServerPlayerEntity;)V"
            )
    )
    private void onPlayerLogIn(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
        NetworkManager.sendToPlayer(player, GameRuleSyncS2CPacket.from(player.getWorld().getGameRules()));
        ServerHookProcessor.INSTANCE.doInitialSync(player, player);
    }

    @Inject(
            method = "respawnPlayer",
            at = @At("RETURN")
    )
    private void respawnPlayer(ServerPlayerEntity player, boolean alive, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayerEntity> cir) {
        ServerHookProcessor.INSTANCE.doInitialSync(cir.getReturnValue(), cir.getReturnValue());
    }
}
