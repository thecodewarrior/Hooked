package thecodewarrior.hooked.common.items

import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.InventoryCrafting
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemSword
import net.minecraft.item.crafting.IRecipe
import net.minecraft.item.crafting.ShapelessRecipes
import net.minecraft.world.World
import net.minecraftforge.common.ForgeHooks
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.oredict.OreDictionary
import net.minecraftforge.oredict.ShapedOreRecipe
import thecodewarrior.hooked.common.HookType
import thecodewarrior.hooked.common.block.ModBlocks
import java.util.*
import java.util.concurrent.ThreadLocalRandom

/**
 * Created by TheCodeWarrior
 */
object ModRecipes {

    init {
        val wood_hook = ItemStack(ModItems.hook, 1, HookType.WOOD.ordinal)
        val iron_hook = ItemStack(ModItems.hook, 1, HookType.IRON.ordinal)
        val diamond_hook = ItemStack(ModItems.hook, 1, HookType.DIAMOND.ordinal)
        val red_hook = ItemStack(ModItems.hook, 1, HookType.RED.ordinal)
        val ender_hook = ItemStack(ModItems.hook, 1, HookType.ENDER.ordinal)

        val plant_fiber = ItemStack(ModItems.micro, 1, 0)
        val rope = ItemStack(ModItems.micro, 1, 1)
        val iron_chain_link = ItemStack(ModItems.micro, 1, 2)
        val iron_chain = ItemStack(ModItems.micro, 1, 3)

        //region Microcrafting
        GameRegistry.addRecipe(ShapelessOreToolRecipe(plant_fiber, "treeSapling", ToolClass({ it.item is ItemSword })))

        GameRegistry.addRecipe(ShapedOreRecipe(rope,
                " FF",
                "FFF",
                "FF ",
                'F', plant_fiber
                ))

        GameRegistry.addRecipe(ShapedOreRecipe(iron_chain_link,
                " II",
                "I I",
                "II ",
                'I', "ingotIron"))

        GameRegistry.addRecipe(ShapedOreRecipe(iron_chain,
                "  L",
                " L ",
                "L  ",
                'L', iron_chain_link))
        //endregion Microcrafting

        //region hooks
        GameRegistry.addRecipe(ShapedOreRecipe(wood_hook,
                "SSP",
                " LS",
                "L S",
                'S', "stickWood",
                'P', Items.STONE_PICKAXE,
                'L', rope))

        GameRegistry.addRecipe(ShapedOreRecipe(iron_hook,
                "IIP",
                " LI",
                "L I",
                'I', "ingotIron",
                'P', Items.IRON_PICKAXE,
                'L', iron_chain))

        GameRegistry.addRecipe(ShapedOreRecipe(diamond_hook,
                "DDP",
                " HD",
                "D D",
                'D', "gemDiamond",
                'P', Items.DIAMOND_PICKAXE,
                'H', iron_hook))

        GameRegistry.addRecipe(ShapedOreRecipe(red_hook,
                "PrR",
                " Hr",
                "C P",
                'P', Blocks.PISTON,
                'H', diamond_hook,
                'C', Items.COMPARATOR,
                'R', Blocks.REDSTONE_BLOCK,
                'r', Items.REDSTONE))

        GameRegistry.addRecipe(ShapedOreRecipe(ender_hook,
                "RPE",
                " HP",
                "D R",
                'H', diamond_hook,
                'R', Items.BLAZE_ROD,
                'P', Items.ENDER_PEARL,
                'E', Items.ENDER_EYE,
                'D', Items.BLAZE_POWDER))
        //endregion hooks

        //region balloons

        for(i in 0..15) {
            GameRegistry.addRecipe(ShapedOreRecipe(ItemStack(ModBlocks.balloon, 1, i),
                    " I ",
                    "IWI",
                    " S ",
                    'I', Blocks.IRON_BARS,
                    'W', ItemStack(Blocks.WOOL, 1, i),
                    'S', "string"))
        }

        //endregion balloons
    }

}

class ShapelessOreToolRecipe : IRecipe {
    protected var output: ItemStack? = null
    /**
     * Returns the input for this recipe, any mod accessing this value should never
     * manipulate the values in this array as it will effect the recipe itself.
     * @return The recipes input vales.
     */
    var input = ArrayList<Any>()
        protected set

    constructor(result: Block, vararg recipe: Any) : this(ItemStack(result), *recipe) {
    }

    constructor(result: Item, vararg recipe: Any) : this(ItemStack(result), *recipe) {
    }

    constructor(result: ItemStack, vararg recipe: Any) {
        output = result.copy()
        for (element in recipe) {
            if (element is ItemStack) {
                input.add(element.copy())
            } else if (element is Item) {
                input.add(ItemStack(element))
            } else if (element is Block) {
                input.add(ItemStack(element))
            } else if (element is String) {
                input.add(OreDictionary.getOres(element))
            } else if (element is ToolClass){
                input.add(element)
            } else {
                var ret = "Invalid shapeless ore recipe: "
                for (tmp in recipe) {
                    ret += tmp.toString() + ", "
                }
                ret += output
                throw RuntimeException(ret)
            }
        }
    }

    internal constructor(recipe: ShapelessRecipes, replacements: Map<ItemStack, String>) {
        output = recipe.recipeOutput

        for (ingredient in recipe.recipeItems) {
            var finalObj: Any = ingredient
            for ((key, value) in replacements) {
                if (OreDictionary.itemMatches(key, ingredient, false)) {
                    finalObj = OreDictionary.getOres(value)
                    break
                }
            }
            input.add(finalObj)
        }
    }

    /**
     * Returns the size of the recipe area
     */
    override fun getRecipeSize(): Int {
        return input.size
    }

    override fun getRecipeOutput(): ItemStack? {
        return output
    }

    /**
     * Returns an Item that is the result of this recipe
     */
    override fun getCraftingResult(var1: InventoryCrafting): ItemStack? {
        return output!!.copy()
    }

    /**
     * Used to check if a recipe matches current crafting inventory
     */
    override fun matches(var1: InventoryCrafting, world: World): Boolean {
        val required = ArrayList(input)

        for (x in 0..var1.sizeInventory - 1) {
            val slot = var1.getStackInSlot(x)

            if (slot != null) {
                var inRecipe = false
                val req = required.iterator()

                while (req.hasNext()) {
                    var match = false

                    val next = req.next()

                    if (next is ItemStack) {
                        match = OreDictionary.itemMatches(next, slot, false)
                    } else if (next is List<*>) {
                        val itr = (next as List<ItemStack>).iterator()
                        while (itr.hasNext() && !match) {
                            match = OreDictionary.itemMatches(itr.next(), slot, false)
                        }
                    } else if (next is ToolClass) {
                        match = slot in next
                    }

                    if (match) {
                        inRecipe = true
                        required.remove(next)
                        break
                    }
                }

                if (!inRecipe) {
                    return false
                }
            }
        }

        return required.isEmpty()
    }

    override fun getRemainingItems(inv: InventoryCrafting) //getRecipeLeftovers
            : Array<ItemStack?> {
        val required = input.filter { it is ToolClass } as MutableList<ToolClass>
        return Array(inv.sizeInventory) { i ->
            val stack = inv.getStackInSlot(i) ?: return@Array null

            val found = required.find { stack in it }
            if(found != null) {
                required.remove(found)
                val newStack = stack.copy()
                newStack.attemptDamageItem(1, ThreadLocalRandom.current())
                if(newStack.itemDamage == newStack.maxDamage)
                    return@Array null
                return@Array newStack
            }
            return@Array ForgeHooks.getContainerItem(stack)
        }
    }
}

data class ToolClass(val check: (stack: ItemStack) -> Boolean) {

    operator fun contains(stack: ItemStack?): Boolean {
        return stack != null && check(stack)
    }
}
