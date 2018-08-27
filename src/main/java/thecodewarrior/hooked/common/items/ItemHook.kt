package thecodewarrior.hooked.common.items

import baubles.api.BaubleType
import baubles.api.BaublesApi
import baubles.api.IBauble
import com.teamwizardry.librarianlib.features.base.item.ItemMod
import com.teamwizardry.librarianlib.features.kotlin.nbt
import com.teamwizardry.librarianlib.features.kotlin.toRl
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagByte
import net.minecraft.nbt.NBTTagString
import net.minecraft.util.ActionResult
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumHand
import net.minecraft.world.World
import thecodewarrior.hooked.client.KeyBinds
import thecodewarrior.hooked.common.HookTypeEnum
import thecodewarrior.hooked.common.hook.HookType
import java.util.*

/**
 * Created by TheCodeWarrior
 */
class ItemHook : ItemMod("hook", *HookTypeEnum.values().map { "hook_" + it.toString().toLowerCase(Locale.ROOT) }.toTypedArray()), IBauble {
    init {
        maxStackSize = 1
    }

    override fun addInformation(stack: ItemStack, worldIn: World?, tooltip: MutableList<String>, flagIn: ITooltipFlag) {
        super.addInformation(stack, worldIn, tooltip, flagIn)
        val type = getType(stack)!!

        if(GuiScreen.isShiftKeyDown()) {
            type.addFullInformation(stack, tooltip)
        } else {
            type.addBasicInformation(stack, tooltip)
        }
        tooltip.add(I18n.format("tooltip.hooked:hook.${type.registryName}.info"))
    }

    override fun onItemRightClick(worldIn: World, playerIn: EntityPlayer, handIn: EnumHand): ActionResult<ItemStack> {
        if(playerIn.isSneaking) {
            val type = getType(playerIn.getHeldItem(handIn))
            val result = type?.adjustStack(playerIn.getHeldItem(handIn))
            if(result != null)
                return ActionResult(EnumActionResult.SUCCESS, result)
        }
        return super.onItemRightClick(worldIn, playerIn, handIn)
    }

    override fun getBaubleType(p0: ItemStack?): BaubleType {
        return BaubleType.TRINKET
    }

    override fun canEquip(itemstack: ItemStack, player: EntityLivingBase): Boolean {
        if(player !is EntityPlayer)
            return false
        val handler = BaublesApi.getBaublesHandler(player)
        return !(0 until handler.slots).any { handler.getStackInSlot(it).item == ModItems.hook }
    }

    override fun willAutoSync(itemstack: ItemStack?, player: EntityLivingBase?): Boolean {
        return true
    }

    companion object {
        fun getType(stack: ItemStack?): HookType? {
            if(stack == null || stack.item != ModItems.hook) return null
            val resourceLocation = (stack.nbt["type"] as? NBTTagString)?.string?.toRl() ?: "missingno".toRl()
            return HookType.REGISTRY.getValue(resourceLocation)
        }

        fun getEquipped(player: EntityPlayer): ItemStack? {
            val baubles = BaublesApi.getBaublesHandler(player)
            for (i in 0 until baubles.slots) {
                val stack = baubles.getStackInSlot(i)
                if(stack.item == ModItems.hook)
                    return stack
            }
            return null
        }
    }
}
