package dev.thecodewarrior.hooked.hook.processor

import com.teamwizardry.librarianlib.core.util.kotlin.getOrNull
import dev.thecodewarrior.hooked.HookedMod
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.capability.IHookItem
import dev.thecodewarrior.hooked.hook.type.HookType
import dev.thecodewarrior.hooked.network.SyncHookDataPacket
import dev.thecodewarrior.hooked.network.SyncIndividualHooksPacket
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3d
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.network.PacketDistributor
import top.theillusivec4.curios.api.CuriosApi
import java.util.*

/**
 * Processes hooks on the *logical* server. This is present in both the client and dedicated server environments.
 */
object ServerHookProcessor: CommonHookProcessor() {
    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    fun fireHook(data: HookedPlayerData, pos: Vector3d, direction: Vector3d) {
        if (data.type == HookType.NONE) {
            // they seem to think they can fire hooks
            data.serverState.forceFullSyncToClient = true
        } else {
            val hook = Hook(UUID.randomUUID(), data.type, pos, Hook.State.EXTENDING.ordinal, direction, BlockPos.ZERO)
            data.hooks.add(hook)
            // this will cause a full sync to the client, and a single-hook sync to other clients
            data.serverState.dirtyHooks.add(hook)
        }
    }

    @SubscribeEvent
    fun playerPostTick(e: TickEvent.PlayerTickEvent) {
        if (!isServer(e.player)) return
        if (e.phase != TickEvent.Phase.END) return
        val data = getHookData(e.player) ?: return

        val equippedType = getEquippedHook(e.player)?.type ?: HookType.NONE
        if (data.type != equippedType) {
            data.hooks.clear()
            data.type = equippedType
            data.serverState.forceFullSyncToClient = true
            data.serverState.forceFullSyncToOthers = true
        }

        applyHookMotion(e.player, data)
        data.controller.update(e.player, data.hooks, data.playerJumped)
        data.playerJumped = false

        if (data.serverState.forceFullSyncToClient || data.serverState.dirtyHooks.isNotEmpty()) {
            val serverPlayer = e.player as ServerPlayerEntity // fail-fast
            HookedMod.courier.send(
                PacketDistributor.PLAYER.with { serverPlayer },
                SyncHookDataPacket(
                    e.player.entityId,
                    data.serializeNBT()
                )
            )
        }
        if (data.serverState.forceFullSyncToOthers) {
            HookedMod.courier.send(
                PacketDistributor.TRACKING_ENTITY.with { e.player },
                SyncHookDataPacket(
                    e.player.entityId,
                    data.serializeNBT()
                )
            )
        } else if (data.serverState.dirtyHooks.isNotEmpty()) {
            HookedMod.courier.send(
                PacketDistributor.TRACKING_ENTITY.with { e.player },
                SyncIndividualHooksPacket(
                    e.player.entityId,
                    ArrayList(data.serverState.dirtyHooks)
                )
            )
        }
        data.serverState.forceFullSyncToClient = false
        data.serverState.forceFullSyncToOthers = false
        data.serverState.dirtyHooks.clear()
    }

    override fun onHookStateChange(player: PlayerEntity, data: HookedPlayerData, hook: Hook) {
        data.serverState.dirtyHooks.add(hook)
    }

    private fun getEquippedHook(player: PlayerEntity): IHookItem? {
        return CuriosApi.getCuriosHelper()
            .getCuriosHandler(player).getOrNull()
            ?.getStacksHandler("hooked")?.getOrNull()
            ?.stacks?.getStackInSlot(0)
            ?.getCapability(IHookItem.CAPABILITY)
            ?.getOrNull()
    }

    @SubscribeEvent
    fun track(e: PlayerEvent.StartTracking) {
        if (!isServer(e.player)) return
        val target = e.target
        target.getCapability(HookedPlayerData.CAPABILITY).getOrNull()?.also { data ->
            val serverPlayer = e.player as ServerPlayerEntity // fail-fast
            HookedMod.courier.send(
                PacketDistributor.PLAYER.with { serverPlayer },
                SyncHookDataPacket(
                    target.entityId,
                    data.serializeNBT()
                )
            )
        }
    }

    @SubscribeEvent
    fun join(e: EntityJoinWorldEvent) {
        val serverPlayer = e.entity as? ServerPlayerEntity ?: return
        serverPlayer.getCapability(HookedPlayerData.CAPABILITY).getOrNull()?.also { data ->
            HookedMod.courier.send(
                PacketDistributor.PLAYER.with { serverPlayer },
                SyncHookDataPacket(
                    serverPlayer.entityId,
                    data.serializeNBT()
                )
            )
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun breakSpeed(e: PlayerEvent.BreakSpeed) {
        if(!isServer(e.entity)) return
        fixSpeed(e)
    }

    /**
     * Returns true if the passed player is from the logical server.
     */
    private fun isServer(entity: Entity): Boolean {
        return !entity.world.isRemote
    }

    private val logger = HookedMod.makeLogger<ServerHookProcessor>()
}