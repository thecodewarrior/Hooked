package dev.thecodewarrior.hooked

import com.teamwizardry.librarianlib.foundation.registration.ItemSpec
import com.teamwizardry.librarianlib.foundation.registration.LazyItem
import com.teamwizardry.librarianlib.foundation.registration.RegistrationManager
import dev.thecodewarrior.hooked.item.HookItem

object HookedModItems {
    val ironHook: LazyItem = LazyItem()

    internal fun registerItems(registrationManager: RegistrationManager) {
        ironHook.from(registrationManager.add(
            ItemSpec("iron_hook")
                .maxStackSize(1)
                .item { HookItem(it.itemProperties, HookedModHookTypes.iron) }
                .datagen { simpleModel() }
        ))
        registrationManager.itemGroupIcon = ironHook
    }

    internal fun registerItemDatagen(registrationManager: RegistrationManager) {
        registrationManager.datagen.itemTags.add(HookedMod.HOOKED_CURIOS_TAG, ironHook.get())
    }
}
