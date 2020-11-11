package dev.thecodewarrior.hooked.util

import com.teamwizardry.librarianlib.math.plus
import com.teamwizardry.librarianlib.math.vec
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d

fun PlayerEntity.getWaistPos(): Vec3d {
    return this.positionVector + vec(0, this.eyeHeight / 2, 0)
}

fun PlayerEntity.getWaistPos(partialTicks: Float): Vec3d {
    val x = MathHelper.lerp(partialTicks.toDouble(), prevPosX, posX)
    val y = MathHelper.lerp(partialTicks.toDouble(), prevPosY, posY)
    val z = MathHelper.lerp(partialTicks.toDouble(), prevPosZ, posZ)
    return vec(x, y + this.eyeHeight / 2, z)
}

fun Vec3d.isFinite(): Boolean {
    return x.isFinite() && y.isFinite() && z.isFinite()
}
