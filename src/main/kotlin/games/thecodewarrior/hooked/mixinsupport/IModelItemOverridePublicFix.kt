package games.thecodewarrior.hooked.mixinsupport

import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.world.World

interface IModelItemOverridePublicFix {
    /**
     * Override and return a nonnull value to override the base `matches` method
     */
    fun matchesOverride(stack: ItemStack, world: World?, entity: LivingEntity?): Boolean?

    /**
     * Make base method publically accessible
     */
    @JvmDefault
    fun matchesAccess(stack: ItemStack, world: World?, entity: LivingEntity?): Boolean { return false }
}