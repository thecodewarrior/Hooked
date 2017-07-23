package thecodewarrior.hooked.client

import com.teamwizardry.librarianlib.features.kotlin.*
import com.teamwizardry.librarianlib.features.network.PacketHandler
import net.minecraft.client.Minecraft
import net.minecraft.client.settings.KeyBinding
import net.minecraftforge.client.model.animation.Animation
import net.minecraftforge.client.settings.KeyConflictContext
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Keyboard
import thecodewarrior.hooked.common.HookType
import thecodewarrior.hooked.common.capability.HooksCap
import thecodewarrior.hooked.common.network.PacketFireHook
import thecodewarrior.hooked.common.network.PacketRetractHook
import thecodewarrior.hooked.common.network.PacketRetractHooks

/**
 * Created by TheCodeWarrior
 */
object KeyBinds {
    val keyFire = KeyBinding("key.hooked.fire", KeyConflictContext.IN_GAME, Keyboard.KEY_C, "key.category.movement")
    val minCos = Math.cos(Math.toRadians(10.0))

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
        val player = Minecraft.getMinecraft().player

        if (keyFire.isPressed) {
            if (player.isSneaking) {
                player.ifCap(HooksCap.CAPABILITY, null) { cap ->
                    val look = player.getLook(Animation.getPartialTickTime())
                    val eye = player.getPositionEyes(Animation.getPartialTickTime())
                    val found = cap.hooks.maxBy { Math.max(
                            (it.pos - eye).normalize() dot look ,
                            ((it.pos + it.direction * cap.hookType!!.hookLength) - eye).normalize() dot look
                    )}
                    // max because cos(theta) increases as theta approaches 0
                    // the dot of two normalized vectors is cos(theta) where theta is the angle between them
                    // and instead of a bunch of inverse cosines, I check the cosine itself.

                    if (found != null && Math.max(
                            (found.pos - eye).normalize() dot look ,
                            ((found.pos + found.direction * cap.hookType!!.hookLength) - eye).normalize() dot look
                    ) > minCos) {
                        PacketHandler.NETWORK.sendToServer(PacketRetractHook().apply {
                            uuid = found.uuid
                            doTheThing(player)
                        })
                    }
                }

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
        if(!wasDown && jumpDown) {
            player.ifCap(HooksCap.CAPABILITY, null) { cap ->
                if(cap.hookType != HookType.RED) {
                    PacketHandler.NETWORK.sendToServer(PacketRetractHooks().apply {
                        jumping = true
                        doTheThing(player)
                    })
                }
            }
            if (jumpTimer == 0) {
                jumpTimer = 7
            } else {
                player.ifCap(HooksCap.CAPABILITY, null) { cap ->
                    if (cap.hookType == HookType.RED) {
                        PacketHandler.NETWORK.sendToServer(PacketRetractHooks().apply {
                            jumping = true
                            doTheThing(player)
                        })
                    }
                }
            }
        }
    }
}

