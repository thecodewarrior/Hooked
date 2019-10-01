package games.thecodewarrior.hooked.common.items

import baubles.api.BaubleType
import baubles.api.BaublesApi
import baubles.api.IBauble
import com.teamwizardry.librarianlib.core.client.ModelHandler
import com.teamwizardry.librarianlib.features.base.IExtraVariantHolder
import com.teamwizardry.librarianlib.features.base.item.ItemMod
import com.teamwizardry.librarianlib.features.kotlin.nbt
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagString
import net.minecraft.util.NonNullList
import net.minecraft.world.World
import games.thecodewarrior.hooked.common.config.HookTypes
import games.thecodewarrior.hooked.common.hook.HookType
import net.minecraft.client.renderer.block.model.ModelResourceLocation

/**
 * Created by TheCodeWarrior
 */
class ItemHook : ItemMod("hook"), IExtraVariantHolder, IBauble {
    init {
        maxStackSize = 1
    }

    override fun getSubItems(tab: CreativeTabs, subItems: NonNullList<ItemStack>) {
        if (!isInCreativeTab(tab)) return
        subItems.addAll(HookTypes.map {
            val stack = ItemStack(this, 1)
            stack.nbt["type"] = NBTTagString(it.key)
            stack
        })
    }

    override val extraVariants: Array<out String>
        get() = (HookTypes.map { "hook_${it.value.id}" } + listOf("hook_missingno", "hook_tab_icon")).toTypedArray()
    override val meshDefinition: ((stack: ItemStack) -> ModelResourceLocation)?
        get() = { stack ->
            val type = (stack.nbt["type"] as? NBTTagString)?.string ?: "missingno"
            ModelHandler.getResource("hooked", "hook_$type")
                ?: ModelResourceLocation("hooked:hook_missingno")
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

    override fun getTranslationKey(stack: ItemStack): String {
        val type = (stack.nbt["type"] as? NBTTagString)?.string ?: "missingno"
        return "item.hooked:hook.$type"
    }

    override fun addInformation(stack: ItemStack, worldIn: World?, tooltip: MutableList<String>, flagIn: ITooltipFlag) {
        val prefix = getTranslationKey(stack)
        val info = I18n.format(
            if(GuiScreen.isShiftKeyDown())
                "$prefix.tip.detail"
            else
                "$prefix.tip.normal"
        )
        if(info.isNotBlank())
            tooltip.addAll(info.split("\\n"))

        if(flagIn.isAdvanced) {
            val type = (stack.nbt["type"] as? NBTTagString)?.string ?: "missingno"
            tooltip.add("§8Type: $type")
        }
    }

    companion object {
        fun getType(stack: ItemStack?): HookType? {
            if(stack == null || stack.item != ModItems.hook) return null
            val name = (stack.nbt["type"] as? NBTTagString)?.string ?: "missingno"
            return HookTypes[name]
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
