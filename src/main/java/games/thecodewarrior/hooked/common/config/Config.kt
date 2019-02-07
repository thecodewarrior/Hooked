package games.thecodewarrior.hooked.common.config

import com.google.gson.GsonBuilder
import com.teamwizardry.librarianlib.features.kotlin.toRl
import net.minecraft.util.ResourceLocation
import games.thecodewarrior.hooked.common.hook.HookType

object HookTypesConfig {
    var entries: List<HookEntry> = listOf()
        private set

    private val gson = GsonBuilder()
        .registerTypeAdapter(HookBehavior::class.java, HookBehaviorAdapter)
        .registerTypeAdapter(HookAppearance::class.java, HookAppearanceAdapter)
        .registerTypeAdapter(ResourceLocation::class.java, ResourceLocation.Serializer())
        .create()

    object HookBehaviorAdapter: DiscriminatorAdapter<HookBehavior>()
    object HookAppearanceAdapter: DiscriminatorAdapter<HookAppearance>()

    init {
        HookBehaviorAdapter.types["basic"] = BasicHookBehavior::class.java
        HookBehaviorAdapter.types["flight"] = FlightHookBehavior::class.java
        HookAppearanceAdapter.types["basic"] = BasicHookAppearance::class.java
        HookAppearanceAdapter.types["flight"] = FlightHookAppearance::class.java
    }

    private class ConfigContainer {
        var entries: List<HookEntry> = mutableListOf()
    }

    fun read(json: String) {
        val container = gson.fromJson(json, ConfigContainer::class.java)
        entries = container.entries
    }
}



