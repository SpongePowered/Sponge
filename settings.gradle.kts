rootProject.name = "SpongeCommon"

include(":SpongeAPI")
pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://repo.spongepowered.org/maven")
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("org.spongepowered.gradle.")) {
                val version = requested.version ?: "0.11.1"
                useModule("org.spongepowered:SpongeGradle:$version")
            }
        }
    }

}