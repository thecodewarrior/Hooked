package thecodewarrior.hooked.common.hook

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation

class FlightHookType(
    name: ResourceLocation,
    /**
     * The number of simultaneous hooks allowed
     */
    count: Int,
    /**
     * The maximum range from impact point to player
     */
    range: Double,
    /**
     * The speed of the fired hooks in m/t
     */
    speed: Double,
    /**
     * The distance from the impact point to where the chain should attach
     */
    hookLength: Double
): BasicHookType(name, count, range, speed, 0.0, hookLength) {

    override fun create(player: EntityPlayer): HookController {
        return FlightHookController(this, player, count, range, speed, hookLength)
    }
}