package dev.thecodewarrior.hooked.hooks

import com.teamwizardry.librarianlib.math.Matrix4dStack
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.client.renderer.SimpleHookRenderer
import net.minecraft.entity.player.PlayerEntity

class BasicHookRenderer(type: BasicHookType): SimpleHookRenderer<BasicHookPlayerController>(type) {
    override fun render(
        player: PlayerEntity,
        matrix: Matrix4dStack,
        partialTicks: Float,
        data: HookedPlayerData,
        controller: BasicHookPlayerController
    ) {
        renderHooks(player, matrix, partialTicks, data, 0.0)
    }
}