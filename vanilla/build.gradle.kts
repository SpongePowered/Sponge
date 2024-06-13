plugins {
    id("org.spongepowered.gradle.vanilla")
    alias(libs.plugins.shadow)
    id("implementation-structure")
    alias(libs.plugins.blossom)
    eclipse
}

val commonProject = parent!!
val transformersProject = parent!!.project(":modlauncher-transformers")
val testPluginsProject: Project? = rootProject.subprojects.find { "testplugins" == it.name }

val apiVersion: String by project
val apiJavaTarget: String by project
val minecraftVersion: String by project
val recommendedVersion: String by project
val organization: String by project
val projectUrl: String by project

description = "The SpongeAPI implementation for Vanilla Minecraft"
version = spongeImpl.generatePlatformBuildVersionString(apiVersion, minecraftVersion, recommendedVersion)

// SpongeVanilla libraries
val installerLibrariesConfig: NamedDomainObjectProvider<Configuration> = configurations.register("spongeInstallerLibraries")
val bootstrapLibrariesConfig: NamedDomainObjectProvider<Configuration> = configurations.register("spongeBootstrapModules") // JVM initial classpath
val launcherLibrariesConfig: NamedDomainObjectProvider<Configuration> = configurations.register("spongeLauncherLibraries") // ModLauncher boot
val serviceLibrariesConfig: NamedDomainObjectProvider<Configuration> = configurations.register("spongeServiceLibraries")
val gameLibrariesConfig: NamedDomainObjectProvider<Configuration> = configurations.register("spongeGameLibraries")

val gameManagedLibrariesConfig: NamedDomainObjectProvider<Configuration> = configurations.register("spongeGameManagedLibraries")

val gameShadedLibrariesConfig: NamedDomainObjectProvider<Configuration> = configurations.register("spongeGameShadedLibraries")

val runTaskOnlyConfig: NamedDomainObjectProvider<Configuration> = configurations.register("runTaskOnly")

// ModLauncher layers
val bootLayerConfig: NamedDomainObjectProvider<Configuration> = configurations.register("bootLayer") {
    extendsFrom(bootstrapLibrariesConfig.get())
    extendsFrom(launcherLibrariesConfig.get())
}
val serviceLayerConfig: NamedDomainObjectProvider<Configuration> = configurations.register("serviceLayer") {
    extendsFrom(bootLayerConfig.get())
    extendsFrom(serviceLibrariesConfig.get())
}
val langLayerConfig: NamedDomainObjectProvider<Configuration> = configurations.register("langLayer") {
    extendsFrom(bootLayerConfig.get())
}
val gameLayerConfig: NamedDomainObjectProvider<Configuration> = configurations.register("gameLayer") {
    extendsFrom(serviceLayerConfig.get())
    extendsFrom(langLayerConfig.get())
    extendsFrom(gameLibrariesConfig.get())
    extendsFrom(configurations.minecraft.get())
}

// SpongeCommon source sets
val accessors: NamedDomainObjectProvider<SourceSet> = commonProject.sourceSets.named("accessors")
val launch: NamedDomainObjectProvider<SourceSet> = commonProject.sourceSets.named("launch")
val applaunch: NamedDomainObjectProvider<SourceSet> = commonProject.sourceSets.named("applaunch")
val mixins: NamedDomainObjectProvider<SourceSet> = commonProject.sourceSets.named("mixins")
val main: NamedDomainObjectProvider<SourceSet> = commonProject.sourceSets.named("main")

// SpongeVanilla source sets
val vanillaInstaller by sourceSets.register("installer") {
    configurations.named(implementationConfigurationName) {
        extendsFrom(installerLibrariesConfig.get())
    }
}

val vanillaMain by sourceSets.named("main") {
    // implementation (compile) dependencies
    spongeImpl.applyNamedDependencyOnOutput(commonProject, accessors.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, launch.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, applaunch.get(), this, project, this.implementationConfigurationName)

    configurations.named(implementationConfigurationName) {
        extendsFrom(gameLayerConfig.get())
    }
}
val vanillaLaunch by sourceSets.register("launch") {
    // implementation (compile) dependencies
    spongeImpl.applyNamedDependencyOnOutput(commonProject, launch.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, applaunch.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, main.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(project, this, vanillaMain, project, vanillaMain.implementationConfigurationName)

    configurations.named(implementationConfigurationName) {
        extendsFrom(gameLayerConfig.get())
    }
}
val vanillaAccessors by sourceSets.register("accessors") {
    spongeImpl.applyNamedDependencyOnOutput(commonProject, mixins.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, accessors.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(project, this, vanillaLaunch, project, vanillaLaunch.implementationConfigurationName)

    configurations.named(implementationConfigurationName) {
        extendsFrom(gameLayerConfig.get())
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
    spongeImpl.applyNamedDependencyOnOutput(project, vanillaAccessors, this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(project, vanillaLaunch, this, project, this.implementationConfigurationName)

    configurations.named(implementationConfigurationName) {
        extendsFrom(gameLayerConfig.get())
    }
}
val vanillaLang by sourceSets.register("lang") {
    configurations.named(implementationConfigurationName) {
        extendsFrom(langLayerConfig.get())
    }
}
val vanillaAppLaunch by sourceSets.register("applaunch") {
    // implementation (compile) dependencies
    spongeImpl.applyNamedDependencyOnOutput(commonProject, applaunch.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(project, this, vanillaLaunch, project, vanillaLaunch.implementationConfigurationName)

    configurations.named(implementationConfigurationName) {
        extendsFrom(serviceLayerConfig.get())
    }
}

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

            // ModLauncher
            jvmArgs("-Dbsl.debug=true") // Uncomment to debug bootstrap classpath
            mainClass("net.minecraftforge.bootstrap.ForgeBootstrap")

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
        }

        all {
            tasks.named(this.name, JavaExec::class) {
                // Put modules in boot layer
                classpath = files(
                    vanillaAppLaunch.output,
                    vanillaAppLaunch.runtimeClasspath,
                    runTaskOnlyConfig
                )

                // Merge applaunch sourcesets in a single module
                val applaunchOutputs = arrayOf(applaunch.get().output, vanillaAppLaunch.output)
                val applaunchDirs: MutableList<File> = mutableListOf()
                applaunchOutputs.forEach {
                    applaunchDirs.add(it.resourcesDir!!)
                    applaunchDirs.addAll(it.classesDirs)
                }
                environment("MOD_CLASSES", applaunchDirs.joinToString(";") { "applaunch%%$it" })
            }
        }
    }

    main.get().resources
            .filter { it.name.endsWith(".accesswidener") }
            .files
            .forEach { accessWideners(it) }

    vanillaMain.resources
            .filter { it.name.endsWith(".accesswidener") }
            .files
            .forEach { accessWideners(it) }
}

configurations.configureEach {
    // Force jopt-simple to be exactly 5.0.4 because Mojang ships that version, but some transitive dependencies request 6.0+
    resolutionStrategy {
        force("net.sf.jopt-simple:jopt-simple:5.0.4")
    }
}

dependencies {
    api(project(":", configuration = "launch"))
    implementation(project(":", configuration = "accessors"))
    implementation(project(commonProject.path))

    vanillaMixins.implementationConfigurationName(project(commonProject.path))

    val installer = installerLibrariesConfig.name
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

    // Add the API as a runtime dependency, just so it gets shaded into the jar
    add(vanillaInstaller.runtimeOnlyConfigurationName, "org.spongepowered:spongeapi:$apiVersion") {
        isTransitive = false
    }

    val bootstrap = bootstrapLibrariesConfig.name
    bootstrap(libs.bootstrap)
    bootstrap(libs.securemodules)
    bootstrap(libs.asm.commons)
    bootstrap(libs.asm.util)

    val launcher = launcherLibrariesConfig.name
    launcher(libs.modlauncher) {
        exclude(group = "org.apache.logging.log4j")
    }
    launcher(apiLibs.pluginSpi) {
        exclude(group = "org.checkerframework", module = "checker-qual")
        exclude(group = "com.google.code.gson", module = "gson")
        exclude(group = "org.apache.logging.log4j", module = "log4j-api")
        exclude(group = "org.apache.commons", module = "commons-lang3")
    }
    launcher(libs.lmaxDisruptor)
    launcher(apiLibs.checkerQual)

    launcher(libs.terminalConsoleAppender) {
        exclude(group = "org.jline", module = "jline-reader")
        exclude(group = "org.apache.logging.log4j", module = "log4j-core")
    }
    launcher(libs.jline.terminal)
    launcher(libs.jline.reader)
    launcher(libs.jline.terminalJansi)

    launcher(libs.log4j.jpl)
    launcher(libs.log4j.api)
    launcher(libs.log4j.core)
    launcher(libs.log4j.slf4j2)

    launcher(platform(apiLibs.configurate.bom))
    launcher(apiLibs.configurate.core) {
        exclude(group = "org.checkerframework", module = "checker-qual")
    }
    launcher(apiLibs.configurate.hocon) {
        exclude(group = "org.spongepowered", module = "configurate-core")
        exclude(group = "org.checkerframework", module = "checker-qual")
    }

    launcher(libs.mixin)
    launcher(libs.asm.tree)
    launcher(libs.guava) {
        exclude(group = "com.google.errorprone", module = "error_prone_annotations")
        exclude(group = "org.checkerframework", module = "checker-qual")
    }

    val service = serviceLibrariesConfig.name
    service(project(transformersProject.path))

    val game = gameLibrariesConfig.name
    game("org.spongepowered:spongeapi:$apiVersion")
    game(platform(apiLibs.adventure.bom)) {
        exclude(group = "org.jetbrains", module = "annotations")
    }
    game(libs.adventure.serializerConfigurate4) {
        exclude(group = "org.checkerframework", module = "checker-qual")
    }
    game(libs.javaxInject)
    game(libs.configurate.jackson) {
        exclude(group = "org.spongepowered", module = "configurate-core")
        exclude(group = "org.checkerframework", module = "checker-qual")
    }
    game(libs.adventure.serializerAnsi) {
        exclude(group = "org.jetbrains", module = "annotations")
        exclude(group = "org.checkerframework", module = "checker-qual")
    }

    // TODO list shaded / managed libraries

    val runTaskOnly = runTaskOnlyConfig.name
    // Allow boot layer manipulation such as merging applaunch sourcesets
    runTaskOnly("net.minecraftforge:bootstrap-dev:2.1.1")
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

    val emitDependencies by registering(org.spongepowered.gradle.impl.OutputDependenciesToJson::class) {
        group = "sponge"
        this.dependencies("bootstrap", launcherLibrariesConfig)
        this.dependencies("main", gameManagedLibrariesConfig)
        this.excludedDependencies(gameShadedLibrariesConfig)

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
