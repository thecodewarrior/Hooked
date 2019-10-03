package games.thecodewarrior.hooked.client

import com.teamwizardry.librarianlib.features.methodhandles.MethodHandleHelper
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.EntityLivingBase
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import games.thecodewarrior.hooked.client.render.HookRenderHandler
import games.thecodewarrior.hooked.common.CommonProxy
import games.thecodewarrior.hooked.common.config.ConfigResourcePack
import games.thecodewarrior.hooked.common.config.inject

/**
 * Created by TheCodeWarrior
 */
class ClientProxy : CommonProxy() {
    init {
        @Suppress("LeakingThis")
        MinecraftForge.EVENT_BUS.register(this)
    }

    val configResources = ConfigResourcePack("hooked_extra", "hooked").inject()

    override fun pre(e: FMLPreInitializationEvent) {
        val configFile = e.suggestedConfigurationFile
        val configName = configFile.nameWithoutExtension

        configResources.directory = configFile.resolveSibling("$configName.resources")
        addDefaultResources()

        super.pre(e)

        KeyBinds
        HookRenderHandler
    }

    override fun setAutoJump(entityLiving: EntityLivingBase, value: Boolean) {
        if(entityLiving is EntityPlayerSP) {
            entityLiving.autoJumpEnabled = value && Minecraft.getMinecraft().gameSettings.autoJump
        }
    }

    fun addDefaultResources() {
        val files = listOf(
            "/",
            "models/hook/example.json",
            "models/item/hook_example.json",
            "README/hook_model_texture.png",
            "README/README.md",
            "textures/hooks/example/chain1.png",
            "textures/hooks/example/chain2.png",
            "textures/hooks/example/hook.png",
            "textures/items/hook_example.png"
        )
        files.forEach {
            if(it.endsWith("/"))
                configResources.addDir("assets/hooked/$it")
            else
                configResources.addDefault("assets/hooked/$it", javaClass.getResourceAsStream("/assets/hooked/default_config/hooked.resources/$it"))
        }
    }
}

private var EntityPlayerSP.autoJumpEnabled by MethodHandleHelper.delegateForReadWrite<EntityPlayerSP, Boolean>(EntityPlayerSP::class.java, "autoJumpEnabled", "field_189811_cr")
