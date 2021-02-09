package dev.thecodewarrior.hooked

import com.teamwizardry.librarianlib.core.util.loc
import com.teamwizardry.librarianlib.foundation.recipe.RecipeGenerator
import com.teamwizardry.librarianlib.foundation.recipe.kotlin.RecipeDslContext
import net.minecraft.block.Blocks
import net.minecraft.data.IFinishedRecipe
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.tags.ItemTags
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.Tags
import net.minecraftforge.registries.ForgeRegistries
import java.util.function.Consumer

object HookedModRecipes: RecipeGenerator() {
    override fun addRecipes(consumer: Consumer<IFinishedRecipe>) {
        val dsl = RecipeDslContext(consumer, "hooked")

        val woodHook = item(loc("hooked:wood_hook"))
        val ironHook = item(loc("hooked:iron_hook"))
        val diamondHook = item(loc("hooked:diamond_hook"))
        val enderHook = item(loc("hooked:ender_hook"))
        val redHook = item(loc("hooked:red_hook"))

        dsl.shaped("wood_hook", woodHook, 1) {
            +"TTP"
            +" ST"
            +"S T"

            'T' *= Tags.Items.RODS_WOODEN
            'P' *= Items.WOODEN_PICKAXE
            'S' *= Tags.Items.STRING

            criteria {
                hasItem("has_string", Tags.Items.STRING)
            }
        }

        dsl.shaped("iron_hook", ironHook, 1) {
            +"IIP"
            +" CI"
            +"C I"

            'I' *= Tags.Items.INGOTS_IRON
            'P' *= Items.IRON_PICKAXE
            'C' *= Items.CHAIN

            criteria {
                hasItem("has_iron", Tags.Items.INGOTS_IRON)
            }
        }

        dsl.shaped("diamond_hook", diamondHook, 1) {
            +" DD"
            +" HD"
            +"D  "

            'D' *= Tags.Items.GEMS_DIAMOND
            'H' *= ironHook

            criteria {
                hasItem("has_iron_hook", ironHook)
            }
        }

        dsl.shaped("ender_hook", enderHook, 1) {
            +"PRE"
            +" HR"
            +"B P"

            'P' *= Tags.Items.ENDER_PEARLS
            'R' *= Tags.Items.RODS_BLAZE
            'E' *= Items.ENDER_EYE
            'H' *= diamondHook
            'B' *= Items.BLAZE_POWDER

            criteria {
                hasItem("has_diamond_hook", diamondHook)
            }
        }

        dsl.shaped("red_hook", redHook, 1) {
            +"PDB"
            +" HD"
            +"C P"

            'P' *= Blocks.PISTON
            'D' *= Tags.Items.DUSTS_REDSTONE
            'B' *= Blocks.REDSTONE_BLOCK
            'H' *= diamondHook
            'C' *= Blocks.COMPARATOR

            criteria {
                hasItem("has_diamond_hook", diamondHook)
            }
        }
    }

    private fun item(id: ResourceLocation): Item {
        return ForgeRegistries.ITEMS.getValue(id)!!
    }
}