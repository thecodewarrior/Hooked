package dev.thecodewarrior.hooked.platform

import com.teamwizardry.librarianlib.core.util.ServiceLoaderHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.render.Camera
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.InputUtil
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.resource.ResourceReloader
import net.minecraft.util.Identifier

interface HookedPlatformClient {
    /**
     * This is in a platform module because NeoForge adds key conflict information to the KeyBinding constructor.
     */
    fun createInGameKeybind(translationKey: String, type: InputUtil.Type, code: Int, category: String): KeyBinding

    fun registerKeybindTickEvent(hook: () -> Unit)

    fun registerRenderWorldEvent(hook: (MatrixStack, Camera, VertexConsumerProvider) -> Unit)

    fun registerClientResourceReloader(loader: ResourceReloader, identifier: Identifier)

    companion object {
        @JvmStatic
        val instance by ServiceLoaderHelper.required<HookedPlatformClient>()
    }
}