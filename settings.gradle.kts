rootProject.name = "SpongeCommon"

include("SpongeAPI")
include(":SpongeVanilla")
project(":SpongeVanilla").projectDir = file("vanilla")
pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://files.minecraftforge.net/maven")
        maven("https://repo-new.spongepowered.org/repository/maven-public")
        maven("https://repo.spongepowered.org/maven")
        gradlePluginPortal()
    }

}