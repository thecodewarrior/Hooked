package dev.thecodewarrior.hooked.fabric

import dev.thecodewarrior.hooked.capability.HookedPlayerData
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryWrapper
import org.ladysnake.cca.api.v3.component.Component

class HookedPlayerDataComponent(player: PlayerEntity): Component {
    val data = HookedPlayerData(player)

    override fun readFromNbt(tag: NbtCompound, registryWrapper: RegistryWrapper.WrapperLookup) {
        data.readFromNbt(tag)
    }

    override fun writeToNbt(tag: NbtCompound, registryWrapper: RegistryWrapper.WrapperLookup) {
        data.writeToNbt(tag)
    }
}