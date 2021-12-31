package dev.thecodewarrior.hooked.hooks

import com.teamwizardry.librarianlib.core.util.mixinCast
import com.teamwizardry.librarianlib.core.util.vec
import com.teamwizardry.librarianlib.math.*
import dev.thecodewarrior.hooked.bridge.PlayerMixinBridge
import dev.thecodewarrior.hooked.hook.Hook
import dev.thecodewarrior.hooked.hook.HookControllerDelegate
import dev.thecodewarrior.hooked.hook.HookPlayerController
import dev.thecodewarrior.hooked.util.fromWaistPos
import dev.thecodewarrior.hooked.util.getWaistPos
import net.minecraft.block.ShapeContext
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.collection.ReusableStream
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import java.util.stream.Stream
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.sqrt

open class BasicHookPlayerController(val player: PlayerEntity, val type: BasicHookType): HookPlayerController() {
    override fun fireHooks(
        delegate: HookControllerDelegate,
        pos: Vec3d,
        direction: Vec3d,
        sneaking: Boolean,
        addHook: (pos: Vec3d, direction: Vec3d) -> Hook
    ): Boolean {
        if(delegate.cooldown == 0) {
            val tag = if(sneaking) {
                0
            } else {
                (delegate.hooks.maxOfOrNull { it.tag } ?: 0) + 1
            }
            val hook = addHook(pos, direction)
            hook.tag = tag
            delegate.triggerCooldown()
            return true
        } else {
            return false
        }
    }

    override fun onHookHit(delegate: HookControllerDelegate, hook: Hook) {
        super.onHookHit(delegate, hook)
        if(hook.tag > 0) {
            if(delegate.hooks.any { it.tag > hook.tag }) { // a more recent "solo" hook exists, immediately retract
                delegate.retractHook(hook, true)
            } else {
                for(other in delegate.hooks) {
                    if(other !== hook && other.state == Hook.State.PLANTED && other.tag < hook.tag) {
                        delegate.retractHook(other)
                    }
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
            delegate.retractHook(hook)
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
            player.x - player.prevX,
            player.x - player.prevY,
            player.x - player.prevZ
        )
        val stuckAngle = if (deltaPos == Vec3d.ZERO) 0.0 else acos(actualMotion.normalize() dot deltaNormal)

        var boostAABB: Box? = null

        if (deltaLen < type.pullStrength * 2) {
            boostAABB = player.boundingBox.offset(deltaPos) // the player's bounding box centered around the targetPos
        }
        if(stuckAngle > Math.toRadians(80.0)) {
            boostAABB = player.boundingBox
        }

        if(boostAABB != null) {
            // the maximum step height along the four cardinal directions
            val stepHeight = boostTestOffsets.maxOf { computeStepHeight(player, boostAABB, it, type.boostHeight) }
            // the absolute target height
            val targetHeight = boostAABB.minY + stepHeight
            // the height relative to the player's current position
            val jumpHeight = targetHeight - player.y
            val gravity = 0.08

            player.jump()
            if(jumpHeight > 0) {
                player.velocity = vec(
                    player.velocity.x,
                    sqrt(2 * gravity * jumpHeight),
                    player.velocity.z
                )
            }

            return
        }

        // if we don't do anything special, just give them a bit of a boost
        player.velocity += deltaNormal * (type.pullStrength * 0.2)
    }

    override fun update(delegate: HookControllerDelegate) {
        if (delegate.hooks.none { it.state == Hook.State.PLANTED }) {
            return
        }

        player.fallDistance = 0f
        clearFlyingKickTimer(player)

        applyRestoringForce(player, player.fromWaistPos(getTargetPoint(delegate.hooks)), type.pullStrength)
    }

    protected fun getTargetPoint(hooks: Collection<Hook>): Vec3d {
        var plantedCount = 0
        var targetPoint = Vec3d.ZERO
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
     * Based on the step height code from `Entity.adjustMovementForCollisions`.
     */
    protected fun computeStepHeight(
        player: PlayerEntity,
        box: Box,
        movement: Vec3d,
        maxHeight: Double
    ): Double {
        val shapeContext = ShapeContext.of(player)
        val voxelShape: VoxelShape = player.world.getWorldBorder().asVoxelShape()
        val stream = if (VoxelShapes.matchesAnywhere(
                voxelShape,
                VoxelShapes.cuboid(box.contract(1.0E-7)),
                BooleanBiFunction.AND
            )
        ) Stream.empty() else Stream.of(voxelShape)
        val stream2: Stream<VoxelShape> = player.world.getEntityCollisions(player, box.stretch(movement), { true })
        val reusableStream: ReusableStream<VoxelShape> = ReusableStream(Stream.concat(stream2, stream))
        val vec3d = if (movement.lengthSquared() == 0.0) movement else Entity.adjustMovementForCollisions(
            player,
            movement,
            box,
            player.world,
            shapeContext,
            reusableStream
        )
        val collidedX = movement.x != vec3d.x
        val collidedZ = movement.z != vec3d.z
        if (collidedX || collidedZ) {
            var vec3d2 = Entity.adjustMovementForCollisions(
                player,
                Vec3d(movement.x, maxHeight, movement.z),
                box,
                player.world,
                shapeContext,
                reusableStream
            )
            val vec3d3 = Entity.adjustMovementForCollisions(
                player,
                Vec3d(0.0, maxHeight, 0.0),
                box.stretch(movement.x, 0.0, movement.z),
                player.world,
                shapeContext,
                reusableStream
            )
            if (vec3d3.y < maxHeight) {
                val vec3d4 = Entity.adjustMovementForCollisions(
                    player,
                    Vec3d(movement.x, 0.0, movement.z),
                    box.offset(vec3d3),
                    player.world,
                    shapeContext,
                    reusableStream
                ).add(vec3d3)
                if (vec3d4.horizontalLengthSquared() > vec3d2.horizontalLengthSquared()) {
                    vec3d2 = vec3d4
                }
            }
            if (vec3d2.horizontalLengthSquared() > vec3d.horizontalLengthSquared()) {
                return vec3d2.add(
                    Entity.adjustMovementForCollisions(
                        player,
                        Vec3d(0.0, -vec3d2.y + movement.y, 0.0),
                        box.offset(vec3d2),
                        player.world,
                        shapeContext,
                        reusableStream
                    )
                ).y
            }
        }

        return vec3d.y
    }

    companion object {

        private val boostTestRange = 1.0
        private val boostTestOffsets = listOf(
            vec(boostTestRange, 0, 0),
            vec(0, 0, boostTestRange),
            vec(-boostTestRange, 0, 0),
            vec(0, 0, -boostTestRange),
        )
    }
}