package thecodewarrior.hooked.common.network

import com.teamwizardry.librarianlib.features.kotlin.ifCap
import com.teamwizardry.librarianlib.features.network.PacketBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import thecodewarrior.hooked.common.capability.HooksCap

/**
 * Created by TheCodeWarrior
 */
class PacketHookedJump : PacketBase() {

    override fun handle(ctx: MessageContext) {
        handle(ctx.serverHandler.player)
    }

    fun handle(player: EntityPlayer) {
        player.ifCap(HooksCap.CAPABILITY, null) { cap ->
            val controller = cap.controller
            if(controller == null) {
                cap.update()
                return@ifCap
            }
            controller.playerJump()
        }
    }
}

