package dev.thecodewarrior.hooked.hooks

import com.teamwizardry.librarianlib.math.Matrix4dStack
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.client.renderer.SimpleHookRenderer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity

open class BasicHookRenderer(type: BasicHookType): SimpleHookRenderer<BasicHookPlayerController>(type) {
    override fun render(
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        player: PlayerEntity,
        style: RenderStyle,
        partialTicks: Float,
        data: HookedPlayerData,
        controller: BasicHookPlayerController
    ) {
        renderHooks(matrices, vertexConsumers, player, style, partialTicks, data, 0.0)
    }

}