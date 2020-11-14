package dev.thecodewarrior.hooked.network

import com.teamwizardry.librarianlib.core.util.Client
import com.teamwizardry.librarianlib.core.util.kotlin.getOrNull
import com.teamwizardry.librarianlib.core.util.sided.ClientSupplier
import com.teamwizardry.librarianlib.courier.CourierPacket
import ll.dev.thecodewarrior.prism.annotation.Refract
import ll.dev.thecodewarrior.prism.annotation.RefractClass
import ll.dev.thecodewarrior.prism.annotation.RefractConstructor
import net.minecraft.util.math.Vec3d
import java.util.*
import com.teamwizardry.librarianlib.math.plus
import com.teamwizardry.librarianlib.math.vec
import dev.thecodewarrior.hooked.HookedMod
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.hook.processor.CommonHookProcessor

import net.minecraftforge.fml.network.NetworkEvent
import kotlin.math.pow
import kotlin.math.sqrt

@RefractClass
data class FireHookPacket @RefractConstructor constructor(
    @Refract val uuid: UUID,
    @Refract val pos: Vec3d,
    @Refract val direction: Vec3d,
): CourierPacket {
    override fun handle(context: NetworkEvent.Context) {
        val player = context.sender!!
        context.enqueueWork {
            player.getCapability(HookedPlayerData.CAPABILITY).getOrNull()?.let { data ->
                val distanceSq = pos.squareDistanceTo(player.getEyePosition(1f))
                val maxDistance = CheatMitigation.fireHookTolerance.getValue(player)
                if(distanceSq > maxDistance.pow(2)) {
                    logger.error(
                        "Player ${player.name} fired a hook from ${sqrt(distanceSq)} blocks away. The tolerance " +
                                "based on their ping of ${player.ping} is $maxDistance"
                    )
                } else {
                    // if the UUID already exists, pick a new one. In the future this will trigger a full sync
                    // i.e. in the future not everything will full sync :P
                    val newUUID = if(data.hooks.any { it.uuid == uuid }) UUID.randomUUID() else uuid
                    CommonHookProcessor.fireHook(data, newUUID, pos, direction.normalize())
                }
            }
        }
    }

    companion object {
        private val logger = HookedMod.makeLogger<FireHookPacket>()
    }
}