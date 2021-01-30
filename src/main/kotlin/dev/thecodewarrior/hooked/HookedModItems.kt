package dev.thecodewarrior.hooked

import com.teamwizardry.librarianlib.core.util.loc
import com.teamwizardry.librarianlib.foundation.registration.ItemSpec
import com.teamwizardry.librarianlib.foundation.registration.LazyItem
import com.teamwizardry.librarianlib.foundation.registration.RegistrationManager
import dev.thecodewarrior.hooked.item.HookItem

object HookedModItems {
    internal fun registerItems(registrationManager: RegistrationManager) {
        HookedModHookTypes.types.forEach { type ->
            val item = registrationManager.add(
                ItemSpec(type.registryName!!.path)
                    .maxStackSize(1)
                    .item { HookItem(it.itemProperties, type) }
                    .datagen {
                        tags(HookedMod.HOOKED_CURIOS_TAG)
                    }
            )
            if(type.registryName == loc("hooked:iron_hook"))
                registrationManager.itemGroupIcon = item
        }
    }
}
