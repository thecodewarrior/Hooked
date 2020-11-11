package dev.thecodewarrior.hooked.hook.processor

import dev.thecodewarrior.hooked.capability.HookedPlayerData
import net.minecraft.client.entity.player.ClientPlayerEntity

object ClientHookProcessor {
    fun clientPlayerEntityPreLivingTickMove(player: ClientPlayerEntity, data: HookedPlayerData) { data.toString()
    }
}