package thecodewarrior.hooked.common.config

import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonObject
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializer
import java.lang.IllegalArgumentException
import java.lang.reflect.Type

abstract class DiscriminatorAdapter<T: Any>: JsonSerializer<T>, JsonDeserializer<T> {
    val types = mutableMapOf<String, Class<out T>>()

    override fun serialize(src: T, typeOfSrc: Type,
        context: JsonSerializationContext): JsonElement {

        val retValue = JsonObject()
        val name = types.entries.find { it.value == src.javaClass }?.key
            ?: throw IllegalArgumentException("Unrecognized object type ${src.javaClass.simpleName}. " +
                "Recognized types: [${types.values.map { it.simpleName }.sorted().joinToString { ", " }}]")
        retValue.addProperty("type", name)
        val elem = context.serialize(src)
        retValue.add("data", elem)
        return retValue
    }

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type,
        context: JsonDeserializationContext): T {
        val jsonObject = json.asJsonObject
        val prim = jsonObject["type"] as JsonPrimitive
        val klass = types[prim.asString]
            ?: throw JsonParseException("Unknown type discriminator `${prim.asString}`. " +
                "Known types are [${types.keys.sorted().joinToString(", ")}]")

        @Suppress("UNCHECKED_CAST")
        return context.deserialize<Any>(jsonObject.get("data"), klass) as T
    }
}
