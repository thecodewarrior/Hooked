package dev.thecodewarrior.hooked.hook

import com.teamwizardry.librarianlib.core.util.kotlin.getOrNull
import com.teamwizardry.librarianlib.courier.CourierServerPlayNetworking
import dev.emi.trinkets.api.TrinketsApi
import dev.thecodewarrior.hooked.Hooked
import dev.thecodewarrior.hooked.bridge.hookData
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.capability.IHookItem
import dev.thecodewarrior.hooked.network.HookEventsPacket
import dev.thecodewarrior.hooked.network.SyncHookDataPacket
import dev.thecodewarrior.hooked.network.SyncIndividualHooksPacket
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.minecraft.entity.player.PlayerEntity
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
        override val type: HookType get() = data.type
        override val controller: HookPlayerController get() = data.controller
        override val player: PlayerEntity get() = data.player
        override val world: World get() = data.player.world
        override val hooks: MutableList<Hook> get() = data.hooks

        // Hooked is designed to be resistant to server-side lag, so we only process the cooldown on the client.
        // Sure, this means it can be exploited, but it doesn't really affect balance, it mostly affects feel.
        override val cooldown: Int get() = 0
        override fun triggerCooldown() {}

        override fun markDirty(hook: Hook) {
            data.syncStatus.dirtyHooks.add(hook)
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
            val hook = hooks.find { it.uuid == event.uuid }
                ?: return
            controller.triggerEvent(this, hook, event)
        }
    }

    fun registerEvents() {
        EntityTrackingEvents.START_TRACKING.register { target, player ->
            if(target is PlayerEntity) {
                CourierServerPlayNetworking.send(
                    player,
                    Hooked.Packets.SYNC_HOOK_DATA,
                    SyncHookDataPacket(
                        target.id,
                        ArrayList(),
                        target.hookData().serializeNBT()
                    )
                )
            }
        }
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register { player, _, _ ->
            CourierServerPlayNetworking.send(
                player,
                Hooked.Packets.SYNC_HOOK_DATA,
                SyncHookDataPacket(
                    player.id,
                    ArrayList(),
                    player.hookData().serializeNBT()
                )
            )
        }
    }

    fun fireHook(
        player: ServerPlayerEntity,
        data: HookedPlayerData,
        pos: Vec3d,
        direction: Vec3d,
        sneaking: Boolean,
        uuids: List<UUID>
    ) {
        if (data.type == HookType.NONE || player.interactionManager.gameMode == GameMode.SPECTATOR) {
            // they seem to think they can fire hooks
            data.syncStatus.forceFullSyncToClient = true
        } else {
            val iter = uuids.iterator()
            data.controller.fireHooks(Context(data), pos, direction, sneaking) { hookPos, hookDirection ->
                val uuid = if (iter.hasNext()) {
                    iter.next()
                } else {
                    logger.warn("Player ${player.name}'s fire hook packet sent too few UUIDs. This shouldn't cause " +
                        "problems, but may indicate a desync.")
                    UUID.randomUUID()
                }
                val hook = Hook(
                    uuid,
                    data.type,
                    hookPos,
                    Hook.State.EXTENDING,
                    hookDirection,
                    BlockPos(0, 0, 0),
                    0
                )
                data.hooks.add(hook)
                // this will cause a full sync to the client, and a single-hook sync to other clients
                data.syncStatus.dirtyHooks.add(hook)
                data.player.incrementStat(Hooked.HookStats.HOOKS_FIRED)

                hook
            }
            if(iter.hasNext()) {
                logger.warn("Player ${player.name}'s fire hook packet sent too many UUIDs. This shouldn't cause large" +
                    "problems, but may indicate a desync.")
            }
        }
    }

    fun jump(data: HookedPlayerData, doubleJump: Boolean, sneaking: Boolean) {
        if (data.type != HookType.NONE) {
            data.controller.jump(Context(data), doubleJump, sneaking)
        }
    }

    fun tick(player: ServerPlayerEntity) {
        val data = player.hookData()

        val equippedType = getEquippedHook(player)?.hookType ?: HookType.NONE
        if (data.type != equippedType) {
            data.hooks.clear()
            data.type = equippedType
            data.syncStatus.forceFullSyncToClient = true
            data.syncStatus.forceFullSyncToOthers = true
        }

        val context = Context(data)
        applyHookMotion(context)
        data.controller.update(context)


        if (data.syncStatus.forceFullSyncToClient || data.syncStatus.dirtyHooks.isNotEmpty()) {
            CourierServerPlayNetworking.send(
                player,
                Hooked.Packets.SYNC_HOOK_DATA,
                SyncHookDataPacket(
                    player.id,
                    data.syncStatus.dirtyHooks.filterTo(ArrayList()) { it.state == Hook.State.REMOVED },
                    data.serializeNBT()
                )
            )
        }
        if (data.syncStatus.forceFullSyncToOthers) {
            val packet = SyncHookDataPacket(
                player.id,
                data.syncStatus.dirtyHooks.filterTo(ArrayList()) { it.state == Hook.State.REMOVED },
                data.serializeNBT()
            )
            PlayerLookup.tracking(player).forEach {
                CourierServerPlayNetworking.send(it, Hooked.Packets.SYNC_HOOK_DATA, packet)
            }
        } else if (data.syncStatus.dirtyHooks.isNotEmpty()) {
            val packet = SyncIndividualHooksPacket(player.id, ArrayList(data.syncStatus.dirtyHooks))
            PlayerLookup.tracking(player).forEach {
                CourierServerPlayNetworking.send(it, Hooked.Packets.SYNC_INDIVIDUAL_HOOKS, packet)
            }
        }
        if (data.syncStatus.queuedEvents.isNotEmpty()) {
            val packet = HookEventsPacket(player.id, ArrayList(data.syncStatus.queuedEvents))
            CourierServerPlayNetworking.send(player, Hooked.Packets.HOOK_EVENTS, packet)
            PlayerLookup.tracking(player).forEach {
                CourierServerPlayNetworking.send(it, Hooked.Packets.HOOK_EVENTS, packet)
            }
        }

        data.syncStatus.forceFullSyncToClient = false
        data.syncStatus.forceFullSyncToOthers = false
        data.syncStatus.dirtyHooks.clear()
        data.syncStatus.queuedEvents.clear()
    }

    private fun getEquippedHook(player: PlayerEntity): IHookItem? {
        val component = TrinketsApi.getTrinketComponent(player).getOrNull() ?: return null
        val stack = component.getEquipped { it.item is IHookItem }.firstOrNull()?.right
        return stack?.item as? IHookItem
    }

    private val logger = Hooked.logManager.makeLogger<ServerHookProcessor>()
}