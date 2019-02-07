package thecodewarrior.hooked.common.hook

import com.teamwizardry.librarianlib.features.kotlin.div
import com.teamwizardry.librarianlib.features.kotlin.nbt
import com.teamwizardry.librarianlib.features.kotlin.plus
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.resources.I18n
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagByte
import net.minecraft.util.math.Vec3d
import thecodewarrior.hooked.client.KeyBinds
import thecodewarrior.hooked.common.items.ItemHook

open class BasicHookController(
    /**
     * The type that created this controller
     */
    type: HookType,
    /**
     * The player this controller is bound to
     */
    player: EntityPlayer,
    /**
     * The number of simultaneous hooks allowed
     */
    val fullCount: Int,
    /**
     * The maximum range from impact point to player
     */
    range: Double,
    /**
     * The speed of the fired hooks in m/t
     */
    speed: Double,
    /**
     * The speed the player is pulled toward the target point in m/t
     */
    pullStrength: Double,
    /**
     * The distance from the impact point to where the chain should attach
     */
    hookLength: Double,
    /**
     * The distance from the impact point to where the chain should attach
     */
    jumpBoost: Double
): HookController(type, player, fullCount, range, speed, pullStrength, hookLength, jumpBoost){

    protected val inhibited: Boolean
        get() {
            val item = ItemHook.getEquipped(player) ?: return false
            return (item.nbt["inhibited"] as? NBTTagByte)?.byte == 1.toByte()
        }

    override fun preTick() {
        count = if(inhibited) 1 else fullCount
    }

    override fun updateTargetPoint() {
        if (plantedHooks.isEmpty()) {
            targetPoint = null
        } else {
            targetPoint = plantedHooks.fold(Vec3d.ZERO) { acc, hook -> acc + hook.pos } / plantedHooks.size
        }
    }

    override fun modifyBreakSpeed(speed: Float): Float {
        if (plantedHooks.isEmpty()) {
            return speed
        } else {
            return speed * 5
        }
    }

    override fun moveBy(offset: Vec3d) {
        // nop
    }
}