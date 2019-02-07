package games.thecodewarrior.hooked.client

import com.teamwizardry.librarianlib.features.kotlin.toRl
import com.teamwizardry.librarianlib.features.methodhandles.MethodHandleHelper
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.EntityLivingBase
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.registries.RegistryBuilder
import games.thecodewarrior.hooked.client.render.BasicHookRenderer
import games.thecodewarrior.hooked.client.render.FlightHookRenderer
import games.thecodewarrior.hooked.client.render.HookRenderHandler
import games.thecodewarrior.hooked.client.render.HookRenderer
import games.thecodewarrior.hooked.common.CommonProxy
import games.thecodewarrior.hooked.common.config.FlightHookAppearance
import games.thecodewarrior.hooked.common.config.HookEntry
import games.thecodewarrior.hooked.common.config.HookTypesConfig
import games.thecodewarrior.hooked.common.hook.HookType

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
        HookRenderer.REGISTRY.register(HookRenderer.missingno)
        HookRenderer.REGISTRY.registerAll(*HookTypesConfig.entries.map {
            it.appearance.createRenderer(hookCache[it.name]!!)
        }.toTypedArray())
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
