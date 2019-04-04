package games.thecodewarrior.hooked.mixinsupport

import com.google.gson.JsonElement
import com.mojang.datafixers.Dynamic
import com.mojang.datafixers.types.JsonOps
import games.thecodewarrior.hooked.util.matchesPattern
import net.minecraft.client.render.model.json.ModelItemOverride
import net.minecraft.datafixers.NbtOps
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.util.Identifier
import net.minecraft.util.TagHelper
import net.minecraft.world.World

class NbtSensitiveModelItemOverride(
    val nbt: Tag,
    val base: ModelItemOverride
): ModelItemOverride(base.modelId, emptyMap()), IModelItemOverridePublicFix {

    // overridden to support compounding of overrides, if for some reason someone wanted to.
    override fun getModelId(): Identifier {
        return base.getModelId()
    }

    override fun matchesOverride(stack: ItemStack, world: World?, entity: LivingEntity?): Boolean? {
        val stackTag = stack.tag ?: emptyTag
        if(!stackTag.matchesPattern(nbt)) return false
        return base.mCast<IModelItemOverridePublicFix>().matchesAccess(stack, world, entity)
    }

    companion object {
        private val emptyTag = CompoundTag()

        @JvmStatic
        fun deserialize(json: JsonElement, baseOverride: ModelItemOverride): NbtSensitiveModelItemOverride {
            val tag = Dynamic.convert(JsonOps.INSTANCE, NbtOps.INSTANCE, json)
            return NbtSensitiveModelItemOverride(tag, baseOverride)
        }
    }
}