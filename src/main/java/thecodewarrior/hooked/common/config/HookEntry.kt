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
    var range: Double = 16.0
    var speed: Double = 0.8
    var pullStrength: Double = 0.5
    var hookLength: Double = 0.5
    var jumpBoost: Double = 0.05

    override fun createType(name: ResourceLocation): HookType = BasicHookType(
        name,
        count,
        range,
        speed,
        pullStrength,
        hookLength,
        jumpBoost
    )
}

class FlightHookBehavior: HookBehavior() {
    var count: Int = 1
    var range: Double = 16.0
    var speed: Double = 0.8
    var pullStrength: Double = 0.5
    var hookLength: Double = 0.5
    var jumpBoost: Double = 0.05

    override fun createType(name: ResourceLocation): HookType = FlightHookType(
        name,
        count,
        range,
        speed,
        pullStrength,
        hookLength,
        jumpBoost
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
