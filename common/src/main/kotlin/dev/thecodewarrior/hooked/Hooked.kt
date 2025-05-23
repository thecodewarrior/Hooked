package dev.thecodewarrior.hooked

import com.teamwizardry.librarianlib.core.util.ModLogManager
import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrarManager
import net.minecraft.registry.RegistryKeys

object Hooked {
    const val MOD_ID = "hooked"
    val logManager = ModLogManager(MOD_ID, "Hooked")
}

object HookedRegistries {
    private val manager: RegistrarManager by lazy { RegistrarManager.get(Hooked.MOD_ID) }
    val ITEM_GROUP by lazy { manager.get(RegistryKeys.ITEM_GROUP) }
    val ITEM by lazy { manager.get(RegistryKeys.ITEM) }
    val SOUND_EVENT by lazy { manager.get(RegistryKeys.SOUND_EVENT) }
    val CUSTOM_STAT by lazy { manager.get(RegistryKeys.CUSTOM_STAT) }
    val DATA_COMPONENT_TYPE by lazy { manager.get(RegistryKeys.DATA_COMPONENT_TYPE) }
}