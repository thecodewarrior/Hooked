package thecodewarrior.hooked.common

import com.teamwizardry.librarianlib.common.network.PacketHandler
import com.teamwizardry.librarianlib.common.util.*
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.ResourceLocation
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
import thecodewarrior.hooked.common.network.PacketHookCapSync
import java.util.concurrent.ThreadLocalRandom

/**
 * Created by TheCodeWarrior
 */
object HookTickHandler {
    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    val rl = ResourceLocation(HookedMod.MODID, "playerHookWielder")

    @SubscribeEvent
    fun playerAttach(e: AttachCapabilitiesEvent.Entity) {
        if (e.entity is EntityPlayer) {
            e.addCapability(rl, HooksCapProvider())
        }
    }

    @SubscribeEvent
    fun track(e: PlayerEvent.StartTracking) {
        if(e.entityPlayer.world.isRemote)
            return
        val target = e.target
        target.ifCap(HooksCap.CAPABILITY, null) {
            PacketHandler.NETWORK.sendTo(PacketHookCapSync(target), e.entityPlayer as EntityPlayerMP)
        }
    }

    @SubscribeEvent
    fun join(e: EntityJoinWorldEvent) {
        val entity = e.entity
        if(entity is EntityPlayer && !entity.world.isRemote) {
            PacketHandler.NETWORK.sendTo(PacketHookCapSync(entity), entity as EntityPlayerMP)
        }
    }

    @SubscribeEvent
    fun breakSpeed(e: PlayerEvent.BreakSpeed) {
        if (!e.entity.onGround) {
            e.entity.ifCap(HooksCap.CAPABILITY, null) {
                if (hooks.count { it.status == EnumHookStatus.PLANTED } > 0) {
                    e.newSpeed = e.newSpeed * 5
                }
            }
        }
    }

    @SubscribeEvent
    fun entityTick(e: LivingEvent.LivingUpdateEvent) {
        HookedMod.PROXY.setAutoJump(e.entityLiving, true)
        if (!e.entity.hasCapability(HooksCap.CAPABILITY, null)) {
            return
        }
        val cap = e.entity.getCapability(HooksCap.CAPABILITY, null)!!

        val type = cap.hookType
        if (type == null) {
            cap.hooks.clear()
            cap.verticalHangDistance = 0.0
            cap.centerPos = null
            return
        }
        if(type != HookType.RED || cap.hooks.size != 1) {
            cap.verticalHangDistance = 0.0
        }

        val waist = getWaistPos(e.entity)
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
                val trace = RaycastUtils.raycast(e.entity.world, hook.pos, hook.pos + hook.direction * (Math.min(type.range - (hook.pos - waist).lengthVector(), type.speed + type.hookLength)))
                if (trace == null || trace.typeOfHit == RayTraceResult.Type.MISS)
                    hook.pos += hook.direction * type.speed
                else {
                    hook.pos = trace.hitVec - hook.direction * type.hookLength
                    hook.status = EnumHookStatus.PLANTED
                    hook.block = trace.blockPos
                    hook.side = trace.sideHit
                    update = true
                    updatePos = true

                }
            }

            if (e.entity.world.isRemote && type == HookType.ENDER) {
                val len = (hook.pos - waist).lengthVector()
                val normal = (hook.pos - waist) / len
                val negNormal = -normal

                var v = 1.0
                while (v < len && len > 2) {

                    if (ThreadLocalRandom.current().nextDouble() < 0.5) {
                        val pos = waist + (normal * v)
                        val vel = if (ThreadLocalRandom.current().nextBoolean()) negNormal else normal
                        e.entity.world.spawnParticle(EnumParticleTypes.PORTAL, true,
                                pos.xCoord + rnd(), pos.yCoord + rnd() + 0.1, pos.zCoord + rnd(),
                                vel.xCoord + rnd(), vel.yCoord + rnd() - 0.65, vel.zCoord + rnd())
                    }

                    v += spacing
                }
            }

            if (hook.pos.squareDistanceTo(e.entity.positionVector) > type.rangeSq) {
                hook.status = EnumHookStatus.TORETRACT
            }

            if (hook.block != null) {
                if (hook.status == EnumHookStatus.PLANTED && e.entity.world.isAirBlock(hook.block)) {
                    hook.status = EnumHookStatus.TORETRACT
                }
            }

            if (hook.status == EnumHookStatus.TORETRACT) {
                hook.pos = waist
                hook.status = EnumHookStatus.RETRACTING
                update = true
            }
        }

        if (cap.hooks.removeAll { it.status == EnumHookStatus.DEAD }) {
            update = true
            updatePos = true
        }
        while (cap.hooks.count { it.status == EnumHookStatus.PLANTED } > type.count) {
            cap.hooks.find { it.status == EnumHookStatus.PLANTED }?.status = EnumHookStatus.TORETRACT
            update = true
            updatePos = true
        }

        if(updatePos) cap.updatePos()
        if(update) cap.update(e.entity)

        if(cap.hookType == HookType.RED && cap.hooks.count { it.status == EnumHookStatus.PLANTED } > 0)
            cap.updateRedMovement(e.entity)

        var shouldSet = false
        cap.centerPos?.subtract(waist)?.let {
            val len = it.lengthVector()
            if (len > type.pullStrength) {
                it.scale(type.pullStrength / len)
            } else {
                shouldSet = true
                it
            }
        }?.let {
            val forceCoeff = 0.5
            if(shouldSet) {
                e.entity.motionX = it.xCoord
                e.entity.motionY = it.yCoord
                e.entity.motionZ = it.zCoord
            } else {
                cap.hooks.forEach {
                    if(it.status != EnumHookStatus.PLANTED)
                        return@forEach

                    val pullVec = it.pos - waist
                    val projection = (vec(e.entity.motionX, e.entity.motionY, e.entity.motionZ) dot pullVec) / pullVec.lengthVector()
                    if (projection < 0) {
                        val add = pullVec * (projection / pullVec.lengthVector())
                        e.entity.motionX -= add.xCoord
                        e.entity.motionY -= add.yCoord
                        e.entity.motionZ -= add.zCoord
                    }
                }
                if (Math.abs(e.entity.motionX) < Math.abs(it.xCoord)) {
                    val adjusted = e.entity.motionX + it.xCoord * forceCoeff
                    if (Math.abs(adjusted) > Math.abs(it.xCoord))
                        e.entity.motionX = it.xCoord
                    else
                        e.entity.motionX = adjusted
                }
                if (Math.abs(e.entity.motionY) < Math.abs(it.yCoord)){
                    val adjusted = e.entity.motionY + it.yCoord * forceCoeff
                    if (Math.abs(adjusted) > Math.abs(it.yCoord))
                        e.entity.motionY = it.yCoord
                    else
                        e.entity.motionY = adjusted
                }
                if (Math.abs(e.entity.motionZ) < Math.abs(it.zCoord)) {
                    val adjusted = e.entity.motionZ + it.zCoord * forceCoeff
                    if (Math.abs(adjusted) > Math.abs(it.zCoord))
                        e.entity.motionZ = it.zCoord
                    else
                        e.entity.motionZ = adjusted
                }
            }
            e.entity.fallDistance = 0f
//            e.entity.onGround = true
            e.entityLiving.jumpTicks = 10
            HookedMod.PROXY.setAutoJump(e.entityLiving, false)
        }

        cap.centerPos?.let {
            if (!e.entity.world.isRemote)
                e.entity.world.spawnParticle(EnumParticleTypes.FLAME, it.xCoord, it.yCoord, it.zCoord, 0.0, 0.0, 0.0, 0)
        }


    }

    fun getWaistPos(e: Entity): Vec3d {
        return e.positionVector + vec(0, e.eyeHeight / 2, 0)
    }
}

private var EntityLivingBase.jumpTicks by MethodHandleHelper.delegateForReadWrite<EntityLivingBase, Int>(EntityLivingBase::class.java, "jumpTicks", "field_70773_bE")
