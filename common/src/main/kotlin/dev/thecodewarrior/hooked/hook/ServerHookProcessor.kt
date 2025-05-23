package dev.thecodewarrior.hooked.hook

import dev.architectury.networking.NetworkManager
import dev.thecodewarrior.hooked.HookStats
import dev.thecodewarrior.hooked.Hooked
import dev.thecodewarrior.hooked.bridge.hookData
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.item.HookProperties
import dev.thecodewarrior.hooked.network.HookEventsS2CPacket
import dev.thecodewarrior.hooked.network.HookedPlayerDataFullSyncS2CPacket
import dev.thecodewarrior.hooked.network.HookedPlayerDataPartialSyncS2CPacket
import dev.thecodewarrior.hooked.platform.HookedPlatformCommon
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.CustomPayload
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.GameMode
import net.minecraft.world.World
import java.util.*

/**
 * Processes hooks on the *logical* server. This is present in both the client and dedicated server environments.
 */
object ServerHookProcessor: CommonHookProcessor() {
    class Context(override val data: HookedPlayerData): HookProcessorContext {
        override val properties: HookProperties get() = data.properties
        override val controller: HookPlayerController get() = data.controller
        override val player: PlayerEntity get() = data.player
        override val world: World get() = data.player.world
        override val hooks: Collection<Hook> get() = data.hooks.values

        // Hooked is designed to be resistant to server-side lag, so we only process the cooldown on the client.
        // Sure, this means it can be exploited, but it doesn't really affect balance, it mostly affects feel.
        override val cooldown: Int get() = 0
        override fun triggerCooldown() {}

        override fun markDirty(hook: Hook) {
            data.syncStatus.dirtyHooks[hook.id] = hook
        }

        override fun forceFullSyncToClient() {
            data.syncStatus.forceFullSyncToClient = true
        }

        override fun forceFullSyncToOthers() {
            data.syncStatus.forceFullSyncToOthers = true
        }

        override fun playFeedbackSound(sound: SoundEvent, volume: Float, pitch: Float) {
            // feedback sounds are played on the client
        }

        override fun playWorldSound(sound: SoundEvent, pos: Vec3d, volume: Float, pitch: Float) {
            data.player.world.playSound(null, pos.x, pos.y, pos.z, sound, SoundCategory.PLAYERS, volume, pitch)
        }

        override fun fireEvent(event: HookEvent) {
            data.syncStatus.queuedEvents.add(event)
            val hook = data.hooks[event.id] ?: return
            controller.triggerEvent(this, hook, event)
        }
    }

    fun onPlayerWorldChanged(player: ServerPlayerEntity) {
        player.hookData().hooks.clear()
        doInitialSync(player, player)
    }

    fun doInitialSync(player: ServerPlayerEntity, target: ServerPlayerEntity) {
        NetworkManager.sendToPlayer(player, target.hookData().createFullSyncPacket(true))
    }

    fun fireHook(
        player: ServerPlayerEntity,
        data: HookedPlayerData,
        pos: Vec3d,
        pitch: Float,
        yaw: Float,
        sneaking: Boolean,
        ids: List<Int>
    ) {
        if (data.maxHooks <= 0 || player.interactionManager.gameMode == GameMode.SPECTATOR) {
            // they seem to think they can fire hooks
            data.syncStatus.forceFullSyncToClient = true
        } else {
            val iter = ids.iterator()
            data.controller.fireHooks(Context(data), pos, pitch, yaw, sneaking) { hookPos, hookPitch, hookYaw ->
                var id = if (iter.hasNext()) {
                    iter.next()
                } else {
                    logger.warn("Player ${player.name}'s fire hook packet sent too few IDs. This may result in " +
                            "out-of-order hooks and cause conflicts later on.")
                    data.nextId()
                }
                if(id in data.hooks) {
                    logger.warn("Player ${player.name}'s fire hook packet had ID conflicts. This may result in " +
                            "out-of-order hooks and cause further conflicts.")
                    while (id in data.hooks) {
                        id += 10
                    }
                }
                val hook = Hook(
                    id, data.properties.hookModel.hookLength,
                    hookPos, hookPitch, hookYaw,
                    Hook.State.EXTENDING,
                    BlockPos(0, 0, 0),
                    0
                )
                data.hooks[id] = hook
                // this will cause a full sync to the client, and a single-hook sync to other clients
                data.syncStatus.dirtyHooks[id] = hook
                data.player.incrementStat(HookStats.hooksFiredStat)

                hook
            }
            if(iter.hasNext()) {
                logger.info("Player ${player.name}'s fire hook packet sent too many IDs. This shouldn't cause large" +
                    "problems, but may indicate a desync.")
            }
        }
    }

    fun jump(data: HookedPlayerData, doubleJump: Boolean, sneaking: Boolean) {
        if (data.maxHooks > 0) {
            data.controller.jump(Context(data), doubleJump, sneaking)
        }
    }

    override fun tick(player: PlayerEntity) {
        player as ServerPlayerEntity
        val data = player.hookData()

        val equippedProperties = HookedPlatformCommon.instance.getEquippedHook(player) ?: HookProperties.NONE
        if (data.properties != equippedProperties) {
            data.hooks.clear()
            data.properties = equippedProperties
            data.syncStatus.forceFullSyncToClient = true
            data.syncStatus.forceFullSyncToOthers = true
        }

        val context = Context(data)
        applyHookMotion(context)
        data.controller.update(context)

        val fullSyncPacket = data.createFullSyncPacket(false)
        val partialSyncPacket = data.createPartialSyncPacket()

        val sendToSelf = mutableListOf<CustomPayload>()
        val sendToOthers = mutableListOf<CustomPayload>()

        if (data.syncStatus.forceFullSyncToClient) {
            sendToSelf.add(fullSyncPacket)
        } else if (data.syncStatus.dirtyHooks.isNotEmpty()) {
            sendToSelf.add(partialSyncPacket)
        }

        if (data.syncStatus.forceFullSyncToOthers) {
            sendToOthers.add(fullSyncPacket)
        } else if (data.syncStatus.dirtyHooks.isNotEmpty()) {
            sendToOthers.add(partialSyncPacket)
        }

        if (data.syncStatus.queuedEvents.isNotEmpty()) {
            val packet = HookEventsS2CPacket(player.id, ArrayList(data.syncStatus.queuedEvents))
            sendToSelf.add(packet)
            sendToOthers.add(packet)
        }

        for (packet in sendToSelf) {
            NetworkManager.sendToPlayer(player, packet)
        }

        if (sendToOthers.isNotEmpty()) {
            val tracking = HookedPlatformCommon.instance.getTrackingPlayers(player)
            for (packet in sendToOthers) {
                NetworkManager.sendToPlayers(tracking, packet)
            }
        }

        data.syncStatus.forceFullSyncToClient = false
        data.syncStatus.forceFullSyncToOthers = false
        data.syncStatus.dirtyHooks.clear()
        data.syncStatus.queuedEvents.clear()
    }

    override fun isHookActive(player: PlayerEntity, reason: HookActiveReason): Boolean {
        val data = player.hookData()
        return data.controller.isActive(Context(data), reason)
    }

    private val logger = Hooked.logManager.makeLogger<ServerHookProcessor>()
}