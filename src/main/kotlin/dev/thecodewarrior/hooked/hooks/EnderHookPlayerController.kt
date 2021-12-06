package dev.thecodewarrior.hooked.hooks

import com.teamwizardry.librarianlib.core.util.sided.clientOnly
import com.teamwizardry.librarianlib.core.util.vec
import com.teamwizardry.librarianlib.math.cross
import dev.thecodewarrior.hooked.hook.Hook
import dev.thecodewarrior.hooked.hook.HookControllerDelegate
import dev.thecodewarrior.hooked.util.getWaistPos
import com.teamwizardry.librarianlib.math.minus
import com.teamwizardry.librarianlib.math.div
import dev.thecodewarrior.hooked.client.glitter.EnderHookParticleSystem
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.vector.Vec3d
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class EnderHookPlayerController(player: PlayerEntity, type: BasicHookType): BasicHookPlayerController(player, type) {
    override fun onHookHit(delegate: HookControllerDelegate, hook: Hook) {
        super.onHookHit(delegate, hook)
        if(delegate.player.world.isRemote) {
            spawnBurst(delegate.player.getWaistPos(), hook.pos)
        }
    }

    override fun onHookMiss(delegate: HookControllerDelegate, hook: Hook) {
        super.onHookMiss(delegate, hook)
        if(delegate.player.world.isRemote) {
            spawnBurst(delegate.player.getWaistPos(), hook.pos)
        }
    }

    override fun onHookDislodge(delegate: HookControllerDelegate, hook: Hook, reason: DislodgeReason) {
        super.onHookDislodge(delegate, hook, reason)
        if(delegate.player.world.isRemote) {
            spawnBurst(delegate.player.getWaistPos(), hook.pos)
        }
    }

    @OnlyIn(Dist.CLIENT)
    private fun spawnBurst(start: Vec3d, end: Vec3d) {
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
            val invLength = MathHelper.fastInvSqrt(axisX * axisX + axisY * axisY + axisZ * axisZ)
            axisX *= invLength
            axisY *= invLength
            axisZ *= invLength

            angle = Math.random() * 2 * Math.PI
            sin = sin(angle)
            cos = cos(angle)

            var lifetime = startLifetime + (endLifetime - startLifetime) * (segmentStart / length)
            lifetime += Math.random() * lifetimeVariance // only randomly live longer, not shorter

            // the same particle color range as portal particles
            val colorRandom = Math.random() * 0.6 + 0.4
            val red = colorRandom * 0.9
            val green = colorRandom * 0.3
            val blue = colorRandom

            EnderHookParticleSystem.spawn(
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