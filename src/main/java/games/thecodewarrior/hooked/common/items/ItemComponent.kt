package games.thecodewarrior.hooked.common.items

import com.teamwizardry.librarianlib.core.client.ModelHandler
import com.teamwizardry.librarianlib.features.base.IExtraVariantHolder
import com.teamwizardry.librarianlib.features.base.item.ItemMod
import com.teamwizardry.librarianlib.features.kotlin.nbt
import games.thecodewarrior.hooked.common.config.HookTypes
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagString
import net.minecraft.util.NonNullList

class ItemComponent : ItemMod("component"), IExtraVariantHolder {

    override fun getSubItems(tab: CreativeTabs, subItems: NonNullList<ItemStack>) {
        if (!isInCreativeTab(tab)) return
        subItems.addAll(HookTypes.components.map {
            val stack = ItemStack(this, 1)
            stack.nbt["type"] = NBTTagString(it)
            stack
        })
    }

    override val extraVariants: Array<out String>
        get() = (HookTypes.components.map { "component_$it" } + listOf("component_missingno")).toTypedArray()
    override val meshDefinition: ((stack: ItemStack) -> ModelResourceLocation)?
        get() = { stack ->
            val type = (stack.nbt["type"] as? NBTTagString)?.string ?: "missingno"
            ModelHandler.getResource("hooked", "component_$type")
                ?: ModelResourceLocation("hooked:component_missingno")
        }

    override fun getTranslationKey(stack: ItemStack): String {
        val type = (stack.nbt["type"] as? NBTTagString)?.string ?: "missingno"
        return "item.hooked:hook.$type"
    }
}
