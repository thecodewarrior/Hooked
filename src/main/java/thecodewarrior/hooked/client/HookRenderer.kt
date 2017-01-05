package thecodewarrior.hooked.client

import com.teamwizardry.librarianlib.common.util.*
import net.minecraft.client.Minecraft
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
        if(lastModel == model && lastQuads != null) {
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

        if(n != null) {
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

        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)
        Minecraft.getMinecraft().blockRendererDispatcher.blockModelRenderer.renderModelBrightnessColor(endHandle.get(), 1f, 1f,1f,1f)

        GlStateManager.popMatrix()


        GlStateManager.pushMatrix()

        val tess = Tessellator.getInstance()
        val vb = tess.buffer

        val distance = (hook.pos - waist).lengthVector()
        val normal = (hook.pos - waist) / distance

        rY = signAngle(vec(0, 0, 1), (normal * vec(1, 0, 1)).normalize(), vec(0, 1, 0))
        rX = signAngle(vec(0, 1, 0), normal, null)

        GlStateManager.translate(waist.xCoord, waist.yCoord, waist.zCoord)
        GlStateManager.rotate(rY, 0f, 1f, 0f)
        GlStateManager.rotate(rX, 1f, 0f, 0f)
        GlStateManager.rotate(45f, 0f, 1f, 0f)

        val radius = 0.5


        val maxV = distance/(radius*2)

        Minecraft.getMinecraft().renderEngine.bindTexture(ropeTextureVertical)
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX)
        // @formatter:off
        vb.pos( radius, 0.0,      0.0).tex(0.0, maxV).endVertex()
        vb.pos(-radius, 0.0,      0.0).tex(1.0, maxV).endVertex()
        vb.pos(-radius, distance, 0.0).tex(1.0, 0.0).endVertex()
        vb.pos( radius, distance, 0.0).tex(0.0, 0.0).endVertex()

        vb.pos( radius, distance, 0.0).tex(0.0, 0.0).endVertex()
        vb.pos(-radius, distance, 0.0).tex(1.0, 0.0).endVertex()
        vb.pos(-radius, 0.0,      0.0).tex(1.0, maxV).endVertex()
        vb.pos( radius, 0.0,      0.0).tex(0.0, maxV).endVertex()
        // @formatter:on
        tess.draw()

        Minecraft.getMinecraft().renderEngine.bindTexture(ropeTextureHorizontal)
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX)
        // @formatter:off
        vb.pos(0.0, 0.0,       radius).tex(0.0, maxV).endVertex()
        vb.pos(0.0, 0.0,      -radius).tex(1.0, maxV).endVertex()
        vb.pos(0.0, distance, -radius).tex(1.0, 0.0).endVertex()
        vb.pos(0.0, distance,  radius).tex(0.0, 0.0).endVertex()

        vb.pos(0.0, distance,  radius).tex(0.0, 0.0).endVertex()
        vb.pos(0.0, distance, -radius).tex(1.0, 0.0).endVertex()
        vb.pos(0.0, 0.0,      -radius).tex(1.0, maxV).endVertex()
        vb.pos(0.0, 0.0,       radius).tex(0.0, maxV).endVertex()
        // @formatter:on
        tess.draw()

        GlStateManager.popMatrix()
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
            vb.putBrightness4(b,b,b,b)
        }
        tess.draw()
    }
}
