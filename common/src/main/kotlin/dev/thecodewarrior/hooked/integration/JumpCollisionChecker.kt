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

        fun expandBoxByOffsets(aabb: Box, offsets: Collection<Vector3d>): Box = aabb
            .stretch(
                offsets.minOf { it.x },
                offsets.minOf { it.y },
                offsets.minOf { it.z },
            )
            .stretch(
                offsets.maxOf { it.x },
                offsets.maxOf { it.y },
                offsets.maxOf { it.z },
            )
    }
}