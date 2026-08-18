package dev.thecodewarrior.hooked.hook

import dev.thecodewarrior.hooked.Hooked
import net.minecraft.entity.player.PlayerEntity
import java.util.Collections
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

interface HookProcessor {
    fun tick(player: PlayerEntity)

    fun isHookActive(player: PlayerEntity, reason: HookActiveReason): Boolean

    fun safeTick(player: PlayerEntity) {
        try {
            tick(player)
            loggedCrashes.remove(player.uuid)
        } catch (e: Exception) {
            if (loggedCrashes.add(player.uuid)) {
                logger.warn("Exception ticking player ${player.uuid}", e)
            }
        }
    }

    companion object {
        private val loggedCrashes: MutableSet<UUID> = Collections.newSetFromMap(ConcurrentHashMap())
        private val logger = Hooked.logManager.makeLogger<HookProcessor>()
    }
}

enum class HookActiveReason {
    TRAVEL_STATS,
    CANCEL_ELYTRA,
    ELYTRA_DAMAGE,
    DISABLE_CLIP_AT_LEDGE,
    DISABLE_AUTO_JUMP,
    BREAK_SPEED,
    MOVED_WRONGLY
}

object NullHookProcessor: HookProcessor {
    override fun tick(player: PlayerEntity) {
    }

    override fun isHookActive(player: PlayerEntity, reason: HookActiveReason): Boolean = false
}
