package dev.thecodewarrior.hooked.network

import com.teamwizardry.librarianlib.core.util.kotlin.getOrNull
import com.teamwizardry.librarianlib.courier.CourierPacket
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.hook.ServerHookProcessor
import ll.dev.thecodewarrior.prism.annotation.Refract
import ll.dev.thecodewarrior.prism.annotation.RefractClass
import ll.dev.thecodewarrior.prism.annotation.RefractConstructor
import net.minecraftforge.fml.network.NetworkEvent

@RefractClass
data class HookJumpPacket @RefractConstructor constructor(
    @Refract val doubleJump: Boolean,
    @Refract val sneaking: Boolean
): CourierPacket {
    override fun handle(context: NetworkEvent.Context) {
        val player = context.sender!!
        context.enqueueWork {
            player.getCapability(HookedPlayerData.CAPABILITY).getOrNull()?.let { data ->
                ServerHookProcessor.jump(data, doubleJump, sneaking)
            }
        }
    }
}