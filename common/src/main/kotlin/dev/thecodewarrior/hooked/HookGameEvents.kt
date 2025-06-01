package dev.thecodewarrior.hooked

import dev.thecodewarrior.hooked.platform.HookedPlatformCommon
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.util.Identifier
import net.minecraft.world.event.GameEvent

/**
 * - vibration frequencies must also be added to `data/neoforge/data_maps/game_event/vibration_frequencies.json`
 * - game events intended for Sculk sensors should be added to `data/minecraft/tags/game_event/vibrations.json`
 */
object HookGameEvents {
    val HOOK_LAND = register("hook_land", 16, 2) // projectile_land is frequency 2

    private fun register(name: String, range: Int, vibrationFrequency: Int): RegistryEntry<GameEvent> {
        val id = Identifier.of(Hooked.MOD_ID, name)
        val entry = HookedPlatformCommon.instance.registerGameEvent(id) { GameEvent(range) }
        HookedPlatformCommon.instance.registerVibrationFrequencyFabric(
            RegistryKey.of(RegistryKeys.GAME_EVENT, id),
            vibrationFrequency
        )
        return entry
    }

}