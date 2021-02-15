package dev.thecodewarrior.hooked.hook

import com.teamwizardry.librarianlib.core.util.mapSrgName
import com.teamwizardry.librarianlib.core.util.vec
import com.teamwizardry.librarianlib.math.minus
import com.teamwizardry.librarianlib.math.plus
import com.teamwizardry.librarianlib.math.times
import com.teamwizardry.librarianlib.prism.SimpleSerializer
import com.teamwizardry.librarianlib.prism.Sync
import dev.thecodewarrior.hooked.HookedModSounds
import ll.dev.thecodewarrior.mirror.Mirror
import net.minecraft.entity.Entity
import net.minecraft.entity.ai.attributes.AttributeModifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.nbt.CompoundNBT
import net.minecraft.network.play.ServerPlayNetHandler
import net.minecraft.util.math.vector.Vector3d
import net.minecraftforge.common.ForgeMod.ENTITY_GRAVITY
import net.minecraftforge.common.util.INBTSerializable
import java.util.UUID
import kotlin.math.abs
import kotlin.math.sign

/**
 * Hook controllers translate the hook data into the player's movement.
 *
 * - Hook controllers will only be changed on the server and synced from on high.
 * - Hook controllers don't manage the movement of hooks, that's up to the hook processor.
 *
 * Hook controllers should have important data serialized using [@Sync][Sync]
 */
abstract class HookPlayerController: INBTSerializable<CompoundNBT> {
    private val serializer = SimpleSerializer.get(this.javaClass)

    /**
     * Called when the controller is removed so it can do any cleanup necessary.
     */
    open fun remove() {}

    /**
     * Called when the player tries to fire a hook
     *
     * @param addHook A function that will create and fire a hook
     */
    abstract fun fireHooks(
        delegate: HookControllerDelegate,
        pos: Vector3d, direction: Vector3d, sneaking: Boolean,
        addHook: (pos: Vector3d, direction: Vector3d) -> Hook
    )

    /**
     * Called when the jump key is pressed on the client
     */
    abstract fun jump(delegate: HookControllerDelegate, doubleJump: Boolean, sneaking: Boolean)

    /**
     * Called after the hook processor updates the hooks
     */
    abstract fun update(delegate: HookControllerDelegate)

    /**
     * Called when a hook hits a block
     */
    open fun onHookHit(delegate: HookControllerDelegate, hook: Hook) {
        delegate.playWorldSound(Hook.hitSound(delegate.world, hook.block), hook.pos, 1f, 1f)
        delegate.playFeedbackSound(HookedModSounds.hookHit, 1f, 1f)
    }

    /**
     * Called when a hook starts retracting because it reached full extension without hitting anything
     */
    open fun onHookMiss(delegate: HookControllerDelegate, hook: Hook) {
        delegate.playFeedbackSound(HookedModSounds.hookMiss, 1f, 1f)
    }

    /**
     * Called when a hook that was planted is dislodged
     */
    open fun onHookDislodge(delegate: HookControllerDelegate, hook: Hook, reason: DislodgeReason) {
        if(reason != DislodgeReason.HOOK_COUNT)
            delegate.playFeedbackSound(HookedModSounds.hookDislodge, 1f, 1f)
    }

    override fun serializeNBT(): CompoundNBT {
        return serializer.createTag(this, Sync::class.java)
    }

    override fun deserializeNBT(nbt: CompoundNBT) {
        serializer.applyTag(nbt, this, Sync::class.java)
    }

    /**
     * Resets the timer on the server that would normally kick a player for flying.
     */
    protected fun clearFlyingKickTimer(player: PlayerEntity) {
        if (player !is ServerPlayerEntity)
            return
        floatingTickCount.setFast(player.connection, 0)
    }

    /**
     * Attempts to pull the player toward the target position with the given force.
     *
     * @param pullForce The max speed the player should be pulled toward the target
     * @param enforcementForce The radius within which the player should be moved directly to the target. Defaults to
     * the [pullForce].
     * @param accelerationFactor How much of [pullForce] to apply per tick (until the player reaches full speed)
     * @param lockPlayer Whether to reset the player's motion when snapping using the [enforcementForce]
     */
    protected fun applyRestoringForce(
        player: PlayerEntity, target: Vector3d,
        pullForce: Double,
        enforcementForce: Double = pullForce,
        accelerationFactor: Double = 0.5,
        arrestingFactor: Double = 1.0,
        lockPlayer: Boolean = true
    ) {
        val deltaPos = target - player.positionVec
        val deltaLen = deltaPos.length()

        if (deltaLen <= enforcementForce) { // close enough that we should set to avoid oscillations
            movePlayer(player, deltaPos)
            if (lockPlayer)
                player.motion = vec(0, 0, 0)
        } else {
            val pull = deltaPos * (pullForce / deltaLen)

            player.motion = vec(
                applyRestoringComponentForce(player.motion.x, pull.x, accelerationFactor, arrestingFactor),
                applyRestoringComponentForce(player.motion.y, pull.y, accelerationFactor, arrestingFactor),
                applyRestoringComponentForce(player.motion.z, pull.z, accelerationFactor, arrestingFactor)
            )
        }
    }

    private fun applyRestoringComponentForce(
        motionComponent: Double,
        pullForceComponent: Double,
        accelerationFactor: Double,
        arrestingFactor: Double
    ): Double {
        val forceFactor = when {
            sign(motionComponent) != sign(pullForceComponent) ->
                arrestingFactor
            abs(motionComponent) < abs(pullForceComponent) ->
                accelerationFactor
            else -> 0.0
        }

        val result = motionComponent + pullForceComponent * forceFactor

        if (forceFactor != 0.0 && abs(result) > abs(pullForceComponent) && sign(result) == sign(pullForceComponent))
            return pullForceComponent

        return result
    }

    protected fun movePlayer(player: PlayerEntity, offset: Vector3d) {
        val allowedOffset = getAllowedMovement.callFast<Vector3d>(player, offset)
        val newPos = player.positionVec + allowedOffset
        player.setPosition(newPos.x, newPos.y, newPos.z)
    }

    enum class DislodgeReason {
        /**
         * The block the hook was attached to broke
         */
        BLOCK_BROKEN,

        /**
         * The player moved too far from the hook
         */
        DISTANCE,

        /**
         * The hook was dislodged because of the hook count limit
         */
        HOOK_COUNT
    }

    companion object {
        val NONE: HookPlayerController = object: HookPlayerController() {
            override fun jump(delegate: HookControllerDelegate, doubleJump: Boolean, sneaking: Boolean) {}
            override fun fireHooks(
                delegate: HookControllerDelegate,
                pos: Vector3d, direction: Vector3d, sneaking: Boolean,
                addHook: (pos: Vector3d, direction: Vector3d) -> Hook
            ) {
            }

            override fun update(delegate: HookControllerDelegate) {}
        }

        private val floatingTickCount = Mirror.reflectClass<ServerPlayNetHandler>()
            .getDeclaredField(mapSrgName("field_147365_f"))
        private val getAllowedMovement = Mirror.reflectClass<Entity>().declaredMethods
            .get(mapSrgName("func_213306_e"), Mirror.reflect<Vector3d>())
    }
}