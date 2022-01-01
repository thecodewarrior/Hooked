package dev.thecodewarrior.hooked.capability

import com.teamwizardry.librarianlib.core.util.kotlin.NbtBuilder
import com.teamwizardry.librarianlib.core.util.vec
import dev.onyxstudios.cca.api.v3.component.Component
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import dev.thecodewarrior.hooked.Hooked
import dev.thecodewarrior.hooked.hook.Hook
import dev.thecodewarrior.hooked.hook.HookEvent
import dev.thecodewarrior.hooked.hook.HookPlayerController
import dev.thecodewarrior.hooked.hook.HookType
import dev.thecodewarrior.hooked.util.CircularArray
import dev.thecodewarrior.hooked.util.CircularMap
import net.fabricmc.fabric.api.util.NbtType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import java.util.*
import kotlin.math.max

/**
 * A capability holding all the data and logic required for actually running the hooks.
 */
class HookedPlayerData(val player: PlayerEntity) : Component, AutoSyncedComponent {
    var type: HookType = HookType.NONE
        set(value) {
            if (value != field) {
                controller.remove()
                controller = value.createController(player)
            }
            field = value
        }

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
        val queuedEvents: MutableList<HookEvent> = mutableListOf()
        /**
         * Hooks queued to be synced to clients
         */
        val dirtyHooks: MutableMap<Int, Hook> = mutableMapOf()
        var forceFullSyncToClient: Boolean = false
        var forceFullSyncToOthers: Boolean = false

        /**
         * Used on the client to store recently fired events. If the client receives an identical event from the server,
         * that event will be ignored
         */
        val recentEvents: CircularArray<HookEvent> = CircularArray(25)

        /**
         * Used on the client to store references to recently removed hooks in case an event references them
         */
        val recentHooks: MutableMap<Int, Hook> = CircularMap(25)

        fun addRecentHook(hook: Hook) {
            recentHooks[hook.id] = hook
        }
        fun addRecentHooks(hooks: Collection<Hook>) {
            hooks.forEach { addRecentHook(it) }
        }
    }

    var syncStatus: SyncStatus = SyncStatus()

    override fun writeToNbt(tag: NbtCompound) {
        tag.putString("Type", Hooked.hookRegistry.getId(type).toString())
        tag.put("Hooks", NbtList().also { it.addAll(hooks.values.map(::writeHook)) })
    }

    override fun readFromNbt(tag: NbtCompound) {
        type = Hooked.hookRegistry.get(Identifier(tag.getString("Type")))
        hooks = tag.getList("Hooks", NbtType.COMPOUND).map(::readHook).associateByTo(TreeMap()) { it.id }
        syncStatus.forceFullSyncToClient = true
        syncStatus.forceFullSyncToOthers = true
    }

    private fun writeHook(hook: Hook): NbtCompound {
        return NbtBuilder.compound {
            "Id" %= int(hook.id)
            "X" %= double(hook.pos.x)
            "Y" %= double(hook.pos.y)
            "Z" %= double(hook.pos.z)
            "State" %= string(hook.state.name)
            "Block" %= compound {
                "X" %= int(hook.block.x)
                "Y" %= int(hook.block.y)
                "Z" %= int(hook.block.z)
            }
            "Tag" %= int(hook.tag)
        }
    }

    private fun readHook(tag: NbtElement): Hook {
        tag as NbtCompound

        val posTag = tag.getCompound("Position")
        val blockTag = tag.getCompound("Block")
        return Hook(
            tag.getInt("Id"),
            this.type,
            vec(tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z")),
            tag.getFloat("Pitch"),
            tag.getFloat("Yaw"),
            Hook.State.valueOf(tag.getString("State")),
            BlockPos(blockTag.getInt("X"), blockTag.getInt("Y"), blockTag.getInt("Z")),
            tag.getInt("Tag")
        )
    }

    fun updateSync() {
        Hooked.Components.HOOK_DATA.sync(player, ::writeUpdatePacket)
    }

    override fun shouldSyncWith(player: ServerPlayerEntity): Boolean {
        return if(player == this.player) {
            syncStatus.forceFullSyncToClient || syncStatus.dirtyHooks.isNotEmpty()
        } else {
            syncStatus.forceFullSyncToOthers || syncStatus.dirtyHooks.isNotEmpty()
        }
    }

    private enum class SyncType {
        FULL, DIRTY
    }

    override fun writeSyncPacket(buf: PacketByteBuf, recipient: ServerPlayerEntity) {
        writeFullPacket(buf)
    }

    private fun writeUpdatePacket(buf: PacketByteBuf, recipient: ServerPlayerEntity) {
        if(recipient == this.player || syncStatus.forceFullSyncToOthers) {
            writeFullPacket(buf)
        } else {
            writeDirtyPacket(buf)
        }
    }

    override fun applySyncPacket(buf: PacketByteBuf) {
        when(SyncType.values()[buf.readVarInt()]) {
            SyncType.FULL -> applyFullPacket(buf)
            SyncType.DIRTY -> applyDirtyPacket(buf)
        }
    }

    private fun writeFullPacket(buf: PacketByteBuf) {
        buf.writeVarInt(SyncType.FULL.ordinal)

        // type
        buf.writeIdentifier(Hooked.hookRegistry.getId(type))

        // hooks
        buf.writeCollection(hooks.values, ::writeHook)
    }

    private fun applyFullPacket(buf: PacketByteBuf) {
        // type
        type = Hooked.hookRegistry.get(buf.readIdentifier())

        // hooks
        val newHooks = buf.readCollection({ mutableListOf() }, ::readHook).associateByTo(TreeMap()) { it.id }
        syncStatus.recentHooks.putAll(hooks.filterKeys { it !in newHooks })
        hooks = newHooks
    }

    private fun writeDirtyPacket(buf: PacketByteBuf) {
        buf.writeVarInt(SyncType.DIRTY.ordinal)
        buf.writeCollection(syncStatus.dirtyHooks.values, ::writeHook)
    }

    private fun applyDirtyPacket(buf: PacketByteBuf) {
        val dirtyHooks = buf.readCollection({ mutableListOf() }, ::readHook)
        for(hook in dirtyHooks) {
            if (hook.state == Hook.State.REMOVED) {
                hooks.remove(hook.id)
                syncStatus.addRecentHook(hook)
            } else {
                hooks[hook.id] = hook
            }
        }
    }

    private fun writeHook(buf: PacketByteBuf, hook: Hook) {
        buf.writeVarInt(hook.id)
        // no hook.type - this set to this.type when reading
        buf.writeDouble(hook.pos.x)
        buf.writeDouble(hook.pos.y)
        buf.writeDouble(hook.pos.z)
        buf.writeFloat(hook.pitch)
        buf.writeFloat(hook.yaw)
        buf.writeVarInt(hook.state.ordinal)
        buf.writeBlockPos(hook.block)
        buf.writeVarInt(hook.tag)
    }

    private fun readHook(buf: PacketByteBuf): Hook {
        return Hook(
            buf.readVarInt(),
            this.type,
            vec(buf.readDouble(), buf.readDouble(), buf.readDouble()),
            buf.readFloat(),
            buf.readFloat(),
            Hook.State.values()[buf.readVarInt()],
            buf.readBlockPos(),
            buf.readVarInt()
        )
    }
}