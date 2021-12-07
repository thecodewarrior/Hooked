package dev.thecodewarrior.hooked.hook

import com.teamwizardry.librarianlib.core.util.Client
import com.teamwizardry.librarianlib.courier.CourierClientPlayNetworking
import dev.thecodewarrior.hooked.Hooked
import dev.thecodewarrior.hooked.bridge.hookData
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.network.FireHookPacket
import dev.thecodewarrior.hooked.network.HookJumpPacket
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.sound.SoundEvent
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.GameMode
import net.minecraft.world.World
import java.util.*

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
        override val hooks: MutableList<Hook> get() = data.hooks

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
            val hook = hooks.find { it.uuid == event.uuid }
                ?: data.syncStatus.recentHooks.find { it.uuid == event.uuid }
                ?: return
            controller.triggerEvent(this, hook, event)
        }
    }

    fun triggerServerEvent(data: HookedPlayerData, event: HookEvent) {
        if(data.syncStatus.recentEvents.contains(event))
            return
        Context(data).fireEvent(event)
    }

    fun syncHook(data: HookedPlayerData, hook: Hook) {
        if(hook.state == Hook.State.REMOVED) {
            data.hooks.removeIf { it.uuid == hook.uuid }
            data.syncStatus.recentHooks.add(hook)
        } else {
            val existingIndex = data.hooks.indexOfFirst { it.uuid == hook.uuid }
            if (existingIndex == -1) {
                data.hooks.add(hook)
            } else {
                data.hooks[existingIndex] = hook
            }
        }
    }

    fun fireHook(data: HookedPlayerData, pos: Vec3d, direction: Vec3d, sneaking: Boolean) {
        if (data.type != HookType.NONE && Client.minecraft.interactionManager?.currentGameMode != GameMode.SPECTATOR) {
            val uuids = arrayListOf<UUID>()
            val shouldSend = data.controller.fireHooks(Context(data), pos, direction, sneaking) { hookPos, hookDirection ->
                val uuid = UUID.randomUUID()
                uuids.add(uuid)
                val hook = Hook(
                    uuid,
                    data.type,
                    hookPos,
                    Hook.State.EXTENDING,
                    hookDirection,
                    BlockPos(0, 0, 0),
                    0
                )
                data.hooks.add(hook)

                hook
            }

            if(shouldSend) {
                CourierClientPlayNetworking.send(
                    Hooked.Packets.FIRE_HOOK,
                    FireHookPacket(
                        pos,
                        direction,
                        sneaking,
                        uuids
                    )
                )
            }
        }
    }

    fun jump(data: HookedPlayerData, doubleJump: Boolean, sneaking: Boolean) {
        if (data.type != HookType.NONE) {
            data.controller.jump(Context(data), doubleJump, sneaking)

            CourierClientPlayNetworking.send(
                Hooked.Packets.HOOK_JUMP,
                HookJumpPacket(doubleJump, sneaking)
            )
        }
    }

    fun tick(player: ClientPlayerEntity) {
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

    private val logger = Hooked.logManager.makeLogger<ClientHookProcessor>()
}