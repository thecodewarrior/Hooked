package games.thecodewarrior.hooked.common.hook

import com.teamwizardry.librarianlib.features.kotlin.toRl
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.registries.IForgeRegistryEntry

abstract class HookType: IForgeRegistryEntry.Impl<HookType>() {

    /**
     * Create a new controller for the specified player
     */
    abstract fun create(player: EntityPlayer): HookController

    /**
     * "Adjusts" the passed stack. Called on sneak+use item. Returns a modified _copy_ of the itemstack, or null if no
     * change occured
     */
    abstract fun adjustStack(stack: ItemStack): ItemStack?

    /**
     * Add basic information to the item tooltip. Mutually exclusive with [addFullInformation]
     */
    @SideOnly(Side.CLIENT)
    abstract fun addBasicInformation(stack: ItemStack, tooltip: MutableList<String>)

    /**
     * Add full information to the item tooltip. Mutually exclusive with [addBasicInformation]
     */
    @SideOnly(Side.CLIENT)
    abstract fun addFullInformation(stack: ItemStack, tooltip: MutableList<String>)

    companion object {
        @JvmStatic
        lateinit var REGISTRY: IForgeRegistry<HookType>

        val missingno = BasicHookType(
            name = "hooked:missingno".toRl(),
            count = 0, range = 0.0,
            speed = 0.0, pullStrength = 0.0,
            hookLength = 0.0, jumpBoost = 0.0,
            cooldown = 0
        )
    }
}