package games.thecodewarrior.hooked.client.render

import com.teamwizardry.librarianlib.core.client.ClientTickHandler
import com.teamwizardry.librarianlib.features.helpers.vec
import com.teamwizardry.librarianlib.features.kotlin.*
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import games.thecodewarrior.hooked.HookLog
import games.thecodewarrior.hooked.common.hook.Hook
import games.thecodewarrior.hooked.common.hook.HookController
import games.thecodewarrior.hooked.common.hook.HookInFlight
import games.thecodewarrior.hooked.common.hook.HookType
import kotlin.math.min

abstract class AbstractHookRenderer<T: HookType, C: HookController>(val type: T): HookRenderer() {
    init {
        registryName = type.registryName
    }
    override fun render(controller: HookController) {
        @Suppress("UNCHECKED_CAST")
        renderController(controller as C)
    }

    open fun renderController(controller: C) {
        preRender(controller)
        controller.extendingHooks.forEach {
            renderHookInFlight(it)
        }
        controller.plantedHooks.forEach {
            renderPlantedHook(it)
        }
        controller.retractingHooks.forEach {
            renderHookInFlight(it)
        }
        postRender(controller)
    }

    /**
     * Specialized render for [HookInFlight]s
     */
    open fun renderHookInFlight(hook: HookInFlight) {
        renderHook(hook.pos, hook.direction)
    }

    /**
     * Specialized render for [Hook]s
     */
    open fun renderPlantedHook(hook: Hook) {
        renderHook(hook.pos, hook.direction)
    }

    /**
     * Generic render called by the default implementations for both [Hook]s and [HookInFlight]s
     *
     * @param pos The position of the back of the hook
     * @param direction The direction the hook is facing
     */
    open fun renderHook(pos: Vec3d, direction: Vec3d) {
        val player = Minecraft.getMinecraft().player
        val waist = HookController.getWaistPos(player) -
            (player.positionVector - vec(player.lastTickPosX, player.lastTickPosY, player.lastTickPosZ)) * (1-ClientTickHandler.partialTicks)
        val distance = (pos - waist).length()
        val normal = (pos - waist) / distance
        if(distance > 1024) {
            games.thecodewarrior.hooked.HookLog.warn("Absurd hook distance: $distance for hook at $pos going in direction $direction. Skipping")
            return
        }


        GlStateManager.pushMatrix()

        var rY = billboardAngle(vec(0, 0, 1), (direction * vec(1, 0, 1)).normalize(), vec(0, 1, 0))
        var rX = billboardAngle(vec(0, 1, 0), direction, null)

        GlStateManager.translate(pos.x, pos.y, pos.z)
        GlStateManager.rotate(rY, 0f, 1f, 0f)
        GlStateManager.rotate(rX, 1f, 0f, 0f)
        GlStateManager.translate(-0.5, 0.0, -0.5)

        renderHookTip(pos, direction)

        GlStateManager.popMatrix()

        GlStateManager.pushMatrix()

        rY = billboardAngle(vec(0, 0, 1), (normal * vec(1, 0, 1)).normalize(), vec(0, 1, 0))
        rX = billboardAngle(vec(0, 1, 0), normal, null)

        GlStateManager.translate(waist.x, waist.y, waist.z)
        GlStateManager.rotate(rY, 0f, 1f, 0f)
        GlStateManager.rotate(rX, 1f, 0f, 0f)
        GlStateManager.rotate(45f, 0f, 1f, 0f)

        renderChain(distance, normal)

        GlStateManager.popMatrix()

        Minecraft.getMinecraft().entityRenderer.disableLightmap()

    }

    /**
     * Renders the tip of the hook. The base of the hook should be at (0,0,0) and extend in the +Y axis.
     *
     * @param pos The real-world position of the hook
     * @param direction The real-world direction of the hook
     */
    open fun renderHookTip(pos: Vec3d, direction: Vec3d) {}

    /**
     * Renders the chain of the hook. The chain should be rendered in the +Y axis with (0,0,0) being the player's waist
     * and (0,[distance],0) being the hook's position.
     *
     * @param distance The distance in the +Y axis from the origin to the hook, i.e. the length of the chain
     * @param direction The normalized real-world direction from the player's waist to the hook
     */
    open fun renderChain(distance: Double, direction: Vec3d) {}

    /**
     * Called immediately before any rendering occurs
     */
    open fun preRender(controller: C) {}

    /**
     * Called immediately after all rendering completes.
     */
    open fun postRender(controller: C) {}

    private fun billboardAngle(a: Vec3d, b: Vec3d, n: Vec3d?): Float {
        val cross = a cross b
        val s = cross.length()
        val c = a dot b
        var angle = MathHelper.atan2(s, c)

        if (n != null) {
            if (n dot cross < 0) { // Or > 0
                angle = -angle
            }
        }

        return Math.toDegrees(angle).toFloat()
    }
}