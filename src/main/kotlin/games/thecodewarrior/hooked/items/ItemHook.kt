package games.thecodewarrior.hooked.items

import games.thecodewarrior.hooked.util.set
import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult

class ItemHook: Item(
    Settings()
        .stackSize(1)
) {
    override fun useOnBlock(usageContext: ItemUsageContext): ActionResult {
        usageContext.itemStack.getOrCreateTag()["hook_type"] = if(usageContext.isPlayerSneaking) "iron" else "diamond"
        return super.useOnBlock(usageContext)
    }
}