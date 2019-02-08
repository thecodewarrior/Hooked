package games.thecodewarrior.hooked.common.hook

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation

class FlightHookType(
    name: ResourceLocation,
    count: Int,
    range: Double,
    speed: Double,
    pullStrength: Double,
    hookLength: Double,
    jumpBoost: Double,
    cooldown: Int
): BasicHookType(name, count, range, speed, pullStrength, hookLength, jumpBoost, cooldown) {

    override fun create(player: EntityPlayer): HookController {
        return FlightHookController(this, player, count, range, speed, pullStrength, hookLength, jumpBoost, cooldown)
    }
}