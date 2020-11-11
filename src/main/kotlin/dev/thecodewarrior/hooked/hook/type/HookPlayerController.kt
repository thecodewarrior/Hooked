package dev.thecodewarrior.hooked.hook.type

import com.teamwizardry.librarianlib.prism.SimpleSerializer
import com.teamwizardry.librarianlib.prism.Sync
import net.minecraft.nbt.CompoundNBT
import net.minecraftforge.common.util.INBTSerializable

/**
 * Hook controllers translate the hook data into the player's movement.
 *
 * - Hook controllers will only be changed on the server and synced from on high.
 * - Hook controllers don't manage the movement of hooks, that's up to the hook processor.
 *
 * Hook controllers should have important data serialized using [@Sync][Sync]
 */
abstract class HookPlayerController(val type: HookType): INBTSerializable<CompoundNBT> {
    private val serializer = SimpleSerializer.get(this.javaClass)

    /**
     * Called before the hook processor updates hooks,
     */
    abstract fun preTick()

    override fun serializeNBT(): CompoundNBT {
        return serializer.createTag(this, Sync::class.java)
    }

    override fun deserializeNBT(nbt: CompoundNBT) {
        serializer.applyTag(nbt, this, Sync::class.java)
    }
}