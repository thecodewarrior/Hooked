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
    if(max < 0) throw IllegalArgumentException("max length $max is negative")
    if(lengthSquared == 0.0) return this
    if(lengthSquared < max * max) return this
    val length = sqrt(lengthSquared)
    return this * (max / length)
}

fun List<Vec3d>.collinear(threshold: Double): Pair<Vec3d, Vec3d>? {
    val points = this.toMutableList()
    if(points.size < 2) return null

    var origin = points.first()
    points.remove(origin)
    var endpoint = points.first()
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

// https://www.ilikebigbits.com/2015_03_04_plane_from_points.html
fun List<Vec3d>.coplanar(threshold: Double): Triple<Vec3d, Vec3d, Vec3d>? {
    val points = this
    if(points.size < 3) return null
    if(points.size == 3) return Triple(points[0], points[1], points[2])

    var sum = Vec3d.ZERO
    points.forEach {
        sum += it
    }
    val centroid = sum * (1.0 / points.size)

    // Calc full 3x3 covariance matrix, excluding symmetries:
    var xx = 0.0; var xy = 0.0; var xz = 0.0
    var yy = 0.0; var yz = 0.0; var zz = 0.0

    points.forEach {
        val r = it - centroid
        xx += r.x * r.x
        xy += r.x * r.y
        xz += r.x * r.z
        yy += r.y * r.y
        yz += r.y * r.z
        zz += r.z * r.z
    }

    val det_x = yy*zz - yz*yz
    val det_y = xx*zz - xz*xz
    val det_z = xx*yy - xy*xy

    val det_max = max(det_x, max(det_y, det_z))
    if(det_max <= 0.0) {
        return null // The points don't span a plane
    }

    // Pick path with best conditioning:
    val dir =
        if(det_max == det_x) {
            Vec3d(
                det_x,
                xz*yz - xy*zz,
                xy*yz - xz*yy
            )
        } else if(det_max == det_y) {
            Vec3d(
                xz*yz - xy*zz,
                det_y,
                xy*xz - yz*xx
            )
        } else {
            Vec3d(
                xy*yz - xz*yy,
                xy*xz - yz*xx,
                det_z
            )
        }

    val normal = dir.normalize()
    val flatPoints = points.map {
        val distance = (it - centroid) dot normal
        if(distance > threshold) return null
        it - normal * distance
    }


//    Some(plane_from_point_and_normal(centroid, normalize(dir)))

    return null
}

// https://gamedev.stackexchange.com/a/96487
fun raySphereIntersection(point: Vec3d, direction: Vec3d, center: Vec3d, radius: Double): Double? {
    val m = point - center
    val b = m dot direction
    val c = (m dot m) - radius * radius

    // Exit if r’s origin outside s (c > 0) and r pointing away from s (b > 0)
    if (c > 0.0f && b > 0.0f) return null
    val discr = b*b - c

    // A negative discriminant corresponds to ray missing sphere
    if (discr < 0.0f) return null

    // Ray now found to intersect sphere, compute smallest t value of intersection
    return -b - sqrt(discr)
}
