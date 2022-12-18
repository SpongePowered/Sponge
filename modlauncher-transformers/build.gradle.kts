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

val apiConfigurateVersion: String by project
val asmVersion: String by project
val checkerVersion: String by project
val log4jVersion: String by project
val modlauncherVersion: String by project

dependencies {
    // AccessWidener transformer
    implementation("net.fabricmc:access-widener:2.1.0") {
        exclude(group="org.apache.logging.log4j")
    }
    // ModLauncher inherited dependencies - strictly should be provided by
    //  the platform making use of this project
    compileOnly("org.apache.logging.log4j:log4j-api:$log4jVersion")
    compileOnly("cpw.mods:modlauncher:$modlauncherVersion") {
        exclude(group = "org.apache.logging.log4j")
        exclude(group = "net.sf.jopt-simple") // uses a newer version than MC
    }

    compileOnly("net.sf.jopt-simple:jopt-simple:5.0.4")
    compileOnly("org.ow2.asm:asm-commons:$asmVersion")
    compileOnly("cpw.mods:grossjava9hacks:1.3.3") {
        exclude(group="org.apache.logging.log4j")
    }
    // Configurate dependencies, also to be provided by the platform
    //  making use of this project
    compileOnly(platform("org.spongepowered:configurate-bom:$apiConfigurateVersion"))
    compileOnly("org.spongepowered:configurate-core") {
        exclude(group = "org.checkerframework", module="checker-qual") // We use our own version
    }
    compileOnly("org.spongepowered:configurate-jackson") {
        exclude(group="org.spongepowered", module="configurate-core")
        exclude(group="org.checkerframework", module="checker-qual") // We use our own version
    }

    // And finally, compile only annotations
    compileOnly("org.checkerframework:checker-qual:$checkerVersion")
}
