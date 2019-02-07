package thecodewarrior.hooked.client.render

import com.teamwizardry.librarianlib.core.client.ClientTickHandler
import com.teamwizardry.librarianlib.features.helpers.vec
import com.teamwizardry.librarianlib.features.kotlin.*
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.client.renderer.block.model.IBakedModel
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.init.Blocks
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.lwjgl.opengl.GL11
import thecodewarrior.hooked.HookedMod
import thecodewarrior.hooked.client.ModelHandle
import thecodewarrior.hooked.common.hook.BasicHookController
import thecodewarrior.hooked.common.hook.BasicHookType
import thecodewarrior.hooked.common.hook.FlightHookController
import thecodewarrior.hooked.common.hook.FlightHookType
import thecodewarrior.hooked.common.hook.HookController
import thecodewarrior.hooked.common.hook.HookType
import thecodewarrior.hooked.common.util.DynamicHull
import thecodewarrior.hooked.common.util.Hull
import thecodewarrior.hooked.common.util.LineSegment
import thecodewarrior.hooked.common.util.Minecraft
import thecodewarrior.hooked.common.util.NoConstraint
import thecodewarrior.hooked.common.util.Point
import thecodewarrior.hooked.common.util.Polygon
import java.awt.Color

/**
 * Created by TheCodeWarrior
 */
class FlightHookRenderer(
    type: BasicHookType,
    /**
     * The gap between the player and the start of the chain. Allows
     */
    playerGap: Double,
    hookModel: ResourceLocation,
    ropeTextureVertical: ResourceLocation,
    ropeTextureHorizontal: ResourceLocation
): BasicHookRenderer(type, playerGap, hookModel, ropeTextureVertical, ropeTextureHorizontal) {
    var points = setOf<Vec3d>()
    var lastChange: Int = 0

    var tris = listOf<List<Vec3d>>()
    var lines = listOf<Pair<Vec3d, Vec3d>>()

    override fun postRender(controller: HookController) {
        super.postRender(controller)
        val flightController = controller as FlightHookController
        val volume = flightController.volume
        if(volume.pointSet != points) {
            points = volume.pointSet.toSet()
            createPrimitives(volume)

            lastChange = ClientTickHandler.ticks
        }
        if(ClientTickHandler.ticks < lastChange + 60) {
            val delta = ClientTickHandler.ticks - lastChange
            drawVolume(Color.PINK, Color.RED, 1 - delta / 60f)
        }
    }

    fun drawVolume(faceColor: Color, lineColor: Color, alpha: Float) {
        val tess = Tessellator.getInstance()
        val vb = tess.buffer

        GlStateManager.disableTexture2D()
        GlStateManager.disableCull()
        GlStateManager.enableBlend()

        GlStateManager.color(lineColor.red/255f, lineColor.green/255f, lineColor.blue/255f, lineColor.alpha/255f * alpha)
        vb.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION)
        lines.forEach {
            vb.pos(it.first).endVertex()
            vb.pos(it.second).endVertex()
        }
        tess.draw()

//        GlStateManager.color(faceColor.red/255f, faceColor.green/255f, faceColor.blue/255f, faceColor.alpha/255f * alpha)
//        tris.forEach { strip ->
//            vb.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION)
//
//            strip.forEach {
//                vb.pos(it).endVertex()
//            }
//
//            tess.draw()
//        }

        GlStateManager.enableTexture2D()
        GlStateManager.enableCull()
        GlStateManager.disableBlend()
    }

    fun createPrimitives(volume: DynamicHull) {
        tris = createTris(volume)
        lines = createLines(volume)
    }

    fun createTris(volume: DynamicHull): List<List<Vec3d>> {
        val shape = volume.shape
        return when(shape) {
            is Polygon -> listOf(createTris(shape))
            is Hull -> shape.faces.map {
                createTris(it)
            }
            else -> emptyList()
        }
    }

    fun createTris(polygon: Polygon): List<Vec3d> {
        val points = mutableListOf<Vec3d>()
        for(i in 0 .. polygon.points.size/2) {
            if(i == 0) {
                points.add(polygon.points[0])
                points.add(polygon.points[1])
            } else {
                points.add(polygon.points[polygon.points.size - i])
                if(i+1 < polygon.points.size)
                    points.add(polygon.points[i+1])
            }
        }
        return points
    }

    fun createLines(volume: DynamicHull): List<Pair<Vec3d, Vec3d>> {
        val shape = volume.shape
        return when(shape) {
            is LineSegment -> listOf(shape.a to shape.b)
            is Polygon -> createLines(shape)
            is Hull -> shape.faces.flatMap { createLines(it) }
            else -> emptyList()
        }
    }

    fun createLines(polygon: Polygon): List<Pair<Vec3d, Vec3d>> {
        val lines = mutableListOf<Pair<Vec3d, Vec3d>>()
        polygon.points.forEachIndexed { i, point ->
            lines.add(point to polygon.points[(i + 1) % polygon.points.size])
        }
        return lines
    }
}
