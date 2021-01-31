package dev.thecodewarrior.hooked.util

import com.teamwizardry.librarianlib.core.util.vec
import com.teamwizardry.librarianlib.math.minus
import com.teamwizardry.librarianlib.math.plus
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.vector.Vector3d
import java.awt.Color

fun PlayerEntity.fromWaistPos(waist: Vector3d): Vector3d {
    return waist - vec(0, this.eyeHeight / 2, 0)
}

fun PlayerEntity.getWaistPos(): Vector3d {
    return this.positionVec + vec(0, this.eyeHeight / 2, 0)
}

fun PlayerEntity.getWaistPos(partialTicks: Float): Vector3d {
    val x = MathHelper.lerp(partialTicks.toDouble(), prevPosX, posX)
    val y = MathHelper.lerp(partialTicks.toDouble(), prevPosY, posY)
    val z = MathHelper.lerp(partialTicks.toDouble(), prevPosZ, posZ)
    return vec(x, y + this.eyeHeight / 2, z)
}

fun Vector3d.isFinite(): Boolean {
    return x.isFinite() && y.isFinite() && z.isFinite()
}

fun Color.withAlpha(alpha: Float): Color {
    return Color(this.red / 255f, this.green / 255f, this.blue / 255f, alpha)
}