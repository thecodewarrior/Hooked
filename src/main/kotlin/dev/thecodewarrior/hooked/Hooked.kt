package dev.thecodewarrior.hooked

import com.teamwizardry.librarianlib.core.util.ModLogManager
import com.teamwizardry.librarianlib.glitter.ParticleSystemManager
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer
import dev.thecodewarrior.hooked.bridge.hookData
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.client.HookRenderManager
import dev.thecodewarrior.hooked.client.Keybinds
import dev.thecodewarrior.hooked.client.glitter.EnderHookParticleSystem
import dev.thecodewarrior.hooked.hook.ClientHookProcessor
import dev.thecodewarrior.hooked.hook.HookType
import dev.thecodewarrior.hooked.hook.ServerHookProcessor
import dev.thecodewarrior.hooked.hooks.*
import dev.thecodewarrior.hooked.network.*
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.fabricmc.fabric.api.event.registry.RegistryAttribute
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.PacketByteBuf
import net.minecraft.resource.ResourceType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvent
import net.minecraft.stat.StatFormatter
import net.minecraft.stat.Stats
import net.minecraft.util.Identifier
import net.minecraft.util.registry.DefaultedRegistry
import net.minecraft.util.registry.Registry
import net.minecraft.world.GameRules
import net.minecraft.world.World
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
            ServerHookProcessor.registerEvents()
            Rules // static initializer registers the gamerule
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
            ServerPlayNetworking.registerGlobalReceiver(Packets.FIRE_HOOK) { server, player, _, buffer, _ ->
                val packet = FireHookPacket.decode(buffer)
                server.execute {
                    processFireHookPacket(packet, player)
                }
            }
            ServerPlayNetworking.registerGlobalReceiver(Packets.HOOK_JUMP) { server, player, _, buffer, _ ->
                val packet = HookJumpPacket.decode(buffer)
                server.execute {
                    processHookJumpPacket(packet, player)
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
                    packet.pitch,
                    packet.yaw,
                    packet.sneaking,
                    packet.ids
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
            registerHookRenderers()
            registerNetworking()
            registerKeybinds()
            EnderHookPlayerController.particleEffect = EnderHookPlayerController.ClientParticleEffect
        }

        private fun registerHookRenderers() {
            ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(HookRenderManager)
            HookRenderManager.registerEvents()
            HookTypes.types.forEach {
                when(it) {
                    is FlightHookType -> HookRenderManager.register(it, FlightHookRenderer(it))
                    is BasicHookType -> HookRenderManager.register(it, BasicHookRenderer(it))
                }
            }
            //HudRenderer
            ParticleSystemManager.add(EnderHookParticleSystem)
        }

        private fun registerKeybinds() {
            KeyBindingHelper.registerKeyBinding(Keybinds.FIRE)
            ClientTickEvents.END_CLIENT_TICK.register(Keybinds::tick)
        }

        private fun registerNetworking() {
            ClientPlayNetworking.registerGlobalReceiver(Packets.HOOK_EVENTS) { client, _, buffer, _ ->
                val packet = HookEventsPacket.decode(buffer)
                client.execute {
                    processHookEventsPacket(packet, client.world!!)
                }
            }
            ClientPlayNetworking.registerGlobalReceiver(Packets.GAMERULE_SYNC) { client, _, buffer, _ ->
                buffer.retain()
                client.execute {
                    Rules.decodeSync(client.world!!.gameRules, buffer)
                    buffer.release()
                }
            }
        }

        private fun processHookEventsPacket(packet: HookEventsPacket, world: World) {
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

    object Components : EntityComponentInitializer {
        @JvmField
        val HOOK_DATA = ComponentRegistry.getOrCreate(Identifier("hooked:hook_data"), HookedPlayerData::class.java)

        override fun registerEntityComponentFactories(registry: EntityComponentFactoryRegistry) {
            registry.registerFor(PlayerEntity::class.java, HOOK_DATA) { HookedPlayerData(it) }
        }
    }

    object Packets {
        val HOOK_EVENTS = Identifier("hooked:hook_events")

        val FIRE_HOOK = Identifier("hooked:fire_hook")
        val HOOK_JUMP = Identifier("hooked:hook_jump")

        @JvmField
        val GAMERULE_SYNC = Identifier("hooked:gamerule_sync")
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
        @JvmField
        val HOOK_ONE_CM = Identifier("hooked:hook_one_cm")
        @JvmField
        val HOOKS_FIRED = Identifier("hooked:hooks_fired")
    }

    object Rules {
        val ALLOW_HOOKS_WHILE_FLYING = GameRuleRegistry.register(
            "allowHooksWhileFlying", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true) { server, _ ->
                if(server != null) {
                    val packet = encodeSync(server.gameRules)
                    server.playerManager.playerList.forEach { player ->
                        ServerPlayNetworking.send(player, Packets.GAMERULE_SYNC, packet)
                    }
                }
            }
        )

        @JvmStatic
        fun encodeSync(gameRules: GameRules): PacketByteBuf {
            val buffer = PacketByteBufs.create()
            buffer.writeBoolean(gameRules.get(ALLOW_HOOKS_WHILE_FLYING).get())
            return buffer
        }

        fun decodeSync(gameRules: GameRules, buf: PacketByteBuf) {
            gameRules.get(ALLOW_HOOKS_WHILE_FLYING).set(buf.readBoolean(), null)
        }
    }
}