package dev.thecodewarrior.hooked.mixin;

import com.teamwizardry.librarianlib.math.Matrix4d;
import dev.thecodewarrior.hooked.bridge.HudSprites;
import dev.thecodewarrior.hooked.hook.ClientHookProcessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(InGameHud.class)
public abstract class MixinInGameHud {
    @Shadow private int scaledWidth;

    @Shadow private int scaledHeight;

    @Shadow protected abstract PlayerEntity getCameraPlayer();

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getAttackCooldownProgress(F)F"))
    private void hooked$injectCrosshair(MatrixStack matrices, CallbackInfo ci) {
        var matrix = new Matrix4d(matrices);
        if(ClientHookProcessor.getHudCooldown() == 0)
            return;

        var spriteWidth = 13;
        var spriteHeight = 9;
        var x = scaledWidth / 2 + 7;
        var y = (scaledHeight - spriteHeight) / 2;
        HudSprites.getCrosshairBackground().draw(matrix, x, y);
        HudSprites.getCrosshairFill().draw(
                matrix, x, y,
                (int) (ClientHookProcessor.getHudCooldown() * HudSprites.getCrosshairFill().getFrameCount()),
                Color.WHITE
        );
    }

    @Inject(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getAttackCooldownProgress(F)F"))
    private void hooked$injectHotbar(float tickDelta, MatrixStack matrices, CallbackInfo ci) {
        var matrix = new Matrix4d(matrices);
        if(ClientHookProcessor.getHudCooldown() == 0)
            return;
        var attackCooldownVisible = this.client.player.getAttackCooldownProgress(0.0f) < 1f;

        var arm = getCameraPlayer().getMainArm();

        var spriteWidth = 11;
        var spriteHeight = 16;

        var y = scaledHeight - 20;
        var x = switch (arm) {
            case LEFT -> scaledWidth / 2 - (91 + 6 + (attackCooldownVisible ? 20 : 0) + spriteWidth);
            case RIGHT -> scaledWidth / 2 + (91 + 6 + (attackCooldownVisible ? 20 : 0));
        };
        HudSprites.getHotbarFrame().draw(matrix, x, y);
        HudSprites.getHotbarFill().draw(
                matrix, x + 1, y + 1,
                (int) (ClientHookProcessor.getHudCooldown() * HudSprites.getHotbarFill().getFrameCount()),
                Color.WHITE
        );
    }
}
