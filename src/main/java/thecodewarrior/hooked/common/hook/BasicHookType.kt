package thecodewarrior.hooked.common.hook

import com.teamwizardry.librarianlib.features.kotlin.nbt
import net.minecraft.client.resources.I18n
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagByte
import net.minecraft.util.ResourceLocation
import thecodewarrior.hooked.client.KeyBinds

class BasicHookType(
        name: ResourceLocation,
        /**
         * The number of simultaneous hooks allowed
         */
        val count: Int,
        /**
         * The maximum range from impact point to player
         */
        val range: Double,
        /**
         * The speed of the fired hooks in m/t
         */
        val speed: Double,
        /**
         * The speed the player is pulled toward the target point in m/t
         */
        val pullStrength: Double,
        /**
         * The distance from the impact point to where the chain should attach
         */
        val hookLength: Double
): HookType() {
    init {
        setRegistryName(name)
    }

    override fun create(player: EntityPlayer): HookController {
        return BasicHookController(this, player, count, range, speed, pullStrength, hookLength)
    }

    private fun inhibited(item: ItemStack): Boolean {
        return (item.nbt["inhibited"] as? NBTTagByte)?.byte == 1.toByte()
    }

    override fun adjustStack(stack: ItemStack): ItemStack? {
        if(count <= 1) return null
        val item = stack.copy()
        if(inhibited(item)) {
            item.nbt["inhibited"] = null
        } else {
            item.nbt["inhibited"] = NBTTagByte(1.toByte())
        }
        return item
    }

    override fun addBasicInformation(stack: ItemStack, tooltip: MutableList<String>) {
        if (count > 1) {
            if (inhibited(stack)) {
                tooltip.add(I18n.format("tooltip.hooked:hook.inhibited"))
            }
        }
        tooltip.add(I18n.format("tooltip.hooked.showControls"))
    }

    override fun addFullInformation(stack: ItemStack, tooltip: MutableList<String>) {
        if (count > 1) {
            if (inhibited(stack)) {
                tooltip.add(I18n.format("tooltip.hooked:hook.inhibited"))
                tooltip.add(I18n.format("tooltip.hooked:hook.inhibited.help"))
            } else {
                tooltip.add(I18n.format("tooltip.hooked:hook.uninhibited.help"))
            }
        }
        val controls = I18n.format("tooltip.hooked:hook.$registryName.controls", KeyBinds.keyFire.displayName)
        tooltip.addAll(controls.split("\\n").map { "- $it" })
    }
}