package dev.thecodewarrior.hooked

import com.teamwizardry.librarianlib.core.util.loc
import com.teamwizardry.librarianlib.foundation.BaseMod
import com.teamwizardry.librarianlib.foundation.util.TagWrappers
import dev.thecodewarrior.hooked.client.HookRenderManager
import dev.thecodewarrior.hooked.client.Keybinds
import dev.thecodewarrior.hooked.client.renderer.BasicHookRenderer
import dev.thecodewarrior.hooked.client.renderer.FlightHookRenderer
import dev.thecodewarrior.hooked.hook.processor.ClientHookProcessor
import dev.thecodewarrior.hooked.hook.processor.ServerHookProcessor
import dev.thecodewarrior.hooked.hook.type.BasicHookType
import dev.thecodewarrior.hooked.hook.type.FlightHookType
import dev.thecodewarrior.hooked.hook.type.HookType
import dev.thecodewarrior.hooked.network.HookJumpPacket
import dev.thecodewarrior.hooked.network.FireHookPacket
import dev.thecodewarrior.hooked.network.SyncHookDataPacket
import dev.thecodewarrior.hooked.network.SyncIndividualHooksPacket
import net.minecraft.inventory.container.PlayerContainer
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.InterModComms
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent
import net.minecraftforge.fml.network.NetworkDirection
import net.minecraftforge.registries.RegistryBuilder
import top.theillusivec4.curios.api.SlotTypeMessage

@Mod("hooked")
object HookedMod: BaseMod(true) {
    val HOOKED_CURIOS_TAG = TagWrappers.item("curios:hooked")

    init {
        HookedModItems.registerItems(registrationManager)
        HookedModCapabilities.registerCapabilities(registrationManager)

        eventBus.register(HookedModHookTypes)

        courier.registerCourierPacket<FireHookPacket>(NetworkDirection.PLAY_TO_SERVER)
        courier.registerCourierPacket<SyncIndividualHooksPacket>(NetworkDirection.PLAY_TO_CLIENT)
        courier.registerCourierPacket<SyncHookDataPacket>(NetworkDirection.PLAY_TO_CLIENT)
        courier.registerCourierPacket<HookJumpPacket>(NetworkDirection.PLAY_TO_SERVER)
    }

    override fun clientSetup(e: FMLClientSetupEvent) {
        ClientRegistry.registerKeyBinding(Keybinds.fireKey)
        HookRenderManager // registers itself for events
        ClientHookProcessor // registers itself for events
        HookedModHookTypes.types.forEach {
            when(it) {
                is FlightHookType -> HookRenderManager.register(it, FlightHookRenderer(it))
                is BasicHookType -> HookRenderManager.register(it, BasicHookRenderer(it))
            }
        }
    }

    override fun commonSetup(e: FMLCommonSetupEvent) {
        ServerHookProcessor // registers itself for events
    }

    override fun interModCommsEnqueue(e: InterModEnqueueEvent) {
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE) {
            SlotTypeMessage.Builder("hooked")
                .size(1)
                .icon(loc("hooked:gui/hook_slot"))
                .build()
        }
    }

    @SubscribeEvent
    fun textureStitch(e: TextureStitchEvent.Pre) {
        if(e.map.textureLocation == PlayerContainer.LOCATION_BLOCKS_TEXTURE) {
            e.addSprite(loc("hooked:gui/hook_slot"))
        }
    }

    override fun createRegistries() {
        RegistryBuilder<HookType>()
            .setName(loc("hooked:hook_type"))
            .setType(HookType::class.java)
            .setDefaultKey(loc("hooked:none"))
            .create()
    }
}