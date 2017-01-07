package thecodewarrior.hooked.common.network

import com.teamwizardry.librarianlib.common.network.PacketBase
import com.teamwizardry.librarianlib.common.util.ifCap
import com.teamwizardry.librarianlib.common.util.saving.Save
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import thecodewarrior.hooked.common.block.ModBlocks
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
        val player = ctx.serverHandler.playerEntity
        player.ifCap(HooksCap.CAPABILITY, null) {
            update(ctx.serverHandler.playerEntity)
            hooks.forEach {
                if (it.uuid == uuid && it.block != null && player.world.getBlockState(it.block).block == ModBlocks.balloon) {
                    val state = player.world.getBlockState(it.block)
                    val drops = state.block.getDrops(player.world, it.block, state, 0)
                    player.world.setBlockToAir(it.block)

                    if(!player.capabilities.isCreativeMode) {
                        drops?.forEach { drop ->
                            player.world.spawnEntity(EntityItem(player.world, player.posX, player.posY, player.posZ, drop))
                        }
                    }
                }
            }
        }
    }

    fun doTheThing(player: EntityPlayer) {
        player.ifCap(HooksCap.CAPABILITY, null) {
            hooks.forEach {
                if (it.uuid == uuid) {
                    it.status = EnumHookStatus.TORETRACT
                }
            }
            updatePos()
        }
    }
}
