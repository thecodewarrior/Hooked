package dev.thecodewarrior.hooked

import com.teamwizardry.librarianlib.core.util.kotlin.loc
import com.teamwizardry.librarianlib.core.util.sided.SidedSupplier
import com.teamwizardry.librarianlib.foundation.BaseMod
import com.teamwizardry.librarianlib.foundation.util.TagWrappers
import dev.thecodewarrior.hooked.client.HookRenderManager
import dev.thecodewarrior.hooked.client.Keybinds
import dev.thecodewarrior.hooked.client.renderer.BasicHookRenderer
import dev.thecodewarrior.hooked.hook.processor.ClientHookProcessor
import dev.thecodewarrior.hooked.hook.processor.ServerHookProcessor
import dev.thecodewarrior.hooked.hook.type.BasicHookPlayerController
import dev.thecodewarrior.hooked.hook.type.BasicHookType
import dev.thecodewarrior.hooked.hook.type.HookType
import dev.thecodewarrior.hooked.network.BasicHookJumpPacket
import dev.thecodewarrior.hooked.network.FireHookPacket
import dev.thecodewarrior.hooked.network.SyncHookDataPacket
import dev.thecodewarrior.hooked.network.SyncIndividualHooksPacket
import net.minecraft.inventory.container.PlayerContainer
import net.minecraft.util.Tuple
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
import top.theillusivec4.curios.api.CuriosAPI
import top.theillusivec4.curios.api.imc.CurioIMCMessage

@Mod("hooked")
object HookedMod: BaseMod(true) {
    val HOOKED_CURIOS_TAG = TagWrappers.item("curios:hooked")
    val proxy: HookedProxy

    init {
        HookedModItems.registerItems(registrationManager)
        HookedModItems.registerItemDatagen(registrationManager)
        HookedModCapabilities.registerCapabilities(registrationManager)
        eventBus.register(HookedModHookTypes)
        proxy = SidedSupplier.sided({ HookedClientProxy }, { HookedDedicatedServerProxy })

        courier.registerCourierPacket<FireHookPacket>(NetworkDirection.PLAY_TO_SERVER)
        courier.registerCourierPacket<SyncIndividualHooksPacket>(NetworkDirection.PLAY_TO_CLIENT)
        courier.registerCourierPacket<SyncHookDataPacket>(NetworkDirection.PLAY_TO_CLIENT)
        courier.registerCourierPacket<BasicHookJumpPacket>(NetworkDirection.PLAY_TO_SERVER)
    }

    override fun clientSetup(e: FMLClientSetupEvent) {
        ClientRegistry.registerKeyBinding(Keybinds.fireKey)
        HookRenderManager // registers itself for events
        ClientHookProcessor // registers itself for events
        HookedModHookTypes.types.forEach {
            if(it is BasicHookType) {
                HookRenderManager.register(it, BasicHookRenderer(it.registryName!!))
            }
        }
    }

    override fun commonSetup(e: FMLCommonSetupEvent) {
        ServerHookProcessor // registers itself for events
    }

    override fun interModCommsEnqueue(e: InterModEnqueueEvent) {
        InterModComms.sendTo("curios", CuriosAPI.IMC.REGISTER_TYPE) {
            CurioIMCMessage("hooked").also {
                it.size = 1
            }
        }
        InterModComms.sendTo("curios", CuriosAPI.IMC.REGISTER_ICON) {
            Tuple("hooked", loc("hooked:gui/hook_slot"))
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