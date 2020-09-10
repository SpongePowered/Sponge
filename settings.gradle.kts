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
            "// By default only SpongeCommon and SpongeVanilla are made available\n" +
            "//include(\":SpongeForge\")\n" +
            "//project(\":SpongeForge\").projectDir = file(\"forge\")\n")
}
val spongeForgeEnabledInCi: String = startParameter.projectProperties.getOrDefault("enableSpongeForge", "false")
if (spongeForgeEnabledInCi.toBoolean()) {
    include(":SpongeForge")
    project(":SpongeForge").projectDir = file("forge")
}