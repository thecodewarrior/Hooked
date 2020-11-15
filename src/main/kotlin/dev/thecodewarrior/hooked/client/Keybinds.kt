package dev.thecodewarrior.hooked.client

import com.teamwizardry.librarianlib.core.util.Client
import com.teamwizardry.librarianlib.core.util.kotlin.getOrNull
import dev.thecodewarrior.hooked.HookedMod
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.hook.processor.ClientHookProcessor
import dev.thecodewarrior.hooked.hook.type.BasicHookPlayerController
import dev.thecodewarrior.hooked.hook.type.HookType
import dev.thecodewarrior.hooked.network.BasicHookJumpPacket
import net.minecraft.client.settings.KeyBinding
import net.minecraft.client.util.InputMappings
import net.minecraftforge.client.event.InputEvent
import net.minecraftforge.client.settings.KeyConflictContext
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.SubscribeEvent
import org.lwjgl.glfw.GLFW
import java.util.*

object Keybinds {
    val fireKey = KeyBinding("key.hooked.fire", KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_C, "key.category.hooked")
    var jumpWasDown = false

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun input(e: InputEvent.KeyInputEvent) {
        val player = Client.player ?: return
        player.getCapability(HookedPlayerData.CAPABILITY).getOrNull()?.also { data ->
            if(data.type != HookType.NONE) {
                if (fireKey.isPressed) {
                    val pos = player.getEyePosition(1f)
                    val direction = player.getLook(1f)
                    ClientHookProcessor.fireHook(data, pos, direction)
                }
            }
            val controller = data.controller
            val jumpDown = Client.minecraft.gameSettings.keyBindJump.isKeyDown
            if(controller is BasicHookPlayerController && jumpDown && !jumpWasDown) {
                controller.didJump = true
                HookedMod.courier.sendToServer(BasicHookJumpPacket())
            }
            jumpWasDown = jumpDown
        }
    }
}