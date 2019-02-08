package games.thecodewarrior.hooked.common.hook

import com.teamwizardry.librarianlib.features.kotlin.div
import com.teamwizardry.librarianlib.features.kotlin.nbt
import com.teamwizardry.librarianlib.features.kotlin.plus
import com.teamwizardry.librarianlib.features.saving.Save
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.resources.I18n
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagByte
import net.minecraft.util.math.Vec3d
import games.thecodewarrior.hooked.client.KeyBinds
import games.thecodewarrior.hooked.common.items.ItemHook
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.relauncher.Side
import java.util.UUID

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
    jumpBoost: Double,
    /**
     *
     */
    override val cooldown: Int
): HookController(type, player, fullCount, range, speed, pullStrength, hookLength, jumpBoost), ICooldownHookController {
    protected val inhibited: Boolean
        get() {
            val item = ItemHook.getEquipped(player) ?: return false
            return (item.nbt["inhibited"] as? NBTTagByte)?.byte == 1.toByte()
        }

    @Save
    override var cooldownCounter = 0

    override fun preTick() {
        if(cooldownCounter > 0) cooldownCounter--
        count = if(inhibited) 1 else fullCount
    }

    override fun fireHook(startPos: Vec3d, normal: Vec3d, uuid: UUID) {
        if(cooldownCounter == 0) {
            super.fireHook(startPos, normal, uuid)
            cooldownCounter = cooldown
        } else {
            markDirty()
        }
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