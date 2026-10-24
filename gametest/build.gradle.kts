import org.gradle.internal.DefaultTaskExecutionRequest
import org.spongepowered.gradle.impl.IdeHelper

plugins {
    id("org.spongepowered.gradle.vanilla")
    id("implementation-structure")
    alias(libs.plugins.blossom)
}

val commonProject = parent!!

val apiVersion: String by project
val minecraftVersion: String by project
val recommendedVersion: String by project
val projectUrl: String by project

description = "Minecraft gametests with Sponge loaded: the test declaration, its registration, and the JUnit engine that runs it"
version = spongeImpl.generateImplementationVersionString(apiVersion, minecraftVersion, recommendedVersion)

val commonMain = commonProject.sourceSets.named("main")
val commonAppLaunch = commonProject.sourceSets.named("applaunch")

sourceSets.main {
    spongeImpl.addDependencyToImplementation(commonAppLaunch.get(), this)
    spongeImpl.addDependencyToImplementation(commonMain.get(), this)
    // Compile only: the game provides all of this, none of it belongs in the published metadata. Copied rather than
    // extended, since a configuration may only extend one of its own project
    configurations.named(compileOnlyConfigurationName) {
        dependencies.addAllLater(provider { commonProject.configurations.getByName("gameLayer").allDependencies })
    }
}

minecraft {
    accessWideners(commonMain.get().resources.filter { it.name.endsWith(".accesswidener") })
}

sourceSets.main {
    blossom.resources {
        property("apiVersion", apiVersion)
        property("minecraftVersion", minecraftVersion)
        property("version", version.toString())
    }
}

dependencies {
    api("org.spongepowered:spongeapi:$apiVersion")
    // The JUnit engine and @Testable: a plugin's tests compile against them through this module, and its test JVM needs
    // the launcher at runtime to find Sponge's interceptor. The headless game test server loads no dependencies anyway.
    compileOnlyApi(platform(apiLibs.junit.bom))
    compileOnlyApi(apiLibs.junit.launcher)
    runtimeOnly(platform(apiLibs.junit.bom))
    runtimeOnly(apiLibs.junit.launcher)
}

val manifestAttributes = mapOf(
    "Automatic-Module-Name" to "sponge.gametest",
    "MixinConfigs" to "mixins.sponge.gametest.json"
)

// The same manifest as a folder of the main output, so a dev classpath of folders carries MixinConfigs too: on Neo
// that is how the gametest mixins load, from the jar or, in IntelliJ, from this folder beside out/
val devManifest by tasks.registering {
    val output = layout.buildDirectory.dir("generated/devManifest")
    inputs.property("attributes", manifestAttributes)
    outputs.dir(output)
    doLast {
        val manifest = output.get().file("META-INF/MANIFEST.MF").asFile
        manifest.parentFile.mkdirs()
        manifest.writeText("Manifest-Version: 1.0\n" + manifestAttributes.entries.joinToString("") { "${it.key}: ${it.value}\n" })
    }
}
sourceSets.main {
    output.dir(mapOf("builtBy" to devManifest), devManifest.map { it.outputs.files.singleFile })
}

tasks.jar {
    manifest.attributes(manifestAttributes)
    exclude("META-INF/MANIFEST.MF") // the folder's copy; the jar writes its own
}

// IntelliJ builds without Gradle, so the folder is made when the project syncs
if (IdeHelper.isIdeaSync()) {
    afterEvaluate {
        gradle.startParameter.taskRequests.add(DefaultTaskExecutionRequest(listOf(":sponge-gametest:devManifest")))
    }
}

publishing {
    publications {
        register("sponge", MavenPublication::class) {
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifactId = "sponge-gametest"

            pom {
                this.name.set(project.name)
                this.description.set(project.description)
                this.url.set(projectUrl)

                licenses {
                    license {
                        this.name.set("MIT")
                        this.url.set("https://opensource.org/licenses/MIT")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/SpongePowered/Sponge.git")
                    developerConnection.set("scm:git:ssh://github.com/SpongePowered/Sponge.git")
                    this.url.set(projectUrl)
                }
            }
        }
    }
}
