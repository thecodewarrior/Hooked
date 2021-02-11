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

    private val soundQueue = mutableSetOf<SoundEvent>()

    class Delegate(val data: HookedPlayerData): HookControllerDelegate {
        override val player: PlayerEntity
            get() = data.player
        override val hooks: List<Hook>
            get() = data.hooks

        override fun markDirty(hook: Hook) {
            // nop on the client
        }

        override fun enqueueSound(sound: SoundEvent) {
            soundQueue.add(sound)
        }
    }


    fun playSoundQueue(player: PlayerEntity) {
        for(sound in soundQueue) {
            player.playSound(sound, 1f, 1f)
        }
        soundQueue.clear()
    }

    override fun enqueueSound(sound: SoundEvent) {
        soundQueue.add(sound)
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
            if(sneaking && data.type.allowIndividualRetraction) {
                for(hook in data.hooks) {
                    if(isPointingAtHook(pos, direction, retractThreshold, hook)) {
                        hook.state = Hook.State.RETRACTING
                        data.serverState.dirtyHooks.add(hook)
                        enqueueSound(HookedModSounds.retractHook)
                    }
                }
            } else {
                data.hooks.add(
                    Hook(
                        UUID.randomUUID(),
                        data.type,
                        pos,
                        Hook.State.EXTENDING.ordinal,
                        direction,
                        BlockPos.ZERO
                    )
                )
                enqueueSound(HookedModSounds.fireHook)
            }

            HookedMod.courier.sendToServer(
                FireHookPacket(
                    pos,
                    direction,
                    sneaking
                )
            )
            playSoundQueue(data.player)
        }
    }

    fun jump(data: HookedPlayerData, doubleJump: Boolean, sneaking: Boolean) {
        if (data.type != HookType.NONE) {
            data.controller.jump(Delegate(data), doubleJump, sneaking)
            playSoundQueue(data.player)

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

        applyHookMotion(e.player, data)

        if(e.player == Client.player) {
            data.controller.update(Delegate(data))
            playSoundQueue(data.player)
        } else {
            soundQueue.clear()
        }

        data.serverState.dirtyHooks.clear()
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