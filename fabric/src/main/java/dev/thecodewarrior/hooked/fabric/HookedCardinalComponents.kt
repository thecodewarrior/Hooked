package dev.thecodewarrior.hooked.fabric

import dev.thecodewarrior.hooked.Hooked
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Identifier
import org.ladysnake.cca.api.v3.component.ComponentRegistry
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer

/**
 * Cardinal components entrypoint
 */
object HookedCardinalComponents : EntityComponentInitializer {
    @JvmField
    val HOOK_DATA = ComponentRegistry.getOrCreate(Identifier.of(Hooked.MOD_ID, "hook_data"), HookedPlayerDataComponent::class.java)

    override fun registerEntityComponentFactories(registry: EntityComponentFactoryRegistry) {
        registry.registerFor(PlayerEntity::class.java, HOOK_DATA, ::HookedPlayerDataComponent)
    }
}