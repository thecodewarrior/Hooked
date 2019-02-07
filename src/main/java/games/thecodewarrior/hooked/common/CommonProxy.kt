package games.thecodewarrior.hooked.common

import com.teamwizardry.librarianlib.features.kotlin.toRl
import com.teamwizardry.librarianlib.features.network.PacketHandler
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.registries.RegistryBuilder
import games.thecodewarrior.hooked.common.config.HookTypesConfig
import games.thecodewarrior.hooked.common.hook.HookType
import games.thecodewarrior.hooked.common.items.ModItems
import games.thecodewarrior.hooked.common.network.*

/**
 * Created by TheCodeWarrior
 */
open class CommonProxy {
    init {
        @Suppress("LeakingThis")
        MinecraftForge.EVENT_BUS.register(this)
    }

    protected var hookCache = mapOf<ResourceLocation, HookType>()

    open fun pre(e: FMLPreInitializationEvent) {
        ModItems
        network()
        HookTickHandler


        val hooksFile = e.suggestedConfigurationFile.resolveSibling(e.suggestedConfigurationFile.nameWithoutExtension + ".types.json")
        if(!hooksFile.exists()) {
            val default = javaClass.getResourceAsStream("/assets/hooked/defaultConfig.json").bufferedReader().readText()
            hooksFile.writeText(default)
            HookTypesConfig.read(default)
        } else {
            HookTypesConfig.read(hooksFile.readText())
        }
        hookCache = HookTypesConfig.entries.associate { it.name to it.behavior.createType(it.name) }
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

    @SubscribeEvent
    fun registerHooks(e: RegistryEvent.Register<HookType>) {
        HookType.REGISTRY.register(HookType.missingno)
        HookType.REGISTRY.registerAll(*hookCache.values.toTypedArray())
    }

    @SubscribeEvent
    open fun createRegistries(e: RegistryEvent.NewRegistry) {
        HookType.REGISTRY = RegistryBuilder<HookType>()
            .setType(HookType::class.java)
            .setMaxID(256)
            .setName("hooked:hook_type".toRl())
            .setDefaultKey("hooked:missingno".toRl())
            .create()
    }
}
