package dev.thecodewarrior.hooked

import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier

object HookTags {
    val SOLID_BLOCKS = TagKey.of(RegistryKeys.BLOCK, Identifier.of(Hooked.MOD_ID, "solid_blocks"))
    val IGNORE_BLOCKS = TagKey.of(RegistryKeys.BLOCK, Identifier.of(Hooked.MOD_ID, "ignore_blocks"))
}