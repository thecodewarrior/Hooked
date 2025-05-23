package dev.thecodewarrior.hooked.hook

import net.minecraft.entity.player.PlayerEntity

interface HookProcessor {
    fun tick(player: PlayerEntity)

    fun isHookActive(player: PlayerEntity, reason: HookActiveReason): Boolean
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
