package dev.thecodewarrior.hooked.hook

import com.teamwizardry.librarianlib.core.util.Client
import dev.thecodewarrior.hooked.Hooked
import dev.thecodewarrior.hooked.bridge.hookData
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.network.FireHookPacket
import dev.thecodewarrior.hooked.network.HookJumpPacket
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.sound.SoundEvent
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.GameMode
import net.minecraft.world.World

/**
 * Processes hooks on the *logical* client.
 */
object ClientHookProcessor: CommonHookProcessor() {

    @JvmStatic var hudCooldown: Double = 0.0
    private var cooldownCounter: Int = 0

    class Context(override val data: HookedPlayerData): HookProcessorContext {
        override val type: HookType get() = data.type
        override val controller: HookPlayerController get() = data.controller
        override val player: PlayerEntity get() = data.player
        override val world: World get() = data.player.world
        override val hooks: Collection<Hook> get() = data.hooks.values

        // Note: in the interest of being resistant server-side lag, cooldowns are entirely on the client side.
        override val cooldown: Int get() = cooldownCounter
        override fun triggerCooldown() {
            cooldownCounter = type.cooldown
        }

        override fun markDirty(hook: Hook) {}
        override fun forceFullSyncToClient() {}
        override fun forceFullSyncToOthers() {}

        private val playedSounds = mutableSetOf<SoundEvent>()

        override fun playFeedbackSound(sound: SoundEvent, volume: Float, pitch: Float) {
            if(!playedSounds.add(sound))
                return
            player.playSound(sound, volume, pitch)
        }

        override fun playWorldSound(sound: SoundEvent, pos: Vec3d, volume: Float, pitch: Float) {
            // world sounds are played on the server
        }

        override fun fireEvent(event: HookEvent) {
            data.syncStatus.recentEvents.add(event)
            val hook = data.hooks[event.id]
                ?: data.syncStatus.recentHooks[event.id]
                ?: return
            controller.triggerEvent(this, hook, event)
        }
    }

    fun triggerServerEvent(data: HookedPlayerData, event: HookEvent) {
        if(data.syncStatus.recentEvents.contains(event))
            return
        Context(data).fireEvent(event)
    }

    fun fireHook(data: HookedPlayerData, pos: Vec3d, pitch: Float, yaw: Float, sneaking: Boolean) {
        if (data.type != HookType.NONE && Client.minecraft.interactionManager?.currentGameMode != GameMode.SPECTATOR) {
            val ids = arrayListOf<Int>()
            val shouldSend = data.controller.fireHooks(Context(data), pos, pitch, yaw, sneaking) { hookPos, hookPitch, hookYaw ->
                val id = data.nextId()
                ids.add(id)
                val hook = Hook(
                    id, data.type,
                    hookPos, hookPitch, hookYaw,
                    Hook.State.EXTENDING,
                    BlockPos(0, 0, 0),
                    0
                )
                data.hooks[id] = hook

                hook
            }

            if(shouldSend) {
                ClientPlayNetworking.send(
                    Hooked.Packets.FIRE_HOOK,
                    FireHookPacket(
                        pos, pitch, yaw,
                        sneaking, ids
                    ).encode()
                )
            }
        }
    }

    fun jump(data: HookedPlayerData, doubleJump: Boolean, sneaking: Boolean) {
        if (data.type != HookType.NONE) {
            data.controller.jump(Context(data), doubleJump, sneaking)

            ClientPlayNetworking.send(
                Hooked.Packets.HOOK_JUMP,
                HookJumpPacket(doubleJump, sneaking).encode()
            )
        }
    }

    override fun tick(player: PlayerEntity) {
        val data = player.hookData()

        applyHookMotion(Context(data))

        if(player == Client.player) {
            data.controller.update(Context(data))
            if(data.type.cooldown == 0 || cooldownCounter > data.type.cooldown) {
                cooldownCounter = 0
                hudCooldown = 0.0
            } else if(cooldownCounter > 0) {
                cooldownCounter--

                if(cooldownCounter == 0) {
                    hudCooldown = 0.01 // make sure there's one last frame with a full cooldown
                } else {
                    hudCooldown = cooldownCounter / data.type.cooldown.toDouble()
                }
            } else {
                hudCooldown = 0.0
            }
        }
    }

    override fun isHookActive(player: PlayerEntity, reason: HookActiveReason): Boolean {
        val data = player.hookData()
        return data.controller.isActive(Context(data), reason)
    }

    private val logger = Hooked.logManager.makeLogger<ClientHookProcessor>()
}