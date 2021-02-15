package dev.thecodewarrior.hooked.hook

import com.teamwizardry.librarianlib.core.util.Client
import dev.thecodewarrior.hooked.HookedMod
import dev.thecodewarrior.hooked.HookedModSounds
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.network.FireHookPacket
import dev.thecodewarrior.hooked.network.HookJumpPacket
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.SoundEvent
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.world.World
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.SubscribeEvent
import java.util.*

/**
 * Processes hooks on the *logical* client.
 *
 * The
 */
@OnlyIn(Dist.CLIENT) // fail-fast
object ClientHookProcessor: CommonHookProcessor() {
    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    class Context(override val data: HookedPlayerData): HookProcessorContext {
        override val type: HookType get() = data.type
        override val controller: HookPlayerController get() = data.controller
        override val player: PlayerEntity get() = data.player
        override val world: World get() = data.player.world
        override val hooks: MutableList<Hook> get() = data.hooks

        override fun markDirty(hook: Hook) {
            data.syncStatus.dirtyHooks.add(hook)
        }

        private val playedSounds = mutableSetOf<SoundEvent>()

        override fun playFeedbackSound(sound: SoundEvent, volume: Float, pitch: Float) {
            if(!playedSounds.add(sound))
                return
            player.playSound(sound, volume, pitch)
        }

        override fun playWorldSound(sound: SoundEvent, pos: Vector3d, volume: Float, pitch: Float) {
            // world sounds are played on the server
        }
    }

    fun syncHook(data: HookedPlayerData, hook: Hook) {
        if(hook.state == Hook.State.REMOVED) {
            data.hooks.removeIf { it.uuid == hook.uuid }
        } else {
            val existingIndex = data.hooks.indexOfFirst { it.uuid == hook.uuid }
            if (existingIndex == -1) {
                data.hooks.add(hook)
            } else {
                data.hooks[existingIndex] = hook
            }
        }
    }

    fun fireHook(data: HookedPlayerData, pos: Vector3d, direction: Vector3d, sneaking: Boolean) {
        if (data.type != HookType.NONE) {
            data.controller.fireHooks(Context(data), pos, direction, sneaking) { hookPos, hookDirection ->
                val hook = Hook(
                    UUID.randomUUID(),
                    data.type,
                    hookPos,
                    Hook.State.EXTENDING.ordinal,
                    hookDirection,
                    BlockPos.ZERO,
                    0
                )
                data.hooks.add(hook)

                hook
            }

            HookedMod.courier.sendToServer(
                FireHookPacket(
                    pos,
                    direction,
                    sneaking
                )
            )
        }
    }

    fun jump(data: HookedPlayerData, doubleJump: Boolean, sneaking: Boolean) {
        if (data.type != HookType.NONE) {
            data.controller.jump(Context(data), doubleJump, sneaking)

            HookedMod.courier.sendToServer(
                HookJumpPacket(doubleJump, sneaking)
            )
        }
    }

    @SubscribeEvent
    fun playerPostTick(e: TickEvent.PlayerTickEvent) {
        if (!isClient(e.player)) return
        if (e.phase != TickEvent.Phase.END) return
        val data = getHookData(e.player) ?: return

        applyHookMotion(Context(data))

        if(e.player == Client.player) {
            data.controller.update(Context(data))
        }

        data.syncStatus.dirtyHooks.clear()
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun breakSpeed(e: PlayerEvent.BreakSpeed) {
        if(!isClient(e.entity)) return
        fixSpeed(e)
    }

    /**
     * Returns true if the passed player is from the logical client.
     */
    private fun isClient(entity: Entity): Boolean {
        return entity.world.isRemote
    }

    private val logger = HookedMod.makeLogger<ClientHookProcessor>()
}