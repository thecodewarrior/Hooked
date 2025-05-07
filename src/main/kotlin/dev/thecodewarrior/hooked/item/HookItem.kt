package dev.thecodewarrior.hooked.item

import dev.emi.trinkets.api.TrinketItem
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.world.World

class HookItem(settings: Settings): TrinketItem(settings) {
    override fun appendTooltip(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Text>,
        type: TooltipType
    ) {
        val hookProperties = HookProperties.fromItemStack(stack)
        if (hookProperties != null) {
            if (stack.get(DataComponentTypes.ITEM_NAME) == null) {
                tooltip.add(Text.translatable("$translationKey.tip"))
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
        super.appendTooltip(stack, context, tooltip, type)
    }

    companion object {
        /**
         * On the client this is replaced with { Screen.isShiftDown() }
         */
        var hasShiftDown: () -> Boolean = { false }
    }
}
