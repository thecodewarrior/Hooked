package thecodewarrior.hooked.common.items

import baubles.api.BaubleType
import baubles.api.BaublesApi
import baubles.api.IBauble
import com.teamwizardry.librarianlib.common.base.item.ItemMod
import com.teamwizardry.librarianlib.common.util.ifCap
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import thecodewarrior.hooked.common.HookType
import thecodewarrior.hooked.common.capability.HooksCap

/**
 * Created by TheCodeWarrior
 */
class ItemHook : ItemMod("hook", *HookType.values().map { "hook_" + it.toString().toLowerCase() }.toTypedArray()), IBauble {
    init {
        setMaxStackSize(1)
    }
    override fun getBaubleType(p0: ItemStack?): BaubleType {
        return BaubleType.TRINKET
    }

    override fun onUnequipped(itemstack: ItemStack, player: EntityLivingBase?) {
        player?.ifCap(HooksCap.CAPABILITY, null) {
            hookType = null
            update(player)
        }
    }

    override fun onEquipped(itemstack: ItemStack, player: EntityLivingBase?) {
        player?.ifCap(HooksCap.CAPABILITY, null) {
            hookType = HookType.values()[itemstack.itemDamage % HookType.values().size]
            update(player)
        }
    }

    override fun canEquip(itemstack: ItemStack, player: EntityLivingBase): Boolean {
        if(player !is EntityPlayer)
            return false
        val handler = BaublesApi.getBaublesHandler(player)
        return !(0..handler.slots-1).any { handler.getStackInSlot(it)?.item == ModItems.hook }
    }

    override fun willAutoSync(itemstack: ItemStack?, player: EntityLivingBase?): Boolean {
        return true
    }
}
