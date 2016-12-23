package thecodewarrior.hooks.common

import com.teamwizardry.librarianlib.common.network.PacketHandler
import net.minecraft.entity.EntityLivingBase
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.relauncher.Side
import thecodewarrior.hooks.common.items.ModItems
import thecodewarrior.hooks.common.network.PacketFireHook
import thecodewarrior.hooks.common.network.PacketRetractHooks

/**
 * Created by TheCodeWarrior
 */
open class CommonProxy {
    open fun pre(e: FMLPreInitializationEvent) {
        ModItems
        network()
        HookTickHandler
    }

    open fun init(e: FMLInitializationEvent) {
    }

    open fun post(e: FMLPostInitializationEvent) {
    }

    fun network() {
        PacketHandler.register(PacketFireHook::class.java, Side.SERVER)
        PacketHandler.register(PacketRetractHooks::class.java, Side.SERVER)
    }

    open fun  setAutoJump(entityLiving: EntityLivingBase, value: Boolean) {
    }
}
