package thecodewarrior.hooked.client

import com.teamwizardry.librarianlib.common.util.*
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.VertexBuffer
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
import thecodewarrior.hooked.common.HookType
import thecodewarrior.hooked.common.capability.HookInfo


/**
 * Created by TheCodeWarrior
 */
class HookRenderer(val type: HookType) {

    val endHandle = ModelHandle(ResourceLocation(HookedMod.MODID + ":hook/${type.name.toLowerCase()}"))
    val ropeTextureVertical = ResourceLocation(HookedMod.MODID, "textures/hooks/${type.name.toLowerCase()}/chain1.png")
    val ropeTextureHorizontal = ResourceLocation(HookedMod.MODID, "textures/hooks/${type.name.toLowerCase()}/chain2.png")

    private var lastModel: IBakedModel? = null
    private var lastQuads: List<BakedQuad>? = null
    val quads: List<BakedQuad>
        get() {
            val model = endHandle.get()
            if (lastModel == model && lastQuads != null) {
                return lastQuads!!
            }
            val quads = mutableListOf<BakedQuad>()
            quads.addAll(model.getQuads(Blocks.AIR.defaultState, null, 123456789))
            EnumFacing.values().forEach { quads.addAll(model.getQuads(Blocks.AIR.defaultState, it, 123456789)) }

            lastModel = model
            lastQuads = quads
            return quads
        }

    fun signAngle(a: Vec3d, b: Vec3d, n: Vec3d?): Float {
        val cross = a cross b
        val s = cross.lengthVector()
        val c = a dot b
        var angle = MathHelper.atan2(s, c)

        if (n != null) {
            if (n dot cross < 0) { // Or > 0
                angle = -angle
            }
        }

        return Math.toDegrees(angle).toFloat()
    }

    fun renderHook(waist: Vec3d, hook: HookInfo, world: World) {

        GlStateManager.pushMatrix()

        var rY = signAngle(vec(0, 0, 1), (hook.direction * vec(1, 0, 1)).normalize(), vec(0, 1, 0))
        var rX = signAngle(vec(0, 1, 0), hook.direction, null)

        GlStateManager.translate(hook.pos.xCoord, hook.pos.yCoord, hook.pos.zCoord)
        GlStateManager.rotate(rY, 0f, 1f, 0f)
        GlStateManager.rotate(rX, 1f, 0f, 0f)
        GlStateManager.translate(-0.5, 0.0, -0.5)

        val hookLightPos = BlockPos(hook.pos + hook.direction * (type.hookLength / 2))
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)
        Minecraft.getMinecraft().blockRendererDispatcher.blockModelRenderer.renderModelBrightnessColor(endHandle.get(),
                (world.getLightBrightness(hookLightPos) * 2f).clamp(0f, 1f), 1f, 1f, 1f)

        GlStateManager.popMatrix()

        GlStateManager.pushMatrix()

        val distance = (hook.pos - waist).lengthVector()
        val normal = (hook.pos - waist) / distance

        rY = signAngle(vec(0, 0, 1), (normal * vec(1, 0, 1)).normalize(), vec(0, 1, 0))
        rX = signAngle(vec(0, 1, 0), normal, null)

        GlStateManager.translate(waist.xCoord, waist.yCoord, waist.zCoord)
        GlStateManager.rotate(rY, 0f, 1f, 0f)
        GlStateManager.rotate(rX, 1f, 0f, 0f)
        GlStateManager.rotate(45f, 0f, 1f, 0f)

        val radius = 0.5

        Minecraft.getMinecraft().renderEngine.bindTexture(ropeTextureVertical)
        chain(distance, waist, normal, vec(radius, 0, 0), world)
        Minecraft.getMinecraft().renderEngine.bindTexture(ropeTextureHorizontal)
        chain(distance, waist, normal, vec(0, 0, radius), world)

        GlStateManager.popMatrix()
    }

    fun chain(distance: Double, waist: Vec3d, normal: Vec3d, offset: Vec3d, world: World) {
        val tess = Tessellator.getInstance()
        val vb = tess.buffer

        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR)

        var len = distance
        while (len > 0) {
            if (len > 1) {
                len -= 1.0 // decrement before because we are rendering backward, hook to waist instead of waist to hook
                val blockPos = BlockPos(waist + normal * (len + 0.5))
                chainQuad(blockPos, len, offset, 1.0, world, vb)
            } else {
                chainQuad(BlockPos(waist + normal * (len / 2)), 0.0, offset, len, world, vb)

                len = 0.0
            }
        }
        tess.draw()
    }

    fun chainQuad(blockpos: BlockPos, distance: Double, offset: Vec3d, length: Double, world: World, vb: VertexBuffer) {
        val b = (world.getLightBrightness(blockpos) * 2).clamp(0f, 1f)

        val beg = distance
        val end = distance+length
        // @formatter:off
        vb.pos( offset.xCoord, beg,  offset.zCoord).tex(0.0, length).color(b, b, b, 1f).endVertex()
        vb.pos(-offset.xCoord, beg, -offset.zCoord).tex(1.0, length).color(b, b, b, 1f).endVertex()
        vb.pos(-offset.xCoord, end, -offset.zCoord).tex(1.0, 0.0   ).color(b, b, b, 1f).endVertex()
        vb.pos( offset.xCoord, end,  offset.zCoord).tex(0.0, 0.0   ).color(b, b, b, 1f).endVertex()


        vb.pos( offset.xCoord, end,  offset.zCoord).tex(0.0, 0.0   ).color(b, b, b, 1f).endVertex()
        vb.pos(-offset.xCoord, end, -offset.zCoord).tex(1.0, 0.0   ).color(b, b, b, 1f).endVertex()
        vb.pos(-offset.xCoord, beg, -offset.zCoord).tex(1.0, length).color(b, b, b, 1f).endVertex()
        vb.pos( offset.xCoord, beg,  offset.zCoord).tex(0.0, length).color(b, b, b, 1f).endVertex()
        // @formatter:on
    }

    fun renderModel(quads: List<BakedQuad>, pos: Vec3d, world: World) {
        val b = world.getBlockState(BlockPos(pos)).getPackedLightmapCoords(world, BlockPos(pos))

        val tess = Tessellator.getInstance()
        val vb = tess.buffer

        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM)
        quads.forEach {
            vb.addVertexData(it.vertexData)
            val normal = it.face.directionVec
            vb.putNormal(normal.x.toFloat(), normal.y.toFloat(), normal.z.toFloat())
            vb.putBrightness4(b, b, b, b)
        }
        tess.draw()
    }
}
