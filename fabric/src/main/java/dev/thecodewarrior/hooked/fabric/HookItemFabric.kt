package dev.thecodewarrior.hooked.fabric

import dev.emi.trinkets.TrinketSlot
import dev.emi.trinkets.api.SlotReference
import dev.emi.trinkets.api.Trinket
import dev.emi.trinkets.api.TrinketItem
import dev.emi.trinkets.api.TrinketsApi
import dev.thecodewarrior.hooked.item.HookItemBase
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World
import net.minecraft.world.event.GameEvent
import kotlin.jvm.optionals.getOrNull

class HookItemFabric(settings: Settings): Item(settings), Trinket {

    init {
        TrinketsApi.registerTrinket(this, this)
    }

    override fun appendTooltip(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Text>,
        type: TooltipType
    ) {
        HookItemBase.appendTooltip(this,  stack, context, tooltip, type)
        super.appendTooltip(stack, context, tooltip, type)
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = user.getStackInHand(hand)
        val swapStack = swapItem(user, stack)
        if (swapStack != null) {
            return TypedActionResult.success(swapStack, world.isClient())
        }
        return super.use(world, user, hand)
    }


    companion object {
        /**
         * Attempts to swap the given item into the trinkets inventory. If the item was successfully inserted into the
         * trinkets inventory, the new stack to replace it will be returned, otherwise returns null.
         */
        fun swapItem(user: LivingEntity, stack: ItemStack): ItemStack? {
            if (TrinketItem.equipItem(user, stack)) {
                return ItemStack.EMPTY // inserted without swapping
            }

            val comp = TrinketsApi.getTrinketComponent(user).getOrNull() ?: return null
            val inventories = comp.inventory.values.flatMap { it.values }
            for (inv in inventories) {
                for (i in 0..<inv.size()) {
                    val swappedStack = swapInSlot(user, SlotReference(inv, i), stack)
                    if (swappedStack != null) return swappedStack
                }
            }

            return null
        }

        /**
         * Attempts to swap the given item into the given slot. If the item was successfully inserted into the slot,
         * the old stack from that slot will be returned, otherwise returns null.
         */
        fun swapInSlot(user: LivingEntity, ref: SlotReference, stack: ItemStack): ItemStack? {
            if (TrinketSlot.canInsert(stack, ref, user)) {
                val swapStack = ref.inventory.getStack(ref.index).copy()
                ref.inventory.setStack(ref.index, stack.copy())

                val trinket = TrinketsApi.getTrinket(stack.item)
                val soundEvent = trinket.getEquipSound(stack, ref, user)
                if (!stack.isEmpty && soundEvent != null) {
                    user.emitGameEvent(GameEvent.EQUIP)
                    user.playSound(soundEvent.value(), 1.0f, 1.0f)
                }
                return swapStack
            }
            return null
        }
    }
}