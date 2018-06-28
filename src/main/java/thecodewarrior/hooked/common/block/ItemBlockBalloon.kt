package thecodewarrior.hooked.common.block

import com.teamwizardry.librarianlib.features.base.block.ItemModBlock
import net.minecraft.block.Block
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import thecodewarrior.hooked.client.KeyBinds
import thecodewarrior.hooked.common.HookType

class ItemBlockBalloon(block: Block): ItemModBlock(block) {
    override fun addInformation(stack: ItemStack, worldIn: World?, tooltip: MutableList<String>, flagIn: ITooltipFlag) {
        super.addInformation(stack, worldIn, tooltip, flagIn)
        tooltip.add(I18n.format("tile.hooked:balloon.info", KeyBinds.keyFire.displayName))
    }
}