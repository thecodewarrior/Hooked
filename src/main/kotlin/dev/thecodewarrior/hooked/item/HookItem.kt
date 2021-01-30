package dev.thecodewarrior.hooked.item

import com.teamwizardry.librarianlib.foundation.item.BaseItem
import dev.thecodewarrior.hooked.capability.BasicHookItem
import dev.thecodewarrior.hooked.capability.IHookItem
import dev.thecodewarrior.hooked.hook.type.HookType
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.Direction
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.LazyOptional

class HookItem(properties: Properties, val type: HookType) : BaseItem(properties) {
    private val hookItem = BasicHookItem(type)
    private val provider: ICapabilityProvider = object: ICapabilityProvider {
        private val optHookItem = LazyOptional.of { hookItem }
        override fun <T: Any?> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
            if(cap == IHookItem.CAPABILITY)
                return optHookItem.cast()
            return LazyOptional.empty()
        }
    }

    override fun addInformation(
        stack: ItemStack,
        worldIn: World?,
        tooltip: MutableList<ITextComponent>,
        flagIn: ITooltipFlag
    ) {
        if(Screen.hasShiftDown()) {
            tooltip.add(TranslationTextComponent("$defaultTranslationKey.tip.detail"))
        } else {
            tooltip.add(TranslationTextComponent("$defaultTranslationKey.tip"))
        }

        super.addInformation(stack, worldIn, tooltip, flagIn)
    }

    override fun initCapabilities(stack: ItemStack, nbt: CompoundNBT?): ICapabilityProvider? {
        return provider
    }
}
