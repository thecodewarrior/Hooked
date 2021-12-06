package dev.thecodewarrior.hooked.network

import com.teamwizardry.librarianlib.courier.CourierPacket
import dev.thecodewarrior.hooked.hook.Hook
import ll.dev.thecodewarrior.prism.annotation.Refract
import ll.dev.thecodewarrior.prism.annotation.RefractClass
import ll.dev.thecodewarrior.prism.annotation.RefractConstructor

/**
 * Synchronizes a single hook
 */
@RefractClass
data class SyncIndividualHooksPacket @RefractConstructor constructor(
    @Refract("entityId") val entityId: Int,
    @Refract("hooks") val hooks: ArrayList<Hook>,
): CourierPacket
