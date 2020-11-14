package dev.thecodewarrior.hooked

import com.teamwizardry.librarianlib.core.util.kotlin.loc
import com.teamwizardry.librarianlib.foundation.capability.SimpleCapabilityProvider
import com.teamwizardry.librarianlib.foundation.capability.SimpleCapabilityStorage
import com.teamwizardry.librarianlib.foundation.registration.CapabilitySpec
import com.teamwizardry.librarianlib.foundation.registration.RegistrationManager
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.capability.IHookItem
import dev.thecodewarrior.hooked.hook.type.BasicHookType
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import java.lang.UnsupportedOperationException

object HookedModCapabilities {
    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    internal fun registerCapabilities(registrationManager: RegistrationManager) {
        registrationManager.add(CapabilitySpec(
            HookedPlayerData::class.java,
            SimpleCapabilityStorage()
        ) {
            throw UnsupportedOperationException("HookedPlayerData requires a player reference and thus has no " +
                    "default constructor.")
        })

        registrationManager.add(CapabilitySpec(
            IHookItem::class.java,
            SimpleCapabilityStorage()
        ) {
            throw UnsupportedOperationException("No default zero-arg implementation for IHookItem. Use BasicHookItem.")
        })
    }

    @SubscribeEvent
    fun attachEntityCapabilities(e: AttachCapabilitiesEvent<Entity>) {
        val entity = e.`object`
        if(entity is PlayerEntity) {
            e.addCapability(
                loc("hooked:player_data"),
                SimpleCapabilityProvider(HookedPlayerData.CAPABILITY, HookedPlayerData(entity))
            )
        }
    }
}