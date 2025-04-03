package dev.thecodewarrior.hooked.item

import dev.emi.trinkets.api.TrinketItem
import dev.thecodewarrior.hooked.capability.IHookItem
import dev.thecodewarrior.hooked.hook.HookType
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.world.World

class HookItem(settings: Settings, override val hookType: HookType): TrinketItem(settings), IHookItem {
    override fun appendTooltip(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Text>,
        type: TooltipType
    ) {
        tooltip.add(Text.translatable("$translationKey.tip"))
        if (hasShiftDown()) {
            val fireKeyName = Text.keybind("key.hooked.fire").formatted(Formatting.BOLD)
            tooltip.addAll(hookType.controlLangKeys.map { key ->
                Text.translatable(key, fireKeyName).formatted(Formatting.GRAY)
            })
        } else {
            tooltip.add(
                Text.translatable("hooked.controller.universal.controls.collapsed").formatted(Formatting.GRAY)
            )
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
