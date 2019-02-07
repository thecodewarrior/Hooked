package games.thecodewarrior.hooked.common.util

import com.teamwizardry.librarianlib.features.helpers.vec
import com.teamwizardry.librarianlib.features.kotlin.*
import net.minecraft.util.math.Vec3d

/**
 * Created by TheCodeWarrior
 */
object Barycentric {
    private fun ScTP(a: Vec3d, b: Vec3d, c: Vec3d): Double {
        return a dot (b cross c)
    }

    /*
    http://stackoverflow.com/a/38546111/1541907
     */
    fun toBarycentric(p: Vec3d, a: Vec3d, b: Vec3d, c: Vec3d, d: Vec3d): Vec4d {
        val vap = p - a
        val vbp = p - b

        val vab = b - a
        val vac = c - a
        val vad = d - a

        val vbc = c - b
        val vbd = d - b
        // ScTP computes the scalar triple product
        val va6 = ScTP(vbp, vbd, vbc)
        val vb6 = ScTP(vap, vac, vad)
        val vc6 = ScTP(vap, vad, vab)
        val vd6 = ScTP(vap, vab, vac)
        val v6 = 1 / ScTP(vab, vac, vad)

        return Vec4d(va6 * v6, vb6 * v6, vc6 * v6, vd6 * v6)
    }

    /*
    http://gamedev.stackexchange.com/a/23745
     */
    fun toBarycentric(p: Vec3d, a: Vec3d, b: Vec3d, c: Vec3d): Vec3d
    {
        val v0 = b - a
        val v1 = c - a
        val v2 = p - a

        val d00 = v0 dot v0
        val d01 = v0 dot v1
        val d11 = v1 dot v1
        val d20 = v2 dot v0
        val d21 = v2 dot v1
        val denom = d00 * d11 - d01 * d01
        val v = (d11 * d20 - d01 * d21) / denom
        val w = (d00 * d21 - d01 * d20) / denom
        val u = 1.0f - v - w

        return vec(u,v,w)
    }

    fun toCartesian(p: Vec4d, a: Vec3d, b: Vec3d, c: Vec3d, d: Vec3d): Vec3d {
        val n = p.x + p.y + p.z + p.w
        return a*(p.x/n) + b*(p.y/n) + c*(p.z/n) + d*(p.w/n)
    }

    fun toCartesian(p: Vec3d, a: Vec3d, b: Vec3d, c: Vec3d): Vec3d {
        val n = p.x + p.y + p.z
        return a*(p.x/n) + b*(p.y/n) + c*(p.z/n)
    }
}

class Vec4d(x_: Number, y_: Number, z_: Number, w_: Number) {
    @Transient val xi: Int = x_.toInt()
    @Transient val yi: Int = y_.toInt()
    @Transient val zi: Int = z_.toInt()
    @Transient val wi: Int = w_.toInt()


    @Transient val xf: Float = x_.toFloat()
    @Transient val yf: Float = y_.toFloat()
    @Transient val zf: Float = z_.toFloat()
    @Transient val wf: Float = w_.toFloat()


    @Transient val x: Double = x_.toDouble()
    @Transient val y: Double = y_.toDouble()
    @Transient val z: Double = z_.toDouble()
    @Transient val w: Double = w_.toDouble()

    @delegate: Transient
    val length: Double by lazy { Math.sqrt(x*x + y*y + z*z + w*w) }
}
