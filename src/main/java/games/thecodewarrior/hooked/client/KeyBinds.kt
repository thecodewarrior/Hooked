package games.thecodewarrior.hooked.client

import com.teamwizardry.librarianlib.features.helpers.vec
import com.teamwizardry.librarianlib.features.kotlin.*
import com.teamwizardry.librarianlib.features.network.PacketHandler
import net.minecraft.client.Minecraft
import net.minecraft.client.settings.KeyBinding
import net.minecraft.entity.Entity
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraftforge.client.settings.KeyConflictContext
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Keyboard
import games.thecodewarrior.hooked.common.capability.HooksCap
import games.thecodewarrior.hooked.common.hook.HookController
import games.thecodewarrior.hooked.common.network.PacketFireHook
import games.thecodewarrior.hooked.common.network.PacketRetractHook
import games.thecodewarrior.hooked.common.network.PacketHookedJump
import games.thecodewarrior.hooked.common.network.PacketMove
import games.thecodewarrior.hooked.common.util.Minecraft

/**
 * Created by TheCodeWarrior
 */
object KeyBinds {
    val keyFire = KeyBinding("key.hooked.fire", KeyConflictContext.IN_GAME, Keyboard.KEY_C, "key.category.movement")

    var jumpTimer = 0
    var jumpCount = 0
    var jumpDown = false
    var disableMovement = false

    init {
        MinecraftForge.EVENT_BUS.register(this)
        ClientRegistry.registerKeyBinding(keyFire)
    }

    @SubscribeEvent
    fun clientTick(e: TickEvent.ClientTickEvent) {
        if (e.phase == TickEvent.Phase.END)
            jumpTimer++

        if(disableMovement) return
        val player = Minecraft().player ?: return

        player.ifCap(HooksCap.CAPABILITY, null) { cap ->
            if (cap.controller == null) return@ifCap
            val speed = if(player.isSprinting) 0.25f else 0.2f

            val vertical = when {
                jumpDown && !player.isSneaking -> 1
                !jumpDown && player.isSneaking -> -1
                else -> 0
            } * speed

            var motion = moveRelative(player, player.moveStrafing, player.moveForward, speed)
            motion = vec(motion.x, vertical, motion.z)

            PacketHandler.NETWORK.sendToServer(PacketMove().apply {
                offset = motion
                handle(player)
            })
        }
    }

    @SubscribeEvent
    fun onKeyInput(e: InputEvent.KeyInputEvent) {
        if(disableMovement) return
        val player = Minecraft().player

        player.ifCap(HooksCap.CAPABILITY, null) { cap ->
            if(cap.controller == null) return@ifCap

            if (keyFire.isPressed) {
                if (player.isSneaking) {
                    val controller = cap.controller
                    if (controller != null)
                        retractLookingHook(controller)
                } else {
                    PacketHandler.NETWORK.sendToServer(PacketFireHook().apply {
                        pos = player.getPositionEyes(1f)
                        normal = player.lookVec
                        handle(player)
                    })
                }
            }

            val wasDown = jumpDown
            jumpDown = Minecraft.getMinecraft().gameSettings.keyBindJump.isKeyDown
            if (!wasDown && jumpDown) {
                if(jumpTimer > 7) jumpCount = 0
                jumpTimer = 0
                PacketHandler.NETWORK.sendToServer(PacketHookedJump().apply {
                    count = ++jumpCount
                    handle(player)
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
                handle(player)
            })
        }
    }

    fun moveRelative(entity: Entity, strafe: Float, forward: Float, friction: Float): Vec3d {
        var f = strafe * strafe + forward * forward

        if (f >= 1.0E-4f) {
            f = MathHelper.sqrt(f)

            if (f < 1.0f) {
                f = 1.0f
            }

            f = friction / f
            val f1 = MathHelper.sin(entity.rotationYaw * 0.017453292f) * f
            val f2 = MathHelper.cos(entity.rotationYaw * 0.017453292f) * f
            return vec(strafe * f2 - forward * f1, 0, forward * f2 + strafe * f1)
        }
        return Vec3d.ZERO
    }
}

