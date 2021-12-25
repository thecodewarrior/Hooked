package dev.thecodewarrior.hooked.bridge

import com.teamwizardry.librarianlib.core.util.mixinCast
import dev.thecodewarrior.hooked.Hooked
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import net.minecraft.entity.player.PlayerEntity

interface PlayerMixinBridge {
    var hookedTravelingByHookFlag: Boolean

    /**
     * Set to true by hook controllers so you don't start flying while using a hook
     */
    var hookedShouldAbortElytraFlag: Boolean
}

fun PlayerEntity.bridge(): PlayerMixinBridge = mixinCast(this)
fun PlayerEntity.hookData(): HookedPlayerData = Hooked.Components.HOOK_DATA.get(this)
