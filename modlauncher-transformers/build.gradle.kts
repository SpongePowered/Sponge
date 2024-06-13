plugins {
    eclipse
    idea
}
// Make sure jar is present for other projects
eclipse {
    synchronizationTasks(tasks.jar)
}

val organization: String by project
val projectUrl: String by project

indraSpotlessLicenser {
    licenseHeaderFile(rootProject.file("HEADER.txt"))

    property("name", "Sponge")
    property("organization", organization)
    property("url", projectUrl)
}


dependencies {
    // AccessWidener transformer
    implementation(libs.accessWidener) {
        exclude(group="org.apache.logging.log4j")
    }
    // ModLauncher inherited dependencies - strictly should be provided by
    //  the platform making use of this project
    compileOnly(libs.log4j.api)
    compileOnly(libs.modlauncher) {
        exclude(group = "org.ow2.asm")
        exclude(group = "org.apache.logging.log4j")
    }

    compileOnly(libs.joptSimple)
    compileOnly(libs.asm.commons)
    // Configurate dependencies, also to be provided by the platform
    //  making use of this project
    compileOnly(platform(apiLibs.configurate.bom))
    compileOnly(apiLibs.configurate.core) {
        exclude(group = "org.checkerframework", module="checker-qual") // We use our own version
    }
    compileOnly(libs.configurate.jackson) {
        exclude(group="org.spongepowered", module="configurate-core")
        exclude(group="org.checkerframework", module="checker-qual") // We use our own version
    }

    // And finally, compile only annotations
    compileOnly(apiLibs.checkerQual)
}
