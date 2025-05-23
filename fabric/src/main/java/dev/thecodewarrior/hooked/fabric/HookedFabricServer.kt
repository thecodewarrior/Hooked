package dev.thecodewarrior.hooked.fabric

import dev.thecodewarrior.hooked.HookedServer
import net.fabricmc.api.DedicatedServerModInitializer

object HookedFabricServer: DedicatedServerModInitializer {
    override fun onInitializeServer() {
        HookedServer.init()
    }
}