package dev.thecodewarrior.hooked.hook

import dev.thecodewarrior.hooked.capability.HookedPlayerData
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.SoundEvent
import net.minecraft.util.math.vector.Vec3d
import net.minecraft.world.World

/**
 * Provides the hook controller with access to information or functionality from the hook data or processor
 */
interface HookProcessorContext: HookControllerDelegate {
    val data: HookedPlayerData
    val type: HookType
    val controller: HookPlayerController
    override val hooks: MutableList<Hook>
}