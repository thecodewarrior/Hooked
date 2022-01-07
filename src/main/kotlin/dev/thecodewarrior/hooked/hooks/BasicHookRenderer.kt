package dev.thecodewarrior.hooked.hooks

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
        consumers: VertexConsumerProvider,
        tickDelta: Float,
        data: HookedPlayerData,
        controller: BasicHookPlayerController
    ) {
        val waist = player.getWaistPos(tickDelta)
        matrices.translate(waist.x, waist.y, waist.z)
        renderHooks(matrices, player, consumers, tickDelta, data, 0.0)
    }
}