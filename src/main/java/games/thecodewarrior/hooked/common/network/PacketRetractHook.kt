package games.thecodewarrior.hooked.common.network

import com.teamwizardry.librarianlib.features.kotlin.ifCap
import com.teamwizardry.librarianlib.features.network.PacketBase
import com.teamwizardry.librarianlib.features.saving.Save
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import games.thecodewarrior.hooked.common.capability.HooksCap
import java.util.*

/**
 * Created by TheCodeWarrior
 */
class PacketRetractHook : PacketBase() {

    @Save
    var uuid: UUID = UUID.randomUUID()

    override fun handle(ctx: MessageContext) {
        handle(ctx.serverHandler.player)
    }

    fun handle(player: EntityPlayer) {
        player.ifCap(HooksCap.CAPABILITY, null) { cap ->
            cap.controller?.releaseSpecificHook(uuid)
        }
    }
}
