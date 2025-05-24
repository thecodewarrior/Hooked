package dev.thecodewarrior.hooked.neoforge

import dev.thecodewarrior.hooked.capability.HookedPlayerData
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryWrapper
import net.neoforged.neoforge.common.util.INBTSerializable

class HookedPlayerDataAttachment(player: PlayerEntity) : INBTSerializable<NbtCompound> {
    val data = HookedPlayerData(player)

    override fun serializeNBT(registryWrapper: RegistryWrapper.WrapperLookup): NbtCompound {
        val tag = NbtCompound()
        data.writeToNbt(tag)
        return tag
    }

    override fun deserializeNBT(registryWrapper: RegistryWrapper.WrapperLookup, tag: NbtCompound) {
        data.readFromNbt(tag)
    }
}