plugins {
    id("com.github.johnrengelman.shadow")
    id("templated-resources")
    id("dev.architectury.loom")
    id("sponge-impl.platform-convention")
}

val apiVersion: String by project
val minecraftVersion: String by project
val forgeVersion: String by project
val recommendedVersion: String by project
val organization: String by project
val projectUrl: String by project


description = "The SpongeAPI implementation for MinecraftForge"
version = spongeImpl.generatePlatformBuildVersionString(apiVersion, minecraftVersion, recommendedVersion, forgeVersion)

val testplugins: Project? = rootProject.subprojects.find { "testplugins".equals(it.name) }
val spongeMixinConfigs: MutableSet<String> = spongeImpl.mixinConfigurations
loom {
    // useFabricMixin = false
    mixin.useLegacyMixinAp.set(false)
    forge {
        useCustomMixin.set(false)
        mixinConfigs.addAll(spongeMixinConfigs)
        convertAccessWideners.set(true)
        extraAccessWideners.add("common.accesswidener")
    }
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
            // programArgs("--access_widener.config", "common.accesswidener")
            runDir(project.projectDir.resolve("run").toRelativeString(project.rootDir))
        }
    }
}

// Common source sets and configurations
configurations.configureEach {
    exclude(group = "net.minecraft", module = "joined")
    if (name != "minecraft") { // awful terrible hack sssh
        exclude(group = "com.mojang", module = "minecraft")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${libs.versions.minecraft.get()}")
    forge("net.minecraftforge:forge:${libs.versions.minecraft.get()}-${libs.versions.forge.get()}")
    mappings(loom.officialMojangMappings())

    val apiAdventureVersion: String by project
    val apiConfigurateVersion: String by project
    val apiPluginSpiVersion: String by project
    val log4jVersion: String by project

    /*api(project(":", configuration = "launch")) {
        exclude(group = "org.spongepowered", module = "mixin")
    }
    implementation(project(":", configuration = "accessors")) {
        exclude(group = "org.spongepowered", module = "mixin")
    }
    implementation(project(":")) {
        exclude(group = "org.spongepowered", module = "mixin")
    }*/

    /*forgeMixinsImplementation(project(commonProject.path))

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

    libraries("javax.inject:javax.inject:1") // wat
    libraries("com.zaxxer:HikariCP:2.7.8")
    libraries("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
    // libraries(project(commonProject.path)) // todo: this is better, but seems to be pulling in ASM for some reason
    libraries(platform("net.kyori:adventure-bom:$apiAdventureVersion"))
    libraries("net.kyori:adventure-serializer-configurate4")

    testplugins?.also {
        forgeAppLaunchRuntime(project(it.path)) {
            exclude(group = "org.spongepowered")
        }
    }*/
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

// val mods: ConfigurableFileCollection = extensions.getByType(LoomGradleExtension::class).unmappedModCollection
tasks.withType(net.fabricmc.loom.task.AbstractRunTask::class).configureEach {
    // setClasspath(files(mods, sourceSets.main.get().runtimeClasspath, forgeAppLaunch.runtimeClasspath))
    /*argumentProviders += CommandLineArgumentProvider {
        spongeMixinConfigs.asSequence()
                // .filter { it != "mixins.sponge.core.json" }
                .flatMap { sequenceOf("--mixin.config", it) }
                .toList()
    }*/
}

tasks {
    /*jar {
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

    named("runServer", RunGameTask::class) {
        standardInput = System.`in`
    }
    named("remapSourcesJar", net.fabricmc.loom.task.RemapSourcesJarTask::class) {
        inputs.files(configurations.compileClasspath)
                .ignoreEmptyDirectories()
                .withPropertyName("sourceClasspathWhyDoesLoomNotDoThis")
    }

    val installerResources = project.layout.buildDirectory.dir("generated/resources/installer")
    forgeAppLaunch.resources.srcDir(installerResources)

    val downloadNotNeeded = configurations.register("downloadNotNeeded") {
        extendsFrom(forgeAppLaunchConfig.get())
        extendsFrom(vanillaInstallerConfig.get())
    }

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
    }*/
}

publishing {
    /*publications {
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
    }*/
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
