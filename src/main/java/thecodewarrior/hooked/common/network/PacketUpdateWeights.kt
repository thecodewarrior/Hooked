package thecodewarrior.hooked.common.network

import com.teamwizardry.librarianlib.features.kotlin.ifCap
import com.teamwizardry.librarianlib.features.network.PacketBase
import com.teamwizardry.librarianlib.features.saving.Save
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
        doTheThing(ctx.serverHandler.player)
        ctx.serverHandler.player.ifCap(HooksCap.CAPABILITY, null) { cap ->
            cap.updatePos()
        }
    }

    fun doTheThing(player: EntityPlayer) {
        val w = weights ?: return

        player.ifCap(HooksCap.CAPABILITY, null) { cap ->
            cap.verticalHangDistance = vertical
            cap.hooks.forEach {
                if(it.uuid in w) {
                    it.weight = w[it.uuid] ?: it.weight
                }
            }
        }
    }
}
