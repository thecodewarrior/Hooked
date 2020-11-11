package dev.thecodewarrior.hooked.item

import dev.thecodewarrior.hooked.capability.BasicHookItem
import dev.thecodewarrior.hooked.capability.IHookItem
import dev.thecodewarrior.hooked.hook.type.HookType
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.Direction
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.LazyOptional

class HookItem(properties: Properties, val type: HookType) : Item(properties) {
    private val hookItem = BasicHookItem(type)
    private val provider: ICapabilityProvider = object: ICapabilityProvider {
        private val optHookItem = LazyOptional.of { hookItem }
        override fun <T: Any?> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
            if(cap == IHookItem.CAPABILITY)
                return optHookItem.cast()
            return LazyOptional.empty()
        }
    }

    override fun initCapabilities(stack: ItemStack, nbt: CompoundNBT?): ICapabilityProvider? {
        return provider
    }
}
