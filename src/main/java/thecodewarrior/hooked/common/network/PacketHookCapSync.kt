package thecodewarrior.hooked.common.network

import com.teamwizardry.librarianlib.features.kotlin.hasNullSignature
import com.teamwizardry.librarianlib.features.kotlin.writeNonnullSignature
import com.teamwizardry.librarianlib.features.kotlin.writeNullSignature
import com.teamwizardry.librarianlib.features.network.PacketBase
import com.teamwizardry.librarianlib.features.saving.AbstractSaveHandler
import com.teamwizardry.librarianlib.features.saving.Save
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import net.minecraftforge.fml.relauncher.FMLLaunchHandler
import thecodewarrior.hooked.common.capability.HooksCap

/**
 * Created by TheCodeWarrior
 */
class PacketHookCapSync(player: Entity? = null) : PacketBase() {

    @Save var eid: Int = 0
    var cap: HooksCap? = null
    init {
        player?.let {
            eid = it.entityId
            cap = it.getCapability(HooksCap.CAPABILITY, null)
        }
    }

    // Buf is always null serverside
    private var buf: ByteBuf? = null

    override fun handle(ctx: MessageContext) {
        if (FMLLaunchHandler.side().isServer) return

        val b = buf

        val player = Minecraft.getMinecraft().world.getEntityByID(eid)
        cap = player?.getCapability(HooksCap.CAPABILITY, null)
        if(b == null || player == null || cap == null) return

        AbstractSaveHandler.readAutoBytes(cap!!, b, true)

        cap = null
    }

    override fun readCustomBytes(buf: ByteBuf) {
        if (buf.hasNullSignature()) return
        this.buf = buf.copy()
    }

    override fun writeCustomBytes(buf: ByteBuf) {
        cap?.let {
            buf.writeNonnullSignature()

            AbstractSaveHandler.writeAutoBytes(it, buf, true)
        }
        if(cap == null)
            buf.writeNullSignature()
    }
}
