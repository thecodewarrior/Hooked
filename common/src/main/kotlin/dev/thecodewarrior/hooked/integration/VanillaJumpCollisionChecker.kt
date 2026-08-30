package dev.thecodewarrior.hooked.integration

import net.minecraft.entity.Entity
import net.minecraft.util.math.Box
import org.joml.Vector3d

object VanillaJumpCollisionChecker : JumpCollisionChecker {
    override fun findNonCollidingBoundingBoxes(
        entity: Entity,
        aabb: Box,
        offsets: Collection<Vector3d>
    ): Collection<Vector3d> {
        if (offsets.isEmpty()) return emptyList()

        val considerationBounds = JumpCollisionChecker.expandBoxByOffsets(aabb, offsets)
            .expand(1.0)

        val remainingOffsets = offsets.mapTo(mutableListOf()) { it to aabb.offset(it.x, it.y, it.z) }
        val entityCollisions = entity.world.getEntityCollisions(entity, considerationBounds)
        val blockCollisions = entity.world.getBlockCollisions(entity, considerationBounds)

        for (voxelShape in entityCollisions.asSequence() + blockCollisions.asSequence()) {
            voxelShape.forEachBox { minX, minY, minZ, maxX, maxY, maxZ ->
                remainingOffsets.removeIf { (_, box) ->
                    box.intersects(minX, minY, minZ, maxX, maxY, maxZ)
                }
            }

            if (remainingOffsets.isEmpty()) {
                return emptyList()
            }
        }

        return remainingOffsets.map { (offset, _) -> offset }
    }
}