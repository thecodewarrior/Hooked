package dev.thecodewarrior.hooked.client.renderer

import com.teamwizardry.librarianlib.math.Matrix4dStack
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.hook.type.BasicHookPlayerController
import dev.thecodewarrior.hooked.hook.type.BasicHookType
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