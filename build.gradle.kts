import net.minecraftforge.gradle.user.TaskSourceCopy
import net.minecraftforge.gradle.user.patcherUser.forge.ForgeExtension
import net.minecraftforge.gradle.user.patcherUser.forge.ForgePlugin
import org.gradle.api.internal.HasConvention
import org.gradle.plugins.ide.idea.model.IdeaLanguageLevel
import org.jetbrains.kotlin.contracts.model.structure.UNKNOWN_COMPUTATION.type
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformJvmPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlin.reflect.KClass

val SourceSet.kotlin: SourceDirectorySet
    get() =
        (this as HasConvention)
                .convention
                .getPlugin(KotlinSourceSet::class.java)
                .kotlin

fun SourceSet.kotlin(action: SourceDirectorySet.() -> Unit) =
        kotlin.action()

inline fun <reified T: Task> task(name: String, vararg args: Pair<String, Any>, noinline cfg: T.() -> Unit = { })
        = if (!T::class.isAbstract) task(name, T::class, *args, cfg = cfg) else task(mapOf(*args), name, closureOf(cfg))

fun <T: Task> task(name: String, type: KClass<T>, vararg args: Pair<String, Any>, cfg: T.() -> Unit = { })
        = task(mapOf(*args, "type" to type.java), name, closureOf(cfg))

inline fun <reified T : Task> TaskContainer.withType(name: String, cfg: T.() -> Unit = { })
        = withType(T::class.java).getByName(name).apply(cfg)

inline fun <reified T : Task> TaskContainer.withTypeIfPresent(name: String, cfg: T.() -> Unit = { })
        = withType(T::class.java).findByName(name)?.apply(cfg)


buildscript {
    repositories {
        jcenter()
        maven(url = "https://files.minecraftforge.net/maven/")
        maven(url = "https://plugins.gradle.org/m2/" )
    }

    val kotlinVersion = extra["kotlin_version"].toString()

    dependencies {
        classpath(kotlin("gradle-plugin", version = kotlinVersion))
        classpath("net.minecraftforge.gradle:ForgeGradle:2.3-SNAPSHOT")
    }
}

plugins {
    idea
    java
}

apply<KotlinPlatformJvmPlugin>()
apply<ForgePlugin>()

val modVersion = extra["mod_version"].toString()
val modName = extra["mod_name"].toString().toLowerCase()
val modDescription = project.findProperty("mod_description")?.toString() ?: ""
val capitalizedName = modName.capitalize()
val modFancyName = project.findProperty("mod_fancy_name") ?: capitalizedName
val modAuthor = extra["mod_author"].toString().toLowerCase()
val corePlugin = project.findProperty("core_plugin") == "true"
val corePluginClass = "$modAuthor.$modName.asm.${capitalizedName}ClassTransformer"
val corePluginArgs = mutableListOf<String>()
if (corePlugin)
    corePluginArgs.add(corePluginClass)
if (project.findProperty("other_core_plugins")?.toString()?.isNotEmpty() == true)
    corePluginArgs.add(extra["other_core_plugins"].toString())

val corePluginArgString = "-Dfml.coreMods.load=" + corePluginArgs.joinToString(",")

val mcVersion = extra["mc_version"].toString()
val forgeProjectVersion = extra["forge_version"].toString()
val mcpProjectVersion = extra["mcp_version"].toString()
val jeiVersion = extra["jei_version"].toString()
val kotlinVersion = extra["kotlin_version"].toString()
val liblibVersion = extra["liblib_version"].toString()

version = modVersion
group = "$modAuthor.$modName"
setProperty("archivesBaseName", modName)



java.sourceCompatibility = JavaVersion.toVersion(1.8)
java.targetCompatibility = JavaVersion.toVersion(1.8)

extensions.getByType(ForgeExtension::class.java).apply {
    version = "$mcVersion-$forgeProjectVersion"
    mappings = mcpProjectVersion
    runDir = "run"

    if (corePlugin) {
        clientJvmArgs = listOf(corePluginArgString)
        serverJvmArgs = listOf(corePluginArgString)
    }

    replaceIn("$capitalizedName.java")
    replaceIn("mcmod.info")

    replace("%VERSION%", modVersion)
    replace("required-after:librarianlib", "required-after:librarianlib@[4.12,)")
}

for (set in sourceSets) {
    if (set != null && set.name == "test") continue
    val taskName = "source${set.name.capitalize()}Kotlin"
    val dir = File(project.buildDir, "sources/${set.name}/kotlin")

    if (tasks.findByName(taskName) == null)
        task<TaskSourceCopy>(taskName) {
            setSource(set.kotlin)
            setOutput(dir)
        }

    val compileTask = tasks.withTypeIfPresent<KotlinCompile>(set.getCompileTaskName("kotlin"))
    if (compileTask != null) {
        compileTask.source = fileTree(dir)
        compileTask.dependsOn(taskName)
        val dirPath = dir.toPath()
        tasks.withType<KotlinCompile> {
            include { it.file.toPath().startsWith(dirPath) }
        }
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        javaParameters = true
        jvmTarget = "1.8"
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}

val ideaTask: Task = tasks.getByName("idea")
val genTask: Task = tasks.getByName("genIntellijRuns") {
    shouldRunAfter(ideaTask)
}

idea {
    module {
        excludeDirs.addAll(listOf(file("run"), file("out"), file("gradle"), file(".idea")))
    }

    if (project != null) project {
        jdkName = "1.8"
        languageLevel = IdeaLanguageLevel("1.8")
    }
}

task("sourceJar", Jar::class, "overwrite" to true, "dependsOn" to "sourceMainJava") {
    from("src/main/java")
    classifier = "sources"
}

task("deobfJar", Jar::class) {
    from(sourceSets["main"].output)
    classifier = "deobf"
}

if (corePlugin) {
    tasks.withType<Jar> {
        manifest {
            attributes(mapOf(
                    "FMLCorePluginContainsFMLMod" to "true",
                    "FMLCorePlugin" to corePluginClass))
        }
    }
}

fun copyFileTask(name: String, from: String, to: String, target: String) {
    task(name, Copy::class) {
        onlyIf { !file("src/main/$to/$target").exists() }

        from("gradle/assets/$from")
        expand(mapOf("mod_id" to modName,
                "mod_class" to capitalizedName,
                "mod_name" to modFancyName,
                "mod_group" to "$modAuthor.$modName"))

        into("src/main/$to/")
        rename { target }
    }
}

copyFileTask("copyBaseModFile", "basic_class.kt", "java/$modAuthor/$modName", "$capitalizedName.kt")
copyFileTask("copyModInfoFile", "mcmod.info", "resources", "mcmod.info")
copyFileTask("copyResourcesFile", "pack.mcmeta", "resources", "pack.mcmeta")

task("copyAsm", Copy::class) {
    onlyIf { corePlugin && !file("src/main/java/$modAuthor/$modName/asm").exists() }
    from("gradle/assets/asm/")
    expand(mapOf("mod_id" to modName,
            "mod_class" to capitalizedName,
            "mod_name" to modFancyName,
            "mod_group" to "$modAuthor.$modName",
            "mod_group_uri" to "$modAuthor/$modName"))
    into("src/main/java/$modAuthor/$modName/asm/")
    rename { capitalizedName + it }
}

task<Task>("copyModFiles") {
    dependsOn("copyBaseModFile")
    dependsOn("copyModInfoFile")
    dependsOn("copyResourcesFile")
    dependsOn("copyAsm")
}

tasks.getByName("setupDecompWorkspace") {
    dependsOn("copyModFiles")
}

task<Task>("setup") {
    dependsOn("setupDecompWorkspace")
    dependsOn("ideaModule")
    dependsOn("genIntellijRuns")
}

repositories {
    mavenCentral()
    maven(url = "http://maven.bluexin.be/repository/snapshots/")
    maven(url = "http://dvs1.progwml6.com/files/maven")
    maven(url = "http://maven.shadowfacts.net/")
}

dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    compile("net.shadowfacts:Forgelin:1.6.0")
	compile("com.teamwizardry.librarianlib:librarianlib-$mcVersion:$liblibVersion-SNAPSHOT")

    compile("mezz.jei:jei_$mcVersion:$jeiVersion:api")
    runtime("mezz.jei:jei_$mcVersion:$jeiVersion")
}

tasks.withType<ProcessResources> {
    val props = mapOf("version" to modVersion,
            "forge_version" to forgeProjectVersion,
            "mc_version" to mcVersion,
            "mod_id" to modName,
            "mod_name" to modFancyName,
            "description" to modDescription,
            "author" to modAuthor)

    inputs.properties(props)

    from(sourceSets["main"].resources.srcDirs) {
        include("mcmod.info", "pack.mcmeta")
        expand(props)
    }

    from(sourceSets["main"].resources.srcDirs) {
        exclude("mcmod.info", "pack.mcmeta")
    }

    rename("(.+_at.cfg)", "META-INF/$1")
}
