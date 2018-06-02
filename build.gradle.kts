import net.minecraftforge.gradle.user.TaskSourceCopy
import net.minecraftforge.gradle.user.patcherUser.forge.ForgeExtension
import net.minecraftforge.gradle.user.patcherUser.forge.ForgePlugin
import org.gradle.api.internal.HasConvention
import org.gradle.internal.impldep.bsh.commands.dir
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

inline fun <reified T: Task> task(name: String, vararg args: Pair<String, Any>): Task
        = task<T>(name, *args) { }

inline fun <reified T: Task> task(name: String, vararg args: Pair<String, Any>, noinline cfg: T.() -> Unit): Task
        = if (!T::class.isAbstract) task(name, T::class, *args, cfg = cfg) else task(mapOf(*args), name, closureOf(cfg))


fun <T: Task> task(name: String, type: KClass<T>, vararg args: Pair<String, Any>): Task
        = task(name, type, *args) { }

fun <T: Task> task(name: String, type: KClass<T>, vararg args: Pair<String, Any>, cfg: T.() -> Unit): Task
        = task(mapOf(*args, "type" to type.java), name, closureOf(cfg))


inline fun <reified T : Task> TaskContainer.withType(name: String): T
        = withType(name) {}

inline fun <reified T : Task> TaskContainer.withType(name: String, cfg: T.() -> Unit): T
        = withType(T::class.java).getByName(name).apply(cfg)


inline fun <reified T : Task> TaskContainer.withTypeIfPresent(name: String): T?
        = withTypeIfPresent(name) {}

inline fun <reified T : Task> TaskContainer.withTypeIfPresent(name: String, cfg: T.() -> Unit): T?
        = withType(T::class.java).findByName(name)?.apply(cfg)


buildscript {
    repositories {
        jcenter()
        maven(url = "https://files.minecraftforge.net/maven")
        maven(url = "https://plugins.gradle.org/m2/")
    }

    val kotlinVersion = extra["kotlin_version"].toString()

    dependencies {
        classpath("net.minecraftforge.gradle:ForgeGradle:2.3-SNAPSHOT")
        classpath(kotlin("gradle-plugin", version = kotlinVersion))
    }
}

plugins {
    idea
    java
    maven
}

apply<KotlinPlatformJvmPlugin>()
apply<ForgePlugin>()

val modVersion = extra["mod_version"].toString()
val modBuild = extra["build_number"].toString()
val modName = extra["mod_name"].toString().toLowerCase()
val corePlugin = project.findProperty("core_plugin")?.toString()
val corePluginArgString = corePlugin?.let { "-Dfml.coreMods.load=$it" }
val modGroup = extra["mod_group"].toString()

val mcVersion = extra["mc_version"].toString()
val forgeProjectVersion = extra["forge_version"].toString()
val mcpProjectVersion = extra["mc_mappings"].toString()
val kotlinVersion = extra["kotlin_version"].toString()
val liblibVersion = extra["liblib_version"].toString()

version = "$modVersion.$modBuild"
group = modGroup
setProperty("archivesBaseName", modName)



java.sourceCompatibility = JavaVersion.toVersion(1.8)
java.targetCompatibility = JavaVersion.toVersion(1.8)

extensions.getByType(ForgeExtension::class.java).apply {
    version = "$mcVersion-$forgeProjectVersion"
    mappings = mcpProjectVersion
    runDir = "run"

	corePluginArgString?.let {
		clientJvmArgs = listOf(it)
		serverJvmArgs = listOf(it)
	}

    replaceIn("HookedMod.kt")
    replaceIn("mcmod.info")

    replace("GRADLE:VERSION", modVersion)
    replace("GRADLE:BUILD", modBuild)
}

for (set in java.sourceSets) {
    if (set == null || set.name == "test") continue
    val taskName = "source${set.name.capitalize()}Kotlin"
    val dir = File(project.buildDir, "sources/${set.name}/kotlin")

    if (tasks.findByName(taskName) == null)
        task<TaskSourceCopy>(taskName) {
            setSource(set.kotlin)
            setOutput(dir)
        }

    val compileTask = tasks.withTypeIfPresent<KotlinCompile>(set.getCompileTaskName("kotlin"))
    if (compileTask != null) {
        compileTask.dependsOn(taskName)
        val dirPath = dir.toPath()
        tasks.withType<KotlinCompile>("compileKotlin") {
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

        inheritOutputDirs = true
        testSourceDirs = setOf()
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
    from(java.sourceSets["main"].output)
    classifier = "deobf"
}

tasks.withType<Jar> {
    from(java.sourceSets["main"].output)

    archiveName = "$modName-$version.jar"
    dependsOn(configurations.getByName("compile"))

	corePlugin?.let {
		manifest {
			attributes(mapOf(
					"FMLCorePluginContainsFMLMod" to "true",
					"FMLCorePlugin" to it))
		}
	}
}

task<Task>("setup") {
    dependsOn("setupDecompWorkspace")
    dependsOn("ideaModule")
    dependsOn("genIntellijRuns")
}

artifacts {
    add("archives", tasks.withType<Jar>("sourceJar"))
    add("archives", tasks.withType<Jar>("deobfJar"))
}

repositories {
    mavenCentral()
    maven(url = "http://maven.bluexin.be/repository/snapshots/")
    maven(url = "http://maven.shadowfacts.net/")
}

dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    compile("net.shadowfacts:Forgelin:1.6.0")
	compile("com.teamwizardry.librarianlib:librarianlib-$mcVersion:$liblibVersion-SNAPSHOT:deobf")
}

tasks.withType<ProcessResources>("processResources") {
    val props = mapOf("version" to version,
            "forge_version" to forgeProjectVersion,
            "mc_version" to mcVersion,
            "mod_id" to modName)

    inputs.properties(props)

    from(java.sourceSets["main"].resources.srcDirs) {
        include("mcmod.info")
        expand(props)
    }

    from(java.sourceSets["main"].resources.srcDirs) {
        exclude("mcmod.info")
    }

    rename("(.+_at.cfg)", "META-INF/$1")
}

