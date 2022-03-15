import net.fabricmc.loom.LoomGradleExtension
import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RunGameTask
import org.zeroturnaround.zip.transform.ZipEntryTransformer
import java.io.StringWriter

buildscript {
    repositories {
        maven("https://repo.spongepowered.org/repository/maven-public") {
            name = "sponge"
        }
        maven("https://maven.architectury.dev/")
    }
    dependencies {
        classpath("dev.architectury:architectury-loom:0.7.2-SNAPSHOT") {
            exclude("dev.architectury", "tiny-remapper")
        }

        classpath("dev.architectury:tiny-remapper") {
            attributes {
                attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.SHADOWED))
            }
            version {
                branch = "fix/mappings-refresh"
            }
        }
    }
}

plugins {
    id("com.github.johnrengelman.shadow")
    id("implementation-structure")
    id("templated-resources")
}

apply(plugin = "dev.architectury.loom") // we can't use the plugins block because we have to declare the dep in buildscript

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

java {
    withSourcesJar()
}

extensions.configure(LoomGradleExtension::class) {
    useFabricMixin = false
    silentMojangMappingsLicense()

    runs {
        configureEach {
            if (JavaVersion.current().isJava11Compatible) {
                vmArgs(
                        //"-Dfabric.log.level=debug",
                        "--add-exports=java.base/sun.security.util=ALL-UNNAMED", // ModLauncher
                        "--add-opens=java.base/java.util.jar=ALL-UNNAMED" // ModLauncher
                )
            }
            programArgs("--access_widener.config", "common.accesswidener")
            runDir(project.projectDir.resolve("run").toRelativeString(project.rootDir))
        }
    }
}

// Forge extra configurations
val forgeBootstrapLibrariesConfig: NamedDomainObjectProvider<Configuration> = configurations.register("bootstrapLibraries")
val mlTransformersConfig: NamedDomainObjectProvider<Configuration> = configurations.register("mltransformers")
val forgeLibrariesConfig: NamedDomainObjectProvider<Configuration> = configurations.register("spongeLibraries") {
    extendsFrom(mlTransformersConfig.get())
}
val forgeAppLaunchConfig: NamedDomainObjectProvider<Configuration> = configurations.register("applaunch") {
    extendsFrom(forgeBootstrapLibrariesConfig.get())
    extendsFrom(mlTransformersConfig.get())
    extendsFrom(configurations.getByName("minecraftNamed"))
    extendsFrom(configurations.getByName("loaderLibraries"))
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
        extendsFrom(forgeLibrariesConfig.get())
    }
}
val forgeLaunch by sourceSets.register("launch") {
    // implementation (compile) dependencies
    spongeImpl.applyNamedDependencyOnOutput(commonProject, launch.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, applaunch.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, main.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(project, this, forgeMain, project, forgeMain.implementationConfigurationName)

    configurations.named(implementationConfigurationName) {
        extendsFrom(forgeAppLaunchConfig.get())
        extendsFrom(forgeLibrariesConfig.get())
    }
    configurations.named(runtimeClasspathConfigurationName) {
        extendsFrom(mlTransformersConfig.get())
    }
}
val forgeAccessors by sourceSets.register("accessors") {
    spongeImpl.applyNamedDependencyOnOutput(commonProject, mixins.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(project, this, forgeLaunch, project, forgeLaunch.implementationConfigurationName)

    configurations.named(implementationConfigurationName) {
        extendsFrom(forgeAppLaunchConfig.get())
        extendsFrom(forgeLibrariesConfig.get())
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
}
val forgeAppLaunch by sourceSets.register("applaunch") {
    // implementation (compile) dependencies
    spongeImpl.applyNamedDependencyOnOutput(commonProject, applaunch.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, launch.get(), forgeLaunch, project, this.implementationConfigurationName)
    // spongeImpl.applyNamedDependencyOnOutput(project, vanillaInstaller, this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(project, this, forgeLaunch, project, forgeLaunch.implementationConfigurationName)
    // runtime dependencies - literally add the rest of the project, because we want to launch the game
    spongeImpl.applyNamedDependencyOnOutput(project, forgeMixins, this, project, this.runtimeOnlyConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(project, forgeLaunch, this, project, this.runtimeOnlyConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, mixins.get(), this, project, this.runtimeOnlyConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, main.get(), this, project, this.runtimeOnlyConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(commonProject, accessors.get(), this, project, this.runtimeOnlyConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(project, forgeMain, this, project, this.runtimeOnlyConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(transformersProject, mlTransformers.get(), this, project, this.runtimeOnlyConfigurationName)
}
val forgeMixinsImplementation by configurations.named(forgeMixins.implementationConfigurationName) {
    extendsFrom(forgeLibrariesConfig.get())
    extendsFrom(forgeAppLaunchConfig.get())
}
configurations.named(forgeAppLaunch.implementationConfigurationName) {
    extendsFrom(forgeAppLaunchConfig.get())
    extendsFrom(launchConfig.get())
    extendsFrom(mlTransformersConfig.get())
}
val forgeAppLaunchRuntime by configurations.named(forgeAppLaunch.runtimeOnlyConfigurationName)

configurations.configureEach {
    exclude(group = "net.minecraft", module = "joined")
    if (name != "minecraft") { // awful terrible hack sssh
        exclude(group = "com.mojang", module = "minecraft")
    }
}

dependencies {
    "minecraft"("com.mojang:minecraft:${minecraftVersion}")
    "forge"("net.minecraftforge:forge:$minecraftVersion-$forgeVersion")
    "mappings"(project.extensions.getByType(LoomGradleExtension::class).officialMojangMappings())

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

    forgeMixinsImplementation(project(commonProject.path))

    val appLaunch = forgeBootstrapLibrariesConfig.name
    appLaunch("org.spongepowered:spongeapi:$apiVersion") { isTransitive = false }
    appLaunch("org.spongepowered:plugin-spi:$apiPluginSpiVersion")
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
    mlTransformersConfig.name(project(transformersProject.path))

    val libraries = forgeLibrariesConfig.name
    libraries("org.spongepowered:spongeapi:$apiVersion")
    libraries("javax.inject:javax.inject:1") // wat
    libraries("com.zaxxer:HikariCP:2.6.3")
    libraries("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
    // libraries(project(commonProject.path)) // todo: this is better, but seems to be pulling in ASM for some reason
    libraries(platform("net.kyori:adventure-bom:$apiAdventureVersion"))
    libraries("net.kyori:adventure-serializer-configurate4")

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
val mods: ConfigurableFileCollection = extensions.getByType(LoomGradleExtension::class).unmappedModCollection
tasks.withType(net.fabricmc.loom.task.AbstractRunTask::class) {
    setClasspath(files(mods, sourceSets.main.get().runtimeClasspath, forgeAppLaunch.runtimeClasspath))
    argumentProviders += CommandLineArgumentProvider {
        mixinConfigs.asSequence()
                // .filter { it != "mixins.sponge.core.json" }
                .flatMap { sequenceOf("--mixin.config", it) }
                .toList()
    }
}

tasks {
    jar {
        manifest {
            from(forgeManifest)
            attributes("MixinConfigs" to mixinConfigs.joinToString(","))
        }

        from(commonProject.sourceSets.main.map { it.output })
        from(commonProject.sourceSets.named("mixins").map {it.output })
        from(commonProject.sourceSets.named("accessors").map {it.output })
        from(commonProject.sourceSets.named("launch").map {it.output })
        from(commonProject.sourceSets.named("applaunch").map {it.output })
        // from(sourceSets.main.map {it.output })
        from(forgeAppLaunch.output)
        from(forgeLaunch.output)
        from(forgeAccessors.output)
        from(forgeMixins.output)

        duplicatesStrategy = DuplicatesStrategy.WARN
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

    named("runServer", RunGameTask::class) {
        standardInput = System.`in`
    }
    named("remapSourcesJar", net.fabricmc.loom.task.RemapSourcesJarTask::class) {
        inputs.files(configurations.compileClasspath)
                .ignoreEmptyDirectories()
                .withPropertyName("sourceClasspathWhyDoesLoomNotDoThis")
    }

    fun fixLoomCorruptedMixinJsonsAndRemapAccessWidener(jar: File, remapper: org.objectweb.asm.commons.Remapper) {
        // strip the refmap attribute that loom forcibly adds to our json files
        // aaaaaaa
        val gson = com.google.gson.Gson()
        val mixinTransformer = ZipEntryTransformer { src, zipEntry, dest ->
            val json: com.google.gson.JsonObject
            src.bufferedReader(Charsets.UTF_8).use {
                json = gson.fromJson(it, com.google.gson.JsonObject::class.java)
            }
            json.remove("refmap")
            zipEntry.compressedSize = -1
            dest.putNextEntry(zipEntry)
            dest.bufferedWriter(Charsets.UTF_8).also {
                gson.toJson(json, it)
                it.flush()
            }
        }
        val awTransformer = ZipEntryTransformer { src, zipEntry, dest ->
            val named = net.fabricmc.accesswidener.AccessWidener()
            src.bufferedReader(Charsets.UTF_8).use {
                net.fabricmc.accesswidener.AccessWidenerReader(named).read(it)
            }
            val srg = net.fabricmc.accesswidener.AccessWidenerRemapper(named, remapper, "srg").remap()
            val output = StringWriter().also {
                net.fabricmc.accesswidener.AccessWidenerWriter(srg).write(it)
            }.toString()
            zipEntry.compressedSize = -1
            dest.putNextEntry(zipEntry)
            dest.writer(Charsets.UTF_8).also {
                it.write(output)
                it.flush()
            }
        }
        val entries = mixinConfigs.map { org.zeroturnaround.zip.transform.ZipEntryTransformerEntry(it, mixinTransformer) } +
            org.zeroturnaround.zip.transform.ZipEntryTransformerEntry("common.accesswidener", awTransformer)
        org.zeroturnaround.zip.ZipUtil.transformEntries(jar, entries.toTypedArray())
    }

    named("remapJar", RemapJarTask::class) {
        val remapper = arrayOfNulls<org.objectweb.asm.commons.Remapper>(1)
        remapOptions {
            this.extension(dev.architectury.tinyremapper.extension.mixin.MixinExtension())
            this.extraStateProcessor { remapper[0] = it.remapper }
            this.threads(1)
        }
        doLast {
            fixLoomCorruptedMixinJsonsAndRemapAccessWidener(archiveFile.get().asFile, remapper[0]!!)
        }
    }

    val installerResources = project.layout.buildDirectory.dir("generated/resources/installer")
    forgeAppLaunch.resources.srcDir(installerResources)

    /*val downloadNotNeeded = configurations.register("downloadNotNeeded") {
        extendsFrom(forgeAppLaunchConfig.get())
        extendsFrom(vanillaInstallerConfig.get())
    }*/

    val emitDependencies by registering(org.spongepowered.gradle.impl.OutputDependenciesToJson::class) {
        group = "sponge"
        // everything in applaunch
        this.dependencies("main", forgeLibrariesConfig)
        // except what we're providing through the installer
        this.excludedDependencies(forgeAppLaunchConfig)

        outputFile.set(installerResources.map { it.file("org/spongepowered/forge/applaunch/loading/moddiscovery/libraries.json") })
    }
    named(forgeAppLaunch.processResourcesTaskName).configure {
        dependsOn(emitDependencies)
    }

    shadowJar {
        mergeServiceFiles()
        group = "shadow"

        configurations = listOf(forgeBootstrapLibrariesConfig.get())

        archiveClassifier.set("universal-dev")
        manifest {
            attributes(mapOf(
                "Access-Widener" to "common.accesswidener",
                "Superclass-Transformer" to "common.superclasschange,forge.superclasschange",
                "Multi-Release" to true,
                "MixinConfigs" to mixinConfigs.joinToString(",")
            ))
            from(forgeManifest)
        }
        from(commonProject.sourceSets.main.map { it.output })
        from(commonProject.sourceSets.named("mixins").map {it.output })
        from(commonProject.sourceSets.named("accessors").map {it.output })
        from(commonProject.sourceSets.named("launch").map {it.output })
        from(commonProject.sourceSets.named("applaunch").map {it.output })
        from(transformersProject.sourceSets.named("main").map { it.output })

        // Pull dependencies from the mlTransformers project
        from(mlTransformersConfig.get().files)

        from(forgeAppLaunch.output)
        from(forgeLaunch.output)
        from(forgeAccessors.output)
        from(forgeMixins.output)

        // Make sure to relocate access widener so that we don't conflict with other
        // coremods also using access widener
        relocate("net.fabricmc.accesswidener", "org.spongepowered.forge.libs.accesswidener")
    }

    val remapShadowJar = register("remapShadowJar", RemapJarTask::class) {
        input.set(shadowJar.flatMap { it.archiveFile })
        archiveClassifier.set("universal")
        remapAccessWidener.set(true)
        addNestedDependencies.set(true)
        val remapper = arrayOfNulls<org.objectweb.asm.commons.Remapper>(1)
        remapOptions {
            this.extension(dev.architectury.tinyremapper.extension.mixin.MixinExtension())
            this.extraStateProcessor { remapper[0] = it.remapper }
            this.threads(1) // thread-safety issues?
        }
        doLast {
            fixLoomCorruptedMixinJsonsAndRemapAccessWidener(archiveFile.get().asFile, remapper[0]!!)
        }
    }

    assemble {
        dependsOn(remapShadowJar)
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

/*configurations.forEach { set: Configuration  ->
    val seen = mutableSetOf<Configuration>()
    println("Parents of ${set.name}:")
    printParents(set, "", seen)

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
}*/