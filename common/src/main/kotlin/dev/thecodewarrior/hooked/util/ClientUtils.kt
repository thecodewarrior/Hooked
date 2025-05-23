package dev.thecodewarrior.hooked.util

import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Vec3d

@Suppress("NOTHING_TO_INLINE")
inline fun VertexConsumer.vertex(stack: MatrixStack, x: Number, y: Number, z: Number): VertexConsumer {
    return this.vertex(stack.peek(), x.toFloat(), y.toFloat(), z.toFloat())
}

fun VertexConsumer.vertex(stack: MatrixStack, v: Vec3d): VertexConsumer {
    return this.vertex(stack.peek(), v.toVector3f())
}

@Suppress("NOTHING_TO_INLINE")
inline fun VertexConsumer.normal(stack: MatrixStack, x: Number, y: Number, z: Number): VertexConsumer {
    return this.normal(stack.peek(), x.toFloat(), y.toFloat(), z.toFloat())
}

fun VertexConsumer.normal(stack: MatrixStack, v: Vec3d): VertexConsumer {
    return this.normal(stack.peek(), v.x.toFloat(), v.y.toFloat(), v.z.toFloat())
}
