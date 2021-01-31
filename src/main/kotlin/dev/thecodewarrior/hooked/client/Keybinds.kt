package dev.thecodewarrior.hooked.client

import com.teamwizardry.librarianlib.core.util.Client
import com.teamwizardry.librarianlib.core.util.kotlin.getOrNull
import dev.thecodewarrior.hooked.HookedMod
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.hook.processor.ClientHookProcessor
import dev.thecodewarrior.hooked.hook.type.BasicHookPlayerController
import dev.thecodewarrior.hooked.hook.type.FlightHookPlayerController
import dev.thecodewarrior.hooked.hook.type.HookType
import dev.thecodewarrior.hooked.network.HookJumpPacket
import net.minecraft.client.settings.KeyBinding
import net.minecraft.client.util.InputMappings
import net.minecraftforge.client.event.InputEvent
import net.minecraftforge.client.settings.KeyConflictContext
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import org.lwjgl.glfw.GLFW

object Keybinds {
    val fireKey = KeyBinding(
        "key.hooked.fire",
        KeyConflictContext.IN_GAME,
        InputMappings.Type.KEYSYM,
        GLFW.GLFW_KEY_C,
        "key.category.hooked"
    )
    var jumpWasDown = false
    var doubleJumpTimer = 0

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun input(e: InputEvent.KeyInputEvent) {
        val jumpDown = Client.minecraft.gameSettings.keyBindJump.isKeyDown
        val jumpPressed = jumpDown && !jumpWasDown
        jumpWasDown = jumpDown

        val player = Client.player ?: return
        player.getCapability(HookedPlayerData.CAPABILITY).getOrNull()?.also { data ->
            if(data.type == HookType.NONE) {
                return@also
            }

            val sneakPressed = Client.minecraft.gameSettings.keyBindSneak.isKeyDown
            if (fireKey.isPressed) {
                val pos = player.getEyePosition(1f)
                val direction = player.getLook(1f)
                ClientHookProcessor.fireHook(data, pos, direction, sneakPressed)
            }

            if (jumpPressed) {
                val jumpState = HookedPlayerData.JumpState(
                    doubleJump = doubleJumpTimer != 0,
                    sneaking = sneakPressed
                )
                data.jumpState = jumpState
                HookedMod.courier.sendToServer(HookJumpPacket(jumpState))

                if (doubleJumpTimer == 0) {
                    doubleJumpTimer = 7
                }
            }
        }
    }

    @SubscribeEvent
    fun tick(e: TickEvent.ClientTickEvent) {
        if (e.phase != TickEvent.Phase.END) return
        if (doubleJumpTimer > 0) {
            doubleJumpTimer--
        }
    }
}