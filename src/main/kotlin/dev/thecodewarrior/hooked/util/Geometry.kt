package dev.thecodewarrior.hooked.util

import com.teamwizardry.librarianlib.math.*
import dev.thecodewarrior.hooked.quickhull3d.Point3d
import dev.thecodewarrior.hooked.quickhull3d.QuickHull3D
import net.minecraft.util.math.Vec3d
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

interface Constraint {
    fun constrain(point: Vec3d): Vec3d
}
object NoConstraint: Constraint {
    override fun constrain(point: Vec3d): Vec3d {
        return point
    }
}

class DynamicHull: Constraint {
    var pointSet = setOf<Vec3d>()
        private set(value) {
            field = value
            points = value.toList()
        }
    var points = listOf<Vec3d>()
        private set

    var shape: Constraint = NoConstraint
        private set

    fun update(newPoints: List<Vec3d>) {
        val uniquePoints = newPoints.toSet()
        if(uniquePoints == pointSet) return
        pointSet = uniquePoints
        if(points.isEmpty()) {
            shape = NoConstraint
            return
        }
        if(points.size == 1) {
            shape = Point(points[0])
            return
        }
        val line = toLine(0.5)
        if(line != null) {
            shape = line
            return
        }
        val polygon = toPolygon(0.1)
        if(polygon != null) {
            shape = polygon
            return
        }
        val hull = toHull(0.5)
        if(hull != null) {
            shape = hull
            return
        }
        shape = NoConstraint
    }

    override fun constrain(point: Vec3d): Vec3d {
        return shape.constrain(point)
    }

    fun toLine(threshold: Double): LineSegment? {
        val points = this.points.toMutableList()
        if(points.size < 2) return null

        var origin = points.first()
        points.remove(origin)
        var endpoint = points.first()
        points.remove(endpoint)
        if(points.isEmpty()) return LineSegment(origin, endpoint)

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

        return LineSegment(origin, endpoint)
    }

    // https://www.ilikebigbits.com/2015_03_04_plane_from_points.html
    fun toPolygon(threshold: Double): Polygon? {
        if(points.size < 3) return null
        if(points.size == 3) return Polygon(listOf(points[0], points[1], points[2]))

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
            // `- centroid` because QuickHull3D uses absolute coordinates in its distance threshold, so we need to use
            // small relative coordinates
            it - normal * distance - centroid
        }


        val otherPoint = normal * (threshold*10)
        val hull = QuickHull3D()
        hull.explicitDistanceTolerance = threshold / 100.0
        hull.build((listOf(otherPoint.toPoint3d()) +
            flatPoints.map {
                Point3d(it.x, it.y, it.z)
            }).toTypedArray()
        )

        val vertices = hull.vertices
        val otherIndex = vertices.indexOfFirst {
            it.x == otherPoint.x && it.y == otherPoint.y && it.z == otherPoint.z
        }
        val faces = hull.faces

        val face = faces.first { otherIndex !in it }

        return Polygon(face.map {
            vertices[it].toVec3d() + centroid
        })
    }

    fun toHull(threshold: Double): Hull? {
        try {
            return Hull(points, threshold)
        } catch(e: Exception) {
            return null
        }
    }
}

data class Point(val point: Vec3d): Constraint {
    override fun constrain(point: Vec3d): Vec3d {
        return point
    }
}

data class LineSegment(val a: Vec3d, val b: Vec3d): Constraint {
    private val length = (b - a).length()
    private val normalized = if(length == 0.0) vec(0.0, 1.0, 0.0) else (b - a) / length

    override fun constrain(point: Vec3d): Vec3d {
        val dot = (point - a) dot normalized
        return a + normalized * dot.clamp(0.0, length)
    }
}

data class Polygon(val points: List<Vec3d>): Constraint {
    /**
     * The list of edges in 3d world space
     */
    val edges: List<LineSegment>
    /**
     * [points] transformed onto the plane with the Z component discarded
     */
    val points2d: List<Vec2d>
    /**
     * Transforms points in the world to points relative to the plane. This is the inverse of [planeToPoint]
     */
    val pointToPlane: Matrix4d
    /**
     * Transforms points relative to the plane (x/y coordinates + z depth) to points in the world.
     * * (0, 0, 0) translates to [a]
     * * the +X axis faces along `(b - a).normalize()`
     * * the +Y axis faces along `((b - a) cross normal).normalize()`
     * * the +Z axis is placed along `normal`
     */
    val planeToPoint: Matrix4d

    /**
     * points[0], used as the "origin" for 2d space and when calculating the normal
     */
    val a: Vec3d
    /**
     * points[[bIndex]], used as the first point when calculating the normal
     */
    val b: Vec3d
    val bIndex: Int
    /**
     * points[[cIndex]], used as the second point when calculating the normal
     */
    val c: Vec3d
    val cIndex: Int

    /**
     * The normal vector, pointing out of the clockwise side of the face. defined as
     * `((b - a) cross (c - a)).normalize()`
     */
    val normal: Vec3d

    init {
        a = points[0]
        bIndex = max(1, points.size/3)
        b = points[bIndex]
        cIndex = min(points.size-1, bIndex + max(1, points.size/3))
        c = points[cIndex]

        val edge1 = b - a
        val edge2 = c - a
        normal = (edge1 cross edge2).normalize()
        val axis1 = edge1.normalize()
        val axis2 = (edge1 cross normal).normalize()

        edges = points.mapIndexed { i, point ->
            LineSegment(point, points[(i+1) % points.size])
        }

        planeToPoint = Matrix4d(
            axis1.x, axis2.x, normal.x, a.x,
            axis1.y, axis2.y, normal.y, a.y,
            axis1.z, axis2.z, normal.z, a.z,
            0.0, 0.0, 0.0, 1.0
        )
        pointToPlane = planeToPoint.invert()

        points2d = points.map { pointToPlane(it) }
    }

    private fun pointToPlane(point: Vec3d): Vec2d {
        val transformed = pointToPlane * point
        return Vec2d(transformed.x, transformed.y)
    }

    private fun planeToPoint(point: Vec2d): Vec3d {
        return planeToPoint * Vec3d(point.x, point.y, 0.0)
    }

    override fun constrain(point: Vec3d): Vec3d {
        val point2d = pointToPlane(point)
        if(point2d in this) return planeToPoint(point2d)
        return edges.map { it.constrain(point) }.minByOrNull { (it - point).lengthSquared() }!!
    }

    // https://stackoverflow.com/a/8721483
    operator fun contains(test: Vec2d): Boolean {
        var i = 0
        var j = points2d.size - 1
        var result = false
        while (i < points2d.size) {
            if (points2d[i].y > test.y != points2d[j].y > test.y && test.x < (points2d[j].x - points2d[i].x) * (test.y - points2d[i].y) / (points2d[j].y - points2d[i].y) + points2d[i].x) {
                result = !result
            }
            j = i++
        }
        return result
    }

}

data class Hull(val points: List<Vec3d>, val threshold: Double): Constraint {
    val faces: List<Polygon>

    init {
        val origin = points[0]
        val quickHull = QuickHull3D()
        quickHull.explicitDistanceTolerance = threshold / 100.0
        quickHull.build(points.map { (it - origin).toPoint3d() }.toTypedArray())
        val vertices = quickHull.vertices.map { it.toVec3d() + origin }
        faces = quickHull.faces.map { indices ->
            Polygon(indices.map { vertices[it] })
        }
    }

    override fun constrain(point: Vec3d): Vec3d {
        if(faces.all { (point-it.a) dot it.normal <= 0 }) // point is inside the hull
            return point
        return faces.map { it.constrain(point) }.minBy { (point - it).lengthSquared() }!!
    }
}

private fun Vec3d.toPoint3d() = Point3d(x, y, z)
private fun Point3d.toVec3d() = Vec3d(x, y, z)

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
