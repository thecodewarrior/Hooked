package dev.thecodewarrior.hooked.neoforge

import dev.thecodewarrior.hooked.Hooked
import dev.thecodewarrior.hooked.HookedCommon
import dev.thecodewarrior.hooked.bridge.bridge
import dev.thecodewarrior.hooked.hook.HookActiveReason
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.tag.FluidTags
import net.neoforged.bus.api.EventPriority
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.attachment.AttachmentType
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries
import java.util.function.Supplier


@Mod(Hooked.MOD_ID)
class HookedNeoForgeCommon(modEventBus: IEventBus) {
    init {
        HookedCommon.init()
        ATTACHMENT_TYPES.register(modEventBus)
        NeoForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun breakSpeedEvent(event: PlayerEvent.BreakSpeed) {
        if (event.entity.bridge().isHookActive(HookActiveReason.BREAK_SPEED)) {
            if (event.entity.isSubmergedIn(FluidTags.WATER)) {
                val submergedSpeed: Double =
                    event.entity.getAttributeInstance(EntityAttributes.PLAYER_SUBMERGED_MINING_SPEED)!!.getValue()
                if (submergedSpeed > 0.0) {
                    event.newSpeed /= submergedSpeed.toFloat()
                }
            }

            if (!event.entity.isOnGround) {
                event.newSpeed *= 5.0f
            }
        }
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