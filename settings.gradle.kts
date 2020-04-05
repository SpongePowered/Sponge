rootProject.name = "SpongeCommon"

include(":SpongeAPI")
include(":testplugins")
pluginManagement {
    repositories {
        jcenter()
        mavenLocal()
        mavenCentral()
        maven("https://repo.spongepowered.org/maven")
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("org.spongepowered.gradle.")) {
                val version = requested.version ?: "0.11.1-SNAPSHOT"
                useModule("org.spongepowered:spongegradle:$version")
            }
        }
    }

}