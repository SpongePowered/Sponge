plugins {
    id("sponge-impl.published-convention")
}

val asmVersion: String by project
val checkerVersion: String by project
val log4jVersion: String by project
val modlauncherVersion: String by project

dependencies {
    // AccessWidener transformer
    implementation("net.fabricmc:access-widener:1.0.2") {
        exclude(group="org.apache.logging.log4j")
    }
    // ModLauncher inherited dependencies - strictly should be provided by
    //  the platform making use of this project
    compileOnly("org.apache.logging.log4j:log4j-api:$log4jVersion")
    compileOnly(libs.modlauncher) {
        exclude(group = "org.apache.logging.log4j")
        exclude(group = "net.sf.jopt-simple") // uses a newer version than MC
    }

    compileOnly("net.sf.jopt-simple:jopt-simple:5.0.3")
    compileOnly("org.ow2.asm:asm-commons:$asmVersion")
    compileOnly("cpw.mods:grossjava9hacks:1.3.3") {
        exclude(group="org.apache.logging.log4j")
    }
    // Configurate dependencies, also to be provided by the platform
    // making use of this project
    compileOnly(platform(apiLibs.configurate.bom))
    compileOnly(apiLibs.configurate.jackson) {
        exclude(group="org.checkerframework", module="checker-qual") // We use our own version
    }

    // And finally, compile only annotations
    compileOnly("org.checkerframework:checker-qual:$checkerVersion")
}
