package dev.thecodewarrior.hooked.integration

import dev.architectury.platform.Platform
import net.minecraft.entity.Entity
import net.minecraft.util.math.Box
import org.joml.Vector3d

interface JumpCollisionChecker {
    fun findNonCollidingBoundingBoxes(entity: Entity, aabb: Box, offsets: Collection<Vector3d>): Collection<Vector3d>

    companion object {
        val vanillaInstance: JumpCollisionChecker by lazy {
            VanillaJumpCollisionChecker
        }

        val sableInstance: JumpCollisionChecker by lazy {
            if (Platform.isModLoaded("sable")) SableJumpCollisionChecker else NoopJumpCollisionChecker
        }

        fun expandBoxByOffsets(aabb: Box, offsets: Collection<Vector3d>): Box = Box(
            aabb.minX + offsets.minOf { it.x },
            aabb.minY + offsets.minOf { it.y },
            aabb.minZ + offsets.minOf { it.z },
            aabb.maxX + offsets.maxOf { it.x },
            aabb.maxY + offsets.maxOf { it.y },
            aabb.maxZ + offsets.maxOf { it.z },
        )
    }
}