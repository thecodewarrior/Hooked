package thecodewarrior.hooked.client

import com.teamwizardry.librarianlib.features.kotlin.toRl
import com.teamwizardry.librarianlib.features.methodhandles.MethodHandleHelper
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.EntityLivingBase
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.registries.RegistryBuilder
import thecodewarrior.hooked.client.render.BasicHookRenderer
import thecodewarrior.hooked.client.render.FlightHookRenderer
import thecodewarrior.hooked.client.render.HookRenderHandler
import thecodewarrior.hooked.client.render.HookRenderer
import thecodewarrior.hooked.common.CommonProxy
import thecodewarrior.hooked.common.hook.HookController
import thecodewarrior.hooked.common.hook.HookTypes

/**
 * Created by TheCodeWarrior
 */
class ClientProxy : CommonProxy() {
    init {
        @Suppress("LeakingThis")
        MinecraftForge.EVENT_BUS.register(this)
    }

    override fun pre(e: FMLPreInitializationEvent) {
        super.pre(e)

        KeyBinds
        HookRenderHandler

    }

    @SubscribeEvent
    fun registerRenderers(e: RegistryEvent.Register<HookRenderer>) {
        HookRenderer.REGISTRY.registerAll(
                BasicHookRenderer(HookTypes.missingno, 0.0),
                BasicHookRenderer(HookTypes.wood, 0.0),
                BasicHookRenderer(HookTypes.iron, 0.0),
                BasicHookRenderer(HookTypes.diamond, 0.0),
                FlightHookRenderer(HookTypes.red, 0.0),
                BasicHookRenderer(HookTypes.ender, 0.0)
        )
    }

    override fun setAutoJump(entityLiving: EntityLivingBase, value: Boolean) {
        if(entityLiving is EntityPlayerSP) {
            entityLiving.autoJumpEnabled = value && Minecraft.getMinecraft().gameSettings.autoJump
        }
    }

    override fun createRegistries(e: RegistryEvent.NewRegistry) {
        super.createRegistries(e)
        HookRenderer.REGISTRY = RegistryBuilder<HookRenderer>()
                .setType(HookRenderer::class.java)
                .setMaxID(256)
                .setName("hooked:hook_renderer".toRl())
                .setDefaultKey("missingno".toRl())
                .create()
    }
}

private var EntityPlayerSP.autoJumpEnabled by MethodHandleHelper.delegateForReadWrite<EntityPlayerSP, Boolean>(EntityPlayerSP::class.java, "autoJumpEnabled", "field_189811_cr")
