package dev.thecodewarrior.hooked.neoforge

import dev.thecodewarrior.hooked.Hooked
import dev.thecodewarrior.hooked.HookedCommon
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.attachment.AttachmentType
import net.neoforged.neoforge.items.ItemStackHandler
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries
import java.util.function.Supplier


@Mod(Hooked.MOD_ID)
class HookedNeoForgeCommon(modEventBus: IEventBus) {
    init {
        HookedCommon.init()
        ATTACHMENT_TYPES.register(modEventBus)
    }

    companion object {
        val ATTACHMENT_TYPES: DeferredRegister<AttachmentType<*>> =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Hooked.MOD_ID)

        val HOOK_DATA: Supplier<AttachmentType<HookedPlayerDataAttachment>> =
            ATTACHMENT_TYPES.register("hook_data", Supplier {
                AttachmentType.serializable { holder ->
                    HookedPlayerDataAttachment(holder as PlayerEntity)
                }.build()
            })
    }
}