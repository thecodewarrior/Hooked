package thecodewarrior.hooked.client.render

import net.minecraft.client.renderer.texture.TextureMap
import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.registries.IForgeRegistryEntry
import thecodewarrior.hooked.common.hook.HookController

abstract class HookRenderer: IForgeRegistryEntry.Impl<HookRenderer>() {
    abstract fun render(controller: HookController)

    abstract fun reloadResources()
    abstract fun registerSprites(map: TextureMap)
    companion object {
        @JvmStatic
        lateinit var REGISTRY: IForgeRegistry<HookRenderer>
    }
}