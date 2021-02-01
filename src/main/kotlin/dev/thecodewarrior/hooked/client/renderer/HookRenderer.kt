package dev.thecodewarrior.hooked.client.renderer

import com.teamwizardry.librarianlib.math.Matrix4dStack
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.hook.HookPlayerController
import net.minecraft.entity.player.PlayerEntity

abstract class HookRenderer<C: HookPlayerController> {
    abstract fun render(
        player: PlayerEntity,
        matrix: Matrix4dStack,
        partialTicks: Float,
        data: HookedPlayerData,
        controller: C
    )
}