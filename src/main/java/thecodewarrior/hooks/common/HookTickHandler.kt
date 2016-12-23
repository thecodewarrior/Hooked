package thecodewarrior.hooks.common

import com.teamwizardry.librarianlib.common.util.*
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.entity.living.LivingEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import thecodewarrior.hooks.HooksMod
import thecodewarrior.hooks.common.capability.EnumHookStatus
import thecodewarrior.hooks.common.capability.HooksCap
import thecodewarrior.hooks.common.capability.HooksCapProvider

/**
 * Created by TheCodeWarrior
 */
object HookTickHandler {
    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    val rl = ResourceLocation(HooksMod.MODID, "playerHookWielder")

    @SubscribeEvent
    fun playerAttach(e: AttachCapabilitiesEvent.Entity) {
        if (e.entity is EntityPlayer) {
            e.addCapability(rl, HooksCapProvider())
        }
    }

    @SubscribeEvent
    fun breakSpeed(e: PlayerEvent.BreakSpeed) {
        if(!e.entity.onGround) {
            e.entity.ifCap(HooksCap.CAPABILITY, null) {
                if(hooks.count { it.status == EnumHookStatus.PLANTED } > 0) {
                    e.newSpeed = e.newSpeed * 5
                }
            }
        }
    }

    @SubscribeEvent
    fun entityTick(e: LivingEvent.LivingUpdateEvent) {
        HooksMod.PROXY.setAutoJump(e.entityLiving, true)
        if (!e.entity.hasCapability(HooksCap.CAPABILITY, null)) {
            return
        }
        val cap = e.entity.getCapability(HooksCap.CAPABILITY, null)!!

        val type = cap.hookType
        if (type == null) {
            cap.hooks.clear()
            return
        }

        val waist = getWaistPos(e.entity)

        for (hook in cap.hooks) {
            if(hook.status == EnumHookStatus.RETRACTING)
                hook.status = EnumHookStatus.DEAD

            if (hook.status == EnumHookStatus.EXTENDING) {
                val trace = RaycastUtils.raycast(e.entity.world, hook.pos, hook.pos + hook.direction * (Math.min(type.range-(hook.pos-waist).lengthVector(),type.speed+type.hookLength)))
                if (trace == null || trace.typeOfHit == RayTraceResult.Type.MISS)
                    hook.pos += hook.direction * type.speed
                else {
                    hook.pos = trace.hitVec - hook.direction * type.hookLength
                    hook.status = EnumHookStatus.PLANTED
                    hook.block = trace.blockPos
                    hook.side = trace.sideHit
                    cap.updatePos()
                }
            }
            if(hook.pos.squareDistanceTo(e.entity.positionVector) > type.rangeSq) {
                hook.status = EnumHookStatus.TORETRACT
            }
            if(hook.block != null) {
                if (hook.status == EnumHookStatus.PLANTED && e.entity.world.isAirBlock(hook.block)) {
                    hook.status = EnumHookStatus.TORETRACT
                }
            }
            if(hook.status == EnumHookStatus.TORETRACT) {
                hook.pos = waist
                hook.status = EnumHookStatus.RETRACTING
            }
        }
        if(cap.hooks.removeAll { it.status == EnumHookStatus.DEAD }) {
            cap.updatePos()
        }

        while(cap.hooks.count { it.status == EnumHookStatus.PLANTED } > type.count) {
            cap.hooks.find { it.status == EnumHookStatus.PLANTED }?.status = EnumHookStatus.TORETRACT
            cap.updatePos()
        }


        cap.centerPos?.subtract(waist)?.let {
            val len = it.lengthVector()
            if(len > type.pullStrength)
                it.scale(type.pullStrength/len)
            else
                it
        }?.let {
            e.entity.motionX = it.xCoord
            e.entity.motionY = it.yCoord
            e.entity.motionZ = it.zCoord
            e.entity.fallDistance = 0f
            e.entity.onGround = true
            e.entityLiving.jumpTicks = 10
            HooksMod.PROXY.setAutoJump(e.entityLiving, false)
        }
        cap.centerPos?.let {
            if(!e.entity.world.isRemote)
                e.entity.world.spawnParticle(EnumParticleTypes.FLAME, it.xCoord, it.yCoord, it.zCoord, 0.0, 0.0, 0.0, 0)
        }
    }

    fun getWaistPos(e: Entity): Vec3d {
        return e.positionVector + vec(0, e.eyeHeight / 2, 0)
    }
}

private var EntityLivingBase.jumpTicks by MethodHandleHelper.delegateForReadWrite<EntityLivingBase, Int>(EntityLivingBase::class.java, "jumpTicks", "field_70773_bE")
