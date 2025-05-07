package dev.thecodewarrior.hooked.client.glitter

import com.teamwizardry.librarianlib.core.util.vec
import com.teamwizardry.librarianlib.math.cross
import com.teamwizardry.librarianlib.math.div
import com.teamwizardry.librarianlib.math.minus
import dev.thecodewarrior.hooked.item.ChainAppearance
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import org.joml.Vector3f
import kotlin.jvm.optionals.getOrNull
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

object ChainShatterParticleSpawner {
    fun spawnBurst(start: Vec3d, end: Vec3d, appearance: ChainAppearance) {
        this.impl.spawnBurst(start, end, appearance)
    }

    var impl: ChainShatterParticleSpawnerImpl = ServerParticleImpl

    interface ChainShatterParticleSpawnerImpl {
        fun spawnBurst(start: Vec3d, end: Vec3d, appearance: ChainAppearance)
    }

    object ServerParticleImpl: ChainShatterParticleSpawnerImpl {
        override fun spawnBurst(start: Vec3d, end: Vec3d, appearance: ChainAppearance) {
            /* nop */
        }
    }

    object ClientParticleImpl: ChainShatterParticleSpawnerImpl {
        override fun spawnBurst(start: Vec3d, end: Vec3d, appearance: ChainAppearance) {
            val minColor = appearance.particleColorMin.getOrNull() ?: Vector3f(0f, 0f, 0f)
            val maxColor = appearance.particleColorMax.getOrNull() ?: minColor

            val delta = end - start
            val length = delta.length()
            val normal = delta / length

            val right = (delta cross vec(0, 1, 0)).let {
                if(it.lengthSquared() < 0.00001) {
                    vec(1, 0, 0)
                } else {
                    it.normalize()
                }
            }
            val up = (delta cross right).normalize()

            val burstVelocity = 0.1
            val burstVelocityVariance = 0.02
            val axisShift = 0.2

            // time in seconds
            val startLifetime = 1.0
            val endLifetime = 2.0
            val lifetimeVariance = 0.5

            var segmentStart = 0.0
            while(segmentStart < length) {
                val segmentSize = min(length - segmentStart, 0.25 + Math.random() * 0.75)
                val segmentCenter = segmentStart + segmentSize / 2

                val velocity = burstVelocity + (Math.random() * 2 - 1) * burstVelocityVariance

                var angle = Math.random() * 2 * Math.PI
                var sin = sin(angle)
                var cos = cos(angle)

                var axisX = right.x * sin + up.x * cos + normal.x * axisShift
                var axisY = right.y * sin + up.y * cos + normal.y * axisShift
                var axisZ = right.z * sin + up.z * cos + normal.z * axisShift
                val invLength = MathHelper.inverseSqrt(axisX * axisX + axisY * axisY + axisZ * axisZ)
                axisX *= invLength
                axisY *= invLength
                axisZ *= invLength

                angle = Math.random() * 2 * Math.PI
                sin = sin(angle)
                cos = cos(angle)

                var lifetime = startLifetime + (endLifetime - startLifetime) * (segmentStart / length)
                lifetime += Math.random() * lifetimeVariance // only randomly live longer, not shorter

                val colorRandom = Math.random().toFloat()
                val red = MathHelper.lerp(colorRandom, minColor.x, maxColor.x).toDouble()
                val green = MathHelper.lerp(colorRandom, minColor.y, maxColor.y).toDouble()
                val blue = MathHelper.lerp(colorRandom, minColor.z, maxColor.z).toDouble()

                ChainShatterParticleSystem.spawn(
                    (lifetime * 20).toInt(),
                    start.x + normal.x * segmentCenter,
                    start.y + normal.y * segmentCenter,
                    start.z + normal.z * segmentCenter,
                    (right.x * sin + up.x * cos) * velocity,
                    (right.y * sin + up.y * cos) * velocity,
                    (right.z * sin + up.z * cos) * velocity,
                    normal.x, normal.y, normal.z,
                    axisX, axisY, axisZ,
                    0.0, Math.random() * 4 * Math.PI,
                    segmentSize,
                    red, green, blue, 1.0
                )

                segmentStart += segmentSize
            }
        }
    }
}

