package dev.thecodewarrior.hooked.integration

import dev.architectury.platform.Platform
import dev.ryanhcode.sable.companion.SubLevelAccess
import dev.thecodewarrior.hooked.Hooked
import net.minecraft.entity.player.PlayerEntity
import java.util.Collections
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

interface SubLevelTrackingManager {
    fun setTrackingSubLevel(player: PlayerEntity, subLevelAccess: SubLevelAccess?)
    fun isFrozenToSubLevel(player: PlayerEntity): Boolean

    companion object {
        val managerInstance: SubLevelTrackingManager by lazy {
            if (Platform.isModLoaded("sable")) SableSubLevelTrackingManager else NoopSubLevelTrackingManager
        }

        fun setTrackingSubLevel(player: PlayerEntity, subLevelAccess: SubLevelAccess?) {
            try {
                managerInstance.setTrackingSubLevel(player, subLevelAccess)
                loggedCrashes.remove(player.uuid)
            } catch (e: Exception) {
                if (loggedCrashes.add(player.uuid)) {
                    logger.warn("Exception setting tracking sublevel for player ${player.uuid}", e)
                }
            }
        }

        fun isFrozenToSubLevel(player: PlayerEntity): Boolean {
            try {
                val isFrozen = managerInstance.isFrozenToSubLevel(player)
                loggedCrashes.remove(player.uuid)
                return isFrozen
            } catch (e: Exception) {
                if (loggedCrashes.add(player.uuid)) {
                    logger.warn("Exception checking player sublevel freeze state for player ${player.uuid}", e)
                }
                return false
            }
        }

        private val loggedCrashes: MutableSet<UUID> = Collections.newSetFromMap(ConcurrentHashMap())
        private val logger = Hooked.logManager.makeLogger<SubLevelTrackingManager>()
    }
}