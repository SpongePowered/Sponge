import org.spongepowered.gradle.vanilla.repository.MinecraftRepositoryExtension

pluginManagement {
    repositories {
        maven("https://repo.spongepowered.org/repository/maven-public/") {
            name = "sponge"
        }
        maven("https://maven.architectury.dev/")
    }

    plugins {
        id("org.spongepowered.gradle.vanilla") version "0.2.1-SNAPSHOT"
        id("implementation-structure")
    }
}

plugins {
    id("org.spongepowered.gradle.vanilla")
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT) // needed for forge-loom, unfortunately
    repositories {
        maven("https://repo.spongepowered.org/repository/maven-public/") {
            name = "sponge"
        }
    }
    versionCatalogs {
        register("apiLibs") {
            from(files("SpongeAPI/gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "Sponge"

extensions.configure(MinecraftRepositoryExtension::class) {
    injectRepositories(false)
}

// Set up project structure

if (!file("SpongeAPI/gradle.properties").exists()) {
    throw InvalidUserDataException("""
        The SpongeAPI submodule required to build does not appear to be set up.

        To correct this, run
            git submodule update --init --recursive
        from the project's checkout directory.
    """.trimIndent())
}

includeBuild("build-logic")
includeBuild("SpongeAPI") {
    dependencySubstitution {
        substitute(module("org.spongepowered:spongeapi")).using(project(":"))
    }
}
include(":SpongeVanilla")
project(":SpongeVanilla").projectDir = file("vanilla")
include("modlauncher-transformers")
include("generator")
include("modlauncher-patcher")

val testPlugins = file("testplugins.settings.gradle.kts")
if (testPlugins.exists()) {
    apply(from = testPlugins)
} else {
    testPlugins.writeText(listOf(
        "// Uncomment to enable client module for debugging",
        "//include(\":testplugins\")"
    ).joinToString(separator = System.lineSeparator(), postfix = System.lineSeparator()))
}
val testPluginsEnabledInCi: String = startParameter.projectProperties.getOrDefault("enableTestPlugins", "false")
if (testPluginsEnabledInCi.toBoolean()) {
    include(":testplugins")
}

val spongeForge = file("spongeforge.settings.gradle.kts")
if (spongeForge.exists()) {
    apply(from = spongeForge)
} else {
    spongeForge.writeText(listOf(
        "// Uncomment to enable SpongeForge module.",
        "// By default only Sponge and SpongeVanilla are made available",
        "//include(\":SpongeForge\")",
        "//project(\":SpongeForge\").projectDir = file(\"forge\")"
    ).joinToString(separator = System.lineSeparator(), postfix = System.lineSeparator()))
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
                extraProperties["api${key.replaceFirstChar { it.uppercase() }}"] = value
            }
        }
    }

    gradle.beforeProject {
        val extraExt = project.extensions.extraProperties
        extraProperties.forEach { (k, v) -> extraExt.set(k, v) }
    }
}
