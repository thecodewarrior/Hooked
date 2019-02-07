package games.thecodewarrior.hooked.common.network

import com.teamwizardry.librarianlib.features.kotlin.hasNullSignature
import com.teamwizardry.librarianlib.features.kotlin.writeNonnullSignature
import com.teamwizardry.librarianlib.features.kotlin.writeNullSignature
import com.teamwizardry.librarianlib.features.network.PacketBase
import com.teamwizardry.librarianlib.features.saving.AbstractSaveHandler
import com.teamwizardry.librarianlib.features.saving.Save
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import net.minecraftforge.fml.relauncher.FMLLaunchHandler
import net.minecraftforge.fml.relauncher.Side
import games.thecodewarrior.hooked.common.capability.HooksCap

/**
 * Created by TheCodeWarrior
 */
class PacketHookCapSync(player: Entity? = null) : PacketBase() {

    @Save
    var eid: Int = 0
    @Save
    var nbt = NBTTagCompound()

    init {
        player?.let {
            eid = it.entityId
            it.getCapability(HooksCap.CAPABILITY, null)?.writeToNBT(nbt)
        }
    }

    override fun handle(ctx: MessageContext) {
        if(ctx.side == Side.CLIENT) {
            val player = Minecraft.getMinecraft().world.getEntityByID(eid)
            player?.getCapability(HooksCap.CAPABILITY, null)?.readFromNBT(nbt)
        }
    }
}
