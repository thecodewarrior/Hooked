package games.thecodewarrior.hooked.common

import com.teamwizardry.librarianlib.features.network.PacketHandler
import games.thecodewarrior.hooked.common.config.ConfigResourcePack
import games.thecodewarrior.hooked.common.config.HookTypes
import games.thecodewarrior.hooked.common.config.inject
import games.thecodewarrior.hooked.common.hook.BasicHookType
import games.thecodewarrior.hooked.common.hook.FlightHookType
import games.thecodewarrior.hooked.common.hook.HookType
import games.thecodewarrior.hooked.common.items.ModItems
import games.thecodewarrior.hooked.common.network.PacketFireHook
import games.thecodewarrior.hooked.common.network.PacketHookCapSync
import games.thecodewarrior.hooked.common.network.PacketHookedJump
import games.thecodewarrior.hooked.common.network.PacketMove
import games.thecodewarrior.hooked.common.network.PacketRetractHook
import net.minecraft.entity.EntityLivingBase
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.relauncher.Side

/**
 * Created by TheCodeWarrior
 */
open class CommonProxy {
    init {
        @Suppress("LeakingThis")
        MinecraftForge.EVENT_BUS.register(this)
    }

    open fun pre(e: FMLPreInitializationEvent) {
        val configFile = e.suggestedConfigurationFile
        val configName = configFile.nameWithoutExtension

        ModItems
        network()
        HookTickHandler

        HookType.register(BasicHookType::class.java, "basic")
        HookType.register(FlightHookType::class.java, "flight")

        val hooksFile = configFile.resolveSibling("$configName.types.json")
        if(!hooksFile.exists()) {
            val default = javaClass.getResourceAsStream("/assets/hooked/default_config/hooked.types.json").bufferedReader().readText()
            hooksFile.writeText(default)
            HookTypes.read(default)
        } else {
            HookTypes.read(hooksFile.readText())
        }
    }

    open fun init(e: FMLInitializationEvent) {
    }

    open fun post(e: FMLPostInitializationEvent) {
    }

    fun network() {
        PacketHandler.register(PacketFireHook::class.java, Side.SERVER)
        PacketHandler.register(PacketRetractHook::class.java, Side.SERVER)
        PacketHandler.register(PacketHookedJump::class.java, Side.SERVER)

        PacketHandler.register(PacketHookCapSync::class.java, Side.CLIENT)
        PacketHandler.register(PacketMove::class.java, Side.SERVER)
    }

    open fun  setAutoJump(entityLiving: EntityLivingBase, value: Boolean) {
    }
}
