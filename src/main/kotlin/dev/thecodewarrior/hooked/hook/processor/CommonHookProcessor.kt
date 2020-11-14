package dev.thecodewarrior.hooked.hook.processor

import com.teamwizardry.librarianlib.core.util.kotlin.getOrNull
import com.teamwizardry.librarianlib.core.util.kotlin.inconceivable
import com.teamwizardry.librarianlib.etcetera.Raycaster
import com.teamwizardry.librarianlib.math.*
import dev.thecodewarrior.hooked.HookedMod
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.capability.IHookItem
import dev.thecodewarrior.hooked.hook.type.HookType
import dev.thecodewarrior.hooked.util.getWaistPos
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import top.theillusivec4.curios.api.CuriosAPI
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

object CommonHookProcessor {
    private val raycaster = Raycaster()

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    fun getHookData(player: PlayerEntity): HookedPlayerData? {
        return player.getCapability(HookedPlayerData.CAPABILITY).getOrNull()
    }

    fun fireHook(data: HookedPlayerData, uuid: UUID, pos: Vec3d, direction: Vec3d) {
        if(data.type != HookType.NONE) {
            data.hooks.add(Hook(uuid, data.type, pos, Hook.State.EXTENDING.ordinal, direction, BlockPos.ZERO))
        }
        data.markForSync()
    }

    fun getEquippedHook(player: PlayerEntity): IHookItem? {
        return CuriosAPI.getCuriosHandler(player).getOrNull()
            ?.getStackInSlot("hooked", 0)
            ?.getCapability(IHookItem.CAPABILITY)
            ?.getOrNull()
    }

    @SubscribeEvent
    fun playerPostTick(e: TickEvent.PlayerTickEvent) {
        if (e.phase != TickEvent.Phase.END) return
        val player = e.player
        HookedMod.proxy.disableAutoJump(player, false)
        val data = getHookData(player) ?: return

        val equippedType = getEquippedHook(player)?.type ?: HookType.NONE
        if (data.type != equippedType) {
            data.hooks.clear()
            data.type = equippedType
            data.markForSync()
        }

        removeNaN(player, data)
        removeAbsurdLength(player, data)
        removeDuplicates(data)

        updateHooks(player, data)

        data.controller.update(player, data.hooks)
    }

    private fun removeNaN(player: PlayerEntity, data: HookedPlayerData) {
        val iter = data.hooks.iterator()
        for (hook in iter) {
            if (!(hook.pos.x.isFinite() && hook.pos.y.isFinite() && hook.pos.z.isFinite())) {
                iter.remove()
                logger.error("Removing hook $hook that had an infinite or NaN position from player ${player.uniqueID}")
                data.markForSync()
            }
        }
    }

    private fun updateHooks(player: PlayerEntity, data: HookedPlayerData) {
        updateExtending(player, data)
        updatePlanted(player, data)
        updateRetracting(player, data)
    }

    private fun updateExtending(player: PlayerEntity, data: HookedPlayerData) {
        data.hooks.forEach { hook ->
            if(hook.state != Hook.State.EXTENDING) return@forEach

            val distanceLeft = data.type.range - (hook.pos - player.getWaistPos()).length()

            val tip = hook.tipPos
            val castDistance = min(data.type.speed, distanceLeft)

            raycaster.cast(
                player.world,
                Raycaster.BlockMode.COLLISION,
                tip.x, tip.y, tip.z,
                tip.x + hook.direction.x * castDistance,
                tip.y + hook.direction.y * castDistance,
                tip.z + hook.direction.z * castDistance
            )

            hook.pos += hook.direction * (castDistance * raycaster.fraction)

            when (raycaster.hitType) {
                Raycaster.HitType.BLOCK -> {
                    // if we hit a block, plant in it
                    hook.state = Hook.State.PLANTED
                    hook.block = block(raycaster.blockX, raycaster.blockY, raycaster.blockZ)
                    data.markForSync()
                }
                Raycaster.HitType.NONE -> {
                    // if we reached max extension, transition to the retracting state
                    if (distanceLeft <= data.type.speed) {
                        hook.state = Hook.State.RETRACTING
                        data.markForSync()
                    }
                }
                else -> inconceivable("Raycast only included blocks but returned non-block hit type ${raycaster.hitType}")
            }
            raycaster.reset()
        }
    }

    private fun updatePlanted(player: PlayerEntity, data: HookedPlayerData) {
        // a bit of wiggle room before a hook breaks off.
        val breakEpsilon: Double = 1 / 16.0

        data.hooks.forEach { hook ->
            if(hook.state != Hook.State.PLANTED) return@forEach

            if (
                hook.pos.squareDistanceTo(player.getWaistPos()) > (data.type.range + breakEpsilon).pow(2) ||
                player.world.isAirBlock(hook.block)
            ) {
                hook.state = Hook.State.RETRACTING
                data.markForSync()
            }
        }
        var plantedCount = 0
        // count from the end of the list, removing everything after the threshold
        val iter = data.hooks.asReversed().iterator()
        for(hook in iter) {
            if (hook.state == Hook.State.PLANTED) {
                plantedCount++
                if(plantedCount > data.type.count) {
                    iter.remove()
                    data.markForSync()
                }
            }
        }
    }

    private fun updateRetracting(player: PlayerEntity, data: HookedPlayerData) {
        val iterator = data.hooks.iterator()
        for (hook in iterator) {
            if(hook.state != Hook.State.RETRACTING) continue
            val delta = hook.pos - player.getWaistPos()
            val distance = delta.length()

            if (distance < max(data.type.speed, 1.0)) {
                iterator.remove()
                data.markForSync()
            } else {
                val direction = delta / distance
                hook.pos -= direction * min(data.type.speed, distance)
                hook.direction = direction
            }
        }
    }

    private fun removeAbsurdLength(player: PlayerEntity, data: HookedPlayerData) {
        val threshold = 1024
        val waist = player.getWaistPos()
        val iter = data.hooks.iterator()
        for(hook in iter) {
            val distance = waist.distanceTo(hook.pos)
            if(distance > threshold) {
                logger.warn("Hook was an absurd distance ($distance) from player. Removing $hook from $player")
                iter.remove()
            }
        }
    }

    private fun removeDuplicates(data: HookedPlayerData) {
        val uuids = mutableSetOf<UUID>()
        // iterate from the end, thus removing from the beginning
        data.hooks.asReversed().removeIf { !uuids.add(it.uuid) }
    }

    private val logger = HookedMod.makeLogger<CommonHookProcessor>()
}