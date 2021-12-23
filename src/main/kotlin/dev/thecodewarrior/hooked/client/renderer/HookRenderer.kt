package dev.thecodewarrior.hooked.client.renderer

import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.hook.HookPlayerController
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.resource.ResourceReloader
import net.minecraft.util.Identifier

abstract class HookRenderer<C: HookPlayerController> : ResourceReloader {
    abstract fun render(
        matrices: MatrixStack,
        player: PlayerEntity,
        ghost: Boolean,
        tickDelta: Float,
        data: HookedPlayerData,
        controller: C
    )
}