plugins {
    `kotlin-dsl`
    `java-library`
    idea
}
repositories {
    gradlePluginPortal()
    maven("https://repo-new.spongepowered.org/repository/maven-public/")
    mavenLocal()
}


dependencies {
    implementation("net.minecraftforge.gradle:ForgeGradle:3.0.185")
    implementation("org.spongepowered:mixingradle:0.7-SNAPSHOT")
}