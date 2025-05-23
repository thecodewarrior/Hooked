package dev.thecodewarrior.hooked.bridge

import com.teamwizardry.librarianlib.mosaic.Mosaic
import dev.thecodewarrior.hooked.Hooked
import net.minecraft.util.Identifier

object HudSprites {
    @JvmStatic
    private val mosaic = Mosaic(Identifier.of(Hooked.MOD_ID, "textures/gui/hud.png"), 288, 27)
    @JvmStatic
    val hotbarFrame by mosaic
    @JvmStatic
    val hotbarFill by mosaic
    @JvmStatic
    val crosshairBackground by mosaic
    @JvmStatic
    val crosshairFill by mosaic
}