plugins {
    `kotlin-dsl`
    `java-library`
    idea
}
repositories {
    gradlePluginPortal()
    jcenter()
    maven("https://files.minecraftforge.net/maven")
    maven("https://repo.spongepowered.org/maven")
}


dependencies {
    implementation("net.minecraftforge.gradle:ForgeGradle:3.0.+")
}