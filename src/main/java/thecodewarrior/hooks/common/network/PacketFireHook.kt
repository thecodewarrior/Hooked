package thecodewarrior.hooks.common.network

import com.teamwizardry.librarianlib.common.network.PacketBase
import com.teamwizardry.librarianlib.common.util.ifCap
import com.teamwizardry.librarianlib.common.util.saving.Save
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import thecodewarrior.hooks.common.capability.EnumHookStatus
import thecodewarrior.hooks.common.capability.HookInfo
import thecodewarrior.hooks.common.capability.HooksCap

/**
 * Created by TheCodeWarrior
 */
class PacketFireHook : PacketBase() {

    @Save
    var pos: Vec3d = Vec3d.ZERO
    @Save
    var normal: Vec3d = Vec3d.ZERO

    override fun handle(ctx: MessageContext) {
        doTheThing(ctx.serverHandler.playerEntity)

//        HookLog.info("%s @ %s", normal.toString(), pos.toString())
    }

    fun doTheThing(player: EntityPlayer) {
        player.ifCap(HooksCap.CAPABILITY, null) {
            val type = hookType ?: return@ifCap
            if(hooks.count { it.status.active } <= type.count + 1) {
                hooks.addLast(HookInfo(pos, normal, EnumHookStatus.EXTENDING, null, null))
            }
        }
    }
}
