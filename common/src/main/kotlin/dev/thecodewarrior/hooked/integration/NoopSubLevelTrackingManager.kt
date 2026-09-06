package dev.thecodewarrior.hooked.integration

import dev.ryanhcode.sable.companion.SubLevelAccess
import net.minecraft.entity.player.PlayerEntity

object NoopSubLevelTrackingManager : SubLevelTrackingManager {
    override fun setTrackingSubLevel(player: PlayerEntity, subLevelAccess: SubLevelAccess?) {
        // nop
    }

    override fun isFrozenToSubLevel(player: PlayerEntity): Boolean = false
}