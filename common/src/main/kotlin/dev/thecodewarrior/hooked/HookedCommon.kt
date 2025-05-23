package dev.thecodewarrior.hooked

import dev.thecodewarrior.hooked.hooks.HookBehaviors
import dev.thecodewarrior.hooked.item.ItemComponents

object HookedCommon {
    private val logger = Hooked.logManager.makeLogger<HookedCommon>()

    fun init() {
        ItemComponents
        HookBehaviors
        HookItems
        HookSounds
        HookStats
        HookGameRules
        HookTags
        HookCommonNetworking.registerNetworking()
    }
}