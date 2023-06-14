import org.jetbrains.gradle.ext.settings
import org.jetbrains.gradle.ext.taskTriggers

plugins {
    eclipse
    id("org.spongepowered.gradle.vanilla")
    alias(libs.plugins.shadow)
    alias(apiLibs.plugins.spongeConvention) apply false // for version json generation
    alias(libs.plugins.versions)
    id("sponge-impl.platform-convention")
}

val commonProject = project
val apiVersion: String by project
val minecraftVersion: String by project
val recommendedVersion: String by project

val apiAdventureVersion: String by project
val apiConfigurateVersion: String by project
val apiPluginSpiVersion: String by project
val asmVersion: String by project
val log4jVersion: String by project
val modlauncherVersion: String by project
val mixinVersion: String by project
val guavaVersion: String by project
val junitVersion: String by project
val mockitoVersion: String by project
val checkerVersion: String by project

val commonManifest = java.manifest {
    attributes(
        "Specification-Title" to "Sponge",
        "Specification-Vendor" to "SpongePowered",
        "Specification-Version" to apiVersion,
        "Implementation-Title" to project.name,
        "Implementation-Version" to spongeImpl.generateImplementationVersionString(apiVersion, minecraftVersion, recommendedVersion),
        "Implementation-Vendor" to "SpongePowered"
    )
    // These two are included by most CI's
    System.getenv()["GIT_COMMIT"]?.apply { attributes("Git-Commit" to this) }
    System.getenv()["GIT_BRANCH"]?.apply { attributes("Git-Branch" to this) }
}

tasks {
    jar {
        manifest.from(commonManifest)
    }
    check {
        dependsOn(gradle.includedBuild("SpongeAPI").task(":check"))
    }

    prepareWorkspace {
        dependsOn(gradle.includedBuild("SpongeAPI").task(":genEventImpl"))
    }
}

version = spongeImpl.generateImplementationVersionString(apiVersion, minecraftVersion, recommendedVersion)

// create the sourcesets
val main by sourceSets

dependencies {
    // api
    /*api("org.spongepowered:spongeapi:$apiVersion")

    // Database stuffs... likely needs to be looked at
    implementation("com.zaxxer:HikariCP:2.6.3")
    implementation("org.mariadb.jdbc:mariadb-java-client:2.0.3")
    implementation("com.h2database:h2:1.4.196")
    implementation("org.xerial:sqlite-jdbc:3.20.0")
    implementation("javax.inject:javax.inject:1")

    // ASM - required for generating event listeners
    implementation("org.ow2.asm:asm-util:$asmVersion")
    implementation("org.ow2.asm:asm-tree:$asmVersion")

    // Implementation-only Adventure
    implementation(platform("net.kyori:adventure-bom:$apiAdventureVersion"))
    implementation("net.kyori:adventure-serializer-configurate4")

    // Launch Dependencies - Needed to bootstrap the engine(s)
    launchConfig("org.spongepowered:spongeapi:$apiVersion")
    launchConfig("org.spongepowered:plugin-spi:$apiPluginSpiVersion")
    launchConfig("org.spongepowered:mixin:$mixinVersion")
    launchConfig("org.checkerframework:checker-qual:3.13.0")
    launchConfig("com.google.guava:guava:$guavaVersion") {
        exclude(group = "com.google.code.findbugs", module = "jsr305") // We don't want to use jsr305, use checkerframework
        exclude(group = "org.checkerframework", module = "checker-qual") // We use our own version
        exclude(group = "com.google.j2objc", module = "j2objc-annotations")
        exclude(group = "org.codehaus.mojo", module = "animal-sniffer-annotations")
        exclude(group = "com.google.errorprone", module = "error_prone_annotations")
    }
    launchConfig("com.google.code.gson:gson:2.8.0")
    launchConfig("org.ow2.asm:asm-tree:$asmVersion")
    launchConfig("org.ow2.asm:asm-util:$asmVersion")

    // Applaunch -- initialization that needs to occur without game access
    applaunchConfig("org.checkerframework:checker-qual:$checkerVersion")
    applaunchConfig("org.apache.logging.log4j:log4j-api:$log4jVersion")
    applaunchConfig("com.google.guava:guava:$guavaVersion")
    applaunchConfig(platform("org.spongepowered:configurate-bom:$apiConfigurateVersion"))
    applaunchConfig("org.spongepowered:configurate-core") {
        exclude(group = "org.checkerframework", module = "checker-qual") // We use our own version
    }
    applaunchConfig("org.spongepowered:configurate-hocon") {
        exclude(group = "org.spongepowered", module = "configurate-core")
        exclude(group = "org.checkerframework", module = "checker-qual") // We use our own version
    }
    applaunchConfig("org.spongepowered:configurate-jackson") {
        exclude(group = "org.spongepowered", module = "configurate-core")
        exclude(group = "org.checkerframework", module = "checker-qual") // We use our own version
    }
    applaunchConfig("org.apache.logging.log4j:log4j-core:$log4jVersion")

    mixinsConfig(sourceSets.named("main").map { it.output })
    add(mixins.get().implementationConfigurationName, "org.spongepowered:spongeapi:$apiVersion")

    // Tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")
    testImplementation("org.mockito:mockito-inline:$mockitoVersion")*/
}

idea {
    if (project != null) {
        project.settings {
            taskTriggers {
                afterSync(":modlauncher-transformers:build")
            }
        }
    }
}

/*tasks {
    shadowJar {
        mergeServiceFiles()
        archiveClassifier.set("dev")
        manifest {
            attributes(mapOf(
                    "Access-Widener" to "common.accesswidener",
                    "Multi-Release" to true
            ))
            from(commonManifest)
        }
        from(jar)
        from(sourceJar)
        from(mixinsJar)
        from(accessorsJar)
        from(launchJar)
        from(applaunchJar)
        dependencies {
            include(project(":"))
        }
    }
}
            pom {
                artifactId = project.name.toLowerCase()
                this.name.set(project.name)
                this.description.set(project.description)
                this.url.set(projectUrl)

                licenses {
                    license {
                        this.name.set("MIT")
                        this.url.set("https://opensource.org/licenses/MIT")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/SpongePowered/Sponge.git")
                    developerConnection.set("scm:git:ssh://github.com/SpongePowered/Sponge.git")
                    this.url.set(projectUrl)
                }
     */