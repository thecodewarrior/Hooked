package thecodewarrior.hooked.common.capability

import com.teamwizardry.librarianlib.common.network.PacketHandler
import com.teamwizardry.librarianlib.common.util.div
import com.teamwizardry.librarianlib.common.util.plus
import com.teamwizardry.librarianlib.common.util.saving.AbstractSaveHandler
import com.teamwizardry.librarianlib.common.util.saving.Savable
import com.teamwizardry.librarianlib.common.util.saving.Save
import com.teamwizardry.librarianlib.common.util.saving.SaveInPlace
import net.minecraft.entity.Entity
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.fml.common.network.NetworkRegistry
import thecodewarrior.hooked.common.HookType
import thecodewarrior.hooked.common.network.PacketHookCapSync
import java.util.*

/**
 * Created by TheCodeWarrior
 */
enum class EnumHookStatus(val active: Boolean) { EXTENDING(true), PLANTED(true), TORETRACT(false), RETRACTING(false), DEAD(false);  }

@Savable data class HookInfo(var pos: Vec3d, var direction: Vec3d, var status: EnumHookStatus, var block: BlockPos?, var side: EnumFacing?, var uuid: UUID = UUID.randomUUID()) {
    constructor() : this(Vec3d.ZERO, Vec3d.ZERO, EnumHookStatus.PLANTED, null, null)
}

@SaveInPlace
class HooksCap {

    @Save
    var hooks = LinkedList<HookInfo>()

    @Save
    var hookType: HookType? = null

    @Save
    var centerPos: Vec3d? = null

    fun update(player: Entity) {
        if(!player.world.isRemote)
            PacketHandler.NETWORK.sendToAllAround(PacketHookCapSync(player), NetworkRegistry.TargetPoint(player.world.provider.dimension, player.posX, player.posY, player.posZ, 128.0))
    }

    fun updatePos() {
        val filtered = hooks.filter { it.status == EnumHookStatus.PLANTED }
        centerPos = if(filtered.isEmpty()) null else filtered.fold(Vec3d.ZERO) { sum, it -> sum + it.pos } / filtered.size
    }

    fun writeToNBT(compound: NBTTagCompound): NBTTagCompound {
        compound.setTag("auto", AbstractSaveHandler.writeAutoNBT(this, false))
        return compound
    }

    fun readFromNBT(compound: NBTTagCompound) {
        AbstractSaveHandler.readAutoNBT(this, compound.getTag("auto"), false)
    }

    companion object {
        init {
            CapabilityManager.INSTANCE.register(HooksCap::class.java, HooksCapStorage(), { HooksCap() });
        }

        @CapabilityInject(HooksCap::class)
        lateinit var CAPABILITY: Capability<HooksCap>
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

class HooksCapProvider : ICapabilityProvider, INBTSerializable<NBTTagCompound> {
    override fun serializeNBT(): NBTTagCompound {
        return cap.writeToNBT(NBTTagCompound())
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        cap.readFromNBT(nbt)
    }

    val cap = HooksCap()

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

