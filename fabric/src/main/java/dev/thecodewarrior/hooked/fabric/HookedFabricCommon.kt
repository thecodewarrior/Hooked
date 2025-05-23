package dev.thecodewarrior.hooked.fabric

import dev.thecodewarrior.hooked.HookedCommon
import net.fabricmc.api.ModInitializer

object HookedFabricCommon: ModInitializer {
    override fun onInitialize() {
        HookedCommon.init()
    }
}