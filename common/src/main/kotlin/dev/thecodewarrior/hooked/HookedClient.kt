package dev.thecodewarrior.hooked

import com.teamwizardry.librarianlib.glitter.ParticleSystemManager
import dev.thecodewarrior.hooked.client.HookRenderManager
import dev.thecodewarrior.hooked.client.Keybinds
import dev.thecodewarrior.hooked.client.glitter.ChainShatterParticleSpawner
import dev.thecodewarrior.hooked.client.glitter.ChainShatterParticleSystem
import dev.thecodewarrior.hooked.client.renderer.HookModelLoader
import dev.thecodewarrior.hooked.hooks.*
import dev.thecodewarrior.hooked.item.HookItemBase
import dev.thecodewarrior.hooked.platform.HookedPlatformClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier

object HookedClient {
    private val logger = Hooked.logManager.makeLogger<HookedClient>()

    fun init() {
        HookClientNetworking.registerNetworking()
        registerHookRenderers()
        registerKeybinds()
        ChainShatterParticleSpawner.impl = ChainShatterParticleSpawner.ClientParticleImpl
        HookItemBase.hasShiftDown = Screen::hasShiftDown
    }

    private fun registerHookRenderers() {
        HookedPlatformClient.instance.registerResourceReloader(
            ResourceType.CLIENT_RESOURCES,
            HookModelLoader,
            Identifier.of(Hooked.MOD_ID, "hook_model_loader")
        )
        HookRenderManager.register(HookBehaviors.BASIC, BasicHookRenderer())
        HookRenderManager.register(HookBehaviors.FLIGHT, FlightHookRenderer())
        HookRenderManager.registerEvents()
        ParticleSystemManager.add(ChainShatterParticleSystem)
    }

    private fun registerKeybinds() {
        HookedPlatformClient.instance.registerKeybind { Keybinds.FIRE }
        HookedPlatformClient.instance.registerKeybindTickEvent(Keybinds::tick)
    }

}