package thecodewarrior.hooked.client

import com.teamwizardry.librarianlib.features.kotlin.*
import com.teamwizardry.librarianlib.features.network.PacketHandler
import net.minecraft.client.Minecraft
import net.minecraft.client.settings.KeyBinding
import net.minecraftforge.client.settings.KeyConflictContext
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Keyboard
import thecodewarrior.hooked.common.capability.HooksCap
import thecodewarrior.hooked.common.hook.HookController
import thecodewarrior.hooked.common.network.PacketFireHook
import thecodewarrior.hooked.common.network.PacketRetractHook
import thecodewarrior.hooked.common.network.PacketHookedJump
import thecodewarrior.hooked.common.util.Minecraft

/**
 * Created by TheCodeWarrior
 */
object KeyBinds {
    val keyFire = KeyBinding("key.hooked.fire", KeyConflictContext.IN_GAME, Keyboard.KEY_C, "key.category.movement")

    var jumpTimer = 0
    var jumpDown = false

    init {
        MinecraftForge.EVENT_BUS.register(this)
        ClientRegistry.registerKeyBinding(keyFire)
    }

    @SubscribeEvent
    fun clientTick(e: TickEvent.ClientTickEvent) {
        if (jumpTimer > 0 && e.phase == TickEvent.Phase.END)
            jumpTimer--
    }

    @SubscribeEvent
    fun onKeyInput(e: InputEvent.KeyInputEvent) {
        val player = Minecraft().player

        player.ifCap(HooksCap.CAPABILITY, null) { cap ->
            if (keyFire.isPressed) {
                if (player.isSneaking) {
                    val controller = cap.controller
                    if (controller != null)
                        retractLookingHook(controller)
                } else {
                    PacketHandler.NETWORK.sendToServer(PacketFireHook().apply {
                        pos = player.getPositionEyes(1f)
                        normal = player.lookVec
                        doTheThing(player)
                    })
                }
            }

            val wasDown = jumpDown
            jumpDown = Minecraft.getMinecraft().gameSettings.keyBindJump.isKeyDown
            if (!wasDown && jumpDown) {
                PacketHandler.NETWORK.sendToServer(PacketHookedJump().apply {
                    doTheThing(player)
                })
            }
        }
    }

    fun retractLookingHook(controller: HookController) {
        val player = Minecraft().player
        val found = controller.getSpecificHookToRetract()

        if (found != null) {
            PacketHandler.NETWORK.sendToServer(PacketRetractHook().apply {
                uuid = found.uuid
                doTheThing(player)
            })
        }
    }
}

