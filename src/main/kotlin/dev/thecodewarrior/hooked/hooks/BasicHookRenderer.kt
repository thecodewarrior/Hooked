package dev.thecodewarrior.hooked.hooks

import com.teamwizardry.librarianlib.math.Matrix4dStack
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.client.renderer.SimpleHookRenderer
import dev.thecodewarrior.hooked.util.getWaistPos
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity

open class BasicHookRenderer(type: BasicHookType): SimpleHookRenderer<BasicHookPlayerController>(type) {
    override fun render(
        matrices: MatrixStack,
        player: PlayerEntity,
        ghost: Boolean,
        tickDelta: Float,
        data: HookedPlayerData,
        controller: BasicHookPlayerController
    ) {
        val waist = player.getWaistPos(tickDelta)
        matrices.translate(waist.x, waist.y, waist.z)
        renderHooks(matrices, player, ghost, tickDelta, data, 0.0)
    }
}