dependencies {
    implementation(libs.accessWidener) {
        exclude(group="org.apache.logging.log4j")
    }

    compileOnly(libs.log4j.api)
    compileOnly(libs.neo.modlauncher) {
        exclude(group = "org.ow2.asm")
        exclude(group = "org.apache.logging.log4j")
    }

    compileOnly(libs.joptSimple)
    compileOnly(libs.asm.commons)
    compileOnly(apiLibs.checkerQual)

    // Optional
    compileOnly(libs.jacoco.core)
}

tasks {
    jar {
        manifest {
            attributes("Automatic-Module-Name" to "sponge.modlauncher_transformers")
        }
    }
}
