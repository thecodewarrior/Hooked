package thecodewarrior.hooked.common.network

import com.teamwizardry.librarianlib.features.kotlin.ifCap
import com.teamwizardry.librarianlib.features.network.PacketBase
import com.teamwizardry.librarianlib.features.saving.Save
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import thecodewarrior.hooked.common.capability.HooksCap
import kotlin.math.min

/**
 * Created by TheCodeWarrior
 */
class PacketHookedJump : PacketBase() {

    override fun handle(ctx: MessageContext) {
        doTheThing(ctx.serverHandler.player)
    }

    fun doTheThing(player: EntityPlayer) {
        player.ifCap(HooksCap.CAPABILITY, null) { cap ->
            cap.controller?.playerJump()
        }
    }
}

