package thecodewarrior.hooked.common.capability

import com.teamwizardry.librarianlib.features.helpers.vec
import com.teamwizardry.librarianlib.features.kotlin.toRl
import com.teamwizardry.librarianlib.features.math.Vec2d
import com.teamwizardry.librarianlib.features.network.PacketHandler
import com.teamwizardry.librarianlib.features.saving.AbstractSaveHandler
import com.teamwizardry.librarianlib.features.utilities.client.ClientRunnable
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagString
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.MathHelper
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import thecodewarrior.hooked.client.render.HookRenderer
import thecodewarrior.hooked.common.hook.HookController
import thecodewarrior.hooked.common.hook.HookType
import thecodewarrior.hooked.common.items.ItemHook
import thecodewarrior.hooked.common.network.PacketHookCapSync

class HooksCap(val player: EntityPlayer) {

    @SideOnly(Side.CLIENT)
    @JvmField
    var renderer: HookRenderer? = null

    var controller: HookController? = null
        set(value) {
            field = value
            ClientRunnable.run {
                if(value == null) {
                    renderer = null
                } else {
                    renderer = HookRenderer.REGISTRY.getValue(value.type.registryName)
                }
            }
        }

    fun updateController() {
        val item = ItemHook.getEquipped(player)
        if(item == null && controller != null) {
            controller = null
            return
        }
        val type = ItemHook.getType(item)
        if(type != null && type != controller?.type) {
            controller = type.create(player)
        }
    }

    fun update(player: Entity) {
        if (!player.world.isRemote)
            PacketHandler.NETWORK.sendToAllAround(PacketHookCapSync(player), NetworkRegistry.TargetPoint(player.world.provider.dimension, player.posX, player.posY, player.posZ, 128.0))
    }

    fun writeToNBT(compound: NBTTagCompound): NBTTagCompound {
        val controller = controller
        if(controller != null) {
            compound.setTag("id", NBTTagString(controller.type.registryName.toString()))
            compound.setTag("controller", AbstractSaveHandler.writeAutoNBT(controller, false))
        }
        return compound
    }

    fun readFromNBT(compound: NBTTagCompound) {
        val id = (compound.getTag("id") as? NBTTagString)?.string
        if(id != null) {
            val type = HookType.REGISTRY.getValue(id.toRl())
            if(type != null) {
                if(controller?.type != type) {
                    controller = type.create(player)
                }
                controller?.let {
                    AbstractSaveHandler.readAutoNBT(it, compound.getTag("controller"), false)
                }
            }
        }
    }

    companion object {
        init {
            CapabilityManager.INSTANCE.register(HooksCap::class.java, HooksCapStorage()) { HooksCap(null!!) };
        }

        @CapabilityInject(HooksCap::class)
        lateinit var CAPABILITY: Capability<HooksCap>

        fun moveRelative(entity: Entity, strafe: Float, forward: Float, friction: Float): Vec2d {
            var f = strafe * strafe + forward * forward

            if (f >= 1.0E-4f) {
                f = MathHelper.sqrt(f)

                if (f < 1.0f) {
                    f = 1.0f
                }

                f = friction / f
                val f1 = MathHelper.sin(entity.rotationYaw * 0.017453292f) * f
                val f2 = MathHelper.cos(entity.rotationYaw * 0.017453292f) * f
                return vec(strafe * f2 - forward * f1, forward * f2 + strafe * f1)
            }
            return Vec2d.ZERO
        }
    }
}

class HooksCapStorage : Capability.IStorage<HooksCap> {
    override fun readNBT(capability: Capability<HooksCap>, instance: HooksCap, side: EnumFacing?, nbt: NBTBase) {
        instance.readFromNBT(nbt as NBTTagCompound)
    }

    override fun writeNBT(capability: Capability<HooksCap>, instance: HooksCap, side: EnumFacing?): NBTBase {
        return instance.writeToNBT(NBTTagCompound())
    }
}

class HooksCapProvider(player: EntityPlayer) : ICapabilityProvider, INBTSerializable<NBTTagCompound> {
    override fun serializeNBT(): NBTTagCompound {
        return cap.writeToNBT(NBTTagCompound())
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        cap.readFromNBT(nbt)
    }

    val cap = HooksCap(player)

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean {
        return capability == HooksCap.CAPABILITY
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getCapability(capability: Capability<T>, facing: EnumFacing?): T? {
        if (capability == HooksCap.CAPABILITY)
            return cap as T
        return null
    }
}

