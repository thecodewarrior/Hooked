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

        private val loggedCrashes: MutableSet<UUID> = Collections.newSetFromMap(ConcurrentHashMap())
        private val logger = Hooked.logManager.makeLogger<SubLevelTrackingManager>()
    }
}