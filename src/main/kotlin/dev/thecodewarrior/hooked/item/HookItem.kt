package dev.thecodewarrior.hooked.item

import dev.emi.trinkets.api.TrinketItem
import dev.thecodewarrior.hooked.capability.IHookItem
import dev.thecodewarrior.hooked.hook.HookType
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.text.KeybindText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.world.World

class HookItem(settings: Settings, override val hookType: HookType): TrinketItem(settings), IHookItem {
    override fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext
    ) {
        tooltip.add(TranslatableText("$translationKey.tip"))
        if (hasShiftDown()) {
            val fireKeyName = KeybindText("key.hooked.fire").formatted(Formatting.BOLD)
            tooltip.addAll(hookType.controlLangKeys.map { key ->
                TranslatableText(key, fireKeyName).formatted(Formatting.GRAY)
            })
        } else {
            tooltip.add(
                TranslatableText("hooked.controller.universal.controls.collapsed").formatted(Formatting.GRAY)
            )
        }
        super.appendTooltip(stack, world, tooltip, context)
    }

    companion object {
        /**
         * On the client this is replaced with { Screen.isShiftDown() }
         */
        var hasShiftDown: () -> Boolean = { false }
    }
}
