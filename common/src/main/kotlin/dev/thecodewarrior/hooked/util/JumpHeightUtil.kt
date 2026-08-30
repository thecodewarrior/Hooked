package dev.thecodewarrior.hooked.util

import dev.thecodewarrior.hooked.integration.JumpCollisionChecker
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Box
import org.joml.Vector3d
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object JumpHeightUtil {

    fun findJumpTargetOffsets(
        player: PlayerEntity,
        box: Box,
        maxHeight: Double,
    ): Collection<Vector3d> {
        val offsets = computeJumpTestOffsets(player, maxHeight)
        val afterVanilla = JumpCollisionChecker.vanillaInstance.findNonCollidingBoundingBoxes(player, box, offsets)
        val afterSable = JumpCollisionChecker.sableInstance.findNonCollidingBoundingBoxes(player, box, afterVanilla)
        return afterSable
            .groupBy { it.x to it.z }
            .values.map { it.minBy { vec -> vec.y } }
            .filter { it.y > 0 }
    }

    fun movementToDirection(forward: Float, sideways: Float, yaw: Float): Vector3d {
        val s = sin(Math.toRadians(yaw.toDouble()))
        val c = cos(Math.toRadians(yaw.toDouble()))
        if (forward * forward + sideways * sideways < 1.0E-7) {
            return Vector3d(-s, 0.0, c)
        }
        return Vector3d(
            sideways * c - forward * s,
            0.0,
            forward * c + sideways * s
        ).normalize()
    }

    fun computeJumpTestOffsets(player: PlayerEntity, maxHeight: Double): List<Vector3d> {
        val direction = movementToDirection(player.forwardSpeed, player.sidewaysSpeed, player.yaw)

        val sampleAngles = listOf(
            0.0 to 0.5,
            0.0 to 1.0,
            -45.0 to 1.0,
            45.0 to 1.0,
        ).map { (angle, distance) ->
            (angle * PI / 180) to distance
        }

        val testIncrement = 1 / 16.0
        val yOffsets = (0 .. (maxHeight / testIncrement).toInt()).map { it * testIncrement }

        return sampleAngles
            .map { (angle, distance) ->
                direction.rotateY(angle, Vector3d()).mul(distance)
            }
            .flatMap { base ->
                yOffsets.map { y ->
                    base.add(0.0, y, 0.0, Vector3d())
                }
            }
    }
}