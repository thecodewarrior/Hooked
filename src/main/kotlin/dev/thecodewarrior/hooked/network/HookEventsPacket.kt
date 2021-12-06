package dev.thecodewarrior.hooked.network

import com.teamwizardry.librarianlib.courier.CourierPacket
import dev.thecodewarrior.hooked.hook.HookEvent
import ll.dev.thecodewarrior.prism.annotation.Refract
import ll.dev.thecodewarrior.prism.annotation.RefractClass
import ll.dev.thecodewarrior.prism.annotation.RefractConstructor
import java.util.ArrayList

/**
 * Triggers hook events on the client. If the client has recently triggered an identical event, the event is ignored.
 */
@RefractClass
data class HookEventsPacket @RefractConstructor constructor(
    @Refract("entityId") val entityId: Int,
    @Refract("events") val events: ArrayList<HookEvent>,
): CourierPacket {
}
