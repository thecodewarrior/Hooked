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
import dev.thecodewarrior.hooked.hook.processor.ServerHookProcessor

import net.minecraftforge.fml.network.NetworkEvent
import kotlin.math.pow
import kotlin.math.sqrt

@RefractClass
data class FireHookPacket @RefractConstructor constructor(
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
                    data.serverState.forceFullSyncToClient = true
                    logger.error(
                        "Player ${player.name} fired a hook from ${sqrt(distanceSq)} blocks away. The tolerance " +
                                "based on their ping of ${player.ping} is $maxDistance"
                    )
                } else {
                    ServerHookProcessor.fireHook(data, pos, direction.normalize())
                }
            }
        }
    }

    companion object {
        private val logger = HookedMod.makeLogger<FireHookPacket>()
    }
}