plugins {
    id("org.spongepowered.gradle.vanilla")
    id("implementation-structure")
    alias(libs.plugins.blossom)
}

evaluationDependsOn(":SpongeVanilla")
evaluationDependsOn(":sponge-gametest")

val commonProject = rootProject
val vanillaProject = project(":SpongeVanilla")

val apiVersion: String by project
val minecraftVersion: String by project
val recommendedVersion: String by project
val projectUrl: String by project

description = "Runs Minecraft gametests on SpongeVanilla: the game test server's Sponge lifecycle, and /test in a world"
version = spongeImpl.generatePlatformBuildVersionString(apiVersion, minecraftVersion, recommendedVersion)

val commonMain = commonProject.sourceSets.named("main")
val commonMixins = commonProject.sourceSets.named("mixins")
val commonAccessors = commonProject.sourceSets.named("accessors")
val commonAppLaunch = commonProject.sourceSets.named("applaunch")
val commonAppLaunchConf = commonProject.sourceSets.named("applaunchConfig")
val vanillaMain = vanillaProject.sourceSets.named("main")
val vanillaMixins = vanillaProject.sourceSets.named("mixins")
val vanillaAccessors = vanillaProject.sourceSets.named("accessors")
val vanillaAppLaunch = vanillaProject.sourceSets.named("applaunch")

sourceSets.main {
    spongeImpl.addDependencyToImplementation(commonAppLaunchConf.get(), this)
    spongeImpl.addDependencyToImplementation(commonAppLaunch.get(), this)
    spongeImpl.addDependencyToImplementation(commonAccessors.get(), this)
    spongeImpl.addDependencyToImplementation(commonMixins.get(), this)
    spongeImpl.addDependencyToImplementation(commonMain.get(), this)
    spongeImpl.addDependencyToImplementation(vanillaAppLaunch.get(), this)
    spongeImpl.addDependencyToImplementation(vanillaAccessors.get(), this)
    spongeImpl.addDependencyToImplementation(vanillaMixins.get(), this)
    spongeImpl.addDependencyToImplementation(vanillaMain.get(), this)
    // Compile only: the game provides all of this, none of it belongs in the published metadata. Copied rather than
    // extended, since a configuration may only extend one of its own project
    configurations.named(compileOnlyConfigurationName) {
        dependencies.addAllLater(provider { vanillaProject.configurations.getByName("gameLayer").allDependencies })
    }
}

minecraft {
    accessWideners(commonMain.get().resources.filter { it.name.endsWith(".accesswidener") })
    accessWideners(vanillaMain.get().resources.filter { it.name.endsWith(".accesswidener") })
}

sourceSets.main {
    blossom.resources {
        property("version", version.toString())
        property("gametestVersion", project(":sponge-gametest").version.toString())
    }
}

dependencies {
    // api: a plugin compiles its tests against sponge-gametest through this one dependency
    api(project(":sponge-gametest"))
}

tasks.jar {
    manifest {
        attributes(
            "Automatic-Module-Name" to "spongevanilla.gametest",
            "MixinConfigs" to "mixins.spongevanilla.gametest.json"
        )
    }
}

publishing {
    publications {
        register("sponge", MavenPublication::class) {
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifactId = "spongevanilla-gametest"

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
