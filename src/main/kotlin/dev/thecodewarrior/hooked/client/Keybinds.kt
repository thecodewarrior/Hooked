package dev.thecodewarrior.hooked.client

import com.teamwizardry.librarianlib.core.util.Client
import com.teamwizardry.librarianlib.core.util.kotlin.getOrNull
import dev.thecodewarrior.hooked.HookedMod
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.hook.processor.CommonHookProcessor
import dev.thecodewarrior.hooked.hook.type.HookType
import dev.thecodewarrior.hooked.network.FireHookPacket
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

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun input(e: InputEvent.KeyInputEvent) {
        val player = Client.player ?: return
        player.getCapability(HookedPlayerData.CAPABILITY).getOrNull()?.also { data ->
            if(data.type != HookType.NONE) {
                if (fireKey.isPressed) {
                    val uuid = UUID.randomUUID()
                    val pos = player.getEyePosition(1f)
                    val direction = player.getLook(1f)
                    CommonHookProcessor.fireHook(data, uuid, pos, direction)
//                    HookedMod.courier.sendToServer(FireHookPacket(
//                        uuid,
//                        pos,
//                        direction
//                    ))
                }
            }
        }
    }
}