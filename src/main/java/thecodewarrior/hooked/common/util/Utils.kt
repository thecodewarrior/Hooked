package thecodewarrior.hooked.common.util

import com.teamwizardry.librarianlib.features.kotlin.div
import com.teamwizardry.librarianlib.features.kotlin.dot
import com.teamwizardry.librarianlib.features.kotlin.minus
import com.teamwizardry.librarianlib.features.kotlin.plus
import com.teamwizardry.librarianlib.features.kotlin.times
import net.minecraft.client.Minecraft
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

fun Double.finiteOrDefault(defaultValue: Double) = if(this.isFinite()) this else defaultValue
fun Float.finiteOrDefault(defaultValue: Float) = if(this.isFinite()) this else defaultValue

fun Double.finiteOrDefault(defaultValue: () -> Double) = if(this.isFinite()) this else defaultValue()
fun Float.finiteOrDefault(defaultValue: () -> Float) = if(this.isFinite()) this else defaultValue()

@Suppress("FunctionName")
@SideOnly(Side.CLIENT)
fun Minecraft(): Minecraft = Minecraft.getMinecraft()

fun Vec3d.isFinite(): Boolean {
    return x.isFinite() || y.isFinite() || z.isFinite()
}

fun Vec3d.clampLength(max: Double): Vec3d {
    val lengthSquared = this.lengthSquared()
    if(lengthSquared < max * max) return this
    val length = sqrt(lengthSquared)
    return this * (max / length)
}

fun List<Vec3d>.collinear(threshold: Double): Pair<Vec3d, Vec3d>? {
    val points = this.toMutableSet()
    if(points.size < 2) return null

    var origin = points.first()
    points.remove(origin)
    var endpoint = points.last()
    points.remove(endpoint)
    if(points.isEmpty()) return origin to endpoint

    var axis = (endpoint - origin).normalize()

    var max = (endpoint - origin).length()

    val thresholdSq = threshold * threshold
    points.forEach {
        val dot = (it - origin) dot axis
        if((axis * dot - (it - origin)).lengthSquared() > thresholdSq) return null
        if(dot < 0) {
            max += -dot
            origin = it
            axis = (endpoint - origin).normalize()
        } else if(dot > max) {
            max = dot
            endpoint = it
            axis = (endpoint - origin).normalize()
        }
    }

    return origin to endpoint
}
