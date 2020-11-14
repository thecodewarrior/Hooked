package dev.thecodewarrior.hooked

import net.minecraft.entity.player.PlayerEntity
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn

@OnlyIn(Dist.DEDICATED_SERVER)
object HookedDedicatedServerProxy: HookedProxy {
    override fun disableAutoJump(player: PlayerEntity, disable: Boolean) {
        /* nop */
    }
}