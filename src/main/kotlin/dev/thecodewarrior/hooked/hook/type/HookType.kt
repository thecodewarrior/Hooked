package dev.thecodewarrior.hooked.hook.type

import net.minecraft.entity.player.PlayerEntity
import net.minecraftforge.registries.ForgeRegistryEntry

abstract class HookType: ForgeRegistryEntry<HookType>() {
    /**
     * The number of simultaneous hooks allowed
     */
    abstract val count: Int

    /**
     * The maximum range from impact point to player
     */
    abstract val range: Double

    /**
     * The speed of the fired hooks in m/t
     */
    abstract val speed: Double

    /**
     * The distance from the impact point to where the chain should attach
     */
    abstract val hookLength: Double

    /**
     * Create a new player controller
     */
    abstract fun createController(player: PlayerEntity): HookPlayerController

//    @get:SideOnly(Side.CLIENT)
//    @delegate:Transient
//    val renderer: HookRenderer<*, *> by lazy { initRenderer() }

//    @SideOnly(Side.CLIENT)
//    protected abstract fun initRenderer(): HookRenderer<*, *>

    companion object {
        val NONE: HookType = object: HookType() {
            override val count: Int = 0
            override val range: Double = 0.0
            override val speed: Double = 0.0
            override val hookLength: Double = 0.0
            override fun createController(player: PlayerEntity): HookPlayerController = HookPlayerController.NONE
        }
    }
}