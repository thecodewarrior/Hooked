package dev.thecodewarrior.hooked.hook

import com.teamwizardry.librarianlib.core.util.mapSrgName
import com.teamwizardry.librarianlib.core.util.vec
import com.teamwizardry.librarianlib.math.minus
import com.teamwizardry.librarianlib.math.plus
import com.teamwizardry.librarianlib.math.times
import com.teamwizardry.librarianlib.prism.SimpleSerializer
import com.teamwizardry.librarianlib.prism.Sync
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

    abstract fun jump(player: PlayerEntity, hooks: List<Hook>, dirtyHooks: MutableSet<Hook>, doubleJump: Boolean, sneaking: Boolean)

    /**
     * Called after the hook processor updates the hooks
     */
    abstract fun update(player: PlayerEntity, hooks: List<Hook>, dirtyHooks: MutableSet<Hook>)

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
        lockPlayer: Boolean = true
    ) {
        val deltaPos = target - player.positionVec
        val deltaLen = deltaPos.length()

        if (deltaLen <= enforcementForce) { // close enough that we should set to avoid oscillations
            movePlayer(player, deltaPos)
            if(lockPlayer)
                player.motion = vec(0, 0, 0)
        } else {
            val pull = deltaPos * (pullForce / deltaLen)

            player.motion = vec(
                applyRestoringComponentForce(player.motion.x, pull.x, accelerationFactor),
                applyRestoringComponentForce(player.motion.y, pull.y, accelerationFactor),
                applyRestoringComponentForce(player.motion.z, pull.z, accelerationFactor)
            )
        }
    }

    private fun applyRestoringComponentForce(motionComponent: Double, pullForceComponent: Double, accelerationFactor: Double): Double {
        if (abs(motionComponent) < abs(pullForceComponent) || sign(motionComponent) != sign(pullForceComponent)) {
            val adjusted = motionComponent + pullForceComponent * accelerationFactor
            if (abs(adjusted) > abs(pullForceComponent) && sign(adjusted) == sign(pullForceComponent))
                return pullForceComponent
            else
                return adjusted
        }

        return motionComponent
    }

    protected fun disableGravity(player: PlayerEntity) {
        val gravityAttribute = player.getAttribute(ENTITY_GRAVITY.get()) ?: return
        if (gravityAttribute.hasModifier(HOOK_GRAVITY))
            gravityAttribute.applyNonPersistentModifier(HOOK_GRAVITY)
    }

    protected fun enableGravity(player: PlayerEntity) {
        val gravityAttribute = player.getAttribute(ENTITY_GRAVITY.get()) ?: return
        if (gravityAttribute.hasModifier(HOOK_GRAVITY))
            gravityAttribute.removeModifier(HOOK_GRAVITY)
    }

    protected fun movePlayer(player: PlayerEntity, offset: Vector3d) {
        val allowedOffset = getAllowedMovement.callFast<Vector3d>(player, offset)
        val newPos = player.positionVec + allowedOffset
        player.setPosition(newPos.x, newPos.y, newPos.z)
    }

    companion object {
        val NONE: HookPlayerController = object: HookPlayerController() {
            override fun jump(
                player: PlayerEntity,
                hooks: List<Hook>,
                dirtyHooks: MutableSet<Hook>,
                doubleJump: Boolean,
                sneaking: Boolean
            ) {}

            override fun update(
                player: PlayerEntity,
                hooks: List<Hook>,
                dirtyHooks: MutableSet<Hook>
            ) {}
        }

        private val floatingTickCount = Mirror.reflectClass<ServerPlayNetHandler>()
            .getDeclaredField(mapSrgName("field_147365_f"))
        private val getAllowedMovement = Mirror.reflectClass<Entity>().declaredMethods
            .get(mapSrgName("func_213306_e"), Mirror.reflect<Vector3d>())

        private val HOOK_GRAVITY_ID: UUID = UUID.fromString("654bd58d-a1c6-40e7-9d2b-09699b9558fe")
        /**
         * The `MULTIPLY_TOTAL` operation applies a `*= 1 + value` to the final value. By having a modifier of -1 that
         * becomes `*= 0`, always nullifying the gravity.
         */
        private val HOOK_GRAVITY: AttributeModifier = AttributeModifier(
            HOOK_GRAVITY_ID,
            "Hook gravity modifier",
            -1.0,
            AttributeModifier.Operation.MULTIPLY_TOTAL
        )
    }
}