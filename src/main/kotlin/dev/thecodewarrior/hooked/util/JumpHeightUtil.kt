package dev.thecodewarrior.hooked.util

import com.teamwizardry.librarianlib.core.util.vec
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
import kotlin.math.cos
import kotlin.math.sin

object JumpHeightUtil {


    /**
     * Based on the step height code from `Entity.adjustMovementForCollisions`.
     *
     * Returns the final movement
     */
    fun computeStepTarget(
        player: PlayerEntity,
        box: Box,
        movement: Vec3d,
        maxHeight: Double
    ): Vec3d {
        val shapeContext = ShapeContext.of(player)
        val voxelShape: VoxelShape = player.world.worldBorder.asVoxelShape()
        val stream = if (VoxelShapes.matchesAnywhere(
                voxelShape,
                VoxelShapes.cuboid(box.contract(1.0E-7)),
                BooleanBiFunction.AND
            )
        ) Stream.empty() else Stream.of(voxelShape)
        val stream2: Stream<VoxelShape> = player.world.getEntityCollisions(player, box.stretch(movement), { true })
        val reusableStream: ReusableStream<VoxelShape> = ReusableStream(Stream.concat(stream2, stream))
        val horizontal = if (movement.lengthSquared() == 0.0) movement else Entity.adjustMovementForCollisions(
            player,
            movement,
            box,
            player.world,
            shapeContext,
            reusableStream
        )
        val collidedX = movement.x != horizontal.x
        val collidedZ = movement.z != horizontal.z
        if (collidedX || collidedZ) {
            var rising = Entity.adjustMovementForCollisions(
                player,
                Vec3d(movement.x, maxHeight, movement.z),
                box,
                player.world,
                shapeContext,
                reusableStream
            )
            val ceiling = Entity.adjustMovementForCollisions(
                player,
                Vec3d(0.0, maxHeight, 0.0),
                box.stretch(movement.x, 0.0, movement.z),
                player.world,
                shapeContext,
                reusableStream
            )
            if (ceiling.y < maxHeight) {
                val ceilingHorizontal = Entity.adjustMovementForCollisions(
                    player,
                    Vec3d(movement.x, 0.0, movement.z),
                    box.offset(ceiling),
                    player.world,
                    shapeContext,
                    reusableStream
                ).add(ceiling)
                if (ceilingHorizontal.horizontalLengthSquared() > rising.horizontalLengthSquared()) {
                    rising = ceilingHorizontal
                }
            }
            if (rising.horizontalLengthSquared() > horizontal.horizontalLengthSquared()) {
                return rising.add( // move down to the floor
                    Entity.adjustMovementForCollisions(
                        player,
                        Vec3d(0.0, -rising.y + movement.y, 0.0),
                        box.offset(rising),
                        player.world,
                        shapeContext,
                        reusableStream
                    )
                )
            }
        }

        return horizontal
    }

    fun movementToDirection(forward: Float, sideways: Float, yaw: Float): Vec3d {
        val s = sin(Math.toRadians(yaw.toDouble()))
        val c = cos(Math.toRadians(yaw.toDouble()))
        if (forward * forward + sideways * sideways < 1.0E-7) {
            return vec(-s, 0, c)
        }
        return vec(sideways * c - forward * s, 0, forward * c + sideways * s).normalize()
    }

}