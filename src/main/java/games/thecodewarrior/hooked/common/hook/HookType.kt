package games.thecodewarrior.hooked.common.hook

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.teamwizardry.librarianlib.features.kotlin.toRl
import games.thecodewarrior.hooked.client.render.HookRenderer
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.registries.IForgeRegistryEntry
import java.lang.reflect.Type

abstract class HookType {
    abstract val name: String

    abstract val model: String
    /**
     * The number of simultaneous hooks allowed
     */
    abstract val count: Int

    /**
     * The maximum range from impact point to player
     */
    abstract val range: Double

    /**
     * The speed of the fired hooks in m/t
     */
    abstract val speed: Double

    /**
     * The speed the player is pulled toward the target point in m/t
     */
    abstract val pullStrength: Double

    /**
     * The distance from the impact point to where the chain should attach
     */
    abstract val hookLength: Double

    /**
     * The distance from the impact point to where the chain should attach
     */
    abstract val jumpBoost: Double

    abstract val cooldown: Int

    /**
     * Create a new controller for the specified player
     */
    abstract fun createController(player: EntityPlayer): HookController<*>

    @get:SideOnly(Side.CLIENT)
    @delegate:SideOnly(Side.CLIENT)
    @delegate:Transient
    val renderer: HookRenderer<*, *> by lazy { initRenderer() }

    @SideOnly(Side.CLIENT)
    protected abstract fun initRenderer(): HookRenderer<*, *>

    companion object {
        fun register(clazz: Class<out HookType>, name: String) {
            types[name] = clazz
        }
        private val types = HashBiMap.create<String, Class<out HookType>>()
        private val names = types.inverse()
    }

    object Serializer: JsonSerializer<HookType>, JsonDeserializer<HookType> {
        override fun serialize(src: HookType, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val name = names[src.javaClass]
                ?: throw IllegalArgumentException("Unrecognized object type ${src.javaClass.simpleName}. " +
                    "Recognized types: [${names.keys.map { it.simpleName }.sorted().joinToString { ", " }}]")
            val elem = context.serialize(src).asJsonObject
            elem.addProperty("type", name)
            return elem
        }

        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): HookType {
            val jsonObject = json.asJsonObject
            val name = jsonObject["type"].asString
            val klass = types[name]
                ?: throw JsonParseException("Unknown type discriminator `$name`. " +
                    "Known types are [${types.keys.sorted().joinToString(", ")}]")

            @Suppress("UNCHECKED_CAST")
            return context.deserialize<Any>(jsonObject, klass) as HookType
        }
    }
}