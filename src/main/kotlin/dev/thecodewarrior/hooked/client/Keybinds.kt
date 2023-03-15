package dev.thecodewarrior.hooked.client

import com.teamwizardry.librarianlib.core.util.Client
import dev.thecodewarrior.hooked.bridge.hookData
import dev.thecodewarrior.hooked.hook.ClientHookProcessor
import dev.thecodewarrior.hooked.hook.HookType
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

object Keybinds {
    val FIRE = KeyBinding(
        "key.hooked.fire",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_R,
        "key.category.hooked"
    )
    var jumpWasDown = false
    var doubleJumpTimer = 0

    fun tick(client: MinecraftClient) {
        val jumpDown = Client.minecraft.options.jumpKey.isPressed
        val jumpPressed = jumpDown && !jumpWasDown
        jumpWasDown = jumpDown

        val player = Client.player ?: return
        val data = player.hookData()

        if(data.type != HookType.NONE) {
            val sneakPressed = Client.minecraft.options.sneakKey.isPressed
            if (FIRE.wasPressed()) {
                ClientHookProcessor.fireHook(player, data, player.eyePos, player.pitch, player.yaw, sneakPressed)
                while(FIRE.wasPressed()) { /* consume excess keypresses */ }
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