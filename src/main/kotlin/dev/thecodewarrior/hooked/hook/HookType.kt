package dev.thecodewarrior.hooked.hook

import com.teamwizardry.librarianlib.core.util.loc
import net.minecraft.entity.player.PlayerEntity
import net.minecraftforge.registries.ForgeRegistryEntry
import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.registries.RegistryManager

abstract class HookType(
    /**
     * The number of simultaneous hooks allowed
     */
    val count: Int,
    /**
     * The maximum range from impact point to player
     */
    val range: Double,
    /**
     * The speed of the fired hooks in m/t
     */
    val speed: Double,
    /**
     * The distance from the impact point to where the chain should attach
     */
    val hookLength: Double,
): ForgeRegistryEntry<HookType>() {

    abstract val translationBase: String

    /**
     * The language keys to add to the item tooltip
     */
    abstract val controlLangKeys: List<String>

    /**
     * Create a new player controller
     */
    abstract fun createController(player: PlayerEntity): HookPlayerController

    companion object {
        val NONE: HookType = object: HookType(0, 0.0, 0.0, 0.0) {
            init {
                registryName = loc("hooked:none")
            }

            override val translationBase: String = "hooked.controller.none"
            override val controlLangKeys: List<String> = emptyList()

            override fun createController(player: PlayerEntity): HookPlayerController = HookPlayerController.NONE
        }

        val REGISTRY: IForgeRegistry<HookType> by lazy {
            RegistryManager.ACTIVE.getRegistry(HookType::class.java)
        }
    }
}