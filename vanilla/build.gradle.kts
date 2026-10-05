import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.spongepowered.gradle.impl.IdeHelper

plugins {
    id("org.spongepowered.gradle.vanilla")
    alias(libs.plugins.shadow)
    id("implementation-structure")
    alias(libs.plugins.blossom)
    jacoco
}

val commonProject = parent!!
val bootstrapProject = commonProject.project(":bootstrap")
val libraryManagerProject = commonProject.project(":library-manager")
val testPluginsProject: Project? = rootProject.subprojects.find { "testplugins" == it.name }

val apiVersion: String by project
val apiJavaTarget: String by project
val minecraftVersion: String by project
val recommendedVersion: String by project
val projectUrl: String by project

description = "The SpongeAPI implementation for Vanilla Minecraft"
version = spongeImpl.generatePlatformBuildVersionString(apiVersion, minecraftVersion, recommendedVersion)

// SpongeVanilla libraries
val installerLibrariesConfig = configurations.register("installerLibraries")
val bootLibrariesConfig = configurations.register("bootLibraries")
val gameLibrariesConfig = configurations.register("gameLibraries") {
    extendsFrom(configurations.minecraft.get())
}

val gameManagedLibrariesConfig = configurations.register("gameManagedLibraries")

val bootShadedLibrariesConfig = configurations.register("bootShadedLibraries")
val gameShadedLibrariesConfig = configurations.register("gameShadedLibraries")

val testModulesConfig = configurations.register("testModules")

// ModLauncher layers
val bootLayerConfig = configurations.register("bootLayer") {
    extendsFrom(bootLibrariesConfig.get())
}
val gameLayerConfig = configurations.register("gameLayer") {
    extendsFrom(bootLayerConfig.get())
    extendsFrom(gameLibrariesConfig.get())
}

// Bootstrap source sets
val bootstrapMain = bootstrapProject.sourceSets.named("main")
val bootstrapForge = bootstrapProject.sourceSets.named("forge")

// SpongeCommon source sets
val commonAccessors = commonProject.sourceSets.named("accessors")
val commonAppLaunch = commonProject.sourceSets.named("applaunch")
val commonAppLaunchConf = commonProject.sourceSets.named("applaunchConfig")
val commonMixins = commonProject.sourceSets.named("mixins")
val commonMain = commonProject.sourceSets.named("main")
val commonTest = commonProject.sourceSets.named("test")

// SpongeVanilla source sets
// Prod launch
val installer by sourceSets.register("installer") {
    spongeImpl.addDependencyToImplementation(commonAppLaunchConf.get(), this)

    spongeImpl.addDependencyToImplementation(bootstrapMain.get(), this)
    spongeImpl.addDependencyToImplementation(bootstrapForge.get(), this)

    configurations.named(implementationConfigurationName) {
        extendsFrom(installerLibrariesConfig.get())
    }
}

// Boot layer
val appLaunch by sourceSets.register("applaunch") {
    spongeImpl.addDependencyToImplementation(commonAppLaunchConf.get(), this)
    spongeImpl.addDependencyToImplementation(commonAppLaunch.get(), this)

    configurations.named(implementationConfigurationName) {
        extendsFrom(bootLayerConfig.get())
    }
}

// Game layer
val accessors by sourceSets.register("accessors") {
    spongeImpl.addDependencyToImplementation(commonAccessors.get(), this)

    configurations.named(implementationConfigurationName) {
        extendsFrom(gameLayerConfig.get())
    }
}
val mixins by sourceSets.register("mixins") {
    spongeImpl.addDependencyToImplementation(commonAppLaunchConf.get(), this)
    spongeImpl.addDependencyToImplementation(commonAppLaunch.get(), this)
    spongeImpl.addDependencyToImplementation(commonAccessors.get(), this)
    spongeImpl.addDependencyToImplementation(commonMixins.get(), this)
    spongeImpl.addDependencyToImplementation(commonMain.get(), this)
    spongeImpl.addDependencyToImplementation(appLaunch, this)
    spongeImpl.addDependencyToImplementation(accessors, this)

    configurations.named(implementationConfigurationName) {
        extendsFrom(gameLayerConfig.get())
    }
}
val main by sourceSets.named("main") {
    spongeImpl.addDependencyToImplementation(commonAppLaunchConf.get(), this)
    spongeImpl.addDependencyToImplementation(commonAppLaunch.get(), this)
    spongeImpl.addDependencyToImplementation(commonAccessors.get(), this)
    spongeImpl.addDependencyToImplementation(commonMain.get(), this)
    spongeImpl.addDependencyToImplementation(appLaunch, this)
    spongeImpl.addDependencyToImplementation(accessors, this)

    spongeImpl.addDependencyToImplementation(this, mixins)

    configurations.named(implementationConfigurationName) {
        extendsFrom(gameLayerConfig.get())
    }

    // The rest of the project because we want everything in the initial classpath
    spongeImpl.addDependencyToRuntimeOnly(commonMixins.get(), this)
    spongeImpl.addDependencyToRuntimeOnly(mixins, this)

    // The bootstrap
    spongeImpl.addDependencyToRuntimeOnly(bootstrapMain.get(), this)
    spongeImpl.addDependencyToRuntimeOnly(bootstrapForge.get(), this)
}
val testSources = sourceSets.named("test") {
    spongeImpl.addDependencyToImplementation(commonTest.get(), this)

    spongeImpl.addDependencyToImplementation(bootstrapMain.get(), this)
    spongeImpl.addDependencyToImplementation(bootstrapForge.get(), this)

    configurations.named(implementationConfigurationName) {
        extendsFrom(testModulesConfig.get())
    }
}

val mixinConfigs = spongeImpl.mixinConfigurations

minecraft {
    accessWideners(commonMain.get().resources.filter { it.name.endsWith(".accesswidener") })
    accessWideners(main.resources.filter { it.name.endsWith(".accesswidener") })
}

configurations.configureEach {
    // Force jopt-simple to be exactly 5.0.4 because Mojang ships that version, but some transitive dependencies request 6.0+
    resolutionStrategy {
        force("net.sf.jopt-simple:jopt-simple:5.0.4")
    }
}

configurations.testRuntimeOnly {
    exclude(module = "testplugins")
}

dependencies {
    val installer = installerLibrariesConfig.name
    installer(libs.securemodules)
    installer(libs.asm.commons)
    installer(libs.asm.util)
    installer(libs.jarjar.fs)

    installer(apiLibs.gson)
    installer(apiLibs.checkerQual)
    installer(libs.joptSimple)
    installer(libs.tinylog.api)
    installer(libs.tinylog.impl)

    installer(project(libraryManagerProject.path))

    // optional at runtime
    "installerCompileOnly"(platform(apiLibs.junit.bom))
    "installerCompileOnly"(apiLibs.junit.launcher)

    val boot = bootLibrariesConfig.name
    boot(libs.securemodules)
    boot(libs.asm.commons)
    boot(libs.asm.util)

    boot(libs.modlauncher) {
        exclude(group = "org.apache.logging.log4j")
    }
    boot(apiLibs.pluginSpi)
    boot(libs.lmaxDisruptor)
    boot(apiLibs.checkerQual)

    boot(libs.terminalConsoleAppender) {
        exclude(group = "org.jline", module = "jline-reader")
        exclude(group = "org.apache.logging.log4j", module = "log4j-core")
    }
    boot(libs.jline.terminal)
    boot(libs.jline.reader)
    boot(libs.jline.terminalJansi)

    boot(libs.log4j.jpl)
    boot(libs.log4j.api)
    boot(libs.log4j.core)
    boot(libs.log4j.slf4j2)

    boot(libs.accessWidener)
    boot(libs.mixin)
    boot(libs.mixinextras.common)
    boot(libs.asm.tree)
    boot(libs.guava) {
        exclude(group = "com.google.errorprone", module = "error_prone_annotations")
        exclude(group = "org.checkerframework", module = "checker-qual")
    }

    // All minecraft dependencies except itself
    spongeImpl.copyModulesExcludingPrefix(configurations.minecraft.get(), "net.minecraft", "joined", bootLibrariesConfig.get())

    val game = gameLibrariesConfig.name
    game("org.spongepowered:spongeapi:$apiVersion")
    game(libs.javaxInject)
    game(platform(apiLibs.adventure.bom))
    game(libs.adventure.serializerConfigurate4)
    game(libs.adventure.serializerAnsi)

    val gameShadedLibraries = gameShadedLibrariesConfig.name
    gameShadedLibraries("org.spongepowered:spongeapi:$apiVersion") { isTransitive = false }

    spongeImpl.copyModulesExcludingProvided(gameLibrariesConfig.get(), bootLayerConfig.get(), gameManagedLibrariesConfig.get())

    testPluginsProject?.also {
        runtimeOnly(project(it.path))
    }

    testImplementation(platform(apiLibs.junit.bom))
    testImplementation(apiLibs.junit.api)
    testImplementation(apiLibs.junit.params)
    testImplementation(apiLibs.junit.launcher)
    testRuntimeOnly(apiLibs.junit.engine)

    val test = testModulesConfig.name
    test(apiLibs.mockito)

    testRuntimeOnly(libs.jacoco.core) {
        exclude(group = "org.ow2.asm")
    }
}

minecraft {
    runs {
        // Full development environment
        server() {
            args("--nogui", "--launchTarget", "sponge_server_dev")
        }
        client() {
            args("--launchTarget", "sponge_client_dev")
        }

        // Lightweight integration tests
        server("integrationTestServer") {
            args("--launchTarget", "sponge_server_it")
        }
        client("integrationTestClient") {
            args("--launchTarget", "sponge_client_it")
        }

        // Configure bootstrap dev
        val bootFileNames = spongeImpl.buildRuntimeFileNames(bootLayerConfig.get())
        val gameShadedFileNames = spongeImpl.buildRuntimeFileNames(gameShadedLibrariesConfig.get())

        configureEach {
            targetVersion(apiJavaTarget.toInt())
            workingDirectory(project.file("run/"))

            if (IdeHelper.isIdeaActive()) {
                // IntelliJ does not properly report its compatibility
                jvmArgs("-Dterminal.ansi=true", "-Djansi.mode=force")
            }

            jvmArgs(
                "-Dlog4j.configurationFile=log4j2_dev.xml",
                "-Dmixin.dumpTargetOnFailure=true",
                // "-Dmixin.debug.verbose=true",
                "-Dmixin.debug.countInjections=true",
                "-Dmixin.debug.strict=true"
            )

            allArgumentProviders += CommandLineArgumentProvider {
                mixinConfigs.asSequence()
                    .flatMap { sequenceOf("--mixin.config", it) }
                    .toList()
            }

            // ModLauncher
            // jvmArgs("-Dsponge.bootstrap.debug=true") // Uncomment to debug bootstrap classpath
            mainClass("org.spongepowered.bootstrap.forge.VanillaBootstrap")

            // Configure resources
            jvmArgs("-Dsponge.dev.root=" + project.rootDir)
            jvmArgs("-Dsponge.dev.boot=$bootFileNames")
            jvmArgs("-Dsponge.dev.gameShaded=$gameShadedFileNames")
        }
    }
}

val vanillaManifest = java.manifest {
    attributes(
        "Specification-Title" to "SpongeVanilla",
        "Specification-Vendor" to "SpongePowered",
        "Specification-Version" to apiVersion,
        "Implementation-Title" to project.name,
        "Implementation-Version" to version,
        "Implementation-Vendor" to "SpongePowered"
    )
    // These two are included by most CI's
    System.getenv()["GIT_COMMIT"]?.apply { attributes("Git-Commit" to this) }
    System.getenv()["GIT_BRANCH"]?.apply { attributes("Git-Branch" to this) }
}

appLaunch.apply {
    blossom.resources {
        property("minecraftVersion", minecraftVersion)
    }
}
main.apply {
    blossom.resources {
        property("apiVersion", apiVersion)
        property("version", version.toString())
        property("description", description.toString())
        property("minecraftVersion", minecraftVersion)
    }
}
installer.apply {
    blossom.javaSources {
        property("minecraftVersion", minecraftVersion)
    }
}

sourceSets.configureEach {
    val sourceSet = this
    if (sourceSet.name != "main") {
        tasks.register(sourceSet.name + "Jar", Jar::class.java) {
            group = "build"
            archiveClassifier.set(sourceSet.name)
            manifest.from(vanillaManifest)
            from(sourceSet.output)
        }
    }
}

tasks {
    jar {
        manifest.from(vanillaManifest)
    }

    val installerJar by existing(Jar::class) {
        manifest.attributes(
            "Main-Class" to "org.spongepowered.vanilla.installer.InstallerMain",
            "Multi-Release" to true
        )
    }

    val integrationTest by registering {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        dependsOn("integrationTestServer", "integrationTestClient")
    }

    val installerResources = project.layout.buildDirectory.dir("generated/resources/installer")
    installer.resources.srcDir(installerResources)

    val emitDependencies by registering(org.spongepowered.gradle.impl.OutputDependenciesToJson::class) {
        group = "sponge"
        this.dependencies("bootstrap", bootLibrariesConfig)
        this.dependencies("main", gameManagedLibrariesConfig)
        this.excludeDependencies(configurations.minecraft)
        this.excludeDependencies(gameShadedLibrariesConfig)

        outputFile.set(installerResources.map { it.file("sponge-libraries.json") })
    }

    named(installer.processResourcesTaskName) {
        dependsOn(emitDependencies)
    }

    val bootShadowJar by register("bootShadowJar", ShadowJar::class) {
        group = "build"
        archiveClassifier.set("boot")

        mergeServiceFiles()
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        configurations = listOf(bootShadedLibrariesConfig.get())

        manifest {
            from(vanillaManifest)
            attributes("Automatic-Module-Name" to "spongevanilla.boot")
        }

        from(commonAppLaunchConf.map { it.output })
        from(commonAppLaunch.map { it.output })
        from(appLaunch.output)
    }

    val installerShadowJar by register("installerShadowJar", ShadowJar::class) {
        group = "build"
        archiveClassifier.set("installer-shadow")

        mergeServiceFiles()
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        configurations = listOf(installerLibrariesConfig.get())
        exclude("META-INF/INDEX.LIST", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "**/module-info.class")

        manifest {
            from(vanillaManifest)
            attributes(
                "Premain-Class" to "org.spongepowered.vanilla.installer.Agent",
                "Main-Class" to "org.spongepowered.vanilla.installer.InstallerMain",
                "Automatic-Module-Name" to "spongevanilla.installer",
                "Launch-Target" to "sponge_server_prod",
                "Multi-Release" to true
            )
            attributes(mapOf("Implementation-Version" to libs.versions.asm.get()), "org/objectweb/asm/")
        }

        from(commonAppLaunchConf.map { it.output })
        from(installer.output)
        from(bootstrapMain.map { it.output })
        from(bootstrapForge.map { it.output })

        exclude("org/spongepowered/bootstrap/dev")
    }

    shadowJar {
        archiveClassifier.set("mod")

        mergeServiceFiles()
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        configurations = listOf(gameShadedLibrariesConfig.get())

        manifest {
            from(vanillaManifest)
            attributes(
                "Superclass-Transformer" to "common.superclasschange,vanilla.superclasschange",
                "Access-Widener" to "common.accesswidener",
                "MixinConfigs" to mixinConfigs.joinToString(","),
                "Multi-Release" to true
            )
        }

        from(commonMain.map { it.output })
        from(commonMixins.map { it.output })
        from(commonAccessors.map { it.output })

        from(accessors.output)
        from(mixins.output)
    }

    val universalJar = register("universalJar", Jar::class) {
        group = "build"
        archiveClassifier.set("universal")

        manifest.from(installerShadowJar.manifest)

        from(installerShadowJar.archiveFile.map { zipTree(it) })

        into("jars") {
            from(shadowJar)
            rename("spongevanilla-(.*)-mod.jar", "spongevanilla-mod.jar")

            from(bootShadowJar)
            rename("spongevanilla-(.*)-boot.jar", "spongevanilla-boot.jar")
        }
    }

    assemble {
        dependsOn(universalJar)
    }

    test {
        useJUnitPlatform()

        maxHeapSize = "4G"
        testClassesDirs = commonTest.get().output.classesDirs + testSources.get().output.classesDirs

        val runServer = minecraft.runs.server().get()
        jvmArgs(runServer.allJvmArguments())
        jvmArgs("--module-path=" + testModulesConfig.get().asPath)
        jvmArgs("--add-modules=net.bytebuddy.agent,net.bytebuddy")
        jvmArgs("-Dsponge.test.args=" + runServer.allArguments().joinToString(" "))
        jvmArgs("-Dsponge.jacoco.packages=org.spongepowered")
        jvmArgs("-Djunit.platform.launcher.interceptors.enabled=true")
        jvmArgs("-Djunit.jupiter.extensions.autodetection.enabled=true")
        workingDir = layout.buildDirectory.dir("test-run").get().asFile

        doFirst {
            // reset test directory
            workingDir.deleteRecursively()
            workingDir.mkdirs()
            workingDir.resolve("eula.txt").writeText("eula=true")
        }

        extensions.configure(JacocoTaskExtension::class) {
            excludeClassLoaders = listOf("cpw.mods.modlauncher.TransformingClassLoader")
        }

        finalizedBy(jacocoTestReport)
    }

    jacocoTestReport {
        sourceSets(commonAppLaunchConf.get(), commonAppLaunch.get(), commonAccessors.get(), commonMixins.get(), commonMain.get())
        sourceSets(appLaunch, accessors, mixins, main)
        dependsOn(test)
    }
}

publishing {
    publications {
        register("sponge", MavenPublication::class) {
            artifact(tasks["universalJar"])

            artifact(tasks["jar"])
            artifact(tasks["sourcesJar"])

            artifact(tasks["installerJar"])
            artifact(tasks["installerSourcesJar"])

            artifact(tasks["mixinsJar"])
            artifact(tasks["mixinsSourcesJar"])

            artifact(tasks["accessorsJar"])
            artifact(tasks["accessorsSourcesJar"])

            artifact(tasks["applaunchJar"])
            artifact(tasks["applaunchSourcesJar"])

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
