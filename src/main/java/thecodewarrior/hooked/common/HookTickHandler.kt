package thecodewarrior.hooked.common

import baubles.api.BaublesApi
import com.teamwizardry.librarianlib.features.helpers.vec
import com.teamwizardry.librarianlib.features.kotlin.*
import com.teamwizardry.librarianlib.features.methodhandles.MethodHandleHelper
import com.teamwizardry.librarianlib.features.network.PacketHandler
import com.teamwizardry.librarianlib.features.utilities.RaycastUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.living.LivingEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import thecodewarrior.hooked.HookedMod
import thecodewarrior.hooked.common.capability.EnumHookStatus
import thecodewarrior.hooked.common.capability.HooksCap
import thecodewarrior.hooked.common.capability.HooksCapProvider
import thecodewarrior.hooked.common.items.ItemHook
import thecodewarrior.hooked.common.items.ModItems
import thecodewarrior.hooked.common.network.PacketHookCapSync
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.min

/**
 * Created by TheCodeWarrior
 */
object HookTickHandler {
    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    val rl = ResourceLocation(HookedMod.MODID, "playerHookWielder")

    @SubscribeEvent
    fun playerAttach(e: AttachCapabilitiesEvent<Entity>) {
        if (e.`object` is EntityPlayer) e.addCapability(rl, HooksCapProvider())
    }

    @SubscribeEvent
    fun track(e: PlayerEvent.StartTracking) {
        if (e.entityPlayer.world.isRemote)
            return
        val target = e.target
        target.ifCap(HooksCap.CAPABILITY, null) {
            PacketHandler.NETWORK.sendTo(PacketHookCapSync(target), e.entityPlayer as EntityPlayerMP)
        }
    }

    @SubscribeEvent
    fun join(e: EntityJoinWorldEvent) {
        val entity = e.entity
        if (entity is EntityPlayer && !entity.world.isRemote) {
            PacketHandler.NETWORK.sendTo(PacketHookCapSync(entity), entity as EntityPlayerMP)
        }
    }

    @SubscribeEvent
    fun breakSpeed(e: PlayerEvent.BreakSpeed) {
        if (!e.entity.onGround) {
            e.entity.ifCap(HooksCap.CAPABILITY, null) { cap ->
                if (cap.hooks.count { it.status == EnumHookStatus.PLANTED } > 0) {
                    e.newSpeed = e.newSpeed * 5
                }
            }
        }
    }

    @SubscribeEvent
    fun entityTick(e: LivingEvent.LivingUpdateEvent) {
        val entity = e.entity as? EntityPlayer ?: return

        HookedMod.PROXY.setAutoJump(e.entityLiving, true)
        if (!entity.hasCapability(HooksCap.CAPABILITY, null)) {
            return
        }
        val cap = entity.getCapability(HooksCap.CAPABILITY, null)!!

        val hookItem = ItemHook.getItem(entity)
        val itemType = ItemHook.getType(hookItem)
        if (itemType != cap.hookType) {
            cap.hookType = itemType
            cap.hooks.clear()
            cap.verticalHangDistance = 0.0
            cap.centerPos = null
            cap.update(entity)
        }
        val type = cap.hookType ?: return
        val count = if(hookItem?.let { ItemHook.isInhibited(hookItem) } == true) 1 else type.count

        if (type != HookType.RED || cap.hooks.size != 1) {
            cap.verticalHangDistance = 0.0
        }

        val waist = getWaistPos(entity)
        val spacing = 0.1
        val rnd = { ThreadLocalRandom.current().nextDouble(-0.05, 0.05) }
        var update = false
        var updatePos = false

        for (hook in cap.hooks) {
            if (hook.status == EnumHookStatus.RETRACTING) {
                hook.status = EnumHookStatus.DEAD
                update = true
            }

            if (hook.status == EnumHookStatus.EXTENDING) {
                val distanceLeft = type.range - (hook.pos - waist).length()
                val speed = type.speed
                val speedOrRemaining = min(speed, distanceLeft) + type.hookLength
                val trace = RaycastUtils.raycast(entity.world, hook.pos, hook.pos + hook.direction * speedOrRemaining)
                if (trace == null || trace.typeOfHit == RayTraceResult.Type.MISS) {
                    hook.pos += hook.direction * speedOrRemaining
                } else {
                    hook.pos = trace.hitVec - hook.direction * type.hookLength
                    hook.status = EnumHookStatus.PLANTED
                    hook.block = trace.blockPos
                    hook.side = trace.sideHit
                    update = true
                    updatePos = true
                }
            }

            if (entity.world.isRemote && type == HookType.ENDER) {
                val len = (hook.pos - waist).length()
                val normal = (hook.pos - waist) / len
                val negNormal = -normal

                var v = 1.0
                while (v < len && len > 2) {

                    if (ThreadLocalRandom.current().nextDouble() < 0.5) {
                        val pos = waist + (normal * v)
                        val vel = if (ThreadLocalRandom.current().nextBoolean()) negNormal else normal
                        entity.world.spawnParticle(EnumParticleTypes.PORTAL, true,
                                pos.x + rnd(), pos.y + rnd() + 0.1, pos.z + rnd(),
                                vel.x + rnd(), vel.y + rnd() - 0.65, vel.z + rnd())
                    }

                    v += spacing
                }
            }

            if (hook.pos.squareDistanceTo(waist) > type.rangeSq) {
                hook.status = EnumHookStatus.TORETRACT
            }

            if (hook.block != null) {
                if (hook.status == EnumHookStatus.PLANTED && entity.world.isAirBlock(hook.block)) {
                    hook.status = EnumHookStatus.TORETRACT
                }
            }

            if (hook.status == EnumHookStatus.TORETRACT) {
                hook.block = BlockPos(hook.pos)
                hook.pos = waist
                hook.status = EnumHookStatus.RETRACTING
                update = true
            }
        }

        if (cap.hooks.removeAll { it.status == EnumHookStatus.DEAD }) {
            update = true
            updatePos = true
        }
        while (cap.hooks.count { it.status == EnumHookStatus.PLANTED } > count) {
            cap.hooks.find { it.status == EnumHookStatus.PLANTED }?.status = EnumHookStatus.TORETRACT
            update = true
            updatePos = true
        }

        if (updatePos) cap.updatePos()
        if (update) cap.update(entity)

        if (cap.centerPos == null) {
            cap.updatePos()
            cap.update(entity)
        }

        if (cap.hookType == HookType.RED && cap.hooks.count { it.status == EnumHookStatus.PLANTED } > 0)
            cap.updateRedMovement(entity)

        var shouldSet = false
        cap.centerPos?.subtract(waist)?.let {
            val len = it.length()
            if (len > type.pullStrength) {
                it.scale(type.pullStrength / len)
            } else {
                shouldSet = true
                it
            }
        }?.let {
            val forceCoeff = 0.5
            if (shouldSet) {
                entity.motionX = it.x
                entity.motionY = it.y
                entity.motionZ = it.z
            } else {
                cap.hooks.forEach {
                    if (it.status != EnumHookStatus.PLANTED)
                        return@forEach

                    val pullVec = it.pos - waist
                    val projection = (vec(e.entity.motionX, e.entity.motionY, e.entity.motionZ) dot pullVec) / pullVec.length()
                    if (projection < 0) {
                        val add = pullVec * (projection / pullVec.length())
                        entity.motionX -= add.x
                        entity.motionY -= add.y
                        entity.motionZ -= add.z
                    }
                }
                if (Math.abs(e.entity.motionX) < Math.abs(it.x)) {
                    val adjusted = e.entity.motionX + it.x * forceCoeff
                    if (Math.abs(adjusted) > Math.abs(it.x))
                        entity.motionX = it.x
                    else
                        entity.motionX = adjusted
                }
                if (Math.abs(e.entity.motionY) < Math.abs(it.y)) {
                    val adjusted = e.entity.motionY + it.y * forceCoeff
                    if (Math.abs(adjusted) > Math.abs(it.y))
                        entity.motionY = it.y
                    else
                        entity.motionY = adjusted
                }
                if (Math.abs(e.entity.motionZ) < Math.abs(it.z)) {
                    val adjusted = e.entity.motionZ + it.z * forceCoeff
                    if (Math.abs(adjusted) > Math.abs(it.z))
                        entity.motionZ = it.z
                    else
                        entity.motionZ = adjusted
                }
            }
            entity.fallDistance = 0f
//            entity.onGround = true
            entity.jumpTicks = 10
            HookedMod.PROXY.setAutoJump(e.entityLiving, false)
        }

        cap.centerPos?.let {
            if (!e.entity.world.isRemote)
                e.entity.world.spawnParticle(EnumParticleTypes.FLAME, it.x, it.y, it.z, 0.0, 0.0, 0.0, 0)
        }
    }

    fun getWaistPos(e: Entity): Vec3d {
        return e.positionVector + vec(0, e.eyeHeight / 2, 0)
    }
}

private var EntityLivingBase.jumpTicks by MethodHandleHelper.delegateForReadWrite<EntityLivingBase, Int>(EntityLivingBase::class.java, "jumpTicks", "field_70773_bE")
