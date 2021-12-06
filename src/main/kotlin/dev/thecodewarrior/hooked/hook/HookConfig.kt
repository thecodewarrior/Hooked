package dev.thecodewarrior.hooked.hook

import net.minecraft.nbt.CompoundNBT
import net.minecraftforge.common.ForgeConfigSpec

open class HookConfig {
    private var count
    open fun addToConfig(config: ForgeConfigSpec.Builder) {
        config.comment("The number of simultaneous hooks allowed")
        config.define()
        config.defineInRange("count", )
    }
}