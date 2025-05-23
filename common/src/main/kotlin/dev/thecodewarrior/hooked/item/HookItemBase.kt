package dev.thecodewarrior.hooked.item

import net.minecraft.component.DataComponentTypes
import net.minecraft.item.Item
import net.minecraft.item.Item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text
import net.minecraft.util.Formatting

/**
 * Common methods used in the platform-dependent item implementation
 */
object HookItemBase {
    /**
     * On the client this is replaced with { Screen.isShiftDown() }
     */
    var hasShiftDown: () -> Boolean = { false }

    // this should be run at the start of the item's appendTooltip method, before `super.appendTooltip`
    fun appendTooltip(
        item: Item,
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Text>,
        type: TooltipType
    ) {
        val hookProperties = HookProperties.fromItemStack(stack)
        if (hookProperties != null) {
            if (stack.get(DataComponentTypes.ITEM_NAME) == null) {
                tooltip.add(Text.translatable("${item.translationKey}.tip"))
            }
            if (hasShiftDown()) {
                val fireKeyName = Text.keybind("key.hooked.fire").formatted(Formatting.BOLD)
                tooltip.addAll(hookProperties.behavior.controlsHelpText(hookProperties, fireKeyName))
            } else {
                tooltip.add(
                    Text.translatable("hooked.controller.universal.controls.collapsed").formatted(Formatting.GRAY)
                )
            }
        }
    }
}