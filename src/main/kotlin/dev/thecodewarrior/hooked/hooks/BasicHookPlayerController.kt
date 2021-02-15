package dev.thecodewarrior.hooked.hooks

import com.teamwizardry.librarianlib.core.util.mixinCast
import com.teamwizardry.librarianlib.core.util.vec
import com.teamwizardry.librarianlib.math.*
import dev.thecodewarrior.hooked.HookedModSounds
import dev.thecodewarrior.hooked.bridge.HookTravelFlag
import dev.thecodewarrior.hooked.hook.Hook
import dev.thecodewarrior.hooked.hook.HookControllerDelegate
import dev.thecodewarrior.hooked.hook.HookProcessorContext
import dev.thecodewarrior.hooked.hook.HookPlayerController
import dev.thecodewarrior.hooked.util.fromWaistPos
import dev.thecodewarrior.hooked.util.getWaistPos
import net.minecraft.entity.Entity
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ReuseableStream
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.shapes.ISelectionContext
import net.minecraft.util.math.vector.Vector3d
import net.minecraftforge.common.ForgeMod
import java.util.UUID
import kotlin.math.abs
import kotlin.math.sqrt

class BasicHookPlayerController(val player: PlayerEntity, val type: BasicHookType): HookPlayerController() {
    override fun remove() {
        mixinCast<HookTravelFlag>(player).travelingByHook = false
    }

    override fun fireHooks(
        delegate: HookControllerDelegate,
        pos: Vector3d,
        direction: Vector3d,
        sneaking: Boolean,
        addHook: (pos: Vector3d, direction: Vector3d) -> Hook
    ) {
        val hook = addHook(pos, direction)
        hook.tag = if(sneaking) 0 else EXCLUSIVE_HOOK_TAG
    }

    override fun onHookHit(delegate: HookControllerDelegate, hook: Hook) {
        super.onHookHit(delegate, hook)
        if(hook.tag == EXCLUSIVE_HOOK_TAG) {
            for(other in delegate.hooks) {
                if(other !== hook && other.state == Hook.State.PLANTED) {
                    other.state = Hook.State.RETRACTING
                    delegate.markDirty(other)

                    delegate.playWorldSound(Hook.hitSound(delegate.world, other.block), other.pos, 1f, 1f)
                    delegate.playFeedbackSound(HookedModSounds.retractHook, 1f, 1f)
                }
            }
        }
    }

    override fun jump(
        delegate: HookControllerDelegate,
        doubleJump: Boolean,
        sneaking: Boolean
    ) {
        if (delegate.hooks.any { it.state == Hook.State.PLANTED }) {
            performJump(delegate)
        }

        for(hook in delegate.hooks) {
            if(hook.state == Hook.State.PLANTED) {
                delegate.playWorldSound(Hook.hitSound(delegate.world, hook.block), hook.pos, 1f, 1f)
                delegate.playFeedbackSound(HookedModSounds.retractHook, 1f, 1f)
            }
            hook.state = Hook.State.RETRACTING
            delegate.markDirty(hook)
        }
    }

    private fun performJump(
        delegate: HookControllerDelegate
    ) {
        val waist = player.getWaistPos()
        val targetPos = getTargetPoint(delegate.hooks)
        val deltaPos = targetPos - waist
        val deltaLen = deltaPos.length()
        val deltaNormal = deltaPos / deltaLen
        val actualMotion = vec(
            player.posX - player.prevPosX,
            player.posY - player.prevPosY,
            player.posZ - player.prevPosZ
        )
        val motionTowardTarget = if (deltaPos == Vector3d.ZERO) 0.0 else actualMotion dot deltaNormal

        var boostAABB: AxisAlignedBB? = null

        if (deltaLen < type.pullStrength * 2) {
            boostAABB = player.boundingBox.offset(deltaPos) // the player's bounding box centered around the targetPos
        }
        if(abs(motionTowardTarget) < 0.1) {
            boostAABB = player.boundingBox
        }

        if(boostAABB != null) {
            // the maximum step height along the four cardinal directions
            val stepHeight = boostTestOffsets.maxOf { computeStepHeight(player, boostAABB, it, type.boostHeight) }
            // the absolute target height
            val targetHeight = boostAABB.minY + stepHeight
            // the height relative to the player's current position
            val jumpHeight = targetHeight - player.posY
            val gravity = getPlayerGravity(player)

            player.jump()
            if(jumpHeight > 0) {
                player.motion = vec(
                    player.motion.x,
                    sqrt(2 * gravity * jumpHeight),
                    player.motion.z
                )
            }

            return
        }

        // if we don't do anything special, just give them a bit of a boost
        player.motion += deltaNormal * (type.pullStrength * 0.2)
    }

    override fun update(delegate: HookControllerDelegate) {
        if (delegate.hooks.none { it.state == Hook.State.PLANTED }) {
            mixinCast<HookTravelFlag>(player).travelingByHook = false
            return
        }

        // we have at least one planted hook
        mixinCast<HookTravelFlag>(player).travelingByHook = true
        player.fallDistance = 0f
        clearFlyingKickTimer(player)

        applyRestoringForce(player, player.fromWaistPos(getTargetPoint(delegate.hooks)), type.pullStrength)
    }

    protected fun getTargetPoint(hooks: List<Hook>): Vector3d {
        var plantedCount = 0
        var targetPoint = Vector3d.ZERO
        hooks.forEach { hook ->
            if (hook.state == Hook.State.PLANTED) {
                targetPoint += hook.pos
                plantedCount++
            }
        }
        targetPoint /= plantedCount
        return targetPoint
    }

    /**
     * Get the current gravity strength for the given player.
     *
     * Based on this code from `LivingEntity.travel(travelVector)`:
     *
     * ```java
     * double d0 = 0.08D;
     * ModifiableAttributeInstance gravity = this.getAttribute(net.minecraftforge.common.ForgeMod.ENTITY_GRAVITY.get());
     * boolean flag = this.getMotion().y <= 0.0D;
     * if (flag && this.isPotionActive(Effects.SLOW_FALLING)) {
     *     if (!gravity.hasModifier(SLOW_FALLING)) gravity.applyNonPersistentModifier(SLOW_FALLING);
     *     this.fallDistance = 0.0F;
     * } else if (gravity.hasModifier(SLOW_FALLING)) {
     *     gravity.removeModifier(SLOW_FALLING);
     * }
     * d0 = gravity.getValue();
     * ```
     */
    protected fun getPlayerGravity(player: PlayerEntity): Double {
        val playerAttribute = player.getAttribute(ForgeMod.ENTITY_GRAVITY.get()) ?: return 0.08
        // the moment we start moving up the slow falling modifier will be removed. In order to properly reflect this
        // in our output, we need to make a copy and remove that modifier
        gravityAttribute.copyValuesFromInstance(playerAttribute)
        gravityAttribute.removeModifier(SLOW_FALLING_ID)
        return gravityAttribute.value
    }

    private val gravityAttribute = ModifiableAttributeInstance(ForgeMod.ENTITY_GRAVITY.get()) {}

    /**
     * Based on the step height code from `Entity.getAllowedMovement`.
     */
    protected fun computeStepHeight(
        player: PlayerEntity,
        playerAABB: AxisAlignedBB,
        offset: Vector3d,
        maxHeight: Double
    ): Double {
        val selectionContext = ISelectionContext.forEntity(player)
        val voxelStream = ReuseableStream(player.world.func_230318_c_(player, playerAABB.expand(offset)) { true })
        val vector3d = if (offset.lengthSquared() == 0.0) offset else Entity.collideBoundingBoxHeuristically(
            player, offset, playerAABB,
            player.world, selectionContext, voxelStream
        )

        val collidedX = offset.x != vector3d.x
        val collidedZ = offset.z != vector3d.z

        if (maxHeight > 0.0f && (collidedX || collidedZ)) {
            var vector3d1 = Entity.collideBoundingBoxHeuristically(
                player, Vector3d(offset.x, maxHeight, offset.z), playerAABB,
                player.world, selectionContext, voxelStream
            )
            val vector3d2 = Entity.collideBoundingBoxHeuristically(
                player, Vector3d(0.0, maxHeight, 0.0), playerAABB.expand(offset.x, 0.0, offset.z),
                player.world, selectionContext, voxelStream
            )
            if (vector3d2.y < maxHeight) {
                val vector3d3 = Entity.collideBoundingBoxHeuristically(
                    player, Vector3d(offset.x, 0.0, offset.z), playerAABB.offset(vector3d2),
                    player.world, selectionContext, voxelStream
                ).add(vector3d2)
                if (Entity.horizontalMag(vector3d3) > Entity.horizontalMag(vector3d1)) {
                    vector3d1 = vector3d3
                }
            }
            if (Entity.horizontalMag(vector3d1) > Entity.horizontalMag(vector3d)) {
                return vector3d1.add(
                    Entity.collideBoundingBoxHeuristically(
                        player, Vector3d(0.0, -vector3d1.y + offset.y, 0.0), playerAABB.offset(vector3d1),
                        player.world, selectionContext, voxelStream
                    )
                ).y
            }
        }

        return vector3d.y
    }

    companion object {
        // this is private in `LivingEntity`
        private val SLOW_FALLING_ID = UUID.fromString("A5B6CF2A-2F7C-31EF-9022-7C3E7D5E6ABA")

        private val boostTestRange = 1.0
        private val boostTestOffsets = listOf(
            vec(boostTestRange, 0, 0),
            vec(0, 0, boostTestRange),
            vec(-boostTestRange, 0, 0),
            vec(0, 0, -boostTestRange),
        )

        private val EXCLUSIVE_HOOK_TAG: Int = 1
    }
}