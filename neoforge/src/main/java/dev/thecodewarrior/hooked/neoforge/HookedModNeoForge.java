package dev.thecodewarrior.hooked.neoforge;

import dev.thecodewarrior.hooked.HookedCommon;
import net.neoforged.fml.common.Mod;

import dev.thecodewarrior.hooked.Hooked;

@Mod(Hooked.MOD_ID)
public final class HookedModNeoForge {
    public HookedModNeoForge() {
        // Run our common setup.
        HookedCommon.INSTANCE.init();
    }
}
