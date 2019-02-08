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
    type: BasicHookType,
    /**
     * The player this controller is bound to
     */
    player: EntityPlayer
): HookController<BasicHookType>(type, player), ICooldownHookController {
    protected val inhibited: Boolean
        get() {
            val item = ItemHook.getEquipped(player) ?: return false
            return (item.nbt["inhibited"] as? NBTTagByte)?.byte == 1.toByte()
        }

    override var count: Int = type.count

    override fun preTick() {
        count = if(inhibited) 1 else type.count
    }

    override fun updateTargetPoint() {
        if (plantedHooks.isEmpty()) {
            targetPoint = null
        } else {
            targetPoint = plantedHooks.fold(Vec3d.ZERO) { acc, hook -> acc + hook.pos } / plantedHooks.size
        }
    }

    override fun moveBy(offset: Vec3d) {
        // nop
    }
}