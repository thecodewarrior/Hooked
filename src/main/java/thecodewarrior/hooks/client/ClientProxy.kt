package thecodewarrior.hooks.client

import com.teamwizardry.librarianlib.common.util.MethodHandleHelper
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.EntityLivingBase
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import thecodewarrior.hooks.common.CommonProxy

/**
 * Created by TheCodeWarrior
 */
class ClientProxy : CommonProxy() {
    override fun pre(e: FMLPreInitializationEvent) {
        super.pre(e)

        KeyBinds
        HookRenderHandler
    }

    override fun setAutoJump(entityLiving: EntityLivingBase, value: Boolean) {
        if(entityLiving is EntityPlayerSP) {
            entityLiving.autoJumpEnabled = value && Minecraft.getMinecraft().gameSettings.autoJump
        }
    }
}

private var EntityPlayerSP.autoJumpEnabled by MethodHandleHelper.delegateForReadWrite<EntityPlayerSP, Boolean>(EntityPlayerSP::class.java, "autoJumpEnabled", "field_189811_cr")
