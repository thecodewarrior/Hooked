package games.thecodewarrior.hooked.common.config

import com.google.gson.GsonBuilder
import com.teamwizardry.librarianlib.features.kotlin.toRl
import net.minecraft.util.ResourceLocation
import games.thecodewarrior.hooked.common.hook.HookType
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonSerializer
import com.google.gson.JsonDeserializer
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import java.lang.reflect.Type

private val types = mutableMapOf<String, HookType>()
object HookTypes: Map<String, HookType> by types {

    private val gson = GsonBuilder()
        .registerTypeAdapter(HookType::class.java, HookType.Serializer)
        .registerTypeAdapter(ResourceLocation::class.java, ResourceLocation.Serializer())
        .create()

    private class ConfigContainer {
        var entries: List<HookType> = mutableListOf()
    }

    fun read(json: String) {
        val container = gson.fromJson(json, ConfigContainer::class.java)
        container.entries.forEach {
            types[it.name] = it
        }
    }
}

