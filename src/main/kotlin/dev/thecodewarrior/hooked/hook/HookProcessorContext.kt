package dev.thecodewarrior.hooked.hook

import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.item.HookProperties

/**
 * Provides the hook controller with access to information or functionality from the hook data or processor
 */
interface HookProcessorContext: HookControllerDelegate {
    val data: HookedPlayerData
    val controller: HookPlayerController
    override val hooks: Collection<Hook>
}