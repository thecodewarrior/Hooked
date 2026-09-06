package dev.thecodewarrior.hooked.integration

import dev.ryanhcode.sable.Sable
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer
import dev.ryanhcode.sable.companion.SubLevelAccess
import dev.ryanhcode.sable.mixinterface.entity.entity_sublevel_collision.EntityMovementExtension
import net.minecraft.entity.player.PlayerEntity

// this is all wrapped in an exception handler so internal api changes shouldn't completely crash the game
@Suppress("UnstableApiUsage")
object SableSubLevelTrackingManager : SubLevelTrackingManager {
    override fun setTrackingSubLevel(
        player: PlayerEntity,
        subLevelAccess: SubLevelAccess?
    ) {
        if (player !is EntityMovementExtension) return
        val subLevel = subLevelAccess?.let {
            SubLevelContainer.getContainer(player.world)?.getSubLevel(it.uniqueId)
        }

        player.`sable$setTrackingSubLevel`(subLevel)
    }
}