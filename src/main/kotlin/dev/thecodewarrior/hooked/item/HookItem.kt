package dev.thecodewarrior.hooked.item

import com.teamwizardry.librarianlib.core.util.sided.SidedRunnable
import com.teamwizardry.librarianlib.foundation.item.BaseItem
import dev.thecodewarrior.hooked.capability.BasicHookItem
import dev.thecodewarrior.hooked.capability.IHookItem
import dev.thecodewarrior.hooked.client.Keybinds
import dev.thecodewarrior.hooked.hook.HookType
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.tutorial.Tutorial
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.Direction
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.KeybindTextComponent
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TextFormatting
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.LazyOptional

class HookItem(properties: Properties, val type: HookType): BaseItem(properties) {
    private val hookItem = BasicHookItem(type)
    private val provider: ICapabilityProvider = object: ICapabilityProvider {
        private val optHookItem = LazyOptional.of { hookItem }
        override fun <T: Any?> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
            if (cap == IHookItem.CAPABILITY)
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
        tooltip.add(TranslationTextComponent("$defaultTranslationKey.tip"))
        if (Screen.hasShiftDown()) {
            val fireKeyName = KeybindTextComponent(Keybinds.fireKey.keyDescription).mergeStyle(TextFormatting.BOLD)
            tooltip.addAll(type.controlLangKeys.map { key ->
                TranslationTextComponent(key, fireKeyName).mergeStyle(TextFormatting.GRAY)
            })
        } else {
            tooltip.add(
                TranslationTextComponent("hooked.controller.universal.controls.collapsed")
                    .mergeStyle(TextFormatting.GRAY)
            )
        }

        super.addInformation(stack, worldIn, tooltip, flagIn)
    }

    override fun initCapabilities(stack: ItemStack, nbt: CompoundNBT?): ICapabilityProvider? {
        return provider
    }
}
