package thecodewarrior.hooked.common.network

import com.teamwizardry.librarianlib.features.kotlin.ifCap
import com.teamwizardry.librarianlib.features.network.PacketBase
import com.teamwizardry.librarianlib.features.saving.Save
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import thecodewarrior.hooked.HookLog
import thecodewarrior.hooked.common.capability.HooksCap

/**
 * Created by TheCodeWarrior
 */
class PacketMove : PacketBase() {

    @Save
    var offset: Vec3d = Vec3d.ZERO

    override fun handle(ctx: MessageContext) {
        handle(ctx.serverHandler.player)
    }

    fun handle(player: EntityPlayer) {
        player.ifCap(HooksCap.CAPABILITY, null) { cap ->
            val distance = offset.length()
            if(distance > 3) {
                HookLog.warn("Player ${player.name} moved too fast! Expected less than 3 blocks. " +
                    "Got $offset, a distance of $distance blocks.")
                cap.update()
            }

            val controller = cap.controller
            if(controller == null) {
                cap.update()
                return@ifCap
            }
            controller.moveBy(offset)
        }
    }
}
