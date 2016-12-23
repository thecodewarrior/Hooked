package thecodewarrior.hooks.client

import com.teamwizardry.librarianlib.common.network.PacketHandler
import net.minecraft.client.Minecraft
import net.minecraft.client.settings.KeyBinding
import net.minecraftforge.client.settings.KeyConflictContext
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import org.lwjgl.input.Keyboard
import thecodewarrior.hooks.common.network.PacketFireHook
import thecodewarrior.hooks.common.network.PacketRetractHooks

/**
 * Created by TheCodeWarrior
 */
object KeyBinds {
    val keyFire = KeyBinding("key.hooks:fire", KeyConflictContext.IN_GAME, Keyboard.KEY_C, "key.category.hooks")

    init {
        MinecraftForge.EVENT_BUS.register(this)
        ClientRegistry.registerKeyBinding(keyFire)
    }

    @SubscribeEvent
    fun onKeyInput(e: InputEvent.KeyInputEvent) {
        if (keyFire.isPressed) {
            PacketHandler.NETWORK.sendToServer(PacketFireHook().let {
                it.pos = Minecraft.getMinecraft().player.getPositionEyes(1f)
                it.normal = Minecraft.getMinecraft().player.lookVec
                it.doTheThing(Minecraft.getMinecraft().player)

                it
            })
        }
        if(Minecraft.getMinecraft().gameSettings.keyBindJump.isPressed) {
            PacketHandler.NETWORK.sendToServer(PacketRetractHooks().let {
                it.jumping = true
                it.doTheThing(Minecraft.getMinecraft().player)

                it
            })
        }
    }
}

