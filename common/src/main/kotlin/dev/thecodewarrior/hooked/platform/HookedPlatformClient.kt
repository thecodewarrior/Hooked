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

    /**
     * This is required to allow us to use the `C` keybind by default.
     *
     * The default keybind for Hooked is `C`, but that's already bound to the vanilla `saveToolbarActivatorKey` keybind.
     * NeoForge patches `KeyBinding.onKeyPressed` to process all conflicting keys, so it works fine, but fabric doesn't
     * have that patch.
     *
     * To work around it, when the `saveToolbarActivatorKey` is in conflict with the Hooked keybind, we just use the
     * vanilla keybind instead. Vanilla never accesses `wasPressed()` on that keybind, so we can call that method
     * without interfering with vanilla's usage.
     */
    fun getActualFireKeybinding(): KeyBinding

    fun registerKeybindTickEvent(hook: () -> Unit)

    fun registerRenderWorldEvent(hook: (MatrixStack, Camera, VertexConsumerProvider) -> Unit)

    fun registerClientResourceReloader(loader: ResourceReloader, identifier: Identifier)

    companion object {
        @JvmStatic
        val instance by ServiceLoaderHelper.required<HookedPlatformClient>()
    }
}