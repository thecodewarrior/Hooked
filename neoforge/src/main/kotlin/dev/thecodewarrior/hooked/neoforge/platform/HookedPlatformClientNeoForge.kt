package dev.thecodewarrior.hooked.neoforge.platform

import com.google.auto.service.AutoService
import dev.thecodewarrior.hooked.client.Keybinds
import dev.thecodewarrior.hooked.neoforge.HookedNeoForgeClient
import dev.thecodewarrior.hooked.platform.HookedPlatformClient
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.render.Camera
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.InputUtil
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.resource.ReloadableResourceManagerImpl
import net.minecraft.resource.ResourceReloader
import net.minecraft.util.Identifier
import net.neoforged.neoforge.client.settings.KeyConflictContext

@AutoService(HookedPlatformClient::class)
class HookedPlatformClientNeoForge : HookedPlatformClient {
    override fun createInGameKeybind(
        translationKey: String,
        type: InputUtil.Type,
        code: Int,
        category: String
    ): KeyBinding {
        return KeyBinding(translationKey, KeyConflictContext.IN_GAME, type, code, category)
    }

    override fun getActualFireKeybinding(): KeyBinding {
        return Keybinds.FIRE
    }

    override fun registerKeybindTickEvent(hook: () -> Unit) {
        HookedNeoForgeClient.clientTickHooks.add(hook)
    }

    override fun registerRenderWorldEvent(hook: (MatrixStack, Camera, VertexConsumerProvider) -> Unit) {
        HookedNeoForgeClient.renderEventHooks.add(hook)
    }

    override fun registerClientResourceReloader(loader: ResourceReloader, identifier: Identifier) {
        (MinecraftClient.getInstance().resourceManager as ReloadableResourceManagerImpl).registerReloader(loader)
    }
}