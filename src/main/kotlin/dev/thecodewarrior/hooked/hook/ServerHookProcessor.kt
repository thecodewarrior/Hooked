package dev.thecodewarrior.hooked.hook

import com.teamwizardry.librarianlib.core.util.kotlin.getOrNull
import com.teamwizardry.librarianlib.courier.CourierServerPlayNetworking
import dev.emi.trinkets.api.TrinketsApi
import dev.thecodewarrior.hooked.Hooked
import dev.thecodewarrior.hooked.bridge.hookData
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.capability.IHookItem
import dev.thecodewarrior.hooked.network.HookEventsPacket
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents
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

    fun registerEvents() {
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register { player, _, _ ->
            player.hookData().syncStatus.forceFullSyncToClient = true
        }
    }

    fun fireHook(
        player: ServerPlayerEntity,
        data: HookedPlayerData,
        pos: Vec3d,
        direction: Vec3d,
        sneaking: Boolean,
        ids: List<Int>
    ) {
        if (data.type == HookType.NONE || player.interactionManager.gameMode == GameMode.SPECTATOR) {
            // they seem to think they can fire hooks
            data.syncStatus.forceFullSyncToClient = true
        } else {
            val iter = ids.iterator()
            data.controller.fireHooks(Context(data), pos, direction, sneaking) { hookPos, hookDirection ->
                var id = if (iter.hasNext()) {
                    iter.next()
                } else {
                    logger.info("Player ${player.name}'s fire hook packet sent too few IDs. This may result in " +
                            "out-of-order hooks and cause conflicts later on.")
                    data.nextId()
                }
                if(id in data.hooks) {
                    logger.info("Player ${player.name}'s fire hook packet had ID conflicts. This may result in " +
                            "out-of-order hooks and cause further conflicts.")
                    while (id in data.hooks) {
                        id += 10
                    }
                }
                val hook = Hook(
                    id,
                    data.type,
                    hookPos,
                    Hook.State.EXTENDING,
                    hookDirection,
                    BlockPos(0, 0, 0),
                    0
                )
                data.hooks[id] = hook
                // this will cause a full sync to the client, and a single-hook sync to other clients
                data.syncStatus.dirtyHooks[id] = hook
                data.player.incrementStat(Hooked.HookStats.HOOKS_FIRED)

                hook
            }
            if(iter.hasNext()) {
                logger.info("Player ${player.name}'s fire hook packet sent too many IDs. This shouldn't cause large" +
                    "problems, but may indicate a desync.")
            }
        }
    }

    fun jump(data: HookedPlayerData, doubleJump: Boolean, sneaking: Boolean) {
        if (data.type != HookType.NONE) {
            data.controller.jump(Context(data), doubleJump, sneaking)
        }
    }

    override fun tick(player: PlayerEntity) {
        player as ServerPlayerEntity
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

        // we run this every tick, but it uses `shouldSyncWith` to send updates only when necessary
        data.updateSync()

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

    override fun isHookActive(player: PlayerEntity): Boolean {
        val data = player.hookData()
        return data.controller.isActive(Context(data))
    }

    private fun getEquippedHook(player: PlayerEntity): IHookItem? {
        val component = TrinketsApi.getTrinketComponent(player).getOrNull() ?: return null
        val stack = component.getEquipped { it.item is IHookItem }.firstOrNull()?.right
        return stack?.item as? IHookItem
    }

    private val logger = Hooked.logManager.makeLogger<ServerHookProcessor>()
}