package games.thecodewarrior.hooked

import games.thecodewarrior.hooked.items.ModItems
import net.fabricmc.api.ModInitializer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object Hooked: ModInitializer {
    const val modID: String = "hooked"

    val log: Logger = LogManager.getLogger()

    override fun onInitialize() {
        log.info("Initializing Hooked...")

        log.info("Registering items")
        ModItems.register()
    }
}