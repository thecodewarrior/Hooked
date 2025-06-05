package dev.thecodewarrior.hooked.client

import com.teamwizardry.librarianlib.core.util.Client
import dev.thecodewarrior.hooked.bridge.hookData
import dev.thecodewarrior.hooked.hook.ClientHookProcessor
import dev.thecodewarrior.hooked.platform.HookedPlatformClient
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

object Keybinds {
    val FIRE = HookedPlatformClient.instance.createInGameKeybind(
        "key.hooked.fire",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_C,
        "key.category.hooked"
    )
    var jumpWasDown = false
    var doubleJumpTimer = 0

    fun tick() {
        val jumpDown = Client.minecraft.options.jumpKey.isPressed
        val jumpPressed = jumpDown && !jumpWasDown
        jumpWasDown = jumpDown

        val player = Client.player ?: return
        val data = player.hookData()

        if(data.maxHooks > 0) {
            val sneakPressed = Client.minecraft.options.sneakKey.isPressed
            val fireBinding = HookedPlatformClient.instance.getActualFireKeybinding()
            if (fireBinding.wasPressed()) {
                ClientHookProcessor.fireHook(player, data, player.eyePos, player.pitch, player.yaw, sneakPressed)
                while(fireBinding.wasPressed()) { /* consume excess keypresses */ }
            }

            if (jumpPressed) {
                ClientHookProcessor.jump(data, doubleJumpTimer != 0, sneakPressed)

                if (doubleJumpTimer == 0) {
                    doubleJumpTimer = 7
                }
            }
        }

        if (doubleJumpTimer > 0) {
            doubleJumpTimer--
        }
    }
}