package thecodewarrior.hooked.common.network

import com.teamwizardry.librarianlib.features.kotlin.ifCap
import com.teamwizardry.librarianlib.features.network.PacketBase
import com.teamwizardry.librarianlib.features.saving.Save
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.EnumDyeColor
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import thecodewarrior.hooked.HookLog
import thecodewarrior.hooked.common.capability.EnumHookStatus
import thecodewarrior.hooked.common.capability.HookInfo
import thecodewarrior.hooked.common.capability.HooksCap

/**
 * Created by TheCodeWarrior
 */
class PacketFireHook : PacketBase() {

    @Save
    var pos: Vec3d = Vec3d.ZERO
    @Save
    var normal: Vec3d = Vec3d.ZERO

    override fun handle(ctx: MessageContext) {
        doTheThing(ctx.serverHandler.player)
        ctx.serverHandler.player.ifCap(HooksCap.CAPABILITY, null) { cap ->
            cap.update(ctx.serverHandler.player)
        }
    }

    fun doTheThing(player: EntityPlayer) {
        player.ifCap(HooksCap.CAPABILITY, null) { cap ->
            val spawnDistance = player.positionVector.distanceTo(pos)
            if(spawnDistance > 10) {
                HookLog.warn("Player ${player.name} spawned a hook too far from their body! Expected point within " +
                        "10 blocks of player. Got a point $spawnDistance blocks away.")
                cap.update(player)
            }
            val type = cap.hookType ?: return@ifCap
            if(cap.hooks.count { it.status.active } <= type.count + 1) {
                if(cap.hooks.count { it.status == EnumHookStatus.PLANTED } == 1)
                    cap.hooks.find { it.status == EnumHookStatus.PLANTED }?.weight = 1.0
                val hook = HookInfo(pos, normal.normalize(), EnumHookStatus.EXTENDING, null, null)
                cap.hooks.addLast(hook)
            }
        }
    }
}
