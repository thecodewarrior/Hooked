package dev.thecodewarrior.hooked.hook

import com.teamwizardry.librarianlib.core.util.Client
import dev.architectury.networking.NetworkManager
import dev.thecodewarrior.hooked.HookGameRules
import dev.thecodewarrior.hooked.Hooked
import dev.thecodewarrior.hooked.bridge.hookData
import dev.thecodewarrior.hooked.capability.HookedPlayerData
import dev.thecodewarrior.hooked.hooks.BasicHookPlayerController
import dev.thecodewarrior.hooked.item.HookProperties
import dev.thecodewarrior.hooked.network.ClientHookSyncC2SPacket
import dev.thecodewarrior.hooked.network.HookJumpC2SPacket
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.sound.SoundEvent
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
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
        override val properties: HookProperties get() = data.properties
        override val controller: HookPlayerController get() = data.controller
        override val player: PlayerEntity get() = data.player
        override val world: World get() = data.player.world
        override val hooks: Collection<Hook> get() = data.hooks.values

        override val isSelfClient: Boolean
            get() = data.player == Client.player

        // Note: in the interest of being resistant server-side lag, cooldowns are entirely on the client side.
        override val cooldown: Int get() = cooldownCounter
        override fun triggerCooldown() {
            cooldownCounter = properties.cooldown
        }

        override fun fireHook(pos: Vec3d, pitch: Float, yaw: Float, modifyFn: (Hook) -> Unit) {
            val hook = Hook(
                data.nextId(), data.properties.hookModel.hookLength,
                pos, pitch, yaw,
                Hook.State.EXTENDING,
                BlockPos(0, 0, 0),
                0
            )
            hook.firstTick = true
            data.hooks[hook.id] = hook
            data.syncStatus.syncToServer(hook)
            modifyFn(hook)
        }

        override fun syncHook(hook: Hook, sendToServer: Boolean, sendToClient: Boolean, sendToOthers: Boolean) {
            if (sendToServer) {
                data.syncStatus.syncToServer(hook)
            }
        }

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

    fun fireHook(player: PlayerEntity, data: HookedPlayerData, pos: Vec3d, pitch: Float, yaw: Float, sneaking: Boolean) {
        if(player.isFallFlying && !player.world.gameRules.getBoolean(HookGameRules.ALLOW_HOOKS_WHILE_FLYING)) {
            return
        }
        if (data.maxHooks > 0 && Client.minecraft.interactionManager?.currentGameMode != GameMode.SPECTATOR) {
            val ids = arrayListOf<Int>()
            data.controller.fireHooks(Context(data), pos, pitch, yaw, sneaking)
        }
    }

    fun jump(data: HookedPlayerData, doubleJump: Boolean, sneaking: Boolean) {
        if (data.maxHooks > 0) {
            data.controller.jump(Context(data), doubleJump, sneaking)

            NetworkManager.sendToServer(HookJumpC2SPacket(doubleJump, sneaking))
        }
    }

    override fun tick(player: PlayerEntity) {
        val data = player.hookData()

        applyHookMotion(Context(data))

        if(player == Client.player) {
            data.controller.update(Context(data))
            if(data.properties.cooldown == 0 || cooldownCounter > data.properties.cooldown) {
                cooldownCounter = 0
                hudCooldown = 0.0
            } else if(cooldownCounter > 0) {
                cooldownCounter--

                if(cooldownCounter == 0) {
                    hudCooldown = 0.01 // make sure there's one last frame with a full cooldown
                } else {
                    hudCooldown = cooldownCounter / data.properties.cooldown.toDouble()
                }
            } else {
                hudCooldown = 0.0
            }
            if (data.syncStatus.syncToServerHooks.isNotEmpty()) {
                NetworkManager.sendToServer(ClientHookSyncC2SPacket(
                    data.syncStatus.syncToServerHooks.values.toList()
                ))
            }
        }
        data.syncStatus.syncToServerHooks.clear()
    }

    override fun isHookActive(player: PlayerEntity, reason: HookActiveReason): Boolean {
        val data = player.hookData()
        return data.controller.isActive(Context(data), reason)
    }

    fun previewJumpTarget(player: ClientPlayerEntity): List<Box>? {
        val data = player.hookData()
        val controller = data.controller as? BasicHookPlayerController ?: return null
        if(data.hooks.values.none { it.state == Hook.State.PLANTED }) return null

        return controller.computeJumpTargets(Context(data))?.filter { it.minY > player.y }
    }

    private val logger = Hooked.logManager.makeLogger<ClientHookProcessor>()
}