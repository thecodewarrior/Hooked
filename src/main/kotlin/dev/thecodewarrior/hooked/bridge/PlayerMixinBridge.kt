package dev.thecodewarrior.hooked.bridge

import com.teamwizardry.librarianlib.core.util.mixinCast
import dev.thecodewarrior.hooked.Hooked
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.hook.HookActiveReason
import dev.thecodewarrior.hooked.hook.HookProcessor
import net.minecraft.entity.player.PlayerEntity

interface PlayerMixinBridge {
    val hookProcessor: HookProcessor

    fun isHookActive(reason: HookActiveReason): Boolean {
        return hookProcessor.isHookActive(mixinCast(this), reason)
    }
}

fun PlayerEntity.bridge(): PlayerMixinBridge = mixinCast(this)
fun PlayerEntity.hookData(): HookedPlayerData = Hooked.Components.HOOK_DATA.get(this)
