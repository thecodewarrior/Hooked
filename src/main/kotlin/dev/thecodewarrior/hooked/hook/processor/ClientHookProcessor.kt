package dev.thecodewarrior.hooked.hook.processor

import com.teamwizardry.librarianlib.core.util.Client
import com.teamwizardry.librarianlib.math.dot
import com.teamwizardry.librarianlib.math.minus
import dev.thecodewarrior.hooked.HookedMod
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.hook.type.HookType
import dev.thecodewarrior.hooked.network.FireHookPacket
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.vector.Vector3d
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.SubscribeEvent
import java.util.*
import kotlin.math.acos
import kotlin.math.cos

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

    fun syncHook(data: HookedPlayerData, hook: Hook) {
        if(hook.state == Hook.State.REMOVED) {
            data.hooks.removeIf { it.uuid == hook.uuid }
        }
        val existingIndex = data.hooks.indexOfFirst { it.uuid == hook.uuid }
        if(existingIndex == -1) {
            data.hooks.add(hook)
        } else {
            data.hooks[existingIndex] = hook
        }
    }

    fun fireHook(data: HookedPlayerData, pos: Vector3d, direction: Vector3d, sneaking: Boolean) {
        if (data.type != HookType.NONE) {
            if(sneaking && data.controller.allowIndividualRetraction) {
                for(hook in data.hooks) {
                    if(isPointingAtHook(pos, direction, retractThreshold, hook)) {
                        hook.state = Hook.State.RETRACTING
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

    @SubscribeEvent
    fun playerPostTick(e: TickEvent.PlayerTickEvent) {
        if (!isClient(e.player)) return
        if (e.phase != TickEvent.Phase.END) return
//        HookedMod.proxy.disableAutoJump(player, false)
        val data = getHookData(e.player) ?: return

        applyHookMotion(e.player, data)

        // only apply the controller for our own player
        if(e.player == Client.player) {
            data.controller.update(e.player, data.hooks, data.jumpState)
            data.jumpState = null
        }
    }

    override fun onHookStateChange(player: PlayerEntity, data: HookedPlayerData, hook: Hook) {
        // this exists for server-side syncing
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