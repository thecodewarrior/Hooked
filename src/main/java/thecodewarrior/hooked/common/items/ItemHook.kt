package thecodewarrior.hooked.common.items

import baubles.api.BaubleType
import baubles.api.BaublesApi
import baubles.api.IBauble
import com.teamwizardry.librarianlib.features.base.item.ItemMod
import com.teamwizardry.librarianlib.features.kotlin.ifCap
import com.teamwizardry.librarianlib.features.kotlin.nbt
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagByte
import net.minecraft.util.ActionResult
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumHand
import net.minecraft.world.World
import org.lwjgl.input.Keyboard
import thecodewarrior.hooked.client.KeyBinds
import thecodewarrior.hooked.common.HookType
import thecodewarrior.hooked.common.capability.HooksCap
import java.util.*

/**
 * Created by TheCodeWarrior
 */
class ItemHook : ItemMod("hook", *HookType.values().map { "hook_" + it.toString().toLowerCase(Locale.ROOT) }.toTypedArray()), IBauble {
    init {
        maxStackSize = 1
    }

    override fun addInformation(stack: ItemStack, worldIn: World?, tooltip: MutableList<String>, flagIn: ITooltipFlag) {
        super.addInformation(stack, worldIn, tooltip, flagIn)
        val type = getType(stack)!!
        val hookLangName = type.toString().toLowerCase(Locale.ROOT)

        tooltip.add(I18n.format("tooltip.hooked:hook_$hookLangName.info"))

        if(type.count > 1) {
            if (isInhibited(stack)) {
                tooltip.add(I18n.format("tooltip.hooked:hook.inhibited"))
                if (GuiScreen.isShiftKeyDown())
                    tooltip.add(I18n.format("tooltip.hooked:hook.inhibited.help"))
            } else {
                if (GuiScreen.isShiftKeyDown())
                    tooltip.add(I18n.format("tooltip.hooked:hook.uninhibited.help"))
            }
        }

        if(GuiScreen.isShiftKeyDown()) {
            val controls = I18n.format("tooltip.hooked:hook_$hookLangName.controls", KeyBinds.keyFire.displayName)
            tooltip.addAll(controls.split("\\n").map { "- $it" })
        } else {
            tooltip.add(I18n.format("tooltip.hooked.showControls"))
        }
    }

    override fun onItemRightClick(worldIn: World, playerIn: EntityPlayer, handIn: EnumHand): ActionResult<ItemStack> {
        if(playerIn.isSneaking) {
            val type = getType(playerIn.getHeldItem(handIn))
            if(type?.count ?: 0 > 1) {
                val item = playerIn.getHeldItem(handIn).copy()
                if(isInhibited(item)) {
                    item.nbt["inhibited"] = null
                } else {
                    item.nbt["inhibited"] = NBTTagByte(1.toByte())
                }
                return ActionResult(EnumActionResult.SUCCESS, item)
            }
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
        fun isInhibited(stack: ItemStack): Boolean {
            return (stack.nbt["inhibited"] as? NBTTagByte)?.byte == 1.toByte()
        }

        fun getType(stack: ItemStack?): HookType? {
            if(stack == null || stack.item != ModItems.hook) return null
            return HookType.values()[stack.itemDamage % HookType.values().size]
        }
    }
}
