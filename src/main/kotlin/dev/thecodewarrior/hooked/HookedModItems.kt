package dev.thecodewarrior.hooked

import com.teamwizardry.librarianlib.foundation.registration.ItemSpec
import com.teamwizardry.librarianlib.foundation.registration.LazyItem
import com.teamwizardry.librarianlib.foundation.registration.RegistrationManager
import dev.thecodewarrior.hooked.item.HookItem

object HookedModItems {
    val hooks = mutableListOf<LazyItem>()
    val ironHook: LazyItem = LazyItem()

    internal fun registerItems(registrationManager: RegistrationManager) {
        HookedModHookTypes.types.forEach { type ->
            val item = registrationManager.add(
                ItemSpec(type.registryName!!.path)
                    .maxStackSize(1)
                    .item { HookItem(it.itemProperties, type) }
                    .datagen { simpleModel() }
            )
            if(type.registryName!!.path == "iron_hook")
                ironHook.from(item)
            hooks.add(item)
        }
        registrationManager.itemGroupIcon = ironHook
    }

    internal fun registerItemDatagen(registrationManager: RegistrationManager) {
        registrationManager.datagen.itemTags.add(HookedMod.HOOKED_CURIOS_TAG, *hooks.map { it.get() }.toTypedArray())
    }
}
