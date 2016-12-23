package thecodewarrior.hooks

import com.teamwizardry.librarianlib.common.base.ModCreativeTab
import com.teamwizardry.librarianlib.common.core.LoggerBase
import net.minecraft.item.Item
import net.minecraft.launchwrapper.Launch
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import thecodewarrior.hooks.common.CommonProxy
import thecodewarrior.hooks.common.items.ModItems

@Mod(modid = HooksMod.MODID, version = HooksMod.VERSION, name = HooksMod.MODNAME)
class HooksMod {

    @Mod.EventHandler
    fun preInit(e: FMLPreInitializationEvent) {
        PROXY.pre(e)
    }

    @Mod.EventHandler
    fun init(e: FMLInitializationEvent) {
        PROXY.init(e)
    }

    @Mod.EventHandler
    fun postInit(e: FMLPostInitializationEvent) {
        PROXY.post(e)
    }

    companion object {

        const val MODID = "hooks"
        const val MODNAME = "Hooks"
        const val VERSION = "0.0.0"
        const val CLIENT = "thecodewarrior.hooks.client.ClientProxy"
        const val SERVER = "thecodewarrior.hooks.client.CommonProxy"

        @JvmStatic
        @SidedProxy(clientSide = CLIENT, serverSide = SERVER)
        lateinit var PROXY: CommonProxy

        @JvmField
        val DEV_ENVIRONMENT = Launch.blackboard["fml.deobfuscatedEnvironment"] as Boolean

        val creativeTab = object : ModCreativeTab() {
            override fun getTabIconItem(): Item {
                return ModItems.hook
            }

            init {
                this.registerDefaultTab()
            }
        }
    }

}

object HookLog : LoggerBase("Hooks")
