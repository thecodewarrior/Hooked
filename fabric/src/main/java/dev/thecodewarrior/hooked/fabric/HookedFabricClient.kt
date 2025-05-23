package dev.thecodewarrior.hooked.fabric

import dev.thecodewarrior.hooked.HookedClient
import net.fabricmc.api.ClientModInitializer

object HookedFabricClient: ClientModInitializer {
    override fun onInitializeClient() {
        HookedClient.init()
    }
}