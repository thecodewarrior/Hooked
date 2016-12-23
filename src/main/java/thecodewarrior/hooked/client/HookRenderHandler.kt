package thecodewarrior.hooked.client

import com.teamwizardry.librarianlib.common.util.minus
import com.teamwizardry.librarianlib.common.util.times
import com.teamwizardry.librarianlib.common.util.unaryMinus
import com.teamwizardry.librarianlib.common.util.vec
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraftforge.client.event.RenderPlayerEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.client.model.animation.Animation
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import thecodewarrior.hooked.common.HookTickHandler
import thecodewarrior.hooked.common.HookType
import thecodewarrior.hooked.common.capability.HooksCap

/**
 * Created by TheCodeWarrior
 */
object HookRenderHandler {

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun renderPlayerEvent(e: RenderPlayerEvent) {
        if(Minecraft.getMinecraft().player != e.entity)
            render(e.entity)
    }

    @SubscribeEvent
    fun renderWorldEvent(e: RenderWorldLastEvent) {
        render(Minecraft.getMinecraft().player)
    }

    @SubscribeEvent
    fun stitch(e: TextureStitchEvent) {
        renderers.forEach {
            it.endHandle.getResources().forEach {
                e.map.registerSprite(it)
            }
        }
        renderers.forEach { it.endHandle.purge() }
    }

    fun render(entity: Entity) {
        if(!entity.hasCapability(HooksCap.CAPABILITY, null))
            return
        val cap = entity.getCapability(HooksCap.CAPABILITY, null)!!

        val type = cap.hookType ?: return

        GlStateManager.pushAttrib()
        GlStateManager.pushMatrix()
        val lastPos = vec(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ)
        val partialOffset = (entity.positionVector-lastPos)*(1-Animation.getPartialTickTime())

        val globalize = -(entity.positionVector-partialOffset)
        GlStateManager.translate(globalize.xCoord, globalize.yCoord, globalize.zCoord)
//        GlStateManager.disableTexture2D()

//        GlStateManager.

        val waist = HookTickHandler.getWaistPos(entity)-partialOffset
        for(hook in cap.hooks)
            renderers[type.ordinal].renderHook(waist, hook, entity.world)

        GlStateManager.popMatrix()
        GlStateManager.popAttrib()
    }

    val renderers = HookType.values().map { HookRenderer(it) }.toTypedArray()
}
