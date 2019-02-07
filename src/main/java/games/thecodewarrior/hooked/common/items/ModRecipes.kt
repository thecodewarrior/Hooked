package games.thecodewarrior.hooked.common.items

import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import net.minecraft.inventory.InventoryCrafting
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipe
import net.minecraft.item.crafting.Ingredient
import net.minecraft.util.JsonUtils
import net.minecraft.util.NonNullList
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.crafting.CraftingHelper
import net.minecraftforge.common.crafting.IRecipeFactory
import net.minecraftforge.oredict.ShapelessOreRecipe
import java.util.concurrent.ThreadLocalRandom

/**
 * Created by TheCodeWarrior
 */
class ShapelessOreToolRecipeFactory : IRecipeFactory {
    override fun parse(context: net.minecraftforge.common.crafting.JsonContext?, json: JsonObject?): IRecipe {
        val group = JsonUtils.getString(json, "group", "")

        val ings = NonNullList.create<Ingredient>()
        val tools = NonNullList.create<Ingredient>()
        JsonUtils.getJsonArray(json, "ingredients").forEach { ele ->
            ings.add(CraftingHelper.getIngredient(ele, context))
        }
        JsonUtils.getJsonArray(json, "tools").forEach { ele ->
            tools.add(CraftingHelper.getIngredient(ele, context))
        }

        if (ings.isEmpty())
            throw JsonParseException("No ingredients for shapeless recipe")

        val itemstack = CraftingHelper.getItemStack(JsonUtils.getJsonObject(json, "result"), context)
        return ShapelessOreToolRecipe(if (group.isEmpty()) null else ResourceLocation(group), ings, tools, itemstack)
    }
}

class ShapelessOreToolRecipe(group: ResourceLocation?, input: NonNullList<Ingredient>?, val tools: NonNullList<Ingredient>, result: ItemStack) : ShapelessOreRecipe(group, input, result) {
    override fun getRemainingItems(inv: InventoryCrafting): NonNullList<ItemStack> {
        val list = super.getRemainingItems(inv)
        (0 until inv.sizeInventory).forEach {
            val stack = inv.getStackInSlot(it) ?: return@forEach
            if(tools.any { it.test(stack)}) {
                val newStack = stack.copy()
                newStack.attemptDamageItem(1, ThreadLocalRandom.current(), null)
                if(newStack.itemDamage <= newStack.maxDamage)
                    list[it] = newStack
            }
        }
        return list
    }



}
