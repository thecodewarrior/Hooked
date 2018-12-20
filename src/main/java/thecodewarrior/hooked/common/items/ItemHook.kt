package thecodewarrior.hooked.common.items

import baubles.api.BaubleType
import baubles.api.BaublesApi
import baubles.api.IBauble
import com.teamwizardry.librarianlib.features.base.item.ItemMod
import com.teamwizardry.librarianlib.features.kotlin.nbt
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
import thecodewarrior.hooked.client.KeyBinds
import thecodewarrior.hooked.common.HookType
import net.minecraftforge.fml.common.Optional
import thecodewarrior.hooked.HookLog
import thecodewarrior.hooked.HookedConfig
import java.util.*

/**
 * Created by TheCodeWarrior
 */
@Optional.Interface(iface = "baubles.api.IBauble", modid = "baubles")
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

        fun getItem(player: EntityPlayer): ItemStack? {
            val stacks = mutableListOf<ItemStack>()
            if(HookedConfig.searchLocations and HookedConfig.SEARCH_BAUBLES != 0) {
                stacks.addAll(baubles(player))
            }
            if(HookedConfig.searchLocations and HookedConfig.SEARCH_HANDS != 0) {
                stacks.add(player.heldItemMainhand)
                stacks.add(player.heldItemOffhand)
            }
            if(HookedConfig.searchLocations and HookedConfig.SEARCH_HOTBAR != 0) {
                stacks.addAll(hotbar.map { player.inventory.getStackInSlot(it) })
            }
            if(HookedConfig.searchLocations and HookedConfig.SEARCH_INVENTORY != 0) {
                stacks.addAll(main.map { player.inventory.getStackInSlot(it) })
            }
            return stacks.find { it.item == ModItems.hook }
        }

        private val armor = 36..39
        private val hotbar = 0..8
        private val main = 9..35

        private val baubles: (EntityPlayer) -> List<ItemStack> by lazy {
            return@lazy find@{ player: EntityPlayer ->
                try {
                    val baubles = BaublesApi.getBaublesHandler(player)
                    return@find (0 until baubles.slots).map { baubles.getStackInSlot(it) }
                } catch(e: NoClassDefFoundError) {
                    HookLog.error("Baubles not found! Change Hooked's search_location config property!")
                    throw e
                }
            }
        }
    }
}
