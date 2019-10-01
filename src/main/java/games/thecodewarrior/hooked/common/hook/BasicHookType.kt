package games.thecodewarrior.hooked.common.hook

import com.teamwizardry.librarianlib.features.kotlin.nbt
import com.teamwizardry.librarianlib.features.kotlin.toRl
import net.minecraft.client.resources.I18n
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagByte
import net.minecraft.util.ResourceLocation
import games.thecodewarrior.hooked.client.KeyBinds
import games.thecodewarrior.hooked.client.render.BasicHookRenderer
import games.thecodewarrior.hooked.client.render.HookRenderer

open class BasicHookType: HookType() {
    override var id: String = "missingno"
    override var count: Int = 0
    override var range: Double = 5.0
    override var speed: Double = 0.2
    override var pullStrength: Double = 0.2
    override var hookLength: Double = 0.5
    override var jumpBoost: Double = 0.05
    override var cooldown: Int = 0

    var playerGap: Double = 0.0
    var hookModel: ResourceLocation = "missingno".toRl()
    var verticalRopeTexture: ResourceLocation = "missingno".toRl()
    var horizontalRopeTexture: ResourceLocation = "missingno".toRl()

    override fun initRenderer(): HookRenderer<*, *> {
        return BasicHookRenderer(this)
    }

    override fun createController(player: EntityPlayer): HookController<out BasicHookType> {
        return BasicHookController(this, player)
    }
}