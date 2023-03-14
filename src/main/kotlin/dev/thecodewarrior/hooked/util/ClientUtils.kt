package dev.thecodewarrior.hooked.util

import com.teamwizardry.librarianlib.core.bridge.IMatrix3f
import com.teamwizardry.librarianlib.core.bridge.IMatrix4f
import com.teamwizardry.librarianlib.core.util.mixinCast
import com.teamwizardry.librarianlib.math.Quaternion
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Vec3d
import java.awt.Color

fun Color.withAlpha(alpha: Float): Color {
    return Color(this.red / 255f, this.green / 255f, this.blue / 255f, alpha)
}

fun Quaternion.toMc(): net.minecraft.util.math.Quaternion = net.minecraft.util.math.Quaternion(
    this.x.toFloat(),
    this.y.toFloat(),
    this.z.toFloat(),
    this.w.toFloat()
)

@Suppress("NOTHING_TO_INLINE")
inline fun VertexConsumer.vertex(stack: MatrixStack, x: Number, y: Number, z: Number): VertexConsumer {
    val xf = x.toFloat()
    val yf = y.toFloat()
    val zf = z.toFloat()
    val matrix = mixinCast<IMatrix4f>(stack.peek().positionMatrix)
    return this.vertex(
        matrix.transformX(xf, yf, zf).toDouble(),
        matrix.transformY(xf, yf, zf).toDouble(),
        matrix.transformZ(xf, yf, zf).toDouble(),
    )
}

fun VertexConsumer.vertex(stack: MatrixStack, v: Vec3d): VertexConsumer {
    val xf = v.x.toFloat()
    val yf = v.y.toFloat()
    val zf = v.z.toFloat()
    val matrix = mixinCast<IMatrix4f>(stack.peek().positionMatrix)
    return this.vertex(
        matrix.transformX(xf, yf, zf).toDouble(),
        matrix.transformY(xf, yf, zf).toDouble(),
        matrix.transformZ(xf, yf, zf).toDouble(),
    )
}

@Suppress("NOTHING_TO_INLINE")
inline fun VertexConsumer.normal(stack: MatrixStack, x: Number, y: Number, z: Number): VertexConsumer {
    val xf = x.toFloat()
    val yf = y.toFloat()
    val zf = z.toFloat()
    val matrix = mixinCast<IMatrix3f>(stack.peek().normalMatrix)
    return this.normal(
        matrix.transformX(xf, yf, zf),
        matrix.transformY(xf, yf, zf),
        matrix.transformZ(xf, yf, zf),
    )
}


fun VertexConsumer.normal(stack: MatrixStack, v: Vec3d): VertexConsumer {
    val xf = v.x.toFloat()
    val yf = v.y.toFloat()
    val zf = v.z.toFloat()
    val matrix = mixinCast<IMatrix3f>(stack.peek().normalMatrix)
    return this.normal(
        matrix.transformX(xf, yf, zf),
        matrix.transformY(xf, yf, zf),
        matrix.transformZ(xf, yf, zf),
    )
}
