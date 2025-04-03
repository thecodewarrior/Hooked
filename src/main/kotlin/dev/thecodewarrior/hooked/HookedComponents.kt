package dev.thecodewarrior.hooked

import dev.thecodewarrior.hooked.capability.HookedPlayerData
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Identifier
import org.ladysnake.cca.api.v3.component.ComponentRegistry
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer

/**
 * Cardinal components entrypoint
 */
object HookedComponents : EntityComponentInitializer {
    @JvmField
    val HOOK_DATA = ComponentRegistry.getOrCreate(Identifier.of("hooked:hook_data"), HookedPlayerData::class.java)

    override fun registerEntityComponentFactories(registry: EntityComponentFactoryRegistry) {
        registry.registerFor(PlayerEntity::class.java, HOOK_DATA, ::HookedPlayerData)
    }
}