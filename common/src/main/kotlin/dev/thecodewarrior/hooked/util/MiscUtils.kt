package dev.thecodewarrior.hooked.util

import com.teamwizardry.librarianlib.core.util.vec
import com.teamwizardry.librarianlib.math.Quaternion
import com.teamwizardry.librarianlib.math.minus
import com.teamwizardry.librarianlib.math.plus
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import java.awt.Color

fun PlayerEntity.fromWaistPos(waist: Vec3d): Vec3d {
    return waist - vec(0, this.standingEyeHeight / 2, 0)
}

fun PlayerEntity.getWaistPos(): Vec3d {
    return this.pos + vec(0, this.standingEyeHeight / 2, 0)
}

fun PlayerEntity.getWaistPos(partialTicks: Float): Vec3d {
    val x = MathHelper.lerp(partialTicks.toDouble(), prevX, x)
    val y = MathHelper.lerp(partialTicks.toDouble(), prevY, y)
    val z = MathHelper.lerp(partialTicks.toDouble(), prevZ, z)
    return vec(x, y + this.standingEyeHeight / 2, z)
}

val Entity.actualMotion: Vec3d get() = vec(this.x - this.prevX, this.y - this.prevY, this.z - this.prevZ)

fun Vec3d.isFinite(): Boolean {
    return x.isFinite() && y.isFinite() && z.isFinite()
}
