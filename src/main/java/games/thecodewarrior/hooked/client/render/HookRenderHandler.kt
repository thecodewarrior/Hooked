package games.thecodewarrior.hooked.client.render

import com.teamwizardry.librarianlib.features.helpers.vec
import com.teamwizardry.librarianlib.features.kotlin.minus
import com.teamwizardry.librarianlib.features.kotlin.times
import com.teamwizardry.librarianlib.features.kotlin.toRl
import com.teamwizardry.librarianlib.features.kotlin.unaryMinus
import com.teamwizardry.librarianlib.features.sprite.Texture
import games.thecodewarrior.hooked.common.capability.HooksCap
import games.thecodewarrior.hooked.common.config.HookTypes
import games.thecodewarrior.hooked.common.hook.HookController
import games.thecodewarrior.hooked.common.hook.HookType
import games.thecodewarrior.hooked.common.hook.ICooldownHookController
import games.thecodewarrior.hooked.common.util.Minecraft
import games.thecodewarrior.hooked.common.util.WtfException
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.EnumHandSide
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderPlayerEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.client.model.animation.Animation
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import kotlin.math.max

/**
 * Created by TheCodeWarrior
 */
object HookRenderHandler {
    val cooldownTexture = Texture("hooked:textures/cooldown.png".toRl())
    val hotbarFrame = cooldownTexture.getSprite("hotbar_frame", 13, 18)
    val hotbarFill = cooldownTexture.getSprite("hotbar_fill", 11, 16)
    val cursorBackground = cooldownTexture.getSprite("cursor_background", 13, 9)
    val cursorFill = cooldownTexture.getSprite("cursor_fill", 13, 9)

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
        HookTypes.forEach { _, type ->
            type.renderer.reloadResources()
            type.renderer.registerSprites(e.map)
        }
    }

    fun render(entity: Entity) {
        if(!entity.hasCapability(HooksCap.CAPABILITY, null))
            return
        val cap = entity.getCapability(HooksCap.CAPABILITY, null)!!

        val controller = cap.controller ?: return

        GlStateManager.pushMatrix()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        @Suppress("UNCHECKED_CAST")
        (controller.type.renderer as HookRenderer<HookType, HookController<*>>).render(controller)

        GlStateManager.popMatrix()
    }

    @SubscribeEvent
    fun renderCrosshairOverlay(e: RenderGameOverlayEvent.Post) {
        val mc = Minecraft()
        if(e.type != RenderGameOverlayEvent.ElementType.CROSSHAIRS || mc.gameSettings.attackIndicator != 1) return
        val scaledResolution = ScaledResolution(mc)
        val player = mc.player
        if(!player.hasCapability(HooksCap.CAPABILITY, null))
            return
        val cap = player.getCapability(HooksCap.CAPABILITY, null)!!

        val controller = cap.controller ?: return
        val cooldown: Int
        val cooldownCounter: Int
        if(controller is ICooldownHookController) {
            cooldown = controller.cooldown
            cooldownCounter = controller.cooldownCounter
        } else return
        if(cooldownCounter == 0)
            return

        GlStateManager.pushMatrix()
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO)
        GlStateManager.enableAlpha()
        GlStateManager.disableCull()

        val pointedEntity = mc.pointedEntity

        val mcHeight = when {
            mc.player.getCooledAttackStrength(0.0F) < 1.0f -> 6
            mc.player.getCooledAttackStrength(0.0F) >= 1.0F && mc.player.cooldownPeriod > 5.0F &&
                pointedEntity is EntityLivingBase && pointedEntity.isEntityAlive() -> 9
            else -> 0
        }

        val x = (scaledResolution.scaledWidth / 2 - hotbarFill.height / 2).toDouble()
        val y = (scaledResolution.scaledHeight / 2 + 9 + mcHeight).toDouble()

        val frameFraction = cooldownCounter / cooldown.toDouble()
        val frame = max(0, (frameFraction * hotbarFill.frameCount).toInt() - 1)

        cooldownTexture.bind()
        GlStateManager.translate(x + hotbarFill.height, y + hotbarFill.width, 0.0)
        GlStateManager.rotate(90f, 0f, 0f, 1f)
        GlStateManager.scale(-1.0, 1.0, 1.0)

        hotbarFill.draw(frame, 0f, 0f)

        GlStateManager.enableCull()
        GlStateManager.popMatrix()
    }

    @SubscribeEvent
    fun renderHotbarOverlay(e: RenderGameOverlayEvent.Post) {
        val mc = Minecraft()
        if(e.type != RenderGameOverlayEvent.ElementType.HOTBAR || mc.gameSettings.attackIndicator != 2) return
        val scaledResolution = ScaledResolution(mc)
        val player = mc.player
        if(!player.hasCapability(HooksCap.CAPABILITY, null))
            return
        val cap = player.getCapability(HooksCap.CAPABILITY, null)!!

        val controller = cap.controller ?: return
        val cooldown: Int
        val cooldownCounter: Int
        if(controller is ICooldownHookController) {
            cooldown = controller.cooldown
            cooldownCounter = controller.cooldownCounter
        } else return
        if(cooldownCounter == 0)
            return

        val enumhandside = player.primaryHand.opposite()

        GlStateManager.pushMatrix()
        GlStateManager.enableAlpha()
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F)

        val mcWidth = if(mc.player.getCooledAttackStrength(0.0F) < 1.0) 18 + 2 else 0

        val y = scaledResolution.scaledHeight - 20.toFloat()
        val x = scaledResolution.scaledWidth/2 +
            when(enumhandside) {
                EnumHandSide.LEFT -> (91 + 6) + mcWidth
                EnumHandSide.RIGHT -> -(91 + 4) - mcWidth - hotbarFrame.width
                else -> throw WtfException()
            }.toFloat()

        val frameFraction = cooldownCounter / cooldown.toDouble()
        val frame = max(0, (frameFraction * hotbarFill.frameCount).toInt() - 1)

        cooldownTexture.bind()
        hotbarFrame.draw(0, x, y)
        hotbarFill.draw(frame, x+1, y+1)

        GlStateManager.enableCull()
        GlStateManager.popMatrix()
    }
}
