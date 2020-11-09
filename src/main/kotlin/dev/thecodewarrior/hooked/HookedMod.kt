package dev.thecodewarrior.hooked

import com.teamwizardry.librarianlib.core.util.kotlin.loc
import com.teamwizardry.librarianlib.foundation.BaseMod
import com.teamwizardry.librarianlib.foundation.TagWrappers
import net.minecraft.inventory.container.PlayerContainer
import net.minecraft.util.Tuple
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.InterModComms
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent
import top.theillusivec4.curios.api.CuriosAPI
import top.theillusivec4.curios.api.imc.CurioIMCMessage

@Mod("hooked")
object HookedMod: BaseMod(true) {
    val HOOKED_CURIOS_TAG = TagWrappers.item("curios:hooked")
    init {
        HookedModItems.registerItems(registrationManager)
        HookedModItems.registerItemDatagen(registrationManager)
    }

    @SubscribeEvent
    fun imcEnqueue(e: InterModEnqueueEvent) {
        InterModComms.sendTo("curios", CuriosAPI.IMC.REGISTER_TYPE) {
            CurioIMCMessage("hooked").also {
                it.size = 1
            }
        }
        InterModComms.sendTo("curios", CuriosAPI.IMC.REGISTER_ICON) {
            Tuple("hooked", loc("hooked:gui/hook_slot"))
        }
    }

    @SubscribeEvent
    fun textureStitch(e: TextureStitchEvent.Pre) {
        if(e.map.textureLocation == PlayerContainer.LOCATION_BLOCKS_TEXTURE) {
            e.addSprite(loc("hooked:gui/hook_slot"))
        }
    }
}