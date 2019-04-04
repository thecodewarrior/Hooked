package games.thecodewarrior.hooked

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Environment(EnvType.CLIENT)
object HookedClient: ClientModInitializer {
    val log: Logger = LogManager.getLogger()

    override fun onInitializeClient() {
    }
}