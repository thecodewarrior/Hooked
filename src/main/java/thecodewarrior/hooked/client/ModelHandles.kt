package thecodewarrior.hooked.client

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model.IBakedModel
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.resources.IReloadableResourceManager
import net.minecraft.crash.CrashReport
import net.minecraft.util.ReportedException
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.client.model.ModelLoaderRegistry
import thecodewarrior.hooked.HookedMod
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Created by TheCodeWarrior
 */
class StateHandle(val loc: ModelResourceLocation) {

    fun get(): IBakedModel {
        return getModel(this.loc)
    }

    fun load(): StateHandle {
        getModel(this.loc)
        return this
    }

    fun reload(): StateHandle {
        loadModel(this.loc)
        return this
    }

    val isMissing: Boolean
        get() {
            getModel(this.loc)
            return missingModels.contains(this.loc)
        }

    companion object {

        protected var cache: MutableMap<ModelResourceLocation, IBakedModel> = mutableMapOf()
        protected var missingModels: MutableSet<ModelResourceLocation> = mutableSetOf()
        protected var errors: MutableMap<ResourceLocation, MutableList<String>> = mutableMapOf()

        // ========================================================= STATIC METHODS

        //region creators

        fun of(model: String, variant: String): StateHandle {
            return of(ResourceLocation(model), variant)
        }

        fun of(model: ResourceLocation, variant: String): StateHandle {
            return of(ModelResourceLocation(model, variant))
        }

        fun ofLazy(model: String, variant: String): StateHandle {
            return ofLazy(ResourceLocation(model), variant)
        }

        fun ofLazy(model: ResourceLocation, variant: String): StateHandle {
            return ofLazy(ModelResourceLocation(model, variant))
        }

        fun ofLazy(loc: ModelResourceLocation): StateHandle {
            return StateHandle(loc)
        }

        fun of(loc: ModelResourceLocation): StateHandle {
            return StateHandle(loc).reload()
        }

        //endregion

        private fun getModel(loc: ModelResourceLocation): IBakedModel {
            var model: IBakedModel? = cache[loc]
            if (model != null)
                return model

            loadModel(loc)
            model = cache[loc]
            if (model == null)
                throw IllegalStateException("Cache contained null even after loading for model " + loc)
            return model
        }

        private fun loadModel(loc: ModelResourceLocation) {
            try {
                val mod = if(HookedMod.DEV_ENVIRONMENT) {
                    val baseLoc = ResourceLocation(loc.path, loc.namespace)
                    val m = try {
                        ModelLoaderRegistry.getModel(loc)
                    } catch (e: Exception) {
                        errors.getOrPut(baseLoc) { mutableListOf() }

                        val sw = StringWriter()
                        e.printStackTrace(PrintWriter(sw))
                        errors[baseLoc]?.add("`#${loc.variant}`\n${sw.toString()}")

                        ModelLoaderRegistry.getMissingModel()
                    }

                    m
                } else {
                    ModelLoaderRegistry.getModelOrMissing(loc)
                }

                if (mod === ModelLoaderRegistry.getMissingModel()) {
                    missingModels.add(loc)
                }
                val model = mod.bake(mod.defaultState, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter())
                cache.put(loc, model)
            } catch (e: Exception) {
                throw ReportedException(CrashReport("Error loading custom model " + loc, e))
            }

        }

        fun init() {
            val rm = Minecraft.getMinecraft().resourceManager
            if (rm is IReloadableResourceManager) {
                rm.registerReloadListener {
                    cache.clear()
                    missingModels.clear()
                    errors.clear()
                }
            }
        }
    }
}

class ModelHandle(val loc: ResourceLocation) {

    fun get(): IBakedModel {
        return getModel(this.loc)
    }

    fun getResources(): Collection<ResourceLocation> {
        get()
        return resourceCache.get(this.loc) ?: listOf()
    }

    fun load(): ModelHandle {
        getModel(this.loc)
        return this
    }

    fun purge() {
        cache.remove(this.loc)
        resourceCache.remove(this.loc)
        missingModels.remove(this.loc)
        errors.remove(this.loc)
    }

    fun reload(): ModelHandle {
        purge()
        loadModel(this.loc)
        return this
    }

    val isMissing: Boolean
        get() {
            getModel(this.loc)
            return missingModels.contains(this.loc)
        }

    companion object {

        protected var cache: MutableMap<ResourceLocation, IBakedModel> = mutableMapOf()
        protected var resourceCache: MutableMap<ResourceLocation, Collection<ResourceLocation>> = mutableMapOf()
        protected var missingModels: MutableSet<ResourceLocation> = mutableSetOf()
        protected var errors: MutableMap<ResourceLocation, MutableList<String>> = mutableMapOf()

        // ========================================================= STATIC METHODS

        //region creators

        fun of(model: String): ModelHandle {
            return of(ResourceLocation(model))
        }

        fun ofLazy(model: String): ModelHandle {
            return ofLazy(ResourceLocation(model))
        }

        fun ofLazy(loc: ResourceLocation): ModelHandle {
            return ModelHandle(loc)
        }

        fun of(loc: ResourceLocation): ModelHandle {
            return ModelHandle(loc).reload()
        }

        //endregion

        private fun getModel(loc: ResourceLocation): IBakedModel {
            var model: IBakedModel? = cache[loc]
            if (model != null)
                return model

            loadModel(loc)
            model = cache[loc]
            if (model == null)
                throw IllegalStateException("Cache contained null even after loading for model " + loc)
            return model
        }

        private fun loadModel(loc: ResourceLocation) {
            try {
                val mod = if(HookedMod.DEV_ENVIRONMENT) {
                    val m = try {
                        ModelLoaderRegistry.getModel(loc)
                    } catch (e: Exception) {
                        errors.getOrPut(loc) { mutableListOf() }

                        val sw = StringWriter()
                        e.printStackTrace(PrintWriter(sw))
                        errors[loc]?.add(sw.toString())

                        ModelLoaderRegistry.getMissingModel()
                    }

                    m
                } else {
                    ModelLoaderRegistry.getModelOrMissing(loc)
                }

                if (mod === ModelLoaderRegistry.getMissingModel()) {
                    missingModels.add(loc)
                }
                val model = mod.bake(mod.defaultState, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter())
                cache.put(loc, model)
                resourceCache.put(loc, mod.textures)
            } catch (e: Exception) {
                throw ReportedException(CrashReport("Error loading custom model " + loc, e))
            }

        }

        fun init() {
            val rm = Minecraft.getMinecraft().resourceManager
            if (rm is IReloadableResourceManager) {
                rm.registerReloadListener {
                    cache.clear()
                    missingModels.clear()
                    errors.clear()
                }
            }
        }
    }
}
