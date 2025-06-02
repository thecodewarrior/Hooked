package dev.thecodewarrior.hooked.capability

import dev.thecodewarrior.hooked.Hooked
import dev.thecodewarrior.hooked.hook.Hook
import dev.thecodewarrior.hooked.hook.HookEvent
import dev.thecodewarrior.hooked.hook.HookPlayerController
import dev.thecodewarrior.hooked.item.HookProperties
import dev.thecodewarrior.hooked.network.HookedPlayerDataFullSyncS2CPacket
import dev.thecodewarrior.hooked.network.HookedPlayerDataPartialSyncS2CPacket
import dev.thecodewarrior.hooked.util.CircularArray
import dev.thecodewarrior.hooked.util.CircularMap
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtOps
import java.util.*
import kotlin.math.max

/**
 * A capability holding all the data and logic required for actually running the hooks.
 */
class HookedPlayerData(val player: PlayerEntity) {
    var properties: HookProperties = HookProperties.NONE
        set(value) {
            if (value != field) {
                controller.remove()
                controller = value.behavior.createController(player, value)
            }
            field = value
        }

    val maxHooks: Int
        get() = properties.count

    /**
     * The hooks mapped by ID and sorted by the order they were fired.
     *
     * Ordering note: New hook IDs are sent by the client, and if there are conflicts the hooks will wind up out of
     * order. ID conflicts won't occur unless something breaks or the client is hacked, so it's not an issue.
     */
    var hooks: NavigableMap<Int, Hook> = TreeMap()

    var controller: HookPlayerController = HookPlayerController.NONE

    /**
     * The highest ID, used when generating new IDs. Updated in [nextId] to ensure it's >= the max id in [hooks].
     *
     * We track this separately because if the client is spamming new hooks, the server might send an update in the
     * middle. If we just used the last key in the map, it would start spamming id conflicts.
     */
    private var lastId: Int = 0
        get() {
            if(hooks.isNotEmpty()) {
                field = max(field, hooks.lastKey())
            }
            return field
        }

    fun nextId(): Int {
        return ++lastId
    }

    class SyncStatus {
        /**
         * Events queued to be sent to the client
         */
        val queuedEvents = mutableListOf<HookEvent>()
        val syncToClientHooks = mutableMapOf<Int, Hook>()
        val syncToOthersHooks = mutableMapOf<Int, Hook>()
        val syncToServerHooks = mutableMapOf<Int, Hook>()

        var forceFullSyncToClient: Boolean = false
        var forceFullSyncToOthers: Boolean = false

        /**
         * Used on the client to store recently fired events. If the client receives an identical event from the server,
         * that event will be ignored
         */
        val recentEvents = CircularArray<HookEvent>(25)

        /**
         * Used on the client to store references to recently removed hooks in case an event references them
         */
        val recentHooks = CircularMap<Int, Hook>(25)

        fun addRecentHook(hook: Hook) {
            recentHooks[hook.id] = hook
        }
        fun addRecentHooks(hooks: Collection<Hook>) {
            hooks.forEach { addRecentHook(it) }
        }

        fun syncToClient(hook: Hook) {
            syncToClientHooks[hook.id] = hook
        }
        fun syncToOthers(hook: Hook) {
            syncToOthersHooks[hook.id] = hook
        }
        fun syncToServer(hook: Hook) {
            syncToServerHooks[hook.id] = hook
        }
    }

    var syncStatus: SyncStatus = SyncStatus()

    fun writeToNbt(tag: NbtCompound) {
        tag.put("Properties", properties.toNBT())
        tag.put(
            "Hooks",
            Hook.LIST_CODEC.encodeStart(NbtOps.INSTANCE, hooks.values.toList())
                .resultOrPartial { logger.error("Error writing hooks: $it") }
                .orElse(NbtList())
        )
        tag.put("Controller", NbtCompound().also { controller.saveState(it) })
    }

    fun readFromNbt(tag: NbtCompound) {
        properties = tag.get("Properties")?.let { HookProperties.fromNBT(it) } ?: HookProperties.NONE
        hooks = Hook.LIST_CODEC.parse(NbtOps.INSTANCE, tag.get("Hooks"))
            .resultOrPartial {  logger.error("Error reading hooks: $it") }
            .orElse(emptyList())
            .associateByTo(TreeMap()) { it.id }
        controller.loadState(tag.getCompound("Controller"))

        syncStatus.forceFullSyncToClient = true
        syncStatus.forceFullSyncToOthers = true
    }

    fun createFullSyncPacket(initial: Boolean) = HookedPlayerDataFullSyncS2CPacket(
        entityId = player.id,
        initial = initial,
        properties = properties,
        hooks = hooks.values.toList(),
        controllerState = controller.writeSyncState(initial)
    )

    fun createPartialSyncPacket(toClient: Boolean): HookedPlayerDataPartialSyncS2CPacket {
        val dirtySet = if (toClient) {
            syncStatus.syncToClientHooks
        } else {
            syncStatus.syncToOthersHooks
        }
        return HookedPlayerDataPartialSyncS2CPacket(
            entityId = player.id,
            dirtyHooks = dirtySet.values.toList(),
        )
    }

    companion object {
        val logger = Hooked.logManager.makeLogger<HookedPlayerData>()
    }
}