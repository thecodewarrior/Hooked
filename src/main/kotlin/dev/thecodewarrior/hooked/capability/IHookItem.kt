package dev.thecodewarrior.hooked.capability

import dev.thecodewarrior.hooked.hook.HookType
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject

interface IHookItem {
    val type: HookType

    companion object {
        @JvmStatic
        @CapabilityInject(IHookItem::class)
        lateinit var CAPABILITY: Capability<IHookItem>
            @JvmSynthetic get // the raw field seems to be accessible
            @JvmSynthetic set
    }
}