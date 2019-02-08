package games.thecodewarrior.hooked.client.render

import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.util.ResourceLocation
import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.registries.IForgeRegistryEntry
import games.thecodewarrior.hooked.common.hook.HookController
import games.thecodewarrior.hooked.common.hook.HookType

abstract class HookRenderer<T: HookType, C: HookController<*>>(val type: T) {
    abstract fun render(controller: C)
    abstract fun reloadResources()
    abstract fun registerSprites(map: TextureMap)
}