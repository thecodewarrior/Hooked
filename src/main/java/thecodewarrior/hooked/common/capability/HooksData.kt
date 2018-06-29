package thecodewarrior.hooked.common.capability

import com.teamwizardry.librarianlib.features.helpers.vec
import com.teamwizardry.librarianlib.features.kotlin.*
import com.teamwizardry.librarianlib.features.math.Vec2d
import com.teamwizardry.librarianlib.features.network.PacketHandler
import com.teamwizardry.librarianlib.features.saving.AbstractSaveHandler
import com.teamwizardry.librarianlib.features.saving.Savable
import com.teamwizardry.librarianlib.features.saving.Save
import com.teamwizardry.librarianlib.features.saving.SaveInPlace
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.fml.common.network.NetworkRegistry
import thecodewarrior.hooked.common.HookTickHandler
import thecodewarrior.hooked.common.HookType
import thecodewarrior.hooked.common.network.PacketHookCapSync
import thecodewarrior.hooked.common.network.PacketUpdateWeights
import thecodewarrior.hooked.common.util.Barycentric
import java.util.*

/**
 * Created by TheCodeWarrior
 */
enum class EnumHookStatus(val active: Boolean) { EXTENDING(true), PLANTED(true), TORETRACT(false), RETRACTING(false), DEAD(false); }

@Savable data class HookInfo(var pos: Vec3d, var direction: Vec3d, var status: EnumHookStatus, var block: BlockPos?, var side: EnumFacing?, var weight: Double = 1.0, var uuid: UUID = UUID.randomUUID()) {
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

    @Save
    var verticalHangDistance: Double = 0.0

    fun update(player: Entity) {
        if (!player.world.isRemote)
            PacketHandler.NETWORK.sendToAllAround(PacketHookCapSync(player), NetworkRegistry.TargetPoint(player.world.provider.dimension, player.posX, player.posY, player.posZ, 128.0))
    }

    fun updatePos() {
        val filtered = hooks.filter { it.status == EnumHookStatus.PLANTED }
        if (filtered.size == 1)
            centerPos = filtered[0].pos - vec(0, verticalHangDistance, 0)
        else
            centerPos = if (filtered.isEmpty()) null else filtered.fold(Vec3d.ZERO) { sum, it -> sum + (it.pos * it.weight) } / filtered.sumByDouble { it.weight }
    }

    fun updateRedMovement(player: Entity) {
        if (hooks.count { it.status == EnumHookStatus.PLANTED } != 1)
            verticalHangDistance = 0.0
        if (player !is EntityPlayer)
            return
        if (!player.world.isRemote)
            return
        if (player != Minecraft.getMinecraft().player)
            return

        var movementY = 0.0
        if (player.isSneaking) {
            movementY -= verticalSpeed
        }
        if (Minecraft.getMinecraft().gameSettings.keyBindJump.isKeyDown) {
            movementY += verticalSpeed
        }
        var strafe = if(hooks.count { it.status == EnumHookStatus.PLANTED } == 1) 0f else player.moveStrafing
        var forward = if(hooks.count { it.status == EnumHookStatus.PLANTED } == 1) 0f else player.moveForward

        if (player.isSneaking) {
            strafe /= 0.3f
            forward /= 0.3f
        }
        val horizontal = moveRelative(player, strafe, forward, 0.25f)
        var offset = vec(horizontal.x, movementY, horizontal.y)


        offset = player.world.collideAABB(player.entityBoundingBox, offset, player)

        if (offset.lengthSquared() > 0.001) {
            centerPos = HookTickHandler.getWaistPos(player) + offset
            correctPos()
        }
    }

    fun correctPos() {
        val center = centerPos ?: return
        val filtered = hooks.filter { it.status == EnumHookStatus.PLANTED }

        if (filtered.size == 1) {
            val hook0 = filtered[0]
            verticalHangDistance = hook0.pos.y - Math.min(hook0.pos.y, center.y)
        }
        if (filtered.size == 2) {
            val hook0 = filtered[0]
            val hook1 = filtered[1]

            val closest = closestPointOnLine(center, hook0.pos, hook1.pos).second

            hook1.weight = (closest - hook0.pos).lengthVector() / (hook1.pos - hook0.pos).lengthVector()
            hook0.weight = 1 - hook1.weight

            hook0.weight *= 2
            hook1.weight *= 2
        }
        if (filtered.size == 3) {
            val hook0 = filtered[0]
            val hook1 = filtered[1]
            val hook2 = filtered[2]

            val proj = projectToTri(center, hook0.pos, hook1.pos, hook2.pos)
            val closest = closestPointOnTriangle(proj, hook0.pos, hook1.pos, hook2.pos).second

            val bary = Barycentric.toBarycentric(closest, hook0.pos, hook1.pos, hook2.pos)

            hook0.weight = bary.x * 3
            hook1.weight = bary.y * 3
            hook2.weight = bary.z * 3
        }
        if (filtered.size == 4) {
            val hook0 = filtered[0]
            val hook1 = filtered[1]
            val hook2 = filtered[2]
            val hook3 = filtered[3]

            val closest = closestPointInTetrahedron(center, hook0.pos, hook1.pos, hook2.pos, hook3.pos).second

            val bary = Barycentric.toBarycentric(closest, hook0.pos, hook1.pos, hook2.pos, hook3.pos)

            hook0.weight = bary.x * 4
            hook1.weight = bary.y * 4
            hook2.weight = bary.z * 4
            hook3.weight = bary.w * 4
        }
        updatePos()
        updateWeights()
    }

    // region closest points
    fun closestPointInTetrahedron(p: Vec3d, a: Vec3d, b: Vec3d, c: Vec3d, d: Vec3d): Pair<Double, Vec3d> {
        if (insideTetrahedron(p, a, b, c, d))
            return 0.0 to p

        return listOf(
                (a - p).lengthSquared() to a,
                (b - p).lengthSquared() to b,
                (c - p).lengthSquared() to c,
                (d - p).lengthSquared() to d,

                closestPointOnLineRay(p, a, b),
                closestPointOnLineRay(p, a, c),
                closestPointOnLineRay(p, a, d),
                closestPointOnLineRay(p, b, c),
                closestPointOnLineRay(p, b, d),
                closestPointOnLineRay(p, c, d),

                closestPointOnTrianglePlane(p, a, b, c),
                closestPointOnTrianglePlane(p, a, b, d),
                closestPointOnTrianglePlane(p, a, c, d),
                closestPointOnTrianglePlane(p, b, c, d)
        ).minBy { it.first } ?: Double.POSITIVE_INFINITY to p
    }

    fun closestPointOnTriangle(p: Vec3d, a: Vec3d, b: Vec3d, c: Vec3d): Pair<Double, Vec3d> {
        if (insideTri(p, a, b, c))
            return 0.0 to p

        return listOf(
                (a - p).lengthSquared() to a,
                (b - p).lengthSquared() to b,
                (c - p).lengthSquared() to c,

                closestPointOnLineRay(p, a, b),
                closestPointOnLineRay(p, b, c),
                closestPointOnLineRay(p, c, a)
        ).minBy { it.first } ?: Double.POSITIVE_INFINITY to p
    }

    fun closestPointOnLine(p: Vec3d, a: Vec3d, b: Vec3d): Pair<Double, Vec3d> {
        return listOf(
                (a - p).lengthSquared() to a,
                (b - p).lengthSquared() to b,

                closestPointOnLineRay(p, a, b)
        ).minBy { it.first } ?: Double.POSITIVE_INFINITY to p
    }

    fun closestPointOnLineRay(p: Vec3d, a: Vec3d, b: Vec3d): Pair<Double, Vec3d> {
        var line = (b - a)
        val len = line.lengthVector()
        line /= len

        val v = p - a
        var d = v dot line
        val point = a + line * d

        if (d < 0.0 || d > len)
            return Double.POSITIVE_INFINITY to point
        return (p - point).lengthSquared() to point
    }

    fun closestPointOnTrianglePlane(p: Vec3d, a: Vec3d, b: Vec3d, c: Vec3d): Pair<Double, Vec3d> {
        val proj = projectToTri(p, a, b, c)
        if (insideTri(proj, a, b, c))
            return (p - proj).lengthSquared() to proj
        else
            return Double.POSITIVE_INFINITY to proj
    }

    fun projectToTri(p: Vec3d, a: Vec3d, b: Vec3d, c: Vec3d): Vec3d {
        val normal = triangleNormal(a, b, c)

        val d = (p - a) dot normal
        return p - (normal * d)
    }

    fun insideTri(p: Vec3d, a: Vec3d, b: Vec3d, c: Vec3d): Boolean {
        return sameSide(p, a, b, c) && sameSide(p, b, a, c) && sameSide(p, c, a, b)
    }

    fun sameSide(p1: Vec3d, p2: Vec3d, a: Vec3d, b: Vec3d): Boolean {
        val cp1 = (b - a) cross (p1 - a)
        val cp2 = (b - a) cross (p2 - a)
        return cp1 dot cp2 >= 0
    }

    fun insideTetrahedron(p: Vec3d, a: Vec3d, b: Vec3d, c: Vec3d, d: Vec3d): Boolean {
        return sameSide(a, b, c, d, p) &&
                sameSide(b, c, d, a, p) &&
                sameSide(c, d, a, b, p) &&
                sameSide(d, a, b, c, p)
    }

    fun sameSide(v1: Vec3d, v2: Vec3d, v3: Vec3d, v4: Vec3d, p: Vec3d): Boolean {
        val normal = (v2 - v1) cross (v3 - v1)
        val dotV4 = normal dot (v4 - v1)
        val dotP = normal dot (p - v1)
        return (dotV4 < 0 && dotP < 0) || (dotV4 > 0 && dotP > 0)
    }

    fun triangleNormal(a: Vec3d, b: Vec3d, c: Vec3d): Vec3d {
        val ab = a - b
        val bc = b - c
        return (ab cross bc).normalize()
    }
    // endregion closest points

    fun updateWeights() {
        val filtered = hooks.filter { it.status == EnumHookStatus.PLANTED }
        if (filtered.size > 0)
            PacketHandler.NETWORK.sendToServer(PacketUpdateWeights().apply { vertical = verticalHangDistance; weights = filtered.associate { it.uuid to it.weight } as HashMap<UUID, Double> })
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

        val verticalSpeed = 0.25

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

