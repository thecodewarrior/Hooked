package dev.thecodewarrior.hooked

import com.teamwizardry.librarianlib.glitter.ParticleSystemManager
import dev.thecodewarrior.hooked.bridge.hookData
import dev.thecodewarrior.hooked.client.HookRenderManager
import dev.thecodewarrior.hooked.client.Keybinds
import dev.thecodewarrior.hooked.client.glitter.EnderHookParticleSystem
import dev.thecodewarrior.hooked.hook.ClientHookProcessor
import dev.thecodewarrior.hooked.hooks.*
import dev.thecodewarrior.hooked.item.HookItem
import dev.thecodewarrior.hooked.network.GameRuleSyncS2CPacket
import dev.thecodewarrior.hooked.network.HookEventsS2CPacket
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.client.gui.screen.Screen
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.resource.ResourceType
import net.minecraft.world.World

object HookedClient: ClientModInitializer {
    private val logger = Hooked.logManager.makeLogger<HookedClient>()

    override fun onInitializeClient() {
        registerHookRenderers()
        registerNetworking()
        registerKeybinds()
        EnderHookPlayerController.particleEffect = EnderHookPlayerController.ClientParticleEffect
        HookItem.hasShiftDown = Screen::hasShiftDown
    }

    private fun registerHookRenderers() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(HookRenderManager)
        HookTypes.types.forEach {
            when(it) {
                is FlightHookType -> HookRenderManager.register(it, FlightHookRenderer(it))
                is BasicHookType -> HookRenderManager.register(it, BasicHookRenderer(it))
            }
        }
        HookRenderManager.registerEvents()
        ParticleSystemManager.add(EnderHookParticleSystem)
    }

    private fun registerKeybinds() {
        KeyBindingHelper.registerKeyBinding(Keybinds.FIRE)
        ClientTickEvents.END_CLIENT_TICK.register(Keybinds::tick)
    }

    private fun registerNetworking() {
        ClientPlayNetworking.registerGlobalReceiver(HookEventsS2CPacket.ID) { payload, context ->
            processHookEventsPacket(payload, context.client().world!!)
        }

        ClientPlayNetworking.registerGlobalReceiver(GameRuleSyncS2CPacket.ID) { payload, context ->
            payload.applyTo(context.client().world!!.gameRules)
        }
    }

    private fun processHookEventsPacket(packet: HookEventsS2CPacket, world: World) {
        val entity = world.getEntityById(packet.entityId) ?: return
        if (entity !is PlayerEntity) {
            logger.warn("hook_events - Entity ${packet.entityId} is not a player, so it has no HookedPlayerData")
            return
        }

        packet.events.forEach {
            ClientHookProcessor.triggerServerEvent(entity.hookData(), it)
        }
    }
}