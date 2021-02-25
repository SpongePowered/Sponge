plugins {
    `kotlin-dsl`
    `java-library`
    idea
}
repositories {
    maven("https://repo.spongepowered.org/repository/maven-public/")
}


dependencies {
    implementation("org.spongepowered:vanillagradle:0.1")
    // implementation("org.spongepowered:mixingradle:0.7-SNAPSHOT")
    implementation("com.google.code.gson:gson:2.8.6")
}