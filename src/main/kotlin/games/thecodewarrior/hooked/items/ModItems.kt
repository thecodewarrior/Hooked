package games.thecodewarrior.hooked.items

import games.thecodewarrior.hooked.Hooked
import games.thecodewarrior.hooked.util.ident
import net.minecraft.util.registry.Registry

object ModItems {
    val hook = ItemHook()

    fun register() {
        Registry.ITEM.add(ident(Hooked.modID, "hook"), hook)
    }
}