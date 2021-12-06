package dev.thecodewarrior.hooked.network

import com.teamwizardry.librarianlib.courier.CourierPacket
import dev.thecodewarrior.hooked.hook.Hook
import ll.dev.thecodewarrior.prism.annotation.Refract
import ll.dev.thecodewarrior.prism.annotation.RefractClass
import ll.dev.thecodewarrior.prism.annotation.RefractConstructor
import net.minecraft.nbt.NbtCompound

/**
 * Synchronizes the full hook data
 */
@RefractClass
data class SyncHookDataPacket @RefractConstructor constructor(
    @Refract("entityId") val entityId: Int,
    @Refract("removed") val removed: ArrayList<Hook>,
    @Refract("tag") val tag: NbtCompound,
): CourierPacket
