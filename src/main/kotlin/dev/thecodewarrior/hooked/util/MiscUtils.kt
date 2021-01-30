package dev.thecodewarrior.hooked.util

import com.teamwizardry.librarianlib.core.util.vec
import com.teamwizardry.librarianlib.math.plus
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.vector.Vector3d

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
