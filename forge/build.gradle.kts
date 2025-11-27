import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.minecraftforge.gradle.common.util.RunConfig
import org.gradle.internal.DefaultTaskExecutionRequest
import org.spongepowered.gradle.impl.AWToAT
import org.spongepowered.gradle.impl.IdeHelper

buildscript {
    repositories {
        maven("https://repo.spongepowered.org/repository/maven-public/") {
            name = "sponge"
        }
        maven("https://maven.minecraftforge.net/") {
            name = "forge"
        }
    }
}

plugins {
    alias(libs.plugins.shadow)
    id("implementation-structure")
    alias(libs.plugins.blossom)
    alias(libs.plugins.forgeGradle)
    jacoco
}

val commonProject = parent!!
val bootstrapProject = commonProject.project(":bootstrap")
val transformersProject = commonProject.project(":modlauncher-transformers")
val libraryManagerProject = commonProject.project(":library-manager")
val testPluginsProject: Project? = rootProject.subprojects.find { "testplugins" == it.name }

val apiVersion: String by project
val minecraftVersion: String by project
val forgeVersion: String by project
val recommendedVersion: String by project
val projectUrl: String by project

description = "The SpongeAPI implementation for MinecraftForge"
version = spongeImpl.generatePlatformBuildVersionString(apiVersion, minecraftVersion, recommendedVersion, forgeVersion)

repositories {
    maven("https://repo.spongepowered.org/repository/maven-public/") {
        name = "sponge"
    }
}

// SpongeForge libraries
val bootLibrariesConfig = configurations.register("bootLibraries") {
    // Ideally we would filter minecraft itself and only keep its dependencies for this layer,
    // but I couldn't find a way to do it without breaking ForgeGradle.
    extendsFrom(configurations.minecraft.get())
}
val serviceLibrariesConfig = configurations.register("serviceLibraries")
val gameLibrariesConfig = configurations.register("gameLibraries")

val gameManagedLibrariesConfig = configurations.register("gameManagedLibraries")

val serviceShadedLibrariesConfig = configurations.register("serviceShadedLibraries")
val gameShadedLibrariesConfig = configurations.register("gameShadedLibraries")

val productionExcludedLibrariesConfig = configurations.register("productionExcludedLibraries")

// ModLauncher layers
val bootLayerConfig = configurations.register("bootLayer") {
    extendsFrom(bootLibrariesConfig.get())
}
val serviceLayerConfig = configurations.register("serviceLayer") {
    extendsFrom(bootLayerConfig.get())
    extendsFrom(serviceLibrariesConfig.get())
}
val langLayerConfig = configurations.register("langLayer") {
    extendsFrom(bootLayerConfig.get())
}
val gameLayerConfig = configurations.register("gameLayer") {
    extendsFrom(serviceLayerConfig.get())
    extendsFrom(langLayerConfig.get())
    extendsFrom(gameLibrariesConfig.get())
}

// Bootstrap source sets
val bootstrapMain = bootstrapProject.sourceSets.named("main")
val bootstrapForge = bootstrapProject.sourceSets.named("forge")

// SpongeCommon source sets
val commonAccessors = commonProject.sourceSets.named("accessors")
val commonLaunch = commonProject.sourceSets.named("launch")
val commonAppLaunch = commonProject.sourceSets.named("applaunch")
val commonAppLaunchConf = commonProject.sourceSets.named("applaunchConfig")
val commonMixins = commonProject.sourceSets.named("mixins")
val commonMain = commonProject.sourceSets.named("main")
val commonTest = commonProject.sourceSets.named("test")

// SpongeForge source sets
// Service layer
val appLaunch by sourceSets.register("applaunch") {
    spongeImpl.addDependencyToImplementation(commonAppLaunchConf.get(), this)
    spongeImpl.addDependencyToImplementation(commonAppLaunch.get(), this)

    configurations.named(implementationConfigurationName) {
        extendsFrom(serviceLayerConfig.get())
    }
}

// Lang layer
val lang by sourceSets.register("lang") {
    configurations.named(implementationConfigurationName) {
        extendsFrom(langLayerConfig.get())
    }
}

// Game layer
val launch by sourceSets.register("launch") {
    spongeImpl.addDependencyToImplementation(commonAppLaunchConf.get(), this)
    spongeImpl.addDependencyToImplementation(commonAppLaunch.get(), this)
    spongeImpl.addDependencyToImplementation(commonLaunch.get(), this)
    spongeImpl.addDependencyToImplementation(commonMain.get(), this)
    spongeImpl.addDependencyToImplementation(appLaunch, this)

    configurations.named(implementationConfigurationName) {
        extendsFrom(gameLayerConfig.get())
    }
}
val accessors by sourceSets.register("accessors") {
    spongeImpl.addDependencyToImplementation(commonAccessors.get(), this)

    configurations.named(implementationConfigurationName) {
        extendsFrom(gameLayerConfig.get())
    }
}
val mixins by sourceSets.register("mixins") {
    spongeImpl.addDependencyToImplementation(commonAppLaunchConf.get(), this)
    spongeImpl.addDependencyToImplementation(commonAppLaunch.get(), this)
    spongeImpl.addDependencyToImplementation(commonLaunch.get(), this)
    spongeImpl.addDependencyToImplementation(commonAccessors.get(), this)
    spongeImpl.addDependencyToImplementation(commonMixins.get(), this)
    spongeImpl.addDependencyToImplementation(commonMain.get(), this)
    spongeImpl.addDependencyToImplementation(appLaunch, this)
    spongeImpl.addDependencyToImplementation(launch, this)
    spongeImpl.addDependencyToImplementation(accessors, this)

    configurations.named(implementationConfigurationName) {
        extendsFrom(gameLayerConfig.get())
    }
}
val main by sourceSets.named("main") {
    spongeImpl.addDependencyToImplementation(commonAppLaunchConf.get(), this)
    spongeImpl.addDependencyToImplementation(commonAppLaunch.get(), this)
    spongeImpl.addDependencyToImplementation(commonLaunch.get(), this)
    spongeImpl.addDependencyToImplementation(commonAccessors.get(), this)
    spongeImpl.addDependencyToImplementation(commonMain.get(), this)
    spongeImpl.addDependencyToImplementation(appLaunch, this)
    spongeImpl.addDependencyToImplementation(launch, this)
    spongeImpl.addDependencyToImplementation(accessors, this)

    spongeImpl.addDependencyToImplementation(this, mixins)

    configurations.named(implementationConfigurationName) {
        extendsFrom(gameLayerConfig.get())
    }

    // The rest of the project because we want everything in the initial classpath
    spongeImpl.addDependencyToRuntimeOnly(commonMixins.get(), this)
    spongeImpl.addDependencyToRuntimeOnly(mixins, this)
    spongeImpl.addDependencyToRuntimeOnly(lang, this)

    // The bootstrap
    spongeImpl.addDependencyToRuntimeOnly(bootstrapMain.get(), this)
    spongeImpl.addDependencyToRuntimeOnly(bootstrapForge.get(), this)
}
val testSources = sourceSets.named("test") {
    spongeImpl.addDependencyToImplementation(commonTest.get(), this)

    spongeImpl.addDependencyToImplementation(bootstrapMain.get(), this)
    spongeImpl.addDependencyToImplementation(bootstrapForge.get(), this)
}

configurations.configureEach {
    // Fix that can be found in Forge MDK too
    resolutionStrategy {
        force("net.sf.jopt-simple:jopt-simple:5.0.4")
    }
}

configurations.testRuntimeOnly {
    exclude(module = "testplugins")
}

dependencies {
    "minecraft"("net.minecraftforge:forge:$minecraftVersion-$forgeVersion")

    val service = serviceLibrariesConfig.name
    service(apiLibs.pluginSpi)
    service(project(transformersProject.path)) {
        exclude(group = "cpw.mods", module = "modlauncher")
    }
    service(project(libraryManagerProject.path))

    val game = gameLibrariesConfig.name
    game("org.spongepowered:spongeapi:$apiVersion")
    game(libs.javaxInject)
    game(platform(apiLibs.adventure.bom))
    game(libs.adventure.serializerConfigurate4)
    game(libs.mixinextras.common)

    val serviceShadedLibraries = serviceShadedLibrariesConfig.name
    serviceShadedLibraries(project(transformersProject.path)) { isTransitive = false }
    serviceShadedLibraries(project(libraryManagerProject.path)) { isTransitive = false }

    val gameShadedLibraries = gameShadedLibrariesConfig.name
    gameShadedLibraries("org.spongepowered:spongeapi:$apiVersion") { isTransitive = false }

    val gameManaged = gameManagedLibrariesConfig.name
    gameManaged(libs.mixinextras.forge) // prod only

    val productionExcluded = productionExcludedLibrariesConfig.name
    productionExcluded(libs.mixinextras.common) // dev only

    afterEvaluate {
        spongeImpl.copyModulesExcludingProvided(serviceLibrariesConfig.get(), bootLayerConfig.get(), serviceShadedLibrariesConfig.get())
        spongeImpl.copyModulesExcludingProvided(gameLibrariesConfig.get(), serviceLayerConfig.get(), gameManagedLibrariesConfig.get())
    }

    testPluginsProject?.also {
        runtimeOnly(project(it.path))
    }

    testImplementation(platform(apiLibs.junit.bom))
    testImplementation(apiLibs.junit.api)
    testImplementation(apiLibs.junit.params)
    testImplementation(apiLibs.junit.launcher)
    testRuntimeOnly(apiLibs.junit.engine)

    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junitJupiter) {
        exclude(group = "org.junit.jupiter", module = "junit-jupiter-api")
    }

    testRuntimeOnly(libs.jacoco.core) {
        exclude(group = "org.ow2.asm")
    }
}

val awFiles: Set<File> = files(commonMain.get().resources, main.resources).filter { it.name.endsWith(".accesswidener") }.files
val atFile = project.layout.buildDirectory.file("generated/resources/at.cfg").get().asFile
AWToAT.convert(awFiles, atFile)

val mixinConfigs: MutableSet<String> = spongeImpl.mixinConfigurations

minecraft {
    mappings("official", "1.21.4")
    accessTransformers.from(atFile)
    reobf = false

    runs {
        configureEach {
            ideaModule("Sponge.SpongeForge.main")

            // jvmArgs("-Dsponge.bootstrap.debug=true") // Uncomment to debug bootstrap classpath
            main("org.spongepowered.bootstrap.forge.ForgeBootstrap")

            args(mixinConfigs.flatMap { sequenceOf("--mixin.config", it) })
            environment("MOD_CLASSES", "nop")
        }

        create("client")

        create("server") {
            args("--nogui")
        }
    }
}

afterEvaluate {
    minecraft {
        // Configure bootstrap dev
        val bootFileNames = spongeImpl.buildRuntimeFileNames(serviceLayerConfig.get()) // service in boot during dev
        val gameShadedFileNames = spongeImpl.buildRuntimeFileNames(gameShadedLibrariesConfig.get())
        runs.configureEach {
            jvmArgs("-Dsponge.dev.root=" + project.rootDir)
            jvmArgs("-Dsponge.dev.boot=$bootFileNames")
            jvmArgs("-Dsponge.dev.gameShaded=$gameShadedFileNames")
        }
    }
}

val forgeManifest = java.manifest {
    attributes(
            "Specification-Title" to "SpongeForge",
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

sourceSets {
    main {
        blossom.resources {
            property("apiVersion", apiVersion)
            property("version", version.toString())
            property("description", description.toString())
            property("forgeVersion", forgeVersion)
        }
    }

    configureEach {
        val sourceSet = this
        if (sourceSet.name != "main") {
            tasks.register(sourceSet.name + "Jar", Jar::class.java) {
                group = "build"
                archiveClassifier.set(sourceSet.name)
                manifest.from(forgeManifest)
                from(sourceSet.output)
            }
        }
    }
}

tasks {
    withType(JavaExec::class) {
        if (group == RunConfig.RUNS_GROUP) {
            standardInput = System.`in`
        }
    }

    jar {
        manifest.from(forgeManifest)
    }

    val langJar by existing(Jar::class) {
        manifest.attributes(
            "Automatic-Module-Name" to "spongeforge.lang",
            "FMLModType" to "LANGPROVIDER"
        )
    }

    val installerResources = project.layout.buildDirectory.dir("generated/resources/installer")
    appLaunch.resources.srcDir(installerResources)

    val emitDependencies by registering(org.spongepowered.gradle.impl.OutputDependenciesToJson::class) {
        group = "sponge"
        this.dependencies("main", gameManagedLibrariesConfig)
        this.excludeDependencies(gameShadedLibrariesConfig)
        this.excludeDependencies(productionExcludedLibrariesConfig)

        outputFile.set(installerResources.map { it.file("sponge-libraries.json") })
    }

    named(appLaunch.processResourcesTaskName) {
        dependsOn(emitDependencies)
    }

    val servicesShadowJar by register("servicesShadowJar", ShadowJar::class) {
        group = "build"
        archiveClassifier.set("services")

        mergeServiceFiles()
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        configurations = listOf(serviceShadedLibrariesConfig.get())
        exclude("META-INF/INDEX.LIST", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "**/module-info.class")

        manifest {
            from(forgeManifest)
            attributes(
                "Automatic-Module-Name" to "spongeforge.services",
                "Multi-Release" to true
            )
        }

        from(commonAppLaunchConf.map { it.output })
        from(commonAppLaunch.map { it.output })
        from(appLaunch.output)

        // Make sure to relocate access widener so that we don't conflict with other coremods
        relocate("net.fabricmc.accesswidener", "org.spongepowered.forge.libs.accesswidener")
    }

    shadowJar {
        archiveClassifier.set("mod")

        mergeServiceFiles()
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        configurations = listOf(gameShadedLibrariesConfig.get())

        manifest {
            from(forgeManifest)
            attributes(
                "Access-Widener" to "common.accesswidener",
                "Superclass-Transformer" to "common.superclasschange,forge.superclasschange",
                "MixinConfigs" to mixinConfigs.joinToString(",")
            )
        }

        from(commonMain.map { it.output })
        from(commonMixins.map { it.output })
        from(commonAccessors.map { it.output })
        from(commonLaunch.map { it.output })

        from(launch.output)
        from(accessors.output)
        from(mixins.output)
    }

    val universalJar = register("universalJar", Jar::class) {
        group = "build"
        archiveClassifier.set("universal")

        manifest.from(servicesShadowJar.manifest)

        from(servicesShadowJar.archiveFile.map { zipTree(it) })

        into("jars") {
            from(shadowJar)
            rename("spongeforge-(.*)-mod.jar", "spongeforge-mod.jar")

            from(langJar)
            rename("spongeforge-(.*)-lang.jar", "spongeforge-lang.jar")
        }
    }

    assemble {
        dependsOn(universalJar)
    }

    test {
        useJUnitPlatform()

        maxHeapSize = "4G"
        testClassesDirs = commonTest.get().output.classesDirs + testSources.get().output.classesDirs

        val runServer = minecraft.runs.getByName("server")
        jvmArgs(runServer.jvmArgs)
        jvmArgs("-Dsponge.test.args=" + runServer.args.joinToString(" "))
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
        sourceSets(commonAppLaunchConf.get(), commonAppLaunch.get(), commonLaunch.get(), commonAccessors.get(), commonMixins.get(), commonMain.get())
        sourceSets(appLaunch, launch, lang, accessors, mixins, main)
        dependsOn(test)
    }
}

if (IdeHelper.isIdeaSync()) {
    afterEvaluate {
        gradle.startParameter.taskRequests.add(DefaultTaskExecutionRequest(listOf(":SpongeForge:genIntellijRuns")))
    }
}

publishing {
    publications {
        register("sponge", MavenPublication::class) {
            artifact(tasks["universalJar"])

            artifact(tasks["jar"])
            artifact(tasks["sourcesJar"])

            artifact(tasks["langJar"])
            artifact(tasks["langSourcesJar"])

            artifact(tasks["mixinsJar"])
            artifact(tasks["mixinsSourcesJar"])

            artifact(tasks["accessorsJar"])
            artifact(tasks["accessorsSourcesJar"])

            artifact(tasks["launchJar"])
            artifact(tasks["launchSourcesJar"])

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
