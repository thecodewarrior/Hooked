package dev.thecodewarrior.hooked.client

import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.systems.RenderSystem
import com.teamwizardry.librarianlib.core.util.Client
import com.teamwizardry.librarianlib.core.util.mapSrgName
import dev.thecodewarrior.hooked.hook.ClientHookProcessor
import ll.dev.thecodewarrior.mirror.Mirror
import net.minecraft.client.GameSettings
import net.minecraft.client.gui.AbstractGui
import net.minecraft.client.settings.AttackIndicatorStatus
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.container.INamedContainerProvider
import net.minecraft.util.HandSide
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.util.math.EntityRayTraceResult
import net.minecraft.util.math.RayTraceResult
import net.minecraft.world.GameType
import net.minecraft.world.World
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.SubscribeEvent

object HudRenderer {
    private val HUD_TEXTURE = ResourceLocation("hooked:textures/gui/cooldown.png")

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun hudEvent(e: RenderGameOverlayEvent.Post) {
        when (e.type) {
            RenderGameOverlayEvent.ElementType.CROSSHAIRS -> drawCrosshair(e.matrixStack)
            RenderGameOverlayEvent.ElementType.HOTBAR -> drawHotbar(e.matrixStack)
            else -> {}
        }
    }

    fun drawCrosshair(matrixStack: MatrixStack) {
        val mc = Client.minecraft
        val gamesettings: GameSettings = mc.gameSettings

        // conditions copied from IngameGui.func_238456_d_()

        // only render the crosshair in first-person
        if (gamesettings.pointOfView.func_243192_a()) {

            // only render if the player isn't in spectator
            // except we should actually render if they're in spectator and looking at a chest, furnace, etc.
            if (mc.playerController!!.currentGameType != GameType.SPECTATOR ||
                this.isTargetNamedMenuProvider(mc.objectMouseOver)) {

                if(
                    gamesettings.showDebugInfo && !gamesettings.hideGUI &&
                    !mc.player!!.hasReducedDebug() && !gamesettings.reducedDebugInfo
                ) {
                    // nop. this is where the debug axes crosshair is rendered
                } else {
                    drawCrosshairCooldownIndicator(matrixStack)
                }
            }
        }
    }

    fun drawCrosshairCooldownIndicator(matrixStack: MatrixStack) {
        val mc = Client.minecraft
        val scaledWidth = Client.window.scaledWidth
        val scaledHeight = Client.window.scaledHeight
        val blitOffset = this.blitOffset.get<Int>(Client.minecraft.ingameGUI)

        if (mc.gameSettings.attackIndicator != AttackIndicatorStatus.CROSSHAIR) return
        if (ClientHookProcessor.hudCooldown == 0.0) return

        mc.getTextureManager().bindTexture(HUD_TEXTURE)

        val spriteWidth = 13
        val spriteHeight = 9

        val spriteX = scaledWidth / 2 + 7
        val spriteY = (scaledHeight - spriteHeight) / 2

        AbstractGui.blit(
            matrixStack,
            spriteX, spriteY, blitOffset,
            0f, 18f,  // uOffset/vOffset
            spriteWidth, spriteHeight,  // uWidth/vHeight
            27, 288 // texture height/width
        )
        val cooldownSprite = (ClientHookProcessor.hudCooldown * 21).toInt()
        AbstractGui.blit(
            matrixStack,
            spriteX, spriteY, blitOffset,
            spriteWidth + spriteWidth * cooldownSprite.toFloat(), 18f,  // uOffset/vOffset
            spriteWidth, spriteHeight,
            27, 288 // texture height/width
        )

        mc.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION)
    }

    // IngameGui.isTargetNamedMenuProvider
    private fun isTargetNamedMenuProvider(rayTraceIn: RayTraceResult?): Boolean {
        when {
            rayTraceIn == null -> {
                return false
            }
            rayTraceIn.type == RayTraceResult.Type.ENTITY -> {
                return (rayTraceIn as EntityRayTraceResult).entity is INamedContainerProvider
            }
            rayTraceIn.type == RayTraceResult.Type.BLOCK -> {
                val blockpos = (rayTraceIn as BlockRayTraceResult).pos
                val world: World = Client.minecraft.world!!
                return world.getBlockState(blockpos).getContainer(world, blockpos) != null
            }
            else -> {
                return false
            }
        }
    }

    private fun drawHotbar(matrixStack: MatrixStack) {
        // from ForgeIngameGui.renderHotbar
        if(Client.minecraft.playerController!!.currentGameType == GameType.SPECTATOR)
            return
        // From IngameGui.renderHotbar
        val playerEntity = getRenderViewPlayer() ?: return
        val mc = Client.minecraft
        if(Client.minecraft.gameSettings.attackIndicator != AttackIndicatorStatus.HOTBAR)
            return

        if (ClientHookProcessor.hudCooldown == 0.0) return

        val scaledWidth = Client.window.scaledWidth
        val scaledHeight = Client.window.scaledHeight
        val blitOffset = this.blitOffset.get<Int>(Client.minecraft.ingameGUI)

        val attachmentSide: HandSide = playerEntity.primaryHand
        var attachmentX = scaledWidth / 2
        when(attachmentSide) {
            HandSide.LEFT -> attachmentX -= 91 + 6
            HandSide.RIGHT -> attachmentX += 91 + 6
        }

        val f = mc.player!!.getCooledAttackStrength(0.0f)
        if (f < 1.0f) {
            when(attachmentSide) {
                HandSide.LEFT -> attachmentX -= 18 + 3
                HandSide.RIGHT -> attachmentX += 18 + 3
            }
        }

        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f)
        mc.getTextureManager().bindTexture(HUD_TEXTURE)

        val spriteWidth = 13
        val spriteHeight = 18

        val spriteX = when(attachmentSide) {
            HandSide.LEFT -> attachmentX - spriteWidth
            HandSide.RIGHT -> attachmentX
        }
        val spriteY = scaledHeight - spriteHeight - 2

        AbstractGui.blit(
            matrixStack,
            spriteX, spriteY, blitOffset,
            0f, 0f,  // uOffset/vOffset
            spriteWidth, spriteHeight,  // uWidth/vHeight
            27, 288 // texture height/width
        )
        val cooldownSprite = (ClientHookProcessor.hudCooldown * 25).toInt()
        AbstractGui.blit(
            matrixStack,
            spriteX + 1, spriteY + 1, blitOffset,
            spriteWidth + (spriteWidth-2) * cooldownSprite.toFloat(), 1f,  // uOffset/vOffset
            spriteWidth - 2, spriteHeight - 2,
            27, 288 // texture height/width
        )

        mc.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION)
    }

    private fun getRenderViewPlayer(): PlayerEntity? {
        return Client.minecraft.getRenderViewEntity() as? PlayerEntity
    }

    private val blitOffset = Mirror.reflectClass<AbstractGui>().getDeclaredField(mapSrgName("field_230662_a_"))
}