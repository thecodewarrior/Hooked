package dev.thecodewarrior.hooked.network

import com.teamwizardry.librarianlib.courier.CourierPacket
import ll.dev.thecodewarrior.prism.annotation.Refract
import ll.dev.thecodewarrior.prism.annotation.RefractClass
import ll.dev.thecodewarrior.prism.annotation.RefractConstructor

@RefractClass
data class HookJumpPacket @RefractConstructor constructor(
    @Refract("doubleJump") val doubleJump: Boolean,
    @Refract("sneaking") val sneaking: Boolean
): CourierPacket