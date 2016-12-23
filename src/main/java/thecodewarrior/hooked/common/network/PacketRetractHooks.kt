package thecodewarrior.hooked.common.network

import com.teamwizardry.librarianlib.common.network.PacketBase
import com.teamwizardry.librarianlib.common.util.ifCap
import com.teamwizardry.librarianlib.common.util.saving.Save
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
        doTheThing(ctx.serverHandler.playerEntity)
    }

    fun doTheThing(player: EntityPlayer) {
        player.ifCap(HooksCap.CAPABILITY, null) {
            if(jumping && hooks.count { it.status == EnumHookStatus.PLANTED } > 0) {
                player.motionX *= 1.25
                player.motionY *= 1.25
                player.motionZ *= 1.25
                player.jump()
            }
            hooks.forEach { it.status = EnumHookStatus.TORETRACT }
            updatePos()
        }
    }
}

