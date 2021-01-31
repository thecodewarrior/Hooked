package dev.thecodewarrior.hooked.network

import com.teamwizardry.librarianlib.core.util.kotlin.getOrNull
import com.teamwizardry.librarianlib.courier.CourierPacket
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.hook.type.BasicHookPlayerController
import dev.thecodewarrior.hooked.hook.type.FlightHookPlayerController
import ll.dev.thecodewarrior.prism.annotation.RefractClass
import ll.dev.thecodewarrior.prism.annotation.RefractConstructor
import net.minecraftforge.fml.network.NetworkEvent

@RefractClass
class RetractFlightHooksPacket @RefractConstructor constructor(): CourierPacket {
    override fun handle(context: NetworkEvent.Context) {
        val player = context.sender!!
        context.enqueueWork {
            player.getCapability(HookedPlayerData.CAPABILITY).getOrNull()?.let { data ->
                (data.controller as? FlightHookPlayerController)?.shouldRetract = true
            }
        }
    }
}