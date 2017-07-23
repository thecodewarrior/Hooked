package thecodewarrior.hooked.common.network

import com.teamwizardry.librarianlib.features.kotlin.ifCap
import com.teamwizardry.librarianlib.features.network.PacketBase
import com.teamwizardry.librarianlib.features.saving.Save
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import thecodewarrior.hooked.common.capability.EnumHookStatus
import thecodewarrior.hooked.common.capability.HooksCap

/**
 * Created by TheCodeWarrior
 */
class PacketRetractHooks : PacketBase() {

    @Save
    var jumping: Boolean = false

    override fun handle(ctx: MessageContext) {
        doTheThing(ctx.serverHandler.player)
    }

    fun doTheThing(player: EntityPlayer) {
        player.ifCap(HooksCap.CAPABILITY, null) { cap ->
            if(jumping && cap.hooks.count { it.status == EnumHookStatus.PLANTED } > 0) {
                player.motionX *= 1.25
                player.motionY *= 1.25
                player.motionZ *= 1.25
                player.jump()
            }
            cap.hooks.forEach { it.status = EnumHookStatus.TORETRACT }
            cap.updatePos()
        }
    }
}

