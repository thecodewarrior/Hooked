package dev.thecodewarrior.hooked.hook

import com.teamwizardry.librarianlib.core.util.mixinCast
import com.teamwizardry.librarianlib.core.util.vec
import com.teamwizardry.librarianlib.etcetera.Raycaster
import com.teamwizardry.librarianlib.math.minus
import com.teamwizardry.librarianlib.math.plus
import com.teamwizardry.librarianlib.math.times
import dev.thecodewarrior.hooked.Hooked
import dev.thecodewarrior.hooked.mixin.EntityAccessMixin
import dev.thecodewarrior.hooked.mixin.FloatingTicksAccess
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShapes
import kotlin.math.abs
import kotlin.math.sign

/**
 * Hook controllers translate the hook data into the player's movement.
 *
 * - Hook controllers will only be changed on the server and synced from on high.
 * - Hook controllers don't manage the movement of hooks, that's up to the hook processor.
 */
abstract class HookPlayerController {
    /**
     * Called when the controller is removed so it can do any cleanup necessary.
     */
    open fun remove() {}

    /**
     * Called when the player tries to fire a hook
     *
     * @param addHook A function that will create and fire a hook
     * @return On the client, a value of true indicates that a packet should be sent to the server. On the server this
     * does nothing.
     */
    abstract fun fireHooks(
        delegate: HookControllerDelegate,
        pos: Vec3d, pitch: Float, yaw: Float, sneaking: Boolean,
        addHook: (pos: Vec3d, pitch: Float, yaw: Float) -> Hook
    ): Boolean

    /**
     * Called when the jump key is pressed on the client
     */
    abstract fun jump(delegate: HookControllerDelegate, doubleJump: Boolean, sneaking: Boolean)

    /**
     * Configures the raycaster. The hook processor considers any hit type other than [Raycaster.HitType.BLOCK] to be a
     * miss, so you can't cast against fluids or entities.
     */
    open fun configureRaycast(request: Raycaster.RaycastRequest) {
        request.withBlockMode(Raycaster.BlockMode.COLLISION)
            .withBlockOverride { state, _, _ ->
                when {
                    state.isIn(Hooked.Tags.SOLID_BLOCKS) -> VoxelShapes.fullCube()
                    state.isIn(Hooked.Tags.IGNORE_BLOCKS) -> VoxelShapes.empty()
                    else -> null
                }
            }
    }

    /**
     * Called after the hook processor updates the hooks
     */
    abstract fun update(delegate: HookControllerDelegate)

    open fun saveState(tag: NbtCompound) {}
    open fun loadState(tag: NbtCompound) {}
    // this synchronization only happens when doing a full sync, not incremental syncs
    /**
     * Write to the synchronization packet. This isn't used for incremental synchronization packets.
     *
     * @param initial true if [buf] is the initial sync packet or false if [buf] a subsequent update packet
     */
    open fun writeSyncState(buf: PacketByteBuf, initial: Boolean) {}
    /**
     * Read from the synchronization packet.
     *
     * @param initial true if [buf] is the initial sync packet or false if [buf] a subsequent update packet
     */
    open fun readSyncState(buf: PacketByteBuf, initial: Boolean) {}

    open fun isActive(delegate: HookControllerDelegate, reason: HookActiveReason): Boolean {
        return delegate.hooks.any { it.state == Hook.State.PLANTED }
    }

    open fun triggerEvent(delegate: HookControllerDelegate, hook: Hook, hookEvent: HookEvent) {
        when (hookEvent.type) {
            HookEvent.EventType.HIT -> onHookHit(delegate, hook)
            HookEvent.EventType.MISS -> onHookMiss(delegate, hook)
            HookEvent.EventType.DISLODGE -> onHookDislodge(
                delegate,
                hook,
                DislodgeReason.values().getOrElse(hookEvent.data) { DislodgeReason.EXPLICIT }
            )
        }
    }

    /**
     * Called when a hook hits a block
     */
    open fun onHookHit(delegate: HookControllerDelegate, hook: Hook) {
        delegate.playWorldSound(Hook.hitSound(delegate.world, hook.block), hook.pos, 1f, 1f)
        delegate.playFeedbackSound(Hooked.Sounds.HOOK_HIT_EVENT, 1f, 1f)
    }

    /**
     * Called when a hook starts retracting because it reached full extension without hitting anything
     */
    open fun onHookMiss(delegate: HookControllerDelegate, hook: Hook) {
        delegate.playFeedbackSound(Hooked.Sounds.HOOK_MISS_EVENT, 1f, 1f)
    }

    /**
     * Called when a hook that was planted is dislodged
     */
    open fun onHookDislodge(delegate: HookControllerDelegate, hook: Hook, reason: DislodgeReason) {
        when(reason) {
            DislodgeReason.BLOCK_BROKEN, DislodgeReason.DISTANCE -> {
                delegate.playFeedbackSound(Hooked.Sounds.HOOK_DISLODGE_EVENT, 1f, 1f)
            }
            DislodgeReason.DISALLOWED -> {
                delegate.playFeedbackSound(Hooked.Sounds.HOOK_DISLODGE_EVENT, 1f, 1f)
            }
            DislodgeReason.HOOK_COUNT -> {}
            DislodgeReason.EXPLICIT -> {
                delegate.playWorldSound(Hook.hitSound(delegate.world, hook.block), hook.pos, 1f, 1f)
                delegate.playFeedbackSound(Hooked.Sounds.RETRACT_HOOK_EVENT, 1f, 1f)
            }
        }
    }

    /**
     * Resets the timer on the server that would normally kick a player for flying.
     */
    protected fun clearFlyingKickTimer(player: PlayerEntity) {
        if (player !is ServerPlayerEntity)
            return
        mixinCast<FloatingTicksAccess>(player.networkHandler).floatingTicks = 0
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
        player: PlayerEntity, target: Vec3d,
        pullForce: Double,
        enforcementForce: Double = pullForce,
        accelerationFactor: Double = 0.5,
        arrestingFactor: Double = 1.0,
        lockPlayer: Boolean = true
    ) {
        val deltaPos = target - player.pos
        val deltaLen = deltaPos.length()

        if (deltaLen <= enforcementForce) { // close enough that we should set to avoid oscillations
            movePlayer(player, deltaPos)
            if (lockPlayer)
                player.velocity = vec(0, 0, 0)
        } else {
            val pull = deltaPos * (pullForce / deltaLen)

            player.velocity = vec(
                applyRestoringComponentForce(player.velocity.x, pull.x, accelerationFactor, arrestingFactor),
                applyRestoringComponentForce(player.velocity.y, pull.y, accelerationFactor, arrestingFactor),
                applyRestoringComponentForce(player.velocity.z, pull.z, accelerationFactor, arrestingFactor)
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

    protected fun movePlayer(player: PlayerEntity, offset: Vec3d) {
        val allowedOffset = mixinCast<EntityAccessMixin>(player).invokeAdjustMovementForCollisions(offset)
        val newPos = player.pos + allowedOffset
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
         * The hook was disallowed. e.g. because the player was flying and allowHooksWhileFlying was false
         */
        DISALLOWED,

        /**
         * The hook was dislodged because of the hook count limit
         */
        HOOK_COUNT,

        /**
         * The player explicitly retracted the hook
         */
        EXPLICIT,
    }

    companion object {
        val NONE: HookPlayerController = None
        private object None: HookPlayerController() {
            override fun jump(delegate: HookControllerDelegate, doubleJump: Boolean, sneaking: Boolean) {}
            override fun fireHooks(
                delegate: HookControllerDelegate,
                pos: Vec3d, pitch: Float, yaw: Float,
                sneaking: Boolean,
                addHook: (pos: Vec3d, pitch: Float, yaw: Float) -> Hook
            ): Boolean {
                return false
            }

            override fun isActive(delegate: HookControllerDelegate, reason: HookActiveReason): Boolean = false

            override fun update(delegate: HookControllerDelegate) {}
        }
    }
}