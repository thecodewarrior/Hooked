package dev.thecodewarrior.hooked.capability

import com.teamwizardry.librarianlib.foundation.capability.BaseCapability
import com.teamwizardry.librarianlib.prism.Save
import dev.thecodewarrior.hooked.hook.processor.Hook
import dev.thecodewarrior.hooked.hook.type.HookPlayerController
import dev.thecodewarrior.hooked.hook.type.HookType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.CompoundNBT
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import java.util.*

/**
 * A capability holding all the data and logic required for actually running the hooks.
 */
class HookedPlayerData(val player: PlayerEntity): BaseCapability() {
    @Save
    var type: HookType = HookType.NONE
        set(value) {
            if (value != field) {
                controller.remove()
                controller = value.createController(player)
            }
            field = value
        }

    @Save
    val hooks: LinkedList<Hook> = LinkedList()

    @Save
    var cooldownCounter: Int = 0

    var controller: HookPlayerController = HookPlayerController.NONE

    var needsSync: Boolean = true

    override fun deserializeNBT(nbt: CompoundNBT) {
        val oldType = type
        super.deserializeNBT(nbt)
        // the SimpleSerializer doesn't use the setter, so we have to update it here
        if (type != oldType) {
            controller.remove()
            controller = type.createController(player)
        }
        controller.deserializeNBT(nbt.getCompound("controller"))
    }

    override fun serializeNBT(): CompoundNBT {
        val nbt = super.serializeNBT()
        nbt.put("controller", controller.serializeNBT())
        return nbt
    }

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