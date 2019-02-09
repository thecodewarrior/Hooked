package games.thecodewarrior.hooked.common.config

import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.TextureUtil
import net.minecraft.client.resources.IResourcePack
import net.minecraft.client.resources.ResourcePackFileNotFoundException
import net.minecraft.client.resources.data.IMetadataSection
import net.minecraft.client.resources.data.MetadataSerializer
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.relauncher.ReflectionHelper
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.awt.image.BufferedImage
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.StandardCharsets

fun <T: IResourcePack> T.inject(): T {
    val defaultResourcePacks = ReflectionHelper.getPrivateValue<MutableList<IResourcePack>, Minecraft>(
        Minecraft::class.java, Minecraft.getMinecraft(), "aD", "field_110449_ao", "defaultResourcePacks"
    )
    defaultResourcePacks.add(this)
    return this
}

class ConfigResourcePack(
    val name: String, val domain: String,
    val image: BufferedImage = BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB)
): HookedAbstractResourcePack() {
    var directory: File? = null

    val overrides = mutableMapOf<String, ByteArray>()

    fun override(name: String, data: ByteArray): ConfigResourcePack {
        overrides[name] = data
        return this
    }

    fun override(name: String, data: String): ConfigResourcePack {
        overrides[name] = data.toByteArray(Charsets.UTF_8)
        return this
    }

    fun addDir(name: String): ConfigResourcePack {
        val file = getFile(name.removeSuffix("/"))
        if (file?.exists() == false && name.removeSuffix("/") !in overrides) {
            file.mkdirs()
        }
        return this
    }

    fun addDefault(name: String, data: InputStream): ConfigResourcePack {
        data.use { data ->
            val file = getFile(name)
            if (file?.exists() == false && name !in overrides) {
                file.parentFile.mkdirs()
                FileUtils.copyInputStreamToFile(data, file)
            }
            return this
        }
    }

    override fun getResourceDomains(): Set<String> = setOf(domain)

    override fun getPackName(): String = name

    override fun getPackImage(): BufferedImage = image

    init {
        override("pack.mcmeta", """
            {
                "pack": {
                    "description": "Config pack: $name",
                    "pack_format": 2
                }
            }
        """.trimIndent())
    }

    @Throws(IOException::class)
    override fun getInputStreamByName(name: String): InputStream {
        overrides[name]?.also {
            return ByteArrayInputStream(it)
        }
        val file = this.getFile(name)

        if (file?.isFile == true) {
            return BufferedInputStream(FileInputStream(file))
        } else {
            throw ResourcePackFileNotFoundException(this.directory ?: File("/packDirectoryIsNull"), name)
        }
    }

    override fun hasResourceName(name: String): Boolean {
        return name in overrides || this.getFile(name)?.isFile == true
    }

    private fun getFile(name: String): File? {
        val directory = this.directory ?: return null

        try {
            return File(directory, stripPrefix(name) ?: return null)
        } catch (var3: IOException) {
        }

        return null
    }

    private fun stripPrefix(name: String): String? {
        if(!name.startsWith("assets/$domain")) return null
        return name.removePrefix("assets/$domain")
    }
}

abstract class HookedAbstractResourcePack: IResourcePack {
    private fun locationToName(location: ResourceLocation): String {
        return String.format("%s/%s/%s", "assets", location.namespace, location.path)
    }

    @Throws(IOException::class)
    override fun getInputStream(location: ResourceLocation): InputStream {
        return this.getInputStreamByName(locationToName(location))
    }

    override fun resourceExists(location: ResourceLocation): Boolean {
        return this.hasResourceName(locationToName(location))
    }

    @Throws(IOException::class)
    protected abstract fun getInputStreamByName(name: String): InputStream

    protected abstract fun hasResourceName(name: String): Boolean

    @Throws(IOException::class)
    override fun <T: IMetadataSection> getPackMetadata(metadataSerializer: MetadataSerializer, metadataSectionName: String): T {
        return readMetadata<IMetadataSection>(metadataSerializer, this.getInputStreamByName("pack.mcmeta"), metadataSectionName) as T
    }

    fun <T: IMetadataSection> readMetadata(metadataSerializer: MetadataSerializer, p_110596_1_: InputStream, sectionName: String): T {
        var jsonobject: JsonObject? = null
        var bufferedreader: BufferedReader? = null

        try {
            bufferedreader = BufferedReader(InputStreamReader(p_110596_1_, StandardCharsets.UTF_8))
            jsonobject = JsonParser().parse(bufferedreader).asJsonObject
        } catch (runtimeexception: RuntimeException) {
            throw JsonParseException(runtimeexception)
        } finally {
            IOUtils.closeQuietly(bufferedreader as Reader?)
        }

        return metadataSerializer.parseMetadataSection<IMetadataSection>(sectionName, jsonobject!!) as T
    }

    @Throws(IOException::class)
    override fun getPackImage(): BufferedImage {
        return TextureUtil.readBufferedImage(this.getInputStreamByName("pack.png"))
    }
}
