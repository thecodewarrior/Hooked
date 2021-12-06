package dev.thecodewarrior.hooked.hook

import com.teamwizardry.librarianlib.core.util.kotlin.getOrNull
import dev.thecodewarrior.hooked.HookedMod
import dev.thecodewarrior.hooked.HookedModStats
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.capability.IHookItem
import dev.thecodewarrior.hooked.network.HookEventsPacket
import dev.thecodewarrior.hooked.network.SyncHookDataPacket
import dev.thecodewarrior.hooked.network.SyncIndividualHooksPacket
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvent
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vec3d
import net.minecraft.world.GameType
import net.minecraft.world.World
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

    fun fireHook(
        player: ServerPlayerEntity,
        data: HookedPlayerData,
        pos: Vec3d,
        direction: Vec3d,
        sneaking: Boolean,
        uuids: List<UUID>
    ) {
        if (data.type == HookType.NONE || player.interactionManager.gameType == GameType.SPECTATOR) {
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
                    BlockPos.ZERO,
                    0
                )
                data.hooks.add(hook)
                // this will cause a full sync to the client, and a single-hook sync to other clients
                data.syncStatus.dirtyHooks.add(hook)
                data.player.addStat(HookedModStats.hooksFired)

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

    @SubscribeEvent
    fun playerPostTick(e: TickEvent.PlayerTickEvent) {
        if (!isServer(e.player)) return
        if (e.phase != TickEvent.Phase.END) return
        val data = getHookData(e.player) ?: return

        val equippedType = getEquippedHook(e.player)?.hookType ?: HookType.NONE
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
            HookedMod.courier.send(
                PacketDistributor.PLAYER.with { e.player as ServerPlayerEntity },
                SyncHookDataPacket(
                    e.player.entityId,
                    data.syncStatus.dirtyHooks.filterTo(ArrayList()) { it.state == Hook.State.REMOVED },
                    data.serializeNBT()
                )
            )
        }
        if (data.syncStatus.forceFullSyncToOthers) {
            HookedMod.courier.send(
                PacketDistributor.TRACKING_ENTITY.with { e.player },
                SyncHookDataPacket(
                    e.player.entityId,
                    data.syncStatus.dirtyHooks.filterTo(ArrayList()) { it.state == Hook.State.REMOVED },
                    data.serializeNBT()
                )
            )
        } else if (data.syncStatus.dirtyHooks.isNotEmpty()) {
            HookedMod.courier.send(
                PacketDistributor.TRACKING_ENTITY.with { e.player },
                SyncIndividualHooksPacket(
                    e.player.entityId,
                    ArrayList(data.syncStatus.dirtyHooks)
                )
            )
        }
        if (data.syncStatus.queuedEvents.isNotEmpty()) {
            HookedMod.courier.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with { e.player },
                HookEventsPacket(
                    e.player.entityId,
                    ArrayList(data.syncStatus.queuedEvents)
                )
            )
            data.syncStatus.queuedEvents.clear()
        }

        data.syncStatus.forceFullSyncToClient = false
        data.syncStatus.forceFullSyncToOthers = false
        data.syncStatus.dirtyHooks.clear()
        data.syncStatus.queuedEvents.clear()
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
            HookedMod.courier.send(
                PacketDistributor.PLAYER.with { e.player as ServerPlayerEntity },
                SyncHookDataPacket(
                    target.entityId,
                    ArrayList(),
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
                    ArrayList(),
                    data.serializeNBT()
                )
            )
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun breakSpeed(e: PlayerEvent.BreakSpeed) {
        if (!isServer(e.entity)) return
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