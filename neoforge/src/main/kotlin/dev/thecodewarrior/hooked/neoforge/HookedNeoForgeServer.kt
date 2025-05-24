package dev.thecodewarrior.hooked.neoforge

import dev.thecodewarrior.hooked.Hooked
import dev.thecodewarrior.hooked.HookedServer
import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.common.Mod

@Mod(Hooked.MOD_ID, dist = [Dist.DEDICATED_SERVER])
class HookedNeoForgeServer {
    init {
        HookedServer.init()
    }
}