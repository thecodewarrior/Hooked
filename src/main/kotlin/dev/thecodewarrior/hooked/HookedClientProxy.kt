package dev.thecodewarrior.hooked

import com.teamwizardry.librarianlib.core.util.Client
import com.teamwizardry.librarianlib.core.util.mapSrgName
import ll.dev.thecodewarrior.mirror.Mirror
import net.minecraft.client.entity.player.ClientPlayerEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn

@OnlyIn(Dist.CLIENT)
object HookedClientProxy: HookedProxy {
    private val autoJumpEnabled = Mirror.reflectClass<ClientPlayerEntity>().getDeclaredField(mapSrgName("field_189811_cr"))

    override fun disableAutoJump(player: PlayerEntity, disable: Boolean) {
        if(player !is ClientPlayerEntity)
            return
        autoJumpEnabled.setFast(player, disable && Client.minecraft.gameSettings.autoJump)
    }
}