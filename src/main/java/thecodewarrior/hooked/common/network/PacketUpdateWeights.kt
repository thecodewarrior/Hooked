package thecodewarrior.hooked.common.network

import com.teamwizardry.librarianlib.common.network.PacketBase
import com.teamwizardry.librarianlib.common.util.ifCap
import com.teamwizardry.librarianlib.common.util.saving.Save
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import thecodewarrior.hooked.common.capability.HooksCap
import java.util.*

/**
 * Created by TheCodeWarrior
 */
class PacketUpdateWeights : PacketBase() {

    @Save
    var vertical: Double = 0.0

    @Save
    var weights: HashMap<UUID, Double>? = null

    override fun handle(ctx: MessageContext) {
        doTheThing(ctx.serverHandler.playerEntity)
        ctx.serverHandler.playerEntity.ifCap(HooksCap.CAPABILITY, null) {
            updatePos()
        }
    }

    fun doTheThing(player: EntityPlayer) {
        val w = weights ?: return

        player.ifCap(HooksCap.CAPABILITY, null) {
            verticalHangDistance = vertical
            hooks.forEach {
                if(it.uuid in w) {
                    it.weight = w[it.uuid] ?: it.weight
                }
            }
        }
    }
}
