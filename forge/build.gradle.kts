plugins {
    id("dev.architectury.loom") version "0.7.2-SNAPSHOT"
    id("implementation-structure")
    id("templated-resources")
}

val commonProject = parent!!
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

loom {
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
val forgeLibrariesConfig = configurations.register("spongeLibraries")
val forgeAppLaunchConfig = configurations.register("applaunch") {
    extendsFrom(forgeLibrariesConfig.get())
    extendsFrom(configurations.minecraftNamed.get())
    extendsFrom(configurations.loaderLibraries.get())
}

// Common source sets and configurations
val launchConfig = commonProject.configurations.named("launch")
val accessors = commonProject.sourceSets.named("accessors")
val launch = commonProject.sourceSets.named("launch")
val applaunch = commonProject.sourceSets.named("applaunch")
val mixins = commonProject.sourceSets.named("mixins")
val main = commonProject.sourceSets.named("main")

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
    }
}
val forgeAccessors by sourceSets.register("accessors") {
    spongeImpl.applyNamedDependencyOnOutput(commonProject, mixins.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(project, this, forgeLaunch, project, forgeLaunch.implementationConfigurationName)

    configurations.named(implementationConfigurationName) {
        extendsFrom(forgeAppLaunchConfig.get())
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
}
val forgeMixinsImplementation by configurations.named(forgeMixins.implementationConfigurationName) {
    extendsFrom(forgeAppLaunchConfig.get())
}
configurations.named(forgeAppLaunch.implementationConfigurationName) {
    extendsFrom(forgeAppLaunchConfig.get())
    extendsFrom(launchConfig.get())
}
val forgeAppLaunchRuntime by configurations.named(forgeAppLaunch.runtimeOnlyConfigurationName)

configurations.configureEach {
    exclude(group = "net.minecraft", module = "joined")
    if (name != "minecraft") { // awful terrible hack sssh
        exclude(group = "com.mojang", module = "minecraft")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    forge("net.minecraftforge:forge:$minecraftVersion-$forgeVersion")
    mappings(minecraft.officialMojangMappings())

    val pluginSpiVersion: String by project
    val timingsVersion: String by project

    api(project(":", configuration = "launch"))
    implementation(project(":", configuration = "accessors"))
    implementation(project(commonProject.path))

    forgeMixinsImplementation(project(commonProject.path))
    add(forgeLaunch.implementationConfigurationName, "org.spongepowered:spongeapi:$apiVersion")

    val appLaunch = forgeAppLaunchConfig.name
    appLaunch("org.spongepowered:spongeapi:$apiVersion")
    appLaunch(platform("net.kyori:adventure-bom:4.7.0"))
    appLaunch("net.kyori:adventure-serializer-configurate4")
    appLaunch("org.spongepowered:plugin-spi:$pluginSpiVersion")
    appLaunch("javax.inject:javax.inject:1")
    appLaunch("com.zaxxer:HikariCP:2.6.3")
    appLaunch("org.apache.logging.log4j:log4j-slf4j-impl:2.11.2")
    appLaunch(platform("org.spongepowered:configurate-bom:4.1.1"))
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
    appLaunch("net.fabricmc:access-widener:1.0.2") {
        exclude(group = "org.apache.logging.log4j")
    }

    val libraries = forgeLibrariesConfig.name
    libraries("org.spongepowered:timings:$timingsVersion")

    // Launch Dependencies - Needed to bootstrap the engine(s)
    // The ModLauncher compatibility launch layer
    testplugins?.also {
        forgeAppLaunchRuntime(project(it.path)) {
            exclude(group = "org.spongepowered")
        }
    }
}

val forgeManifest = the<JavaPluginConvention>().manifest {
    attributes(
            "Specification-Title" to "SpongeForge",
            "Specification-Vendor" to "SpongePowered",
            "Specification-Version" to apiVersion,
            "Implementation-Title" to project.name,
            "Implementation-Version" to spongeImpl.generatePlatformBuildVersionString(apiVersion, minecraftVersion, recommendedVersion, forgeVersion),
            "Implementation-Vendor" to "SpongePowered"
    )
}

val mixinConfigs = spongeImpl.mixinConfigurations
val mods = loom.unmappedModCollection
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

    /* shadowJar {
        mergeServiceFiles()

        configurations = listOf(project.configurations.getByName(vanillaInstaller.runtimeClasspathConfigurationName))

        archiveClassifier.set("universal")
        manifest {
            attributes(mapOf(
                    "Access-Widener" to "common.accesswidener",
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
    }
    assemble {
        dependsOn(shadowJar)
    }*/
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

tasks.templateResources {
    inputs.property("version", project.version)
    inputs.property("description", project.description)
    expand(
        "version" to project.version,
        "description" to project.description
    )

}

tasks.runServer {
    standardInput = System.`in`
}

val sourcesJar by tasks.existing
val forgeAppLaunchJar by tasks.existing
val forgeLaunchJar by tasks.existing
val forgeMixinsJar by tasks.existing

publishing {
    publications {
        register("sponge", MavenPublication::class) {

            artifact(tasks.remapJar) {
                builtBy(tasks.remapJar)
            }
            artifact(sourcesJar) {
                builtBy(tasks.remapSourcesJar)
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