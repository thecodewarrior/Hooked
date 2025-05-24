package dev.thecodewarrior.hooked.neoforge

import dev.thecodewarrior.hooked.item.HookItemBase
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text

class HookItemNeoForge(settings: Settings): Item(settings) {
    override fun appendTooltip(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Text>,
        type: TooltipType
    ) {
        HookItemBase.appendTooltip(this,  stack, context, tooltip, type)
        super.appendTooltip(stack, context, tooltip, type)
    }
}