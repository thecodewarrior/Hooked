package thecodewarrior.hooked.client

import com.teamwizardry.librarianlib.features.kotlin.toRl
import com.teamwizardry.librarianlib.features.methodhandles.MethodHandleHelper
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.EntityLivingBase
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.registries.RegistryBuilder
import thecodewarrior.hooked.client.render.HookRenderHandler
import thecodewarrior.hooked.client.render.HookRenderer
import thecodewarrior.hooked.common.CommonProxy
import thecodewarrior.hooked.common.hook.HookController

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

    override fun createRegistries(e: RegistryEvent.NewRegistry) {
        super.createRegistries(e)
        @Suppress("UNCHECKED_CAST")
        HookRenderer.REGISTRY = RegistryBuilder<HookRenderer<HookController>>()
                .setType(HookRenderer::class.java as Class<HookRenderer<HookController>>)
                .setMaxID(256)
                .setName("hooked:hook_renderer".toRl())
                .setDefaultKey("missingno".toRl())
                .create()
    }
}

private var EntityPlayerSP.autoJumpEnabled by MethodHandleHelper.delegateForReadWrite<EntityPlayerSP, Boolean>(EntityPlayerSP::class.java, "autoJumpEnabled", "field_189811_cr")
