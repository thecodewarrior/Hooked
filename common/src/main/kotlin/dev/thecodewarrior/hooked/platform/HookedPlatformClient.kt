package dev.thecodewarrior.hooked.platform

import com.teamwizardry.librarianlib.core.util.ServiceLoaderHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.render.Camera
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.resource.ResourceReloader
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier

interface HookedPlatformClient {
    fun registerKeybind(provider: () -> KeyBinding)

    fun registerKeybindTickEvent(hook: () -> Unit)

    fun registerRenderWorldEvent(hook: (MatrixStack, Camera, VertexConsumerProvider) -> Unit)

    fun registerResourceReloader(type: ResourceType, loader: ResourceReloader, identifier: Identifier)

    companion object {
        @JvmStatic
        val instance by ServiceLoaderHelper.required<HookedPlatformClient>()
    }
}