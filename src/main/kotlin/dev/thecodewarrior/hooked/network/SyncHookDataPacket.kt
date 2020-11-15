package dev.thecodewarrior.hooked.network

import com.teamwizardry.librarianlib.core.util.Client
import com.teamwizardry.librarianlib.core.util.kotlin.getOrNull
import com.teamwizardry.librarianlib.core.util.sided.ClientRunnable
import com.teamwizardry.librarianlib.core.util.sided.SidedRunnable
import com.teamwizardry.librarianlib.courier.CourierPacket
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.hook.processor.ClientHookProcessor
import dev.thecodewarrior.hooked.hook.processor.Hook
import ll.dev.thecodewarrior.prism.annotation.Refract
import ll.dev.thecodewarrior.prism.annotation.RefractClass
import ll.dev.thecodewarrior.prism.annotation.RefractConstructor
import net.minecraft.client.entity.player.ClientPlayerEntity
import net.minecraft.nbt.CompoundNBT
import net.minecraftforge.fml.network.NetworkEvent

/**
 * Synchronizes the full hook data
 */
@RefractClass
data class SyncHookDataPacket @RefractConstructor constructor(
    @Refract val entityID: Int,
    @Refract val tag: CompoundNBT,
): CourierPacket {
    override fun handle(context: NetworkEvent.Context) {
        context.packetHandled = true
        context.enqueueWork {
            SidedRunnable.client {
                val world = Client.player?.world ?: return@client
                val player = world.getEntityByID(entityID) ?: return@client
                player.getCapability(HookedPlayerData.CAPABILITY).getOrNull()?.deserializeNBT(tag)
            }
        }
    }
}
