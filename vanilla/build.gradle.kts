import org.jetbrains.gradle.ext.TaskTriggersConfig
import org.spongepowered.gradle.impl.GenerateResourceTemplates

plugins {
    id("org.spongepowered.gradle.vanilla")
    id("com.github.johnrengelman.shadow")
    id("implementation-structure")
    id("templated-resources")
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

// Vanilla extra configurations
val vanillaLibrariesConfig = configurations.register("libraries")
val vanillaAppLaunchConfig = configurations.register("applaunch") {
    extendsFrom(vanillaLibrariesConfig.get())
    extendsFrom(configurations.minecraft.get())
}
val vanillaInstallerConfig = configurations.register("installer")

// Common source sets and configurations
val launchConfig = commonProject.configurations.named("launch")
val accessors = commonProject.sourceSets.named("accessors")
val launch = commonProject.sourceSets.named("launch")
val applaunch = commonProject.sourceSets.named("applaunch")
val mixins = commonProject.sourceSets.named("mixins")
val main = commonProject.sourceSets.named("main")

// Vanilla source sets
val vanillaInstaller by sourceSets.register("installer")

val vanillaInstallerJava9 by sourceSets.register("installerJava9") {
    this.java.setSrcDirs(setOf("src/installer/java9"))
    compileClasspath += vanillaInstaller.compileClasspath
    compileClasspath += vanillaInstaller.runtimeClasspath

    tasks.named(compileJavaTaskName, JavaCompile::class) {
        options.release.set(9)
        if (JavaVersion.current() < JavaVersion.VERSION_11) {
            javaCompiler.set(javaToolchains.compilerFor { languageVersion.set(JavaLanguageVersion.of(11)) })
        }
    }

    dependencies.add(implementationConfigurationName, objects.fileCollection().from(vanillaInstaller.output.classesDirs))
}

val vanillaMain by sourceSets.named("main") {
    // implementation (compile) dependencies
    spongeImpl.applyNamedDependencyOnOutput(commonProject, accessors.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, launch.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, applaunch.get(), this, project, this.implementationConfigurationName)
    configurations.named(implementationConfigurationName) {
        extendsFrom(vanillaLibrariesConfig.get())
    }
}
val vanillaLaunch by sourceSets.register("launch") {
    // implementation (compile) dependencies
    spongeImpl.applyNamedDependencyOnOutput(commonProject, launch.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, applaunch.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, main.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(project, this, vanillaMain, project, vanillaMain.implementationConfigurationName)

    configurations.named(implementationConfigurationName) {
        extendsFrom(vanillaAppLaunchConfig.get())
    }
}
val vanillaMixins by sourceSets.register("mixins") {
    // implementation (compile) dependencies
    spongeImpl.applyNamedDependencyOnOutput(commonProject, mixins.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, accessors.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, launch.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, applaunch.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, main.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(project, vanillaMain, this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(project, vanillaLaunch, this, project, this.implementationConfigurationName)
}
val vanillaAppLaunch by sourceSets.register("applaunch") {
    // implementation (compile) dependencies
    spongeImpl.applyNamedDependencyOnOutput(commonProject, applaunch.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, launch.get(), vanillaLaunch, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(project, vanillaInstaller, this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(project, this, vanillaLaunch, project, vanillaLaunch.implementationConfigurationName)
    // runtime dependencies - literally add the rest of the project, because we want to launch the game
    spongeImpl.applyNamedDependencyOnOutput(project, vanillaMixins, this, project, this.runtimeOnlyConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(project, vanillaLaunch, this, project, this.runtimeOnlyConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, mixins.get(), this, project, this.runtimeOnlyConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, main.get(), this, project, this.runtimeOnlyConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, accessors.get(), this, project, this.runtimeOnlyConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(project, vanillaMain, this, project, this.runtimeOnlyConfigurationName)
}
val vanillaMixinsImplementation by configurations.named(vanillaMixins.implementationConfigurationName) {
    extendsFrom(vanillaAppLaunchConfig.get())
}
configurations.named(vanillaInstaller.implementationConfigurationName) {
    extendsFrom(vanillaInstallerConfig.get())
}
configurations.named(vanillaAppLaunch.implementationConfigurationName) {
    extendsFrom(vanillaAppLaunchConfig.get())
    extendsFrom(launchConfig.get())
}
val vanillaAppLaunchRuntime by configurations.named(vanillaAppLaunch.runtimeOnlyConfigurationName)

val mixinConfigs = spongeImpl.mixinConfigurations
minecraft {
    runs {
        // Full development environment
        sequenceOf(8, 11, 16).forEach {
            server("runJava${it}Server") {
                args("--nogui", "--launchTarget", "sponge_server_dev")
                targetVersion(it)
                allArgumentProviders += CommandLineArgumentProvider {
                    mixinConfigs.asSequence()
                            .flatMap { sequenceOf("--mixin.config", it) }
                            .toList()
                }
            }
            client("runJava${it}Client") {
                args("--launchTarget", "sponge_client_dev")
                jvmArgs("-Dmixin.debug.export=true", "-Dmixin.debug=true")
                targetVersion(it)
                allArgumentProviders += CommandLineArgumentProvider {
                    mixinConfigs.asSequence()
                            .flatMap { sequenceOf("--mixin.config", it) }
                            .toList()
                }
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
            jvmArgs("-Dlog4j.configurationFile=log4j2_dev.xml", "-Dmixin.dumpTargetOnFailure=true")
            allJvmArgumentProviders += CommandLineArgumentProvider {
                // Resolve the Mixin artifact for use as a reload agent
                val mixinJar = vanillaAppLaunchConfig.get().resolvedConfiguration
                        .getFiles { it.name == "mixin" && it.group == "org.spongepowered" }
                        .firstOrNull()

                // The mixin agent initializes logging too early, which prevents jansi from properly stripping escape codes in Eclipse.
                val base = if (!org.spongepowered.gradle.vanilla.internal.util.IdeConfigurer.isEclipseImport() && mixinJar != null) {
                    listOf("-javaagent:$mixinJar")
                } else {
                    emptyList()
                }

                // Then add necessary module cracks
                if (!this.name.contains("integrationTest") && !this.name.contains("Java8")) {
                    base + listOf(
                        "--illegal-access=deny", // enable strict mode in prep for Java 16
                        "--add-exports=java.base/sun.security.util=ALL-UNNAMED", // ModLauncher
                        "--add-opens=java.base/java.util.jar=ALL-UNNAMED" // ModLauncher
                    )
                } else {
                    base
                }
            }
            mainClass("org.spongepowered.vanilla.applaunch.Main")
            classpath.setFrom(
                vanillaAppLaunch.output,
                vanillaAppLaunch.runtimeClasspath
            )
            ideaRunSourceSet.set(vanillaAppLaunch)
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
    val asmVersion: String by project
    val guavaVersion: String by project
    val jlineVersion: String by project
    val jansiVersion: String by project
    val log4jVersion: String by project
    val mixinVersion: String by project
    val modlauncherVersion: String by project
    val pluginSpiVersion: String by project
    val timingsVersion: String by project
    val tinyLogVersion: String by project

    api(project(":", configuration = "launch"))
    implementation(project(":", configuration = "accessors"))
    implementation(project(commonProject.path))

    vanillaMixinsImplementation(project(commonProject.path))
    add(vanillaLaunch.implementationConfigurationName, "org.spongepowered:spongeapi:$apiVersion")

    val installer = vanillaInstallerConfig.name
    installer("com.google.code.gson:gson:2.8.0")
    installer("org.spongepowered:configurate-hocon:$apiConfigurateVersion")
    installer("org.spongepowered:configurate-core:$apiConfigurateVersion")
    installer("net.sf.jopt-simple:jopt-simple:5.0.3")
    installer("org.tinylog:tinylog-api:$tinyLogVersion")
    installer("org.tinylog:tinylog-impl:$tinyLogVersion")
    // Override ASM versions, and explicitly declare dependencies so ASM is excluded from the manifest.
    val asmExclusions = sequenceOf("-commons", "-tree", "-analysis", "")
            .map { "asm$it" }
            .onEach {
                installer("org.ow2.asm:$it:$asmVersion")
            }.toSet()
    installer("org.cadixdev:atlas:0.2.1") {
        asmExclusions.forEach { exclude(group = "org.ow2.asm", module = it) } // Use our own ASM version
    }
    installer("org.cadixdev:lorenz-asm:0.5.6") {
        asmExclusions.forEach { exclude(group = "org.ow2.asm", module = it) } // Use our own ASM version
    }
    installer("org.cadixdev:lorenz-io-proguard:0.5.6")

    // Add the API as a runtime dependency, just so it gets shaded into the jar
    add(vanillaInstaller.runtimeOnlyConfigurationName, "org.spongepowered:spongeapi:$apiVersion") {
        isTransitive = false
    }

    val appLaunch = vanillaAppLaunchConfig.name
    appLaunch("org.spongepowered:spongeapi:$apiVersion")
    appLaunch(platform("net.kyori:adventure-bom:$apiAdventureVersion"))
    appLaunch("net.kyori:adventure-serializer-configurate4")
    appLaunch("org.spongepowered:mixin:$mixinVersion")
    appLaunch("org.ow2.asm:asm-util:$asmVersion")
    appLaunch("org.ow2.asm:asm-tree:$asmVersion")
    appLaunch("com.google.guava:guava:$guavaVersion")
    appLaunch("org.spongepowered:plugin-spi:$pluginSpiVersion")
    appLaunch("javax.inject:javax.inject:1")
    appLaunch("org.apache.logging.log4j:log4j-api:$log4jVersion")
    appLaunch("org.apache.logging.log4j:log4j-core:$log4jVersion")
    appLaunch("com.lmax:disruptor:3.4.2")
    appLaunch("com.zaxxer:HikariCP:2.6.3")
    appLaunch("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
    appLaunch(platform("org.spongepowered:configurate-bom:$apiConfigurateVersion"))
    appLaunch("org.spongepowered:configurate-core") {
        exclude(group = "org.checkerframework", module = "checker-qual")
    }
    appLaunch("org.spongepowered:configurate-hocon") {
        exclude(group = "org.spongepowered", module = "configurate-core")
        exclude(group = "org.checkerframework", module = "checker-qual")
    }
    appLaunch("org.spongepowered:configurate-jackson") {
        exclude(group = "org.spongepowered", module = "configurate-core")
        exclude(group = "org.checkerframework", module = "checker-qual")
    }

    val libraries = vanillaLibrariesConfig.name
    libraries("net.minecrell:terminalconsoleappender:1.3.0-SNAPSHOT")
    libraries("org.jline:jline-terminal:$jlineVersion")
    libraries("org.jline:jline-reader:$jlineVersion")
    libraries("org.jline:jline-terminal-jansi:$jlineVersion") {
        exclude(group = "org.fusesource.jansi", module = "jansi")
    }
    // If JLine is updated and updates the jansi dep, the above exclusion
    // and below library can be removed.
    // https://github.com/SpongePowered/Sponge/issues/3429
    libraries("org.fusesource.jansi:jansi:$jansiVersion")
    libraries("org.spongepowered:timings:$timingsVersion")

    // Launch Dependencies - Needed to bootstrap the engine(s)
    // The ModLauncher compatibility launch layer
    appLaunch("cpw.mods:modlauncher:$modlauncherVersion") {
        exclude(group = "org.apache.logging.log4j")
        exclude(group = "net.sf.jopt-simple") // uses a newer version than MC
    }
    appLaunch("org.ow2.asm:asm-commons:$asmVersion")
    appLaunch("cpw.mods:grossjava9hacks:1.3.3") {
        exclude(group = "org.apache.logging.log4j")
    }
    appLaunch("net.fabricmc:access-widener:1.0.2") {
        exclude(group = "org.apache.logging.log4j")
    }

    testplugins?.also {
        vanillaAppLaunchRuntime(project(it.path)) {
            exclude(group = "org.spongepowered")
        }
    }
}

val vanillaManifest = the<JavaPluginConvention>().manifest {
    attributes(
            "Specification-Title" to "SpongeVanilla",
            "Specification-Vendor" to "SpongePowered",
            "Specification-Version" to apiVersion,
            "Implementation-Title" to project.name,
            "Implementation-Version" to spongeImpl.generatePlatformBuildVersionString(apiVersion, minecraftVersion, recommendedVersion),
            "Implementation-Vendor" to "SpongePowered"
    )
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

    val vanillaInstallerJar by registering(Jar::class) {
        archiveClassifier.set("installer")
        manifest{
            from(vanillaManifest)
            attributes(
                    "Premain-Class" to "org.spongepowered.vanilla.installer.Agent",
                    "Agent-Class" to "org.spongepowered.vanilla.installer.Agent",
                    "Launcher-Agent-Class" to "org.spongepowered.vanilla.installer.Agent",
                    "Multi-Release" to true
            )
        }
        from(vanillaInstaller.output)
        into("META-INF/versions/9/") {
            from(vanillaInstallerJava9.output)
        }
    }

    val vanillaAppLaunchJar by registering(Jar::class) {
        archiveClassifier.set("applaunch")
        manifest.from(vanillaManifest)
        from(vanillaAppLaunch.output)
    }
    val vanillaLaunchJar by registering(Jar::class) {
        archiveClassifier.set("launch")
        manifest.from(vanillaManifest)
        from(vanillaLaunch.output)
    }
    val vanillaMixinsJar by registering(Jar::class) {
        archiveClassifier.set("mixins")
        manifest.from(vanillaManifest)
        from(vanillaMixins.output)
    }

    val integrationTest by registering {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        dependsOn("integrationTestServer", "integrationTestClient")
    }

    val installerTemplateSource = project.file("src/installer/templates")
    val installerTemplateDest = project.layout.buildDirectory.dir("generated/sources/installerTemplates")
    val generateInstallerTemplates by registering(Copy::class) {
        group = "sponge"
        description = "Generate classes from templates for the SpongeVanilla installer"
        val properties = mutableMapOf(
                "minecraftVersion" to minecraftVersion
        )
        inputs.properties(properties)

        // Copy template
        from(installerTemplateSource)
        into(installerTemplateDest)
        expand(properties)
    }
    vanillaInstaller.java.srcDir(generateInstallerTemplates.map { it.outputs })

    // Generate templates on IDE import as well
    (rootProject.idea.project as? ExtensionAware)?.also {
        (it.extensions["settings"] as ExtensionAware).extensions.getByType(TaskTriggersConfig::class).afterSync(generateInstallerTemplates)
    }
    project.eclipse {
        synchronizationTasks(generateInstallerTemplates)
    }

    val installerResources = project.layout.buildDirectory.dir("generated/resources/installer")
    vanillaInstaller.resources.srcDir(installerResources)

    val downloadNotNeeded = configurations.register("downloadNotNeeded") {
        extendsFrom(configurations.minecraft.get())
        extendsFrom(vanillaInstallerConfig.get())
    }

    val emitDependencies by registering(org.spongepowered.gradle.impl.OutputDependenciesToJson::class) {
        group = "sponge"
        // everything in applaunch
        this.dependencies(vanillaAppLaunchConfig)
        // except what we're providing through the installer
        this.excludedDependencies(downloadNotNeeded)

        outputFile.set(installerResources.map { it.file("libraries.json") })
    }
    named(vanillaInstaller.processResourcesTaskName).configure {
        dependsOn(emitDependencies)
    }

    shadowJar {
        mergeServiceFiles()

        configurations = listOf(project.configurations.getByName(vanillaInstaller.runtimeClasspathConfigurationName))

        archiveClassifier.set("universal")
        manifest {
            attributes(mapOf(
                    "Access-Widener" to "common.accesswidener",
                    "MixinConfigs" to mixinConfigs.joinToString(","),
                    "Main-Class" to "org.spongepowered.vanilla.installer.InstallerMain",
                    "Launch-Target" to "sponge_server_prod",
                    "Multi-Release" to true,
                    "Premain-Class" to "org.spongepowered.vanilla.installer.Agent",
                    "Agent-Class" to "org.spongepowered.vanilla.installer.Agent",
                    "Launcher-Agent-Class" to "org.spongepowered.vanilla.installer.Agent"
            ))
            from(vanillaManifest)
        }
        from(commonProject.sourceSets.main.map { it.output })
        from(commonProject.sourceSets.named("mixins").map {it.output })
        from(commonProject.sourceSets.named("accessors").map {it.output })
        from(commonProject.sourceSets.named("launch").map {it.output })
        from(commonProject.sourceSets.named("applaunch").map {it.output })
        from(sourceSets.main.map {it.output })
        from(vanillaInstaller.output)
        from(vanillaInstallerJava9.output) {
            into("META-INF/versions/9/")
        }
        from(vanillaAppLaunch.output)
        from(vanillaLaunch.output)
        from(vanillaMixins.output)
        /*dependencies {
            // include(project(":"))
            include("org.spongepowered:spongeapi:$apiVersion")
        } */

        // We cannot have modules in a shaded jar
        exclude("META-INF/versions/*/module-info.class")
        exclude("module-info.class")
    }
    assemble {
        dependsOn(shadowJar)
    }
}

license {
    properties {
        this["name"] = "Sponge"
        this["organization"] = organization
        this["url"] = projectUrl
    }
    header(rootProject.file("HEADER.txt"))

    include("**/*.java")
    newLine(false)
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
}
