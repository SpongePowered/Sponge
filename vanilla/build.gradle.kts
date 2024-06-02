plugins {
    id("org.spongepowered.gradle.vanilla")
    alias(libs.plugins.shadow)
    id("implementation-structure")
    alias(libs.plugins.blossom)
    eclipse
}

val commonProject = parent!!
val apiVersion: String by project
val apiJavaTarget: String by project
val minecraftVersion: String by project
val recommendedVersion: String by project
val organization: String by project
val projectUrl: String by project

val testplugins: Project? = rootProject.subprojects.find { "testplugins".equals(it.name) }

description = "The SpongeAPI implementation for Vanilla Minecraft"
version = spongeImpl.generatePlatformBuildVersionString(apiVersion, minecraftVersion, recommendedVersion)

// Vanilla extra configurations
val vanillaBootstrapLibrariesConfig = configurations.register("bootstrapLibraries")
val vanillaLibrariesConfig = configurations.register("libraries")
val mlTransformersConfig = configurations.register("mlTransformers")
val vanillaAppLaunchConfig = configurations.register("applaunch") {
    extendsFrom(vanillaBootstrapLibrariesConfig.get())
    extendsFrom(configurations.minecraft.get())
    extendsFrom(mlTransformersConfig.get())
}
val mlpatcherConfig = configurations.register("mlpatcher")
val vanillaInstallerConfig = configurations.register("installer") {
    extendsFrom(mlpatcherConfig.get())
    extendsFrom(mlTransformersConfig.get())
}

// Common source sets and configurations
val launchConfig = commonProject.configurations.named("launch")
val accessors = commonProject.sourceSets.named("accessors")
val launch = commonProject.sourceSets.named("launch")
val applaunch = commonProject.sourceSets.named("applaunch")
val mixins = commonProject.sourceSets.named("mixins")
val main = commonProject.sourceSets.named("main")

// Vanilla source sets
val vanillaInstaller by sourceSets.register("installer")

val vanillaMain by sourceSets.named("main") {
    // implementation (compile) dependencies
    spongeImpl.applyNamedDependencyOnOutput(commonProject, accessors.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, launch.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, applaunch.get(), this, project, this.implementationConfigurationName)
    configurations.named(implementationConfigurationName) {
        extendsFrom(vanillaLibrariesConfig.get())
        extendsFrom(vanillaBootstrapLibrariesConfig.get())
    }
}
val vanillaLaunch by sourceSets.register("launch") {
    // implementation (compile) dependencies
    spongeImpl.applyNamedDependencyOnOutput(commonProject, launch.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, applaunch.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, main.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(project, this, vanillaMain, project, vanillaMain.implementationConfigurationName)

    configurations.named(implementationConfigurationName) {
        extendsFrom(vanillaLibrariesConfig.get())
        extendsFrom(vanillaAppLaunchConfig.get())
    }
}
val vanillaAccessors by sourceSets.register("accessors") {
    spongeImpl.applyNamedDependencyOnOutput(commonProject, mixins.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, accessors.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, launch.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, applaunch.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, main.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(project, vanillaMain, this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(project, vanillaLaunch, this, project, this.implementationConfigurationName)
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
    spongeImpl.applyNamedDependencyOnOutput(project, vanillaAccessors, this, project, this.implementationConfigurationName)
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
    spongeImpl.applyNamedDependencyOnOutput(project, vanillaAccessors, this, project, this.runtimeOnlyConfigurationName)

    configurations.named(runtimeClasspathConfigurationName) {
        extendsFrom(vanillaLibrariesConfig.get())
        extendsFrom(mlpatcherConfig.get())
        extendsFrom(mlTransformersConfig.get())
    }
}

val vanillaAccessorsImplementation by configurations.named(vanillaAccessors.implementationConfigurationName) {
    extendsFrom(vanillaAppLaunchConfig.get())
}
val vanillaMixinsImplementation by configurations.named(vanillaMixins.implementationConfigurationName) {
    extendsFrom(vanillaAppLaunchConfig.get())
    extendsFrom(vanillaLibrariesConfig.get())
}
configurations.named(vanillaInstaller.implementationConfigurationName) {
    extendsFrom(vanillaInstallerConfig.get())
}

configurations.named(vanillaAppLaunch.implementationConfigurationName) {
    extendsFrom(vanillaAppLaunchConfig.get())
    extendsFrom(launchConfig.get())
}
val vanillaAppLaunchRuntime by configurations.named(vanillaAppLaunch.runtimeOnlyConfigurationName)

val superclassConfigs = spongeImpl.getNamedConfigurations("superClassChanges")
val mixinConfigs = spongeImpl.mixinConfigurations
minecraft {
    runs {
        // Full development environment
        server("runServer") {
            args("--nogui", "--launchTarget", "sponge_server_dev")
        }
        client("runClient") {
            args("--launchTarget", "sponge_client_dev")
        }

        // Lightweight integration tests
        server("integrationTestServer") {
            args("--launchTarget", "sponge_server_it")
        }
        client("integrationTestClient") {
            args("--launchTarget", "sponge_client_it")
        }

        configureEach {
            targetVersion(apiJavaTarget.toInt())
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
                listOf(
                    "--add-exports=java.base/sun.security.util=ALL-UNNAMED", // ModLauncher
                    "--add-opens=java.base/java.util.jar=ALL-UNNAMED", // ModLauncher
                    "-javaagent:${mlpatcherConfig.get().resolvedConfiguration.files.firstOrNull()}"
                )
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
            classpath.setFrom(
                vanillaAppLaunch.output,
                vanillaAppLaunch.runtimeClasspath,
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
    api(project(":", configuration = "launch"))
    implementation(project(":", configuration = "accessors"))
    implementation(project(commonProject.path))

    mlpatcherConfig.name(project(":modlauncher-patcher"))
    vanillaAccessorsImplementation(project(commonProject.path))
    vanillaMixinsImplementation(project(commonProject.path))

    val installer = vanillaInstallerConfig.name
    installer(apiLibs.gson)
    installer(platform(apiLibs.configurate.bom))
    installer(apiLibs.configurate.hocon)
    installer(apiLibs.configurate.core)
    installer(libs.configurate.jackson)
    installer(libs.joptSimple)
    installer(libs.tinylog.api)
    installer(libs.tinylog.impl)
    // Override ASM versions, and explicitly declare dependencies so ASM is excluded from the manifest.
    val asmExclusions = sequenceOf(libs.asm.asProvider(), libs.asm.commons, libs.asm.tree, libs.asm.analysis)
            .onEach {
                installer(it)
            }.toSet()
    installer(libs.forgeAutoRenamingTool) {
        exclude(group = "net.sf.jopt-simple")
        asmExclusions.forEach { exclude(group = it.get().group, module = it.get().name) } // Use our own ASM version
    }
    mlTransformersConfig.name(rootProject.project(":modlauncher-transformers"))

    // Add the API as a runtime dependency, just so it gets shaded into the jar
    add(vanillaInstaller.runtimeOnlyConfigurationName, "org.spongepowered:spongeapi:$apiVersion") {
        isTransitive = false
    }

    val appLaunch = vanillaAppLaunchConfig.name

    val bootstrapLibraries = vanillaBootstrapLibrariesConfig.name
    val libraries = vanillaLibrariesConfig.name

    // Libraries only needed on the TCL (during main game lifecycle)

    libraries("org.spongepowered:spongeapi:$apiVersion")
    libraries(platform(apiLibs.adventure.bom)) {
        exclude(group = "org.jetbrains", module = "annotations")
    }
    libraries(libs.adventure.serializerConfigurate4) {
        exclude(group = "org.checkerframework", module = "checker-qual")
    }
    libraries(libs.javaxInject)
    libraries(libs.configurate.jackson) {
        exclude(group = "org.spongepowered", module = "configurate-core")
        exclude(group = "org.checkerframework", module = "checker-qual")
    }

    libraries(libs.adventure.serializerAnsi) {
        exclude(group = "org.jetbrains", module = "annotations")
        exclude(group = "org.checkerframework", module = "checker-qual")
    }

    // Libraries needed during applaunch phase and runtime
    bootstrapLibraries(libs.terminalConsoleAppender) {
        exclude(group = "org.jline", module = "jline-reader")
        exclude(group = "org.apache.logging.log4j", module = "log4j-core")
    }
    bootstrapLibraries(apiLibs.checkerQual)
    bootstrapLibraries(libs.jline.terminal)
    bootstrapLibraries(libs.jline.reader)
    bootstrapLibraries(libs.jline.terminalJansi)
    // Must be on the base ClassLoader since ModLauncher has a dependency on log4j
    bootstrapLibraries(libs.log4j.jpl)

    bootstrapLibraries(platform(apiLibs.configurate.bom))
    bootstrapLibraries(apiLibs.configurate.core) {
        exclude(group = "org.checkerframework", module = "checker-qual")
    }
    bootstrapLibraries(apiLibs.configurate.hocon) {
        exclude(group = "org.spongepowered", module = "configurate-core")
        exclude(group = "org.checkerframework", module = "checker-qual")
    }
    bootstrapLibraries(libs.log4j.api)
    bootstrapLibraries(libs.log4j.core)
    bootstrapLibraries(libs.log4j.slf4j2) {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }

    // Mixin and dependencies
    bootstrapLibraries(libs.mixin)
    bootstrapLibraries(libs.asm.util)
    bootstrapLibraries(libs.asm.tree)
    bootstrapLibraries(libs.guava) {
        exclude(group = "com.google.errorprone", module = "error_prone_annotations")
        exclude(group = "org.checkerframework", module = "checker-qual")
    }

    // Launch Dependencies - Needed to bootstrap the engine(s)
    // Not needing to be source-visible past the init phase
    // The ModLauncher compatibility launch layer
    appLaunch(libs.modlauncher) {
        exclude(group = "org.ow2.asm")
        exclude(group = "org.apache.logging.log4j")
        exclude(group = "net.sf.jopt-simple") // uses a newer version than MC
    }
    appLaunch(libs.asm.commons)
    appLaunch(libs.grossJava9Hacks) {
        exclude(group = "org.apache.logging.log4j")
    }
    appLaunch(apiLibs.pluginSpi) {
        exclude(group = "org.checkerframework", module = "checker-qual")
        exclude(group = "com.google.code.gson", module = "gson")
        exclude(group = "org.apache.logging.log4j", module = "log4j-api")
        exclude(group = "org.apache.commons", module = "commons-lang3")
    }
    appLaunch(libs.lmaxDisruptor)

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

vanillaLaunch.apply {
    blossom.resources {
        property("apiVersion", apiVersion)
        property("minecraftVersion", minecraftVersion)
        property("version", provider { project.version.toString() })
    }
}
vanillaInstaller.apply {
    blossom.javaSources {
        property("minecraftVersion", minecraftVersion)
    }
}

tasks {
    jar {
        manifest.from(vanillaManifest)
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
    val vanillaAccessorsJar by registering(Jar::class) {
        archiveClassifier.set("accessors")
        manifest.from(vanillaManifest)
        from(vanillaAccessors.output)
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

    val installerResources = project.layout.buildDirectory.dir("generated/resources/installer")
    vanillaInstaller.resources.srcDir(installerResources)

    val downloadNotNeeded = configurations.register("downloadNotNeeded") {
        extendsFrom(configurations.minecraft.get())
        extendsFrom(vanillaInstallerConfig.get())
    }

    val emitDependencies by registering(org.spongepowered.gradle.impl.OutputDependenciesToJson::class) {
        group = "sponge"
        // everything in applaunch goes -> bootstrap, everything in libraries goes -> main (MC is inserted here too)
        this.dependencies("bootstrap", vanillaAppLaunchConfig)
        this.dependencies("main", vanillaLibrariesConfig)
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
                    "Superclass-Transformer" to "common.superclasschange,vanilla.superclasschange",
                    "Access-Widener" to "common.accesswidener",
                    "MixinConfigs" to mixinConfigs.joinToString(","),
                    "Main-Class" to "org.spongepowered.vanilla.installer.InstallerMain",
                    "Launch-Target" to "sponge_server_prod",
                    "Multi-Release" to true,
                    "Premain-Class" to "org.spongepowered.vanilla.installer.Agent",
                    "Agent-Class" to "org.spongepowered.vanilla.installer.Agent",
                    "Launcher-Agent-Class" to "org.spongepowered.vanilla.installer.Agent"
            ))
            attributes(
                mapOf("Implementation-Version" to libs.versions.asm.get()),
                "org/objectweb/asm/"
            )
            from(vanillaManifest)
        }
        from(commonProject.sourceSets.main.map { it.output })
        from(commonProject.sourceSets.named("mixins").map {it.output })
        from(commonProject.sourceSets.named("accessors").map {it.output })
        from(commonProject.sourceSets.named("launch").map {it.output })
        from(commonProject.sourceSets.named("applaunch").map {it.output })
        from(sourceSets.main.map {it.output })
        from(vanillaInstaller.output)
        from(vanillaAppLaunch.output)
        from(vanillaLaunch.output)
        from(vanillaAccessors.output)
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
val vanillaAccessorsJar by tasks.existing
val vanillaMixinsJar by tasks.existing

publishing {
    publications {
        register("sponge", MavenPublication::class) {

            artifact(shadowJar.get())
            artifact(vanillaInstallerJar.get())
            artifact(vanillaAppLaunchJar.get())
            artifact(vanillaLaunchJar.get())
            artifact(vanillaAccessorsJar.get())
            artifact(vanillaMixinsJar.get())
            artifact(tasks["applaunchSourcesJar"])
            artifact(tasks["launchSourcesJar"])
            artifact(tasks["accessorsSourcesJar"])
            artifact(tasks["mixinsSourcesJar"])
            pom {
                artifactId = project.name.lowercase()
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
