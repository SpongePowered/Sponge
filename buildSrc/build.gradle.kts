plugins {
    `kotlin-dsl`
}

repositories {
    maven {
        name = "sponge"
        setUrl("https://repo.spongepowered.org/maven")
    }
    maven {
        name = "forge"
        setUrl("https://files.minecraftforge.net/maven")
    }
}

dependencies {
    implementation("net.minecrell.licenser:net.minecrell.licenser.gradle.plugin:0.4.1")
    implementation("net.minecraftforge.gradle:ForgeGradle:3.0.142")
    implementation(group = "org.spongepowered", name = "SpongeGradle", version = "0.11.0-SNAPSHOT")
}