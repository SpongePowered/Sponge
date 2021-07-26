import org.spongepowered.gradle.vanilla.repository.MinecraftRepositoryExtension

pluginManagement {
    repositories {
        mavenLocal()
        maven("https://repo.spongepowered.org/repository/maven-public/") {
            name = "sponge"
        }
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        gradlePluginPortal()
    }

    plugins {
        // Default plugin versions
        id("org.spongepowered.gradle.vanilla") version "0.2"
        id("org.cadixdev.licenser") version "0.6.0"
        id("com.github.johnrengelman.shadow") version "7.0.0"
        id("org.spongepowered.gradle.sponge.dev") version "1.0.3"
        id("implementation-structure")
        id("org.jetbrains.gradle.plugin.idea-ext") version "1.0"
        id("com.github.ben-manes.versions") version "0.39.0"
        id("fabric-loom") version "0.9-SNAPSHOT"
        id("com.github.logicfan.gradle.shadow.json-transformer") version "1.0.1"
    }
}

plugins {
    id("org.spongepowered.gradle.vanilla")
    id("fabric-loom")
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven("https://repo.spongepowered.org/repository/maven-public/") {
            name = "sponge"
        }
        mavenLocal()
    }
}

rootProject.name = "Sponge"

extensions.configure(MinecraftRepositoryExtension::class) {
    injectRepositories(false)
}

// Set up project structure

includeBuild("build-logic")
includeBuild("SpongeAPI") {
    dependencySubstitution {
        substitute(module("org.spongepowered:spongeapi")).with(project(":"))
    }
}
include(":SpongeVanilla")
project(":SpongeVanilla").projectDir = file("vanilla")
include("generator")

include(":SpongeFabric")
project(":SpongeFabric").projectDir = file("fabric")

val testPlugins = file("testplugins.settings.gradle.kts")
if (testPlugins.exists()) {
    apply(from = testPlugins)
} else {
    testPlugins.writeText("// Uncomment to enable client module for debugging\n//include(\":testplugins\")\n")
}
val testPluginsEnabledInCi: String = startParameter.projectProperties.getOrDefault("enableTestPlugins", "false")
if (testPluginsEnabledInCi.toBoolean()) {
    include(":testplugins")
}

val spongeForge = file("spongeforge.settings.gradle.kts")
if (spongeForge.exists()) {
    apply(from = spongeForge)
} else {
    spongeForge.writeText("// Uncomment to enable SpongeForge module.\n" +
            "// By default only Sponge and SpongeVanilla are made available\n" +
            "//include(\":SpongeForge\")\n" +
            "//project(\":SpongeForge\").projectDir = file(\"forge\")\n")
}
val spongeForgeEnabledInCi: String = startParameter.projectProperties.getOrDefault("enableSpongeForge", "false")
if (spongeForgeEnabledInCi.toBoolean()) {
    include(":SpongeForge")
    project(":SpongeForge").projectDir = file("forge")
}

// Include properties from API project (with api prefix)
val apiProps = file("SpongeAPI/gradle.properties")
if (apiProps.exists()) {
    val props = java.util.Properties()
    apiProps.bufferedReader(Charsets.UTF_8).use {
        props.load(it)
    }
    val extraProperties = mutableMapOf<String, String>()
    props.stringPropertyNames().forEach { key ->
        val value = props.getProperty(key)
        if (value != null) {
            if (key.startsWith("api")) {
                extraProperties[key] = value
            } else {
                extraProperties["api${key.capitalize()}"] = value
            }
        }
    }

    gradle.beforeProject {
        val extraExt = project.extensions.extraProperties
        extraProperties.forEach { (k, v) -> extraExt.set(k, v) }
    }
}
