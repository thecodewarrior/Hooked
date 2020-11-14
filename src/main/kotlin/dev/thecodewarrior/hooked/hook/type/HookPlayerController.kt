package dev.thecodewarrior.hooked.hook.type

import com.teamwizardry.librarianlib.prism.SimpleSerializer
import com.teamwizardry.librarianlib.prism.Sync
import dev.thecodewarrior.hooked.hook.processor.Hook
import net.minecraft.entity.player.PlayerEntity
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
abstract class HookPlayerController: INBTSerializable<CompoundNBT> {
    private val serializer = SimpleSerializer.get(this.javaClass)

    /**
     * Called when the controller is removed so it can do any cleanup necessary.
     */
    open fun remove() {}

    /**
     * Called after the hook processor updates the hooks
     */
    abstract fun update(player: PlayerEntity, hooks: List<Hook>)

    override fun serializeNBT(): CompoundNBT {
        return serializer.createTag(this, Sync::class.java)
    }

    override fun deserializeNBT(nbt: CompoundNBT) {
        serializer.applyTag(nbt, this, Sync::class.java)
    }

    companion object {
        val NONE: HookPlayerController = object: HookPlayerController() {
            override fun update(player: PlayerEntity, hooks: List<Hook>) {}
        }
    }
}