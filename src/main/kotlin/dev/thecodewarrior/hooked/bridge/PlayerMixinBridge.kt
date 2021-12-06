package dev.thecodewarrior.hooked.bridge

import com.teamwizardry.librarianlib.core.util.mixinCast
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import net.minecraft.entity.player.PlayerEntity

interface PlayerMixinBridge {
    var hookedPlayerData: HookedPlayerData
    var hookedTravelingByHookFlag: Boolean
    var hookedShouldAbortElytraFlag: Boolean
}

fun PlayerEntity.bridge(): PlayerMixinBridge = mixinCast(this)
fun PlayerEntity.hookData(): HookedPlayerData = this.bridge().hookedPlayerData
