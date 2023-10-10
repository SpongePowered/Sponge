import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.LoomGradleExtension
import net.fabricmc.loom.task.RemapJarTask
import org.gradle.configurationcache.extensions.capitalized

buildscript {
    repositories {
        maven("https://repo.spongepowered.org/repository/maven-public") {
            name = "sponge"
        }
        maven("https://maven.architectury.dev/")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

plugins {
    id("com.github.johnrengelman.shadow")
    id("implementation-structure")
    id("templated-resources")
    id("net.smoofyuniverse.loom") version "1.1-SNAPSHOT"
}

val commonProject = parent!!
val transformersProject = parent!!.project(":modlauncher-transformers")
val apiVersion: String by project
val minecraftVersion: String by project
val forgeVersion: String by project
val recommendedVersion: String by project
val organization: String by project
val projectUrl: String by project

val testplugins: Project? = rootProject.subprojects.find { "testplugins".equals(it.name) }

description = "The SpongeAPI implementation for MinecraftForge"
version = spongeImpl.generatePlatformBuildVersionString(apiVersion, minecraftVersion, recommendedVersion, forgeVersion)

repositories {
    maven("https://repo.spongepowered.org/repository/maven-public/") {
        name = "sponge"
    }
}

// SpongeForge libraries
val serviceLibrariesConfig: NamedDomainObjectProvider<Configuration> = configurations.register("spongeServiceLibraries")
val gameLibrariesConfig: NamedDomainObjectProvider<Configuration> = configurations.register("spongeGameLibraries")

val gameManagedLibrariesConfig: NamedDomainObjectProvider<Configuration> = configurations.register("spongeGameManagedLibraries")

val serviceShadedLibrariesConfig: NamedDomainObjectProvider<Configuration> = configurations.register("spongeServiceShadedLibraries")
val gameShadedLibrariesConfig: NamedDomainObjectProvider<Configuration> = configurations.register("spongeGameShadedLibraries")

configurations.named("forgeRuntimeLibrary") {
    extendsFrom(serviceLibrariesConfig.get())
}

// ModLauncher layers
val serviceLayerConfig: NamedDomainObjectProvider<Configuration> = configurations.register("serviceLayer") {
    extendsFrom(serviceLibrariesConfig.get())
    extendsFrom(configurations.getByName("forgeDependencies"))
}
val langLayerConfig: NamedDomainObjectProvider<Configuration> = configurations.register("langLayer") {
    extendsFrom(configurations.getByName("forgeDependencies"))
}
val gameLayerConfig: NamedDomainObjectProvider<Configuration> = configurations.register("gameLayer") {
    extendsFrom(serviceLayerConfig.get())
    extendsFrom(langLayerConfig.get())
    extendsFrom(gameLibrariesConfig.get())

    afterEvaluate {
        extendsFrom(configurations.getByName("minecraftNamed"))
    }
}

// Common source sets and configurations
val launchConfig: NamedDomainObjectProvider<Configuration> = commonProject.configurations.named("launch")
val accessors: NamedDomainObjectProvider<SourceSet> = commonProject.sourceSets.named("accessors")
val launch: NamedDomainObjectProvider<SourceSet> = commonProject.sourceSets.named("launch")
val applaunch: NamedDomainObjectProvider<SourceSet> = commonProject.sourceSets.named("applaunch")
val mixins: NamedDomainObjectProvider<SourceSet> = commonProject.sourceSets.named("mixins")
val main: NamedDomainObjectProvider<SourceSet> = commonProject.sourceSets.named("main")
val mlTransformers: NamedDomainObjectProvider<SourceSet> = transformersProject.sourceSets.named("main")

// Forge source sets
val forgeMain by sourceSets.named("main") {
    // implementation (compile) dependencies
    spongeImpl.applyNamedDependencyOnOutput(commonProject, accessors.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, launch.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, applaunch.get(), this, project, this.implementationConfigurationName)

    configurations.named(implementationConfigurationName) {
        extendsFrom(gameLayerConfig.get())
    }
}
val forgeLaunch by sourceSets.register("launch") {
    // implementation (compile) dependencies
    spongeImpl.applyNamedDependencyOnOutput(commonProject, launch.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, applaunch.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, main.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(project, this, forgeMain, project, forgeMain.implementationConfigurationName)

    configurations.named(implementationConfigurationName) {
        extendsFrom(gameLayerConfig.get())
    }
}
val forgeAccessors by sourceSets.register("accessors") {
    spongeImpl.applyNamedDependencyOnOutput(commonProject, mixins.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(project, this, forgeLaunch, project, forgeLaunch.implementationConfigurationName)

    configurations.named(implementationConfigurationName) {
        extendsFrom(gameLayerConfig.get())
    }
}
val forgeMixins by sourceSets.register("mixins") {
    // implementation (compile) dependencies
    spongeImpl.applyNamedDependencyOnOutput(commonProject, mixins.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, accessors.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, launch.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, applaunch.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, main.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(project, forgeMain, this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(project, forgeAccessors, this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(project, forgeLaunch, this, project, this.implementationConfigurationName)

    configurations.named(implementationConfigurationName) {
        extendsFrom(gameLayerConfig.get())
    }
}
val forgeLang by sourceSets.register("lang") {
    configurations.named(implementationConfigurationName) {
        extendsFrom(langLayerConfig.get())
    }
}
val forgeAppLaunch by sourceSets.register("applaunch") {
    // implementation (compile) dependencies
    spongeImpl.applyNamedDependencyOnOutput(commonProject, applaunch.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(project, this, forgeLaunch, project, forgeLaunch.implementationConfigurationName)

    configurations.named(implementationConfigurationName) {
        extendsFrom(serviceLayerConfig.get())
    }
}

val forgeAppLaunchRuntime by configurations.named(forgeAppLaunch.runtimeOnlyConfigurationName)

configurations.configureEach {
    exclude(group = "net.minecraft", module = "joined")
    if (name != "minecraft") { // awful terrible hack sssh
        exclude(group = "com.mojang", module = "minecraft")
    }
}

sourceSets.configureEach {
    val sourceSet = this
    val isMain = "main".equals(sourceSet.name)
    val classifier = if (isMain) "sources" else (sourceSet.name + "-sources")

    val sourcesJarName: String = if (isMain) "sourcesJar" else (sourceSet.name + "SourcesJar")
    val sourcesJarTask = tasks.register(sourcesJarName, Jar::class) {
        group = "build"
        archiveClassifier.set(classifier + "-dev")
        from(sourceSet.allJava)
    }

    val remapSourcesJarName = "remap" + sourcesJarName.capitalized()

    // remapSourcesJar is already registered (but disabled) by Loom
    if (!isMain) {
        tasks.register(remapSourcesJarName, net.fabricmc.loom.task.RemapSourcesJarTask::class)
    }

    tasks.named(remapSourcesJarName, net.fabricmc.loom.task.RemapSourcesJarTask::class) {
        group = "loom"
        archiveClassifier.set(classifier)
        inputFile.set(sourcesJarTask.flatMap { it.archiveFile })
        dependsOn(sourcesJarTask)
        enabled = true
    }
}

extensions.configure(LoomGradleExtensionAPI::class) {
    silentMojangMappingsLicense()

    mixin {
        useLegacyMixinAp.set(false)
    }

    mods {
        named("main") {
            sourceSet(forgeMixins)
            sourceSet(forgeAccessors)
            sourceSet(forgeLaunch)

            configuration(gameManagedLibrariesConfig.get())
            configuration(gameShadedLibrariesConfig.get())
        }

        create("sponge") {
            sourceSet(main.get(), commonProject)
            sourceSet(mixins.get(), commonProject)
            sourceSet(accessors.get(), commonProject)
            sourceSet(launch.get(), commonProject)
        }
    }
}

dependencies {
    "minecraft"("com.mojang:minecraft:${minecraftVersion}")
    "forge"("net.minecraftforge:forge:$minecraftVersion-$forgeVersion")
    "mappings"(loom.layered {
        officialMojangMappings {
            nameSyntheticMembers = true
        }
    })

    val apiAdventureVersion: String by project
    val apiConfigurateVersion: String by project
    val apiPluginSpiVersion: String by project
    val log4jVersion: String by project

    api(project(":", configuration = "launch")) {
        exclude(group = "org.spongepowered", module = "mixin")
    }
    implementation(project(":", configuration = "accessors")) {
        exclude(group = "org.spongepowered", module = "mixin")
    }
    implementation(project(commonProject.path)) {
        exclude(group = "org.spongepowered", module = "mixin")
    }

    forgeMixins.implementationConfigurationName(project(commonProject.path))

    val serviceLibraries = serviceLibrariesConfig.name
    serviceLibraries("org.spongepowered:plugin-spi:$apiPluginSpiVersion")
    serviceLibraries(project(transformersProject.path))
    serviceLibraries(platform("org.spongepowered:configurate-bom:$apiConfigurateVersion"))
    serviceLibraries("org.spongepowered:configurate-core") {
        exclude(group = "org.checkerframework", module = "checker-qual")
    }
    serviceLibraries("org.spongepowered:configurate-hocon") {
        exclude(group = "org.spongepowered", module = "configurate-core")
        exclude(group = "org.checkerframework", module = "checker-qual")
    }
    serviceLibraries("org.spongepowered:configurate-jackson") {
        exclude(group = "org.spongepowered", module = "configurate-core")
        exclude(group = "org.checkerframework", module = "checker-qual")
    }

    val gameLibraries = gameLibrariesConfig.name
    gameLibraries("org.spongepowered:spongeapi:$apiVersion")
    gameLibraries("javax.inject:javax.inject:1")
    gameLibraries("com.zaxxer:HikariCP:2.7.8")
    gameLibraries(platform("net.kyori:adventure-bom:$apiAdventureVersion"))
    gameLibraries("net.kyori:adventure-serializer-configurate4")

    val serviceShadedLibraries = serviceShadedLibrariesConfig.name
    serviceShadedLibraries(project(transformersProject.path)) { isTransitive = false }

    val gameShadedLibraries = gameShadedLibrariesConfig.name
    gameShadedLibraries("org.spongepowered:spongeapi:$apiVersion") { isTransitive = false }

    afterEvaluate {
        spongeImpl.copyModulesExcludingProvided(serviceLibrariesConfig.get(), configurations.getByName("forgeDependencies"), serviceShadedLibrariesConfig.get())
        spongeImpl.copyModulesExcludingProvided(gameLibrariesConfig.get(), serviceLayerConfig.get(), gameManagedLibrariesConfig.get())
    }

    testplugins?.also {
        forgeAppLaunchRuntime(project(it.path)) {
            exclude(group = "org.spongepowered")
        }
    }
}

val forgeManifest = java.manifest {
    attributes(
            "Specification-Title" to "SpongeForge",
            "Specification-Vendor" to "SpongePowered",
            "Specification-Version" to apiVersion,
            "Implementation-Title" to project.name,
            "Implementation-Version" to spongeImpl.generatePlatformBuildVersionString(apiVersion, minecraftVersion, recommendedVersion, forgeVersion),
            "Implementation-Vendor" to "SpongePowered"
    )
    // These two are included by most CI's
    System.getenv()["GIT_COMMIT"]?.apply { attributes("Git-Commit" to this) }
    System.getenv()["GIT_BRANCH"]?.apply { attributes("Git-Branch" to this) }
}

val mixinConfigs: MutableSet<String> = spongeImpl.mixinConfigurations
val mods: ConfigurableFileCollection = (loom as LoomGradleExtension).unmappedModCollection

tasks {
    jar {
        manifest.from(forgeManifest)

        // Undo Loom devlibs dir
        destinationDirectory.set(project.buildDir.resolve("libs"))
    }
    val forgeAppLaunchJar by registering(Jar::class) {
        archiveClassifier.set("applaunch")
        manifest.from(forgeManifest)
        from(forgeAppLaunch.output)
    }
    val forgeLaunchJar by registering(Jar::class) {
        archiveClassifier.set("launch")
        manifest.from(forgeManifest)
        from(forgeLaunch.output)
    }
    val forgeAccessorsJar by registering(Jar::class) {
        archiveClassifier.set("accessors")
        manifest.from(forgeManifest)
        from(forgeAccessors.output)
    }
    val forgeMixinsJar by registering(Jar::class) {
        archiveClassifier.set("mixins")
        manifest.from(forgeManifest)
        from(forgeMixins.output)
    }
    val forgeLangJar by registering(Jar::class) {
        archiveClassifier.set("lang")
        manifest {
            from(forgeManifest)
            attributes("FMLModType" to "LANGPROVIDER")
        }
        from(forgeLang.output)
    }

    val forgeServicesDevJar by registering(Jar::class) {
        archiveClassifier.set("services-dev")
        manifest.from(forgeManifest)

        from(commonProject.sourceSets.named("applaunch").map { it.output })
        from(forgeAppLaunch.output)

        duplicatesStrategy = DuplicatesStrategy.WARN
    }

    afterEvaluate {
        withType(net.fabricmc.loom.task.AbstractRunTask::class) {
            classpath += files(mods, forgeServicesDevJar, forgeLangJar)

            argumentProviders += CommandLineArgumentProvider {
                mixinConfigs.asSequence()
                        .flatMap { sequenceOf("--mixin.config", it) }
                        .toList()
            }

            sourceSets.forEach {
                dependsOn(it.classesTaskName)
            }
        }
    }

    val installerResources = project.layout.buildDirectory.dir("generated/resources/installer")
    forgeAppLaunch.resources.srcDir(installerResources)

    val emitDependencies by registering(org.spongepowered.gradle.impl.OutputDependenciesToJson::class) {
        group = "sponge"
        this.dependencies("main", gameManagedLibrariesConfig)
        this.excludedDependencies(gameShadedLibrariesConfig)

        outputFile.set(installerResources.map { it.file("org/spongepowered/forge/applaunch/loading/moddiscovery/libraries.json") })
    }
    named(forgeAppLaunch.processResourcesTaskName).configure {
        dependsOn(emitDependencies)
    }

    val forgeServicesShadowJar by register("servicesShadowJar", ShadowJar::class) {
        group = "shadow"
        archiveClassifier.set("services")

        mergeServiceFiles()
        configurations = listOf(serviceShadedLibrariesConfig.get())
        exclude("META-INF/INDEX.LIST", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "module-info.class")

        manifest {
            attributes("Multi-Release" to true)
            from(forgeManifest)
        }

        from(commonProject.sourceSets.named("applaunch").map { it.output })
        from(forgeAppLaunch.output)

        // Make sure to relocate access widener so that we don't conflict with other coremods
        relocate("net.fabricmc.accesswidener", "org.spongepowered.forge.libs.accesswidener")
    }

    shadowJar {
        group = "shadow"
        archiveClassifier.set("mod-dev")

        mergeServiceFiles()
        configurations = listOf(gameShadedLibrariesConfig.get())

        manifest {
            attributes(
                "Superclass-Transformer" to "common.superclasschange,forge.superclasschange",
                "MixinConfigs" to mixinConfigs.joinToString(",")
            )
            from(forgeManifest)
        }

        from(commonProject.sourceSets.main.map { it.output })
        from(commonProject.sourceSets.named("mixins").map {it.output })
        from(commonProject.sourceSets.named("accessors").map {it.output })
        from(commonProject.sourceSets.named("launch").map {it.output })

        from(forgeLaunch.output)
        from(forgeAccessors.output)
        from(forgeMixins.output)
    }

    val remapShadowJar = register("remapShadowJar", RemapJarTask::class) {
        group = "loom"
        archiveClassifier.set("mod")

        inputFile.set(shadowJar.flatMap { it.archiveFile })
        atAccessWideners.add("common.accesswidener")
    }

    val universalJar = register("universalJar", Jar::class) {
        group = "build"
        archiveClassifier.set("universal")

        manifest.from(forgeManifest)

        from(forgeServicesShadowJar.archiveFile.map { zipTree(it) })

        into("jars") {
            from(remapShadowJar)
            rename("spongeforge-(.*)-mod.jar", "spongeforge-mod.jar")

            from(forgeLangJar)
            rename("spongeforge-(.*)-lang.jar", "spongeforge-lang.jar")
        }
    }

    assemble {
        dependsOn(universalJar)
    }

    templateResources {
        val props = mutableMapOf(
                "version" to project.version,
                "description" to project.description
        )
        inputs.properties(props)
        expand(props)
    }
}

afterEvaluate {
    sourceSets.configureEach {
        // Don't apply Mixin AP
        configurations.named(annotationProcessorConfigurationName) {
            exclude(group = "org.spongepowered", module = "mixin")
            exclude(group = "net.fabricmc", module = "fabric-mixin-compile-extensions")
        }
        // And don't pass AP parameters
        tasks.named(compileJavaTaskName, JavaCompile::class) {
            val mixinApArgs = setOf("outRefMapFile", "defaultObfuscationEnv", "outMapFileNamedIntermediary", "inMapFileNamedIntermediary")
            options.compilerArgs.removeIf { mixinApArgs.any { mixin -> it.contains(mixin)} }
        }
    }
}

indraSpotlessLicenser {
    licenseHeaderFile(rootProject.file("HEADER.txt"))

    property("name", "Sponge")
    property("organization", organization)
    property("url", projectUrl)
}

val sourcesJar by tasks.existing
val forgeAppLaunchJar by tasks.existing
val forgeLaunchJar by tasks.existing
val forgeMixinsJar by tasks.existing

publishing {
    publications {
        register("sponge", MavenPublication::class) {
            artifact(tasks.named("remapJar")) {
                builtBy(tasks.named("remapJar"))
            }
            artifact(sourcesJar) {
                builtBy(tasks.named("remapSourcesJar"))
            }
            artifact(tasks.named("remapShadowJar")) {
                builtBy(tasks.named("remapShadowJar"))
            }
            artifact(forgeAppLaunchJar.get())
            artifact(forgeLaunchJar.get())
            artifact(forgeMixinsJar.get())
            artifact(tasks["applaunchSourcesJar"])
            artifact(tasks["launchSourcesJar"])
            artifact(tasks["mixinsSourcesJar"])

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

tasks.register("printConfigsHierarchy") {
    doLast {
        configurations.forEach { conf: Configuration  ->
            val seen = mutableSetOf<Configuration>()
            println("Parents of ${conf.name}:")
            printParents(conf, "", seen)
        }
    }
}

fun printParents(conf: Configuration, indent: String, seen: MutableSet<Configuration>) {
    for (parent in conf.extendsFrom) {
        if (parent in seen) {
            continue
        }
        seen.add(parent)
        println("$indent - ${parent.name}")
        printParents(parent, indent + "  ", seen)
    }
}

tasks.register("printConfigsResolution") {
    doLast {
        configurations.forEach { conf: Configuration  ->
            println()
            println("Artifacts of ${conf.name}:")
            if (conf.isCanBeResolved) {
                try {
                    conf.forEach {
                        println(it)
                    }
                } catch (e: Exception) {
                    println("error")
                }
            } else {
                println("not resolved")
            }
        }
    }
}
