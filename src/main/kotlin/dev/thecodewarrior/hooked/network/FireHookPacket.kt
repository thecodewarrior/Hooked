package dev.thecodewarrior.hooked.network

import com.teamwizardry.librarianlib.courier.CourierPacket
import ll.dev.thecodewarrior.prism.annotation.Refract
import ll.dev.thecodewarrior.prism.annotation.RefractClass
import ll.dev.thecodewarrior.prism.annotation.RefractConstructor
import net.minecraft.util.math.Vec3d
import java.util.*

@RefractClass
data class FireHookPacket @RefractConstructor constructor(
    @Refract("pos") val pos: Vec3d,
    @Refract("direction") val direction: Vec3d,
    @Refract("sneaking") val sneaking: Boolean,
    @Refract("uuids") val uuids: ArrayList<UUID>
): CourierPacket