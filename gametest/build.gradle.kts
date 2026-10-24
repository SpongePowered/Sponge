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

tasks.jar {
    manifest {
        attributes(
            "Automatic-Module-Name" to "sponge.gametest",
            "MixinConfigs" to "mixins.sponge.gametest.json"
        )
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
