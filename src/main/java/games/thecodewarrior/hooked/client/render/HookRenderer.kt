package games.thecodewarrior.hooked.client.render

import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.util.ResourceLocation
import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.registries.IForgeRegistryEntry
import games.thecodewarrior.hooked.common.hook.HookController
import games.thecodewarrior.hooked.common.hook.HookType

abstract class HookRenderer: IForgeRegistryEntry.Impl<HookRenderer>() {
    abstract fun render(controller: HookController)

    abstract fun reloadResources()
    abstract fun registerSprites(map: TextureMap)
    companion object {
        @JvmStatic
        lateinit var REGISTRY: IForgeRegistry<HookRenderer>
        val missingno = BasicHookRenderer(HookType.missingno, 0.0,
            ResourceLocation("missingno"),
            ResourceLocation("missingno"),
            ResourceLocation("missingno")
        )
    }
}