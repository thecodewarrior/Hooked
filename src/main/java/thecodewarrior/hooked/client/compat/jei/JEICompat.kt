package thecodewarrior.hooked.client.compat.jei

import mezz.jei.IngredientBaseListFactory
import mezz.jei.api.BlankModPlugin
import mezz.jei.api.IJeiHelpers
import mezz.jei.api.IModRegistry
import mezz.jei.api.JEIPlugin
import mezz.jei.api.ingredients.IIngredients
import mezz.jei.api.recipe.BlankRecipeWrapper
import mezz.jei.api.recipe.IRecipeHandler
import mezz.jei.api.recipe.VanillaRecipeCategoryUid
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper
import mezz.jei.util.BrokenCraftingRecipeException
import mezz.jei.util.ErrorUtil
import net.minecraft.item.ItemStack
import thecodewarrior.hooked.common.items.ShapelessOreToolRecipe
import thecodewarrior.hooked.common.items.ToolClass
import java.util.*


/**
 * Created by TheCodeWarrior
 */
@JEIPlugin
class JEICompat : BlankModPlugin() {

    override fun register(registry: IModRegistry) {
        registry.addRecipeHandlers(
                ShapelessOreToolRecipeHandler(registry.jeiHelpers)
        )
    }
}

class ShapelessOreToolRecipeHandler(private val jeiHelpers: IJeiHelpers) : IRecipeHandler<ShapelessOreToolRecipe> {
    override fun getRecipeCategoryUid(recipe: ShapelessOreToolRecipe) = getRecipeCategoryUid()
    override fun getRecipeCategoryUid() = VanillaRecipeCategoryUid.CRAFTING
    override fun isRecipeValid(recipe: ShapelessOreToolRecipe) = true
    override fun getRecipeClass() = ShapelessOreToolRecipe::class.java

    override fun getRecipeWrapper(recipe: ShapelessOreToolRecipe) = ShapelessOreToolRecipeWrapper(jeiHelpers, recipe)

}

class ShapelessOreToolRecipeWrapper(private val jeiHelpers: IJeiHelpers, private val recipe: ShapelessOreToolRecipe) : BlankRecipeWrapper(), ICraftingRecipeWrapper {

    init {
        for (input in this.recipe.input) {
            if (input is ItemStack) {
                if (input.stackSize != 1) {
                    input.stackSize = 1
                }
            }
        }
    }

    override fun getIngredients(ingredients: IIngredients) {
        val stackHelper = jeiHelpers.stackHelper
        val recipeOutput = recipe.getRecipeOutput()

        try {
            val adjustedRecipeInputList = recipe.input.map {
                if(it is ToolClass) {
                    ToolClassItemListCache.get(it)
                } else it
            }
            val inputs = stackHelper.expandRecipeItemStackInputs(adjustedRecipeInputList)
            ingredients.setInputLists(ItemStack::class.java!!, inputs)

            if (recipeOutput != null) {
                ingredients.setOutput(ItemStack::class.java!!, recipeOutput!!)
            }
        } catch (e: RuntimeException) {
            val info = ErrorUtil.getInfoFromBrokenCraftingRecipe(recipe, recipe.input, recipeOutput)
            throw BrokenCraftingRecipeException(info, e)
        }

    }

    override fun getOutputs(): List<ItemStack> {
        return listOf(recipe.recipeOutput!!)
    }

}

object ToolClassItemListCache {
    val map = IdentityHashMap<ToolClass, List<ItemStack>>()
    val ingredientList: List<ItemStack> by lazy {
        IngredientBaseListFactory.create(false).map { it.ingredient }.filter { it is ItemStack }.map { it as ItemStack }
    }

    fun get(o: ToolClass): List<ItemStack> {
        return map.getOrPut(o) {
            ingredientList.filter { it in o }
        }
    }
}
