package thecodewarrior.hooked.client.render

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
import thecodewarrior.hooked.common.hook.HookController
import thecodewarrior.hooked.common.hook.HookType
import thecodewarrior.hooked.common.util.Minecraft

/**
 * Created by TheCodeWarrior
 */
open class BasicHookRenderer(
    type: BasicHookType,
    /**
     * The gap between the player and the start of the chain. Allows
     */
    val playerGap: Double,
    val hookModel: ResourceLocation,
    val ropeTextureVertical: ResourceLocation,
    val ropeTextureHorizontal: ResourceLocation
): AbstractHookRenderer<BasicHookType, BasicHookController>(type) {
    val endHandle = ModelHandle(hookModel)

    init {
        val name = type.registryName ?: "hooked:missingno".toRl()
    }


    override fun reloadResources() {
        endHandle.reload()
    }

    override fun registerSprites(map: TextureMap) {
        endHandle.getResources().forEach {
            map.registerSprite(it)
        }
    }

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

    override fun renderHookTip(pos: Vec3d, direction: Vec3d) {
        val world = Minecraft().world
        val hookLightPos = BlockPos(pos + direction * (type.hookLength / 2))
        Minecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)

        val state = world.getBlockState(hookLightPos)
        val lightmap = state.getPackedLightmapCoords(world, hookLightPos)

        val vb = Tessellator.getInstance().buffer
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK)
        quads.forEach { quad ->
            vb.addVertexData(quad.vertexData)
            vb.putBrightness4(lightmap, lightmap, lightmap, lightmap)
        }
        Tessellator.getInstance().draw()
    }

    override fun renderChain(distance: Double, direction: Vec3d) {
        val world = Minecraft.getMinecraft().world
        val player = Minecraft.getMinecraft().player
        val waist = HookController.getWaistPos(player)

        Minecraft.getMinecraft().entityRenderer.enableLightmap()

        val radius = 0.5

        Minecraft.getMinecraft().renderEngine.bindTexture(ropeTextureVertical)
        chain(distance, waist, direction, vec(radius, 0, 0), world)
        Minecraft.getMinecraft().renderEngine.bindTexture(ropeTextureHorizontal)
        chain(distance, waist, direction, vec(0, 0, radius), world)

        Minecraft.getMinecraft().entityRenderer.disableLightmap()
    }

    private fun chain(distance: Double, waist: Vec3d, normal: Vec3d, offset: Vec3d, world: World) {
        val tess = Tessellator.getInstance()
        val vb = tess.buffer

        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR)

        var len = distance
        while (len > playerGap) {
            if (len > playerGap+1) {
                len -= 1.0 // decrement before because we are rendering backward, hook to waist instead of waist to hook
                val blockPos = BlockPos(waist + normal * (len + 0.5))
                chainQuad(blockPos, len, offset, 1.0, world, vb)
            } else {
                val blockPos = BlockPos(waist + normal * len / 2)
                chainQuad(blockPos, playerGap, offset, len-playerGap, world, vb)

                len = 0.0
            }
        }
        tess.draw()
    }

    private fun chainQuad(blockpos: BlockPos, distance: Double, offset: Vec3d, length: Double, world: World, vb: BufferBuilder) {
        val state = world.getBlockState(blockpos)
        val lightmap = state.getPackedLightmapCoords(world, blockpos)
        val skylight = (lightmap shr 16) and 0xFFFF
        val blocklight = lightmap and 0xFFFF
        val b = 1f

        val beg = distance
        val end = distance+length
        // @formatter:off
        vb.pos( offset.x, beg,  offset.z).tex(0.0, length).lightmap(skylight, blocklight).color(b, b, b, 1f).endVertex()
        vb.pos(-offset.x, beg, -offset.z).tex(1.0, length).lightmap(skylight, blocklight).color(b, b, b, 1f).endVertex()
        vb.pos(-offset.x, end, -offset.z).tex(1.0, 0.0   ).lightmap(skylight, blocklight).color(b, b, b, 1f).endVertex()
        vb.pos( offset.x, end,  offset.z).tex(0.0, 0.0   ).lightmap(skylight, blocklight).color(b, b, b, 1f).endVertex()


        vb.pos( offset.x, end,  offset.z).tex(0.0, 0.0   ).lightmap(skylight, blocklight).color(b, b, b, 1f).endVertex()
        vb.pos(-offset.x, end, -offset.z).tex(1.0, 0.0   ).lightmap(skylight, blocklight).color(b, b, b, 1f).endVertex()
        vb.pos(-offset.x, beg, -offset.z).tex(1.0, length).lightmap(skylight, blocklight).color(b, b, b, 1f).endVertex()
        vb.pos( offset.x, beg,  offset.z).tex(0.0, length).lightmap(skylight, blocklight).color(b, b, b, 1f).endVertex()
        // @formatter:on
    }

}
