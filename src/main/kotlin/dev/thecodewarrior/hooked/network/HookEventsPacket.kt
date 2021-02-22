package dev.thecodewarrior.hooked.network

import com.teamwizardry.librarianlib.core.util.Client
import com.teamwizardry.librarianlib.core.util.kotlin.getOrNull
import com.teamwizardry.librarianlib.core.util.sided.SidedRunnable
import com.teamwizardry.librarianlib.courier.CourierPacket
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.hook.ClientHookProcessor
import dev.thecodewarrior.hooked.hook.HookEvent
import ll.dev.thecodewarrior.prism.annotation.Refract
import ll.dev.thecodewarrior.prism.annotation.RefractClass
import ll.dev.thecodewarrior.prism.annotation.RefractConstructor
import net.minecraftforge.fml.network.NetworkEvent
import java.util.ArrayList

/**
 * Triggers hook events on the client. If the client has recently triggered an identical event, the event is ignored.
 */
@RefractClass
data class HookEventsPacket @RefractConstructor constructor(
    @Refract val entityID: Int,
    @Refract val events: ArrayList<HookEvent>,
): CourierPacket {
    override fun handle(context: NetworkEvent.Context) {
        context.enqueueWork {
            SidedRunnable.client {
                val world = Client.player?.world ?: return@client
                val player = world.getEntityByID(entityID) ?: return@client
                player.getCapability(HookedPlayerData.CAPABILITY).getOrNull()?.let { data ->
                    events.forEach {
                        ClientHookProcessor.triggerServerEvent(data, it)
                    }
                }
            }
        }
    }
}
