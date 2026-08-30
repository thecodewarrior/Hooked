package dev.thecodewarrior.hooked.integration

import net.minecraft.entity.Entity
import net.minecraft.util.math.Box
import org.joml.Vector3d

object NoopJumpCollisionChecker : JumpCollisionChecker {
    override fun findNonCollidingBoundingBoxes(
        entity: Entity,
        aabb: Box,
        offsets: Collection<Vector3d>
    ): Collection<Vector3d> {
        return offsets
    }
}
