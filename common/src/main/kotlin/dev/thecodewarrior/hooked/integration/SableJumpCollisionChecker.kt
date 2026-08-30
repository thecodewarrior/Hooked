package dev.thecodewarrior.hooked.integration

import dev.ryanhcode.sable.Sable
import dev.ryanhcode.sable.api.math.LevelReusedVectors
import dev.ryanhcode.sable.api.math.OrientedBoundingBox3d
import dev.ryanhcode.sable.companion.math.BoundingBox3d
import dev.ryanhcode.sable.companion.math.Pose3dc
import dev.ryanhcode.sable.util.SableMathUtils
import net.minecraft.block.ShapeContext
import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import org.joml.Matrix4d
import org.joml.Quaterniond
import org.joml.Vector3d
import kotlin.math.atan2

/**
 * Based on:
 * - [dev.ryanhcode.sable.mixinhelpers.CanFallAtleastHelper]
 * - [dev.ryanhcode.sable.sublevel.entity_collision.SubLevelEntityCollision.hasCollision]
 */
object SableJumpCollisionChecker : JumpCollisionChecker {
    override fun findNonCollidingBoundingBoxes(
        entity: Entity,
        aabb: Box,
        offsets: Collection<Vector3d>
    ): Collection<Vector3d> {
        if (offsets.isEmpty()) return emptyList()

        val world = entity.world
        val shapeContext = ShapeContext.of(entity)

        val considerationBounds = BoundingBox3d(JumpCollisionChecker.expandBoxByOffsets(aabb, offsets))
        considerationBounds.expand(1.0) // fences

        val intersecting = Sable.HELPER.getAllIntersecting(world, considerationBounds)

        val sink = LevelReusedVectors()

        val aabbCenter = Vector3d(
            (aabb.minX + aabb.maxX) / 2.0,
            (aabb.minY + aabb.maxY) / 2.0,
            (aabb.minZ + aabb.maxZ) / 2.0,
        )
        val remainingOffsets = offsets.toMutableList()

        sink.entityBoxOrientation.identity()
        val entityBoundsOBB = OrientedBoundingBox3d(
            aabbCenter.x,
            aabbCenter.y,
            aabbCenter.z,
            aabb.lengthX - 0.1,
            aabb.lengthY,
            aabb.lengthZ - 0.1,
            sink.entityBoxOrientation,
            sink
        )

        val cubeOBB = OrientedBoundingBox3d(sink)
        val localBounds = BoundingBox3d()
        val bakedPose = Matrix4d()
        val satResult = Vector3d()

        for (subLevel in intersecting) {
            val pose = subLevel.lastPose()
            localBounds.set(considerationBounds)
            localBounds.transformInverse(pose, bakedPose)

            val blocks = BlockPos.iterate(
                BlockPos.ofFloored(localBounds.minX, localBounds.minY - 1, localBounds.minZ),
                BlockPos.ofFloored(localBounds.maxX, localBounds.maxY, localBounds.maxZ)
            )

            cubeOBB.orientation.set(pose.orientation())

            sink.entityBoxOrientation.identity().rotateY(getHitBoxYaw(pose))
            entityBoundsOBB.setOrientation(sink.entityBoxOrientation)

            for (block in blocks) {
                val state = world.getBlockState(block)

                val voxelShape = state.getCollisionShape(world, block, shapeContext)

                if (state.isAir) {
                    continue
                }

                voxelShape.forEachBox { minX, minY, minZ, maxX, maxY, maxZ ->
                    cubeOBB.position.set(
                        block.x + (minX + maxX) / 2,
                        block.y + (minY + maxY) / 2,
                        block.z + (minZ + maxZ) / 2,
                    )
                    cubeOBB.dimensions.set(
                        maxX - minX,
                        maxY - minY,
                        maxZ - minZ,
                    )
                    pose.transformPosition(cubeOBB.position)

                    remainingOffsets.removeIf {
                        entityBoundsOBB.position.set(aabbCenter).add(it)
                        OrientedBoundingBox3d.sat(entityBoundsOBB, cubeOBB, satResult)
                        satResult.lengthSquared() > 0 &&
                                satResult.x() != Double.MAX_VALUE &&
                                satResult.y() != Double.MAX_VALUE &&
                                satResult.z() != Double.MAX_VALUE
                    }
                }

                if (remainingOffsets.isEmpty()) {
                    return emptyList()
                }
            }
        }

        return remainingOffsets
    }

    fun getHitBoxYaw(subLevelPose: Pose3dc): Double {
        val subLevelOrientation = subLevelPose.orientation()
        val snapped =
            SableMathUtils.clampQuaternionToGrid(subLevelOrientation, SableMathUtils.GridQuats.REAL, Quaterniond())
        val relativeOrientation = subLevelOrientation.div(snapped, snapped)

        val dot = OrientedBoundingBox3d.UP.dot(
            Vector3d(
                relativeOrientation.x(),
                relativeOrientation.y(),
                relativeOrientation.z()
            )
        )

        return -2.0 * atan2(-dot, relativeOrientation.w())
    }
}