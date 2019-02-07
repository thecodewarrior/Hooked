package games.thecodewarrior.hooked.client.render

import com.teamwizardry.librarianlib.features.helpers.vec
import com.teamwizardry.librarianlib.features.kotlin.minus
import com.teamwizardry.librarianlib.features.kotlin.times
import com.teamwizardry.librarianlib.features.kotlin.unaryMinus
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraftforge.client.event.RenderPlayerEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.client.model.animation.Animation
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import games.thecodewarrior.hooked.common.HookTypeEnum
import games.thecodewarrior.hooked.common.capability.HooksCap
import games.thecodewarrior.hooked.common.hook.HookController

/**
 * Created by TheCodeWarrior
 */
object HookRenderHandler {

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun renderPlayerEvent(e: RenderPlayerEvent.Post) {
        if(Minecraft.getMinecraft().player != e.entity)
            render(e.entity)
    }

    @SubscribeEvent
    fun renderWorldEvent(e: RenderWorldLastEvent) {
        GlStateManager.pushMatrix()
        val player = Minecraft.getMinecraft().player

        val lastPos = vec(player.lastTickPosX, player.lastTickPosY, player.lastTickPosZ)
        val partialOffset = (player.positionVector-lastPos)*(1-Animation.getPartialTickTime())

        val globalize = -(player.positionVector-partialOffset)
        GlStateManager.translate(globalize.x, globalize.y, globalize.z)

//        render(Minecraft.getMinecraft().player)

        for(entity in Minecraft.getMinecraft().world.playerEntities) {
            render(entity)
        }

        GlStateManager.popMatrix()
    }

    @SubscribeEvent
    fun stitch(e: TextureStitchEvent) {
        HookRenderer.REGISTRY.forEach { renderer ->
            renderer.reloadResources()
            renderer.registerSprites(e.map)
        }
    }

    fun render(entity: Entity) {
        if(!entity.hasCapability(HooksCap.CAPABILITY, null))
            return
        val cap = entity.getCapability(HooksCap.CAPABILITY, null)!!

        val controller = cap.controller ?: return

        GlStateManager.pushMatrix()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        cap.renderer?.render(controller)

        GlStateManager.popMatrix()
    }
}
