package dev.thecodewarrior.hooked.capability

import com.teamwizardry.librarianlib.foundation.capability.BaseCapability
import com.teamwizardry.librarianlib.prism.Save
import dev.thecodewarrior.hooked.hook.type.Hook
import dev.thecodewarrior.hooked.hook.type.HookType
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import java.util.*

/**
 * A capability holding all the data and logic required for actually running the hooks.
 */
class HookedPlayerData: BaseCapability() {
    @Save
    var type: HookType = HookType.NONE

    @Save
    val hooks: LinkedList<Hook> = LinkedList()

    @Save
    var cooldownCounter: Int = 0

    var needsSync: Boolean = true

    /**
     * Marks this data to be synced in its entirety with clients.
     *
     * TODO: make single-hook sync packets for other players, since they aren't critical to keep exactly up to date.
     */
    fun markForSync() {
        needsSync = true
    }

    companion object {
        @JvmStatic
        @CapabilityInject(HookedPlayerData::class)
        lateinit var CAPABILITY: Capability<HookedPlayerData>
            @JvmSynthetic get // the raw field seems to be accessible
            @JvmSynthetic set
    }
}