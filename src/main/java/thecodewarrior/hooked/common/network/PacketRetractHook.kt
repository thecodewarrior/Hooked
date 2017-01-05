package thecodewarrior.hooked.common.network

import com.teamwizardry.librarianlib.common.network.PacketBase
import com.teamwizardry.librarianlib.common.util.ifCap
import com.teamwizardry.librarianlib.common.util.saving.Save
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import thecodewarrior.hooked.common.capability.EnumHookStatus
import thecodewarrior.hooked.common.capability.HooksCap
import java.util.*

/**
 * Created by TheCodeWarrior
 */
class PacketRetractHook : PacketBase() {

    @Save
    var uuid: UUID = UUID.randomUUID()

    override fun handle(ctx: MessageContext) {
        doTheThing(ctx.serverHandler.playerEntity)
        ctx.serverHandler.playerEntity.ifCap(HooksCap.CAPABILITY, null) {
            update(ctx.serverHandler.playerEntity)
        }
    }

    fun doTheThing(player: EntityPlayer) {
        player.ifCap(HooksCap.CAPABILITY, null) {
            hooks.forEach { if(it.uuid == uuid) it.status = EnumHookStatus.TORETRACT }
            updatePos()
        }
    }
}
