package dev.thecodewarrior.hooked

import com.teamwizardry.librarianlib.core.util.ModLogManager
import com.teamwizardry.librarianlib.courier.CourierClientPlayNetworking
import com.teamwizardry.librarianlib.courier.CourierPacketType
import com.teamwizardry.librarianlib.courier.CourierServerPlayNetworking
import dev.thecodewarrior.hooked.bridge.hookData
import dev.thecodewarrior.hooked.client.Keybinds
import dev.thecodewarrior.hooked.hook.ClientHookProcessor
import dev.thecodewarrior.hooked.hook.HookType
import dev.thecodewarrior.hooked.hook.ServerHookProcessor
import dev.thecodewarrior.hooked.hooks.BasicHookType
import dev.thecodewarrior.hooked.hooks.EnderHookType
import dev.thecodewarrior.hooked.hooks.FlightHookType
import dev.thecodewarrior.hooked.network.*
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.fabricmc.fabric.api.event.registry.RegistryAttribute
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvent
import net.minecraft.stat.StatFormatter
import net.minecraft.stat.Stats
import net.minecraft.util.Identifier
import net.minecraft.util.registry.DefaultedRegistry
import net.minecraft.util.registry.Registry
import kotlin.math.sqrt

object Hooked {
    val logManager = ModLogManager("hooked", "Hooked")
    lateinit var hookRegistry: DefaultedRegistry<HookType>
        private set

    object CommonInitializer: ModInitializer {
        private val logger = logManager.makeLogger<CommonInitializer>()

        override fun onInitialize() {
            createRegistry()
            HookTypes.registerTypes()
            HookTypes.registerItems()
            registerStats()
            registerSounds()
            registerNetworking()
        }

        private fun createRegistry() {
            hookRegistry = FabricRegistryBuilder.createDefaulted(
                HookType::class.java,
                Identifier("hooked:hook_type"),
                Identifier("hooked:none")
            ).attribute(RegistryAttribute.PERSISTED).buildAndRegister()
        }

        private fun registerStats() {
            Registry.register(Registry.CUSTOM_STAT, HookStats.HOOK_ONE_CM, HookStats.HOOK_ONE_CM)
            Stats.CUSTOM.getOrCreateStat(HookStats.HOOK_ONE_CM, StatFormatter.DISTANCE)
            Registry.register(Registry.CUSTOM_STAT, HookStats.HOOKS_FIRED, HookStats.HOOKS_FIRED)
            Stats.CUSTOM.getOrCreateStat(HookStats.HOOKS_FIRED, StatFormatter.DEFAULT)
        }

        private fun registerSounds() {
            Registry.register(Registry.SOUND_EVENT, Sounds.FIRE_HOOK_ID, Sounds.FIRE_HOOK_EVENT)
            Registry.register(Registry.SOUND_EVENT, Sounds.RETRACT_HOOK_ID, Sounds.RETRACT_HOOK_EVENT)
            Registry.register(Registry.SOUND_EVENT, Sounds.HOOK_HIT_ID, Sounds.HOOK_HIT_EVENT)
            Registry.register(Registry.SOUND_EVENT, Sounds.HOOK_MISS_ID, Sounds.HOOK_MISS_EVENT)
            Registry.register(Registry.SOUND_EVENT, Sounds.HOOK_DISLODGE_ID, Sounds.HOOK_DISLODGE_EVENT)
        }

        private fun registerNetworking() {
            CourierServerPlayNetworking.registerGlobalReceiver(Packets.FIRE_HOOK) { packet, context ->
                context.execute {
                    processFireHookPacket(packet, context.player)
                }
            }
            CourierServerPlayNetworking.registerGlobalReceiver(Packets.HOOK_JUMP) { packet, context ->
                context.execute {
                    processHookJumpPacket(packet, context.player)
                }
            }
        }

        private fun processFireHookPacket(packet: FireHookPacket, player: ServerPlayerEntity) {
            val hookedPlayerData = player.hookData()
            val distanceSq = packet.pos.squaredDistanceTo(player.eyePos)
            val maxDistance = CheatMitigation.fireHookTolerance.getValue(player)
            if (distanceSq > maxDistance * maxDistance) {
                hookedPlayerData.syncStatus.forceFullSyncToClient = true
                logger.error(
                    "Player ${player.name} fired a hook from ${sqrt(distanceSq)} blocks away. The tolerance " +
                            "based on their ping of ${player.pingMilliseconds} is $maxDistance"
                )
            } else {
                ServerHookProcessor.fireHook(
                    player,
                    hookedPlayerData,
                    packet.pos,
                    packet.direction.normalize(),
                    packet.sneaking,
                    packet.uuids
                )
            }
        }

        private fun processHookJumpPacket(packet: HookJumpPacket, player: ServerPlayerEntity) {
            ServerHookProcessor.jump(player.hookData(), packet.doubleJump, packet.sneaking)
        }
    }

    object ClientInitializer: ClientModInitializer {
        private val logger = logManager.makeLogger<ClientInitializer>()

        override fun onInitializeClient() {
            registerNetworking()
            registerKeybinds()
        }

        private fun registerKeybinds() {
            KeyBindingHelper.registerKeyBinding(Keybinds.FIRE)
            ClientTickEvents.END_CLIENT_TICK.register(Keybinds::tick)
        }

        private fun registerNetworking() {
            CourierClientPlayNetworking.registerGlobalReceiver(Packets.HOOK_EVENTS) { packet, context ->
                context.execute {
                    processHookEventsPacket(packet, context.client.player)
                }
            }
            CourierClientPlayNetworking.registerGlobalReceiver(Packets.SYNC_HOOK_DATA) { packet, context ->
                context.execute {
                    processSyncHookDataPacket(packet, context.client.player)
                }
            }
            CourierClientPlayNetworking.registerGlobalReceiver(Packets.SYNC_INDIVIDUAL_HOOKS) { packet, context ->
                context.execute {
                    processSyncIndividualHooksPacket(packet, context.client.player)
                }
            }
        }

        private fun processHookEventsPacket(packet: HookEventsPacket, player: ClientPlayerEntity) {
            val entity = player.world.getEntityById(packet.entityId) ?: return
            if (entity !is PlayerEntity) {
                logger.warn("hook_events - Entity ${packet.entityId} is not a player, so it has no HookedPlayerData")
                return
            }

            packet.events.forEach {
                ClientHookProcessor.triggerServerEvent(entity.hookData(), it)
            }
        }

        private fun processSyncHookDataPacket(packet: SyncHookDataPacket, player: ClientPlayerEntity) {
            val entity = player.world.getEntityById(packet.entityId) ?: return
            if (entity !is PlayerEntity) {
                logger.warn("sync_hook_data - Entity ${packet.entityId} is not a player, so it has no HookedPlayerData")
                return
            }

            val data = player.hookData()
            data.deserializeNBT(packet.tag)
            for (removedHook in packet.removed) {
                data.syncStatus.recentHooks.add(removedHook)
            }
        }

        private fun processSyncIndividualHooksPacket(packet: SyncIndividualHooksPacket, player: ClientPlayerEntity) {
            val entity = player.world.getEntityById(packet.entityId) ?: return
            if (entity !is PlayerEntity) {
                logger.warn("sync_individual_hooks - Entity ${packet.entityId} is not a player, so it has no HookedPlayerData")
                return
            }
            val data = entity.hookData()
            packet.hooks.forEach {
                ClientHookProcessor.syncHook(data, it)
            }
        }
    }

    object Packets {
        val HOOK_EVENTS = CourierPacketType(Identifier("hooked:hook_events"), HookEventsPacket::class.java)
        val SYNC_HOOK_DATA = CourierPacketType(Identifier("hooked:sync_hook_data"), SyncHookDataPacket::class.java)
        val SYNC_INDIVIDUAL_HOOKS =
            CourierPacketType(Identifier("hooked:sync_individual_hooks"), SyncIndividualHooksPacket::class.java)

        val FIRE_HOOK = CourierPacketType(Identifier("hooked:fire_hook"), FireHookPacket::class.java)
        val HOOK_JUMP = CourierPacketType(Identifier("hooked:hook_jump"), HookJumpPacket::class.java)
    }

    object Sounds {
        val FIRE_HOOK_ID = Identifier("hooked:fire_hook")
        val FIRE_HOOK_EVENT = SoundEvent(FIRE_HOOK_ID)
        val RETRACT_HOOK_ID = Identifier("hooked:retract_hook")
        val RETRACT_HOOK_EVENT = SoundEvent(RETRACT_HOOK_ID)
        val HOOK_HIT_ID = Identifier("hooked:hook_hit")
        val HOOK_HIT_EVENT = SoundEvent(HOOK_HIT_ID)
        val HOOK_MISS_ID = Identifier("hooked:hook_miss")
        val HOOK_MISS_EVENT = SoundEvent(HOOK_MISS_ID)
        val HOOK_DISLODGE_ID = Identifier("hooked:hook_dislodge")
        val HOOK_DISLODGE_EVENT = SoundEvent(HOOK_DISLODGE_ID)
    }

    object HookStats {
        val HOOK_ONE_CM = Identifier("hooked:hook_one_cm")
        val HOOKS_FIRED = Identifier("hooked:hooks_fired")
    }
}