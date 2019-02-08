package games.thecodewarrior.hooked.common.hook

import games.thecodewarrior.hooked.client.render.FlightHookRenderer
import games.thecodewarrior.hooked.client.render.HookRenderer
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation

class FlightHookType: BasicHookType() {
    override fun initRenderer(): HookRenderer<*, *> {
        return FlightHookRenderer(this)
    }

    override fun createController(player: EntityPlayer): HookController<out FlightHookType> {
        return FlightHookController(this, player)
    }
}