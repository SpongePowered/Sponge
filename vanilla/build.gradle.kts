import org.jetbrains.gradle.ext.TaskTriggersConfig
import org.spongepowered.gradle.impl.GenerateResourceTemplates

plugins {
    id("org.spongepowered.gradle.vanilla")
    id("com.github.johnrengelman.shadow")
    id("templated-resources")
    id("sponge-impl.platform-convention")
    eclipse
}

val commonProject = parent!!
val apiVersion: String by project
val minecraftVersion: String by project
val recommendedVersion: String by project
val organization: String by project
val projectUrl: String by project

val testplugins: Project? = rootProject.subprojects.find { "testplugins".equals(it.name) }

description = "The SpongeAPI implementation for Vanilla Minecraft"
version = spongeImpl.generatePlatformBuildVersionString(apiVersion, minecraftVersion, recommendedVersion)

val superclassConfigs = spongeImpl.getNamedConfigurations("superClassChanges")
val mixinConfigs = spongeImpl.mixinConfigurations

/*
minecraft {
    runs {
        // Full development environment
        sequenceOf(8, 11, 17).forEach {
            server("runJava${it}Server") {
                args("--nogui", "--launchTarget", "sponge_server_dev")
                targetVersion(it)
            }
            client("runJava${it}Client") {
                args("--launchTarget", "sponge_client_dev")
                targetVersion(it)
            }
        }

        // Lightweight integration tests
        server("integrationTestServer") {
            args("--launchTarget", "sponge_server_it")
        }
        client("integrationTestClient") {
            args("--launchTarget", "sponge_client_it")
        }

        configureEach {
            workingDirectory(project.file("run/"))
            if (org.spongepowered.gradle.vanilla.internal.util.IdeConfigurer.isIdeaImport()) { // todo(zml): promote to API... eventually
                // IntelliJ does not properly report its compatibility
                jvmArgs("-Dterminal.ansi=true", "-Djansi.mode=force")
            }
            jvmArgs(
                    "-Dlog4j.configurationFile=log4j2_dev.xml",
                    "-Dmixin.dumpTargetOnFailure=true",
                    "-Dmixin.debug.verbose=true",
                    "-Dmixin.debug.countInjections=true",
                    "-Dmixin.debug.strict=true",
                    "-Dmixin.debug.strict.unique=false"
            )
            allJvmArgumentProviders += CommandLineArgumentProvider {
                // todo: Mixin agent does not currently work in 0.8.4
                /*// Resolve the Mixin artifact for use as a reload agent
                val mixinJar = vanillaAppLaunchConfig.get().resolvedConfiguration
                        .getFiles { it.name == "mixin" && it.group == "org.spongepowered" }
                        .firstOrNull()

                // The mixin agent initializes logging too early, which prevents jansi from properly stripping escape codes in Eclipse.
                val base = if (!org.spongepowered.gradle.vanilla.internal.util.IdeConfigurer.isEclipseImport() && mixinJar != null) {
                    listOf("-javaagent:$mixinJar")
                } else {
                    emptyList()
                }*/

                // Then add necessary module cracks
                if (!this.name.contains("integrationTest") && !this.name.contains("Java8")) {
                    listOf(
                        "--illegal-access=deny", // enable strict mode in prep for Java 16
                        "--add-exports=java.base/sun.security.util=ALL-UNNAMED", // ModLauncher
                        "--add-opens=java.base/java.util.jar=ALL-UNNAMED" // ModLauncher
                    )
                } else {
                    emptyList()
                }
            }
            allArgumentProviders += CommandLineArgumentProvider {
                mixinConfigs.asSequence()
                        .flatMap { sequenceOf("--mixin.config", it) }
                        .toList()
            }
            allArgumentProviders += CommandLineArgumentProvider {
                superclassConfigs.asSequence()
                    .flatMap { sequenceOf("--superclass_change.config", it) }
                    .toList()
            }
            mainClass("org.spongepowered.vanilla.applaunch.Main")
        }
    }
    commonProject.sourceSets["main"].resources
            .filter { it.name.endsWith(".accesswidener") }
            .files
            .forEach {
                accessWideners(it)
            }

    project.sourceSets["main"].resources
            .filter { it.name.endsWith(".accesswidener") }
            .files
            .forEach { accessWideners(it) }
}

dependencies {
    val apiAdventureVersion: String by project
    val apiConfigurateVersion: String by project
    val apiPluginSpiVersion: String by project
    val asmVersion: String by project
    val guavaVersion: String by project
    val jlineVersion: String by project
    val jansiVersion: String by project
    val log4jVersion: String by project
    val mixinVersion: String by project
    val modlauncherVersion: String by project

    // Libraries only needed on the TCL (during main game lifecycle)

    libraries("org.spongepowered:spongeapi:$apiVersion")
    libraries(platform("net.kyori:adventure-bom:$apiAdventureVersion"))
    libraries("net.kyori:adventure-serializer-configurate4")
    libraries("javax.inject:javax.inject:1")
    libraries("org.spongepowered:configurate-jackson") {
        exclude(group = "org.spongepowered", module = "configurate-core")
        exclude(group = "org.checkerframework", module = "checker-qual")
    }

    // Databases
    libraries("com.zaxxer:HikariCP:2.6.3")

    // Libraries needed during applaunch phase and runtime
    bootstrapLibraries("net.minecrell:terminalconsoleappender:1.3.0")
    bootstrapLibraries("org.jline:jline-terminal:$jlineVersion")
    bootstrapLibraries("org.jline:jline-reader:$jlineVersion")
    bootstrapLibraries("org.jline:jline-terminal-jansi:$jlineVersion") {
        exclude(group = "org.fusesource.jansi", module = "jansi")
    }
    // If JLine is updated and updates the jansi dep, the above exclusion
    // and below library can be removed.
    // https://github.com/SpongePowered/Sponge/issues/3429
    bootstrapLibraries("org.fusesource.jansi:jansi:$jansiVersion")

    bootstrapLibraries(platform("org.spongepowered:configurate-bom:$apiConfigurateVersion"))
    bootstrapLibraries("org.spongepowered:configurate-core") {
        exclude(group = "org.checkerframework", module = "checker-qual")
    }
    bootstrapLibraries("org.spongepowered:configurate-hocon") {
        exclude(group = "org.spongepowered", module = "configurate-core")
        exclude(group = "org.checkerframework", module = "checker-qual")
    }
    bootstrapLibraries("org.apache.logging.log4j:log4j-api:$log4jVersion")
    bootstrapLibraries("org.apache.logging.log4j:log4j-core:$log4jVersion")
    bootstrapLibraries("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")

    // Mixin and dependencies
    bootstrapLibraries("org.spongepowered:mixin:$mixinVersion")
    bootstrapLibraries("org.ow2.asm:asm-util:$asmVersion")
    bootstrapLibraries("org.ow2.asm:asm-tree:$asmVersion")
    bootstrapLibraries("com.google.guava:guava:$guavaVersion")

    // Launch Dependencies - Needed to bootstrap the engine(s)
    // Not needing to be source-visible past the init phase
    // The ModLauncher compatibility launch layer
    appLaunch("cpw.mods:modlauncher:$modlauncherVersion") {
        exclude(group = "org.apache.logging.log4j")
        exclude(group = "net.sf.jopt-simple") // uses a newer version than MC
    }
    appLaunch("org.ow2.asm:asm-commons:$asmVersion")
    appLaunch("cpw.mods:grossjava9hacks:1.3.3") {
        exclude(group = "org.apache.logging.log4j")
    }
    appLaunch("org.spongepowered:plugin-spi:$apiPluginSpiVersion")
    appLaunch("com.lmax:disruptor:3.4.2")
    "applaunchCompileOnly"("org.jetbrains:annotations:22.0.0")

    testplugins?.also {
        vanillaAppLaunchRuntime(project(it.path)) {
            exclude(group = "org.spongepowered")
        }
    }
}

val vanillaManifest = java.manifest {
    attributes(
        "Specification-Title" to "SpongeVanilla",
        "Specification-Vendor" to "SpongePowered",
        "Specification-Version" to apiVersion,
        "Implementation-Title" to project.name,
        "Implementation-Version" to spongeImpl.generatePlatformBuildVersionString(apiVersion, minecraftVersion, recommendedVersion),
        "Implementation-Vendor" to "SpongePowered"
    )
    // These two are included by most CI's
    System.getenv()["GIT_COMMIT"]?.apply { attributes("Git-Commit" to this) }
    System.getenv()["GIT_BRANCH"]?.apply { attributes("Git-Branch" to this) }
}

tasks {
    jar {
        manifest.from(vanillaManifest)
    }

    named("templateLaunchResources", GenerateResourceTemplates::class) {
        inputs.property("version.api", apiVersion)
        inputs.property("version.minecraft", minecraftVersion)
        inputs.property("version.vanilla", project.version)

        expand(
            "apiVersion" to apiVersion,
            "minecraftVersion" to minecraftVersion,
            "version" to project.version
        )
    }

    val integrationTest by registering {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        dependsOn("integrationTestServer", "integrationTestClient")
    }

}

indraSpotlessLicenser {
    licenseHeaderFile(rootProject.file("HEADER.txt"))

    property("name", "Sponge")
    property("organization", organization)
    property("url", projectUrl)
}

val shadowJar by tasks.existing
val vanillaInstallerJar by tasks.existing
val vanillaAppLaunchJar by tasks.existing
val vanillaLaunchJar by tasks.existing
val vanillaMixinsJar by tasks.existing

publishing {
    publications {
        register("sponge", MavenPublication::class) {

            artifact(shadowJar.get())
            artifact(vanillaInstallerJar.get())
            artifact(vanillaAppLaunchJar.get())
            artifact(vanillaLaunchJar.get())
            artifact(vanillaMixinsJar.get())
            artifact(tasks["applaunchSourceJar"])
            artifact(tasks["launchSourceJar"])
            artifact(tasks["mixinsSourceJar"])
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
            }
        }
    }
}*/
