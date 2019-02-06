package thecodewarrior.hooked.common.config

import com.teamwizardry.librarianlib.features.kotlin.toRl
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import thecodewarrior.hooked.client.render.BasicHookRenderer
import thecodewarrior.hooked.client.render.FlightHookRenderer
import thecodewarrior.hooked.client.render.HookRenderer
import thecodewarrior.hooked.common.hook.BasicHookType
import thecodewarrior.hooked.common.hook.FlightHookType
import thecodewarrior.hooked.common.hook.HookType

class HookEntry {
    var name: ResourceLocation = "hooked:missingno".toRl()
    lateinit var behavior: HookBehavior
    lateinit var appearance: HookAppearance
}

abstract class HookBehavior {
    abstract fun createType(name: ResourceLocation): HookType
}

open class BasicHookBehavior: HookBehavior() {
    var count: Int = 1
    var range: Double = 1.0
    var speed: Double = 1.0
    var pullStrength: Double = 1.0
    var hookLength: Double = 1.0

    override fun createType(name: ResourceLocation): HookType = BasicHookType(
        name,
        count,
        range,
        speed,
        pullStrength,
        hookLength
    )
}

class FlightHookBehavior: HookBehavior() {
    var count: Int = 1
    var range: Double = 1.0
    var speed: Double = 1.0
    /**
     * Unused at the moment
     */
    var pullStrength: Double = 1.0
    var hookLength: Double = 1.0

    override fun createType(name: ResourceLocation): HookType = FlightHookType(
        name,
        count,
        range,
        speed,
        hookLength
    )
}

abstract class HookAppearance {
    @SideOnly(Side.CLIENT)
    abstract fun createRenderer(type: HookType): HookRenderer
}
open class BasicHookAppearance: HookAppearance() {
    var playerGap: Double = 0.0
    val hookModel: ResourceLocation = "hooked:missingno".toRl()
    val verticalRope: ResourceLocation = "hooked:missingno".toRl()
    val horizontalRope: ResourceLocation = "hooked:missingno".toRl()

    override fun createRenderer(type: HookType): HookRenderer = BasicHookRenderer(
        type as BasicHookType,
        playerGap,
        hookModel,
        verticalRope,
        horizontalRope
    )
}

class FlightHookAppearance: HookAppearance() {
    var playerGap: Double = 0.0
    val hookModel: ResourceLocation = "hooked:missingno".toRl()
    val verticalRope: ResourceLocation = "hooked:missingno".toRl()
    val horizontalRope: ResourceLocation = "hooked:missingno".toRl()

    override fun createRenderer(type: HookType): HookRenderer = FlightHookRenderer(
        type as BasicHookType,
        playerGap,
        hookModel,
        verticalRope,
        horizontalRope
    )
}
