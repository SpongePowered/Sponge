buildscript {
    repositories {
        mavenLocal()
        maven("https://files.minecraftforge.net/maven")
        maven("https://repo-new.spongepowered.org/repository/maven-public")
        maven("https://repo.spongepowered.org/maven")
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("org.spongepowered:mixingradle:0.7-SNAPSHOT")
    }
}

plugins {
    id("net.minecraftforge.gradle")
    `maven-publish`
    `java-library`
    idea
    eclipse
    id("net.minecrell.licenser") version "0.4.1"
}


apply {
    plugin("org.spongepowered.mixin")
}

val apiProject = project.project("SpongeAPI")
val commonProject = project
val mcpType: String by project
val mcpMappings: String by project
val minecraftDep: String by project
val minecraftVersion: String by project
val recommendedVersion: String by project

minecraft {
    mappings(mcpType, mcpMappings)
    project.sourceSets["main"].resources
            .filter { it.name.endsWith("_at.cfg") }
            .files
            .forEach {
                accessTransformer(it)
                subprojects {

                }
                parent?.minecraft?.accessTransformer(it)
            }
}

tasks {
    compileJava {
        options.compilerArgs.addAll(listOf("-Xmaxerrs", "1000"))
    }
    jar {
        manifest {
            attributes(mapOf(
                    "Specification-Title" to "SpongeCommon",
                    "Specification-Vendor" to "SpongePowered",
                    "Specification-Version" to apiProject.version,
                    "Implementation-Title" to project.name,
                    "Implementation-Version" to generateImplementationVersionString(apiProject.version as String, minecraftVersion, recommendedVersion),
                    "Implementation-Vendor" to "SpongePowered"
//                            "Implementation-Timestamp" to Instant.now().format("yyyy-MM-dd'T'HH:mm:ssZ")
            ))
        }
    }
}
version = generateImplementationVersionString(apiProject.version as String, minecraftVersion, recommendedVersion)

// Configurations
val minecraftConfig by configurations.named("minecraft")

val launchConfig by configurations.register("launch") {
    extendsFrom(minecraftConfig)
}
val accessorsConfig by configurations.register("accessors") {
    extendsFrom(minecraftConfig)
    extendsFrom(launchConfig)
}
val mixinsConfig by configurations.register("mixins") {
    extendsFrom(launchConfig)
    extendsFrom(minecraftConfig)
}
val modlauncherConfig by configurations.register("modlauncher") {
    extendsFrom(launchConfig)
    extendsFrom(minecraftConfig)
}

// create the sourcesets
val main by sourceSets

val launch by sourceSets.registering {
    project.dependencies {
        mixinsConfig(this@registering.output)
    }
    project.dependencies {
        implementation(this@registering.output)
    }

}

val accessors by sourceSets.registering {
    applyNamedDependencyOnOutput(originProject = project, sourceAdding = launch.get(), targetSource = this, implProject = project, dependencyConfigName = this.implementationConfigurationName)
    applyNamedDependencyOnOutput(originProject = project, sourceAdding = this, targetSource = main, implProject = project, dependencyConfigName = main.implementationConfigurationName)
}
val mixins by sourceSets.registering {
    applyNamedDependencyOnOutput(originProject = project, sourceAdding = launch.get(), targetSource = this, implProject = project, dependencyConfigName = this.implementationConfigurationName)
    applyNamedDependencyOnOutput(originProject = project, sourceAdding = accessors.get(), targetSource = this, implProject = project, dependencyConfigName = this.implementationConfigurationName)
    applyNamedDependencyOnOutput(originProject = project, sourceAdding = main, targetSource = this, implProject = project, dependencyConfigName = this.implementationConfigurationName)
}

configure<org.spongepowered.asm.gradle.plugins.MixinExtension> {}
repositories {
    mavenLocal()
    maven("https://files.minecraftforge.net/maven")
    maven {
        name = "Sonatype Snapshots"
        setUrl("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}
dependencies {
    minecraft("net.minecraft:$minecraftDep:$minecraftVersion")

    // api
    api(project(":SpongeAPI"))
    api("org.spongepowered:plugin-spi:0.1.1-SNAPSHOT") {
        exclude(group = "com.google.inject", module = "guice") // We use our own version
    }

    api("net.kyori:adventure-api:4.0.0-SNAPSHOT")
    api("net.kyori:adventure-text-serializer-gson:4.0.0-SNAPSHOT")
    api("net.kyori:adventure-text-serializer-legacy:4.0.0-SNAPSHOT")

    // Database stuffs... likely needs to be looked at
    implementation("com.zaxxer:HikariCP:2.6.3")
    implementation("org.mariadb.jdbc:mariadb-java-client:2.0.3")
    implementation("com.h2database:h2:1.4.196")
    implementation("org.xerial:sqlite-jdbc:3.20.0")
    implementation("com.google.inject:guice:4.1.0")

    // ASM - required for generating event listeners
    implementation("org.ow2.asm:asm-util:6.2")
    implementation("org.ow2.asm:asm-tree:6.2")

    // Launch Dependencies - Needed to bootstrap the engine(s)
    launchConfig(project(":SpongeAPI"))
    launchConfig("org.spongepowered:plugin-spi:0.1.1-SNAPSHOT") {
        exclude(group = "com.google.inject", module = "guice") // We use our own version
    }
    launchConfig("org.spongepowered:mixin:0.8")
    launchConfig("org.checkerframework:checker-qual:2.8.1")
    launchConfig("com.google.guava:guava:21.0") {
        exclude(group = "com.google.code.findbugs", module = "jsr305") // We don't want to use jsr305, use checkerframework
        exclude(group = "org.checkerframework", module = "checker-qual") // We use our own version
        exclude(group = "com.google.j2objc", module = "j2objc-annotations")
        exclude(group = "org.codehaus.mojo", module = "animal-sniffer-annotations")
        exclude(group = "com.google.errorprone", module = "error_prone_annotations")
    }
    launchConfig("com.google.inject:guice:4.1.0")

    launchConfig("com.google.code.gson:gson:2.8.0")
    launchConfig("org.ow2.asm:asm-tree:6.2")
    launchConfig("org.ow2.asm:asm-util:6.2")
    launchConfig("org.apache.logging.log4j:log4j-api:2.11.2")
    launchConfig("org.spongepowered:configurate-core:3.6.1")
    launchConfig("org.spongepowered:configurate-hocon:3.6.1")
    launchConfig("org.spongepowered:configurate-json:3.6.1")
    launchConfig("org.apache.logging.log4j:log4j-core:2.11.2")
    add(launch.get().implementationConfigurationName, launchConfig)

    // Annotation Processor
    "accessorsAnnotationProcessor"(launchConfig)
    "mixinsAnnotationProcessor"(launchConfig)
    "accessorsAnnotationProcessor"("org.spongepowered:mixin:0.8")
    "mixinsAnnotationProcessor"("org.spongepowered:mixin:0.8")
    mixinsConfig(sourceSets["main"].output)
    add(accessors.get().implementationConfigurationName, accessorsConfig)
    add(mixins.get().implementationConfigurationName, mixinsConfig)
    add(mixins.get().implementationConfigurationName, project(":SpongeAPI"))
}

fun debug(logger: Logger, messsage: String) {
    println(message = messsage)
    if (System.getProperty("sponge.gradleDebug", "false")!!.toBoolean()) {
        logger.lifecycle(messsage)
    }
}
fun applyNamedDependencyOnOutput(originProject: Project, sourceAdding: SourceSet, targetSource: SourceSet, implProject: Project, dependencyConfigName: String) {
    debug(implProject.logger, "[${implProject.name}] Adding ${originProject.path}(${sourceAdding.name}) to ${implProject.path}(${targetSource.name}).$dependencyConfigName")
    implProject.dependencies.add(dependencyConfigName, sourceAdding.output)
}

fun generateImplementationVersionString(apiVersion: String, minecraftVersion: String, implRecommendedVersion: String, addedVersionInfo: String? = null): String {
    val isRelease = implRecommendedVersion.endsWith("-SNAPSHOT")
    val apiSplit = apiVersion.replace("-SNAPSHOT", "").split(".")
    val minor = if (apiSplit.size > 1) apiSplit[1] else (if (apiSplit.size > 0) apiSplit.last() else "-1")
    val apiReleaseVersion = "${apiSplit[0]}.$minor"
    return listOfNotNull(minecraftVersion, addedVersionInfo, "$apiReleaseVersion.$implRecommendedVersion").joinToString("-")
}

val organization: String by project
val projectUrl: String by project
license {
    (this as ExtensionAware).extra.apply {
        this["name"] = "Sponge"
        this["organization"] = organization
        this["url"] = projectUrl
    }
    header = apiProject.file("HEADER.txt")

    include("**/*.java")
    newLine = false
}

allprojects {

    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    tasks {
        withType(JavaCompile::class) {
            options.compilerArgs.addAll(listOf("-Xmaxerrs", "1000"))
        }
    }
    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    repositories {
        mavenLocal()
        maven {
            name = "sponge v2"
            setUrl("https://repo-new.spongepowered.org/repository/maven-public/")
        }
        maven("https://repo.spongepowered.org/maven")
        maven {
            name = "Sonatype Snapshots"
            setUrl("https://oss.sonatype.org/content/repositories/snapshots/")
        }
    }
    val spongeSnapshotRepo: String? by project
    val spongeReleaseRepo: String? by project
    tasks {

//    withType<PublishToMavenRepository>().configureEach {
//        onlyIf {
//            (repository == publishing.repositories["GitHubPackages"] &&
//                    !publication.version.endsWith("-SNAPSHOT")) ||
//                    (!spongeSnapshotRepo.isNullOrBlank()
//                            && !spongeReleaseRepo.isNullOrBlank()
//                            && repository == publishing.repositories["spongeRepo"]
//                            && publication == publishing.publications["sponge"])
//
//        }
//    }
    }

    publishing {
        repositories {
            maven {
                name = "GitHubPackages"
                this.url = uri("https://maven.pkg.github.com/spongepowered/${project.name}")
                credentials {
                    username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
                    password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
                }
            }
            // Set by the build server
            maven {
                name = "spongeRepo"
                val repoUrl = if ((version as String).endsWith("-SNAPSHOT")) spongeSnapshotRepo else spongeReleaseRepo
                repoUrl?.apply {
                    url = uri(this)
                }
                val spongeUsername: String? by project
                val spongePassword: String? by project
                credentials {
                    username = spongeUsername ?: System.getenv("ORG_GRADLE_PROJECT_spongeUsername")
                    password = spongePassword ?: System.getenv("ORG_GRADLE_PROJECT_spongePassword")
                }
            }
        }
    }
}

val testplugins: Project? = subprojects.find { "testplugins".equals(it.name) }
if (testplugins != null) {
    project("testplugins") {
        apply {
            plugin("java-library")
            plugin("idea")
            plugin("eclipse")
            plugin("net.minecrell.licenser")
        }

        dependencies {
            implementation(rootProject.project(":SpongeAPI"))
            annotationProcessor(rootProject.project(":SpongeAPI"))
        }

        tasks.jar {
            manifest {
                attributes("Loader" to "java_plain")
            }
        }
        license {
            (this as ExtensionAware).extra.apply {
                this["name"] = "Sponge"
                this["organization"] = organization
                this["url"] = projectUrl
            }
            header = apiProject.file("HEADER.txt")

            include("**/*.java")
            newLine = false
        }
    }
}

project("SpongeVanilla") {
    val vanillaProject = this
    apply {
        plugin("net.minecraftforge.gradle")
        plugin("org.spongepowered.mixin")
        plugin("java-library")
        plugin("maven-publish")
        plugin("idea")
        plugin("eclipse")
        plugin("net.minecrell.licenser")
    }

    description = "The SpongeAPI implementation for Vanilla Minecraft"
    version = generateImplementationVersionString(apiProject.version as String, minecraftVersion, recommendedVersion)

    val vanillaMinecraftConfig by configurations.named("minecraft")
    val vanillaModLauncherConfig by configurations.register("modlauncher") {
        extendsFrom(vanillaMinecraftConfig)
    }

    val vanillaMain by sourceSets.named("main") {
        // implementation (compile) dependencies
        applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = accessors.get(), targetSource = this, implProject = vanillaProject, dependencyConfigName = this.implementationConfigurationName)
        applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = launch.get(), targetSource = this, implProject = vanillaProject, dependencyConfigName = this.implementationConfigurationName)
    }
    val vanillaLaunch by sourceSets.register("launch") {
        // implementation (compile) dependencies
        applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = launch.get(), targetSource = this, implProject = vanillaProject, dependencyConfigName = this.implementationConfigurationName)
        applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = main, targetSource = this, implProject = vanillaProject, dependencyConfigName = this.implementationConfigurationName)
        applyNamedDependencyOnOutput(originProject = vanillaProject, sourceAdding = this, targetSource = vanillaMain, implProject = vanillaProject, dependencyConfigName = vanillaMain.implementationConfigurationName)
    }
    val vanillaAccessors by sourceSets.register("accessors") {
        // implementation (compile) dependencies
        applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = launch.get(), targetSource = this, implProject = vanillaProject, dependencyConfigName = this.implementationConfigurationName)
        applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = accessors.get(), targetSource = this, implProject = vanillaProject, dependencyConfigName = this.implementationConfigurationName)
        applyNamedDependencyOnOutput(originProject = vanillaProject, sourceAdding = this, targetSource = vanillaMain, implProject = vanillaProject, dependencyConfigName = vanillaMain.implementationConfigurationName)
        applyNamedDependencyOnOutput(originProject = vanillaProject, sourceAdding = vanillaLaunch, targetSource = this, implProject = vanillaProject, dependencyConfigName = this.implementationConfigurationName)
    }
    val vanillaMixins by sourceSets.register("mixins") {
        // implementation (compile) dependencies
        applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = mixins.get(), targetSource = this, implProject = vanillaProject, dependencyConfigName = this.implementationConfigurationName)
        applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = accessors.get(), targetSource = this, implProject = vanillaProject, dependencyConfigName = this.implementationConfigurationName)
        applyNamedDependencyOnOutput(originProject = vanillaProject, sourceAdding = vanillaAccessors, targetSource = this, implProject = vanillaProject, dependencyConfigName = this.implementationConfigurationName)
        applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = launch.get(), targetSource = this, implProject = vanillaProject, dependencyConfigName = this.implementationConfigurationName)
        applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = main, targetSource = this, implProject = vanillaProject, dependencyConfigName = this.implementationConfigurationName)
        applyNamedDependencyOnOutput(originProject = vanillaProject, sourceAdding = vanillaMain, targetSource = this, implProject = vanillaProject, dependencyConfigName = this.implementationConfigurationName)
        applyNamedDependencyOnOutput(originProject = vanillaProject, sourceAdding = vanillaLaunch, targetSource = this, implProject = vanillaProject, dependencyConfigName = this.implementationConfigurationName)
    }
    val vanillaModLauncher by sourceSets.register("modlauncher") {
        // implementation (compile) dependencies
        applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = launch.get(), targetSource = this, implProject = vanillaProject, dependencyConfigName = this.implementationConfigurationName)
        applyNamedDependencyOnOutput(originProject = vanillaProject, sourceAdding = vanillaLaunch, targetSource = this, implProject = vanillaProject, dependencyConfigName = this.implementationConfigurationName)
        // runtime dependencies - literally add the rest of the project, because we want to launch the game
        applyNamedDependencyOnOutput(originProject = vanillaProject, sourceAdding = vanillaMixins, targetSource = this, implProject = vanillaProject, dependencyConfigName = this.runtimeConfigurationName)
        applyNamedDependencyOnOutput(originProject = vanillaProject, sourceAdding = vanillaAccessors, targetSource = this, implProject = vanillaProject, dependencyConfigName = this.runtimeConfigurationName)
        applyNamedDependencyOnOutput(originProject = vanillaProject, sourceAdding = vanillaLaunch, targetSource = this, implProject = vanillaProject, dependencyConfigName = this.runtimeConfigurationName)
        applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = mixins.get(), targetSource = this, implProject = vanillaProject, dependencyConfigName = this.runtimeConfigurationName)
        applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = main, targetSource = this, implProject = vanillaProject, dependencyConfigName = this.runtimeConfigurationName)
        applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = accessors.get(), targetSource = this, implProject = vanillaProject, dependencyConfigName = this.runtimeConfigurationName)
        applyNamedDependencyOnOutput(originProject = vanillaProject, sourceAdding = vanillaMain, targetSource = this, implProject = vanillaProject, dependencyConfigName = this.runtimeConfigurationName)
    }

    val vanillaAccessorsAnnotationProcessor by configurations.named(vanillaAccessors.annotationProcessorConfigurationName)
    val vanillaAccessorsImplementation by configurations.named(vanillaAccessors.implementationConfigurationName)
    val vanillaMixinsImplementation by configurations.named(vanillaMixins.implementationConfigurationName) {
        extendsFrom(vanillaMinecraftConfig)
    }
    val vanillaMixinsAnnotationProcessor by configurations.named(vanillaMixins.annotationProcessorConfigurationName)
    val vanillaModLauncherImplementation by configurations.named(vanillaModLauncher.implementationConfigurationName) {
        extendsFrom(launchConfig)
    }
    val vanillaModLauncherRuntime by configurations.named(vanillaModLauncher.runtimeConfigurationName)

    configure<net.minecraftforge.gradle.userdev.UserDevExtension> {
        mappings(mcpType, mcpMappings)
        runs {
            create("server") {
                workingDirectory(vanillaProject.file("./run"))
                args.addAll(listOf("nogui", "--launchTarget", "sponge_server_dev"))
                main = "org.spongepowered.vanilla.modlauncher.Main"
            }

            create("client") {
                environment("target", "client")
                workingDirectory(vanillaProject.file("./run"))
                args.addAll(listOf(
                        "--launchTarget", "sponge_client_dev",
                        "--version", "1.14.4",
                        "--accessToken", "0",
                        "--assetIndex", "1.14",
                        "--assetsDir", (tasks.findByName("downloadAssets") as net.minecraftforge.gradle.common.task.DownloadAssets).output.absolutePath
                ))
                jvmArgs.addAll(listOf(
                        "-Djava.library.path=" + (tasks.findByName("extractNatives") as net.minecraftforge.gradle.common.task.ExtractNatives).output.absolutePath
                ))
                main = "org.spongepowered.vanilla.modlauncher.Main"
            }
        }
        commonProject.sourceSets["main"].resources
                .filter { it.name.endsWith("_at.cfg") }
                .files
                .forEach {
                    accessTransformer(it)
                }

        vanillaProject.sourceSets["main"].resources
                .filter { it.name.endsWith("_at.cfg") }
                .files
                .forEach { accessTransformer(it) }
    }

    dependencies {
        minecraft("net.minecraft:$minecraftDep:$minecraftVersion")

        api(launch.get().output)
        implementation(accessors.get().output)
        implementation(project(commonProject.path)) {
            exclude(group = "net.minecraft", module = "$minecraftDep")
        }
        vanillaMixinsImplementation(project(commonProject.path)) {
            exclude(group = "net.minecraft", module = "$minecraftDep")
        }
        add(vanillaLaunch.implementationConfigurationName, project(":SpongeAPI"))
        add(vanillaLaunch.implementationConfigurationName, vanillaModLauncherConfig)

        vanillaModLauncherConfig(project(":SpongeAPI"))
        vanillaModLauncherConfig("org.spongepowered:mixin:0.8")
        vanillaModLauncherConfig("org.ow2.asm:asm-util:6.2")
        vanillaModLauncherConfig("org.ow2.asm:asm-tree:6.2")
        vanillaModLauncherConfig("org.spongepowered:plugin-spi:0.1.1-SNAPSHOT") {
            exclude(group = "com.google.inject", module = "guice") // We use our own version
        }
        vanillaModLauncherConfig("org.apache.logging.log4j:log4j-api:2.11.2")
        vanillaModLauncherConfig("org.apache.logging.log4j:log4j-core:2.11.2")
        vanillaModLauncherRuntime("com.zaxxer:HikariCP:2.6.3")
        vanillaModLauncherRuntime("org.apache.logging.log4j:log4j-slf4j-impl:2.11.2")

        // Launch Dependencies - Needed to bootstrap the engine(s)
        // The ModLauncher compatibility launch layer
        vanillaModLauncherImplementation("cpw.mods:modlauncher:4.1.+") {
            exclude(group = "org.apache.logging.log4j")
        }
        vanillaModLauncherImplementation("org.ow2.asm:asm-commons:6.2")
        vanillaModLauncherImplementation("cpw.mods:grossjava9hacks:1.1.+") {
            exclude(group = "org.apache.logging.log4j")
        }
        vanillaModLauncherImplementation("net.minecraftforge:accesstransformers:1.0.+:shadowed") {
            exclude(group = "org.apache.logging.log4j")
        }
        vanillaModLauncherImplementation(vanillaModLauncherConfig)
        vanillaMixinsImplementation(vanillaModLauncherConfig)
        vanillaAccessorsImplementation(vanillaModLauncherConfig)

        // Annotation Processor
        vanillaAccessorsAnnotationProcessor(vanillaModLauncherImplementation)
        vanillaMixinsAnnotationProcessor(vanillaModLauncherImplementation)
        vanillaAccessorsAnnotationProcessor("org.spongepowered:mixin:0.8")
        vanillaMixinsAnnotationProcessor("org.spongepowered:mixin:0.8")

        testplugins?.apply {
            vanillaModLauncherRuntime(project(testplugins.path)) {
                exclude(group = "org.spongepowered")
            }
        }
    }


    tasks {
        jar {
            manifest {
                attributes(mapOf(
                        "Specification-Title" to "SpongeVanilla",
                        "Specification-Vendor" to "SpongePowered",
                        "Specification-Version" to apiProject.version,
                        "Implementation-Title" to project.name,
                        "Implementation-Version" to generateImplementationVersionString(apiProject.version as String, minecraftVersion, recommendedVersion),
                        "Implementation-Vendor" to "SpongePowered"
//                            "Implementation-Timestamp" to Instant.now().format("yyyy-MM-dd'T'HH:mm:ssZ")
                ))
            }
        }
    }

    license {
        (this as ExtensionAware).extra.apply {
            this["name"] = "Sponge"
            this["organization"] = organization
            this["url"] = projectUrl
        }
        header = apiProject.file("HEADER.txt")

        include("**/*.java")
        newLine = false
    }
}
val spongeForge: Project? = subprojects.find { "SpongeForge".equals(it.name) }
if (spongeForge != null) {
    project("SpongeForge") {
        val forgeProject = this
        apply {
            plugin("net.minecraftforge.gradle")
            plugin("org.spongepowered.mixin")
            plugin("java-library")
            plugin("maven-publish")
            plugin("idea")
            plugin("eclipse")
            plugin("net.minecrell.licenser")
        }

        val forgeDep: String by project
        val forgeOrg: String by project
        val forgeVersion: String by project

        description = "The SpongeAPI implementation for MinecraftForge"
        version = generateImplementationVersionString(apiProject.version as String, minecraftVersion, recommendedVersion, forgeVersion)

        val forgeMinecraftConfig by configurations.named("minecraft")
        val forgeLaunchConfig by configurations.register("launcher") {
            extendsFrom(forgeMinecraftConfig)
        }

        val forgeMain by sourceSets.named("main") {
            // implementation (compile) dependencies
            applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = accessors.get(), targetSource = this, implProject = forgeProject, dependencyConfigName = this.implementationConfigurationName)
            applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = launch.get(), targetSource = this, implProject = forgeProject, dependencyConfigName = this.implementationConfigurationName)
        }
        val forgeLaunch by sourceSets.register("launch") {
            // implementation (compile) dependencies
            applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = launch.get(), targetSource = this, implProject = forgeProject, dependencyConfigName = this.implementationConfigurationName)
            applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = main, targetSource = this, implProject = forgeProject, dependencyConfigName = this.implementationConfigurationName)
            applyNamedDependencyOnOutput(originProject = forgeProject, sourceAdding = this, targetSource = forgeMain, implProject = forgeProject, dependencyConfigName = forgeMain.implementationConfigurationName)
        }
        val forgeAccessors by sourceSets.register("accessors") {
            // implementation (compile) dependencies
            applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = launch.get(), targetSource = this, implProject = forgeProject, dependencyConfigName = this.implementationConfigurationName)
            applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = accessors.get(), targetSource = this, implProject = forgeProject, dependencyConfigName = this.implementationConfigurationName)
            applyNamedDependencyOnOutput(originProject = forgeProject, sourceAdding = this, targetSource = forgeMain, implProject = forgeProject, dependencyConfigName = forgeMain.implementationConfigurationName)
            applyNamedDependencyOnOutput(originProject = forgeProject, sourceAdding = forgeLaunch, targetSource = this, implProject = forgeProject, dependencyConfigName = this.implementationConfigurationName)
        }
        val forgeMixins by sourceSets.register("mixins") {
            // implementation (compile) dependencies
            applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = mixins.get(), targetSource = this, implProject = forgeProject, dependencyConfigName = this.implementationConfigurationName)
            applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = accessors.get(), targetSource = this, implProject = forgeProject, dependencyConfigName = this.implementationConfigurationName)
            applyNamedDependencyOnOutput(originProject = forgeProject, sourceAdding = forgeAccessors, targetSource = this, implProject = forgeProject, dependencyConfigName = this.implementationConfigurationName)
            applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = launch.get(), targetSource = this, implProject = forgeProject, dependencyConfigName = this.implementationConfigurationName)
            applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = main, targetSource = this, implProject = forgeProject, dependencyConfigName = this.implementationConfigurationName)
            applyNamedDependencyOnOutput(originProject = forgeProject, sourceAdding = forgeMain, targetSource = this, implProject = forgeProject, dependencyConfigName = this.implementationConfigurationName)
            applyNamedDependencyOnOutput(originProject = forgeProject, sourceAdding = forgeLaunch, targetSource = this, implProject = forgeProject, dependencyConfigName = this.implementationConfigurationName)
        }

        val forgeAccessorsAnnotationProcessor by configurations.named(forgeAccessors.annotationProcessorConfigurationName)
        val forgeAccessorsImplementation by configurations.named(forgeAccessors.implementationConfigurationName)
        val forgeMixinsImplementation by configurations.named(forgeMixins.implementationConfigurationName) {
            extendsFrom(forgeMinecraftConfig)
        }
        val forgeMixinsAnnotationProcessor by configurations.named(forgeMixins.annotationProcessorConfigurationName)

        configure<net.minecraftforge.gradle.userdev.UserDevExtension> {
            mappings(mcpType, mcpMappings)
            runs {
                create("server") {
                    workingDirectory(forgeProject.file("./run"))
                    args.addAll(listOf("nogui", "--launchTarget", "sponge_server_dev"))
                    main = "org.spongepowered.forge.modlauncher.Main"
                }

                create("client") {
                    environment("target", "client")
                    workingDirectory(forgeProject.file("./run"))
                    args.addAll(listOf("--launchTarget", "sponge_client_dev", "--version", "1.14.4", "--accessToken", "0"))
                    main = "org.spongepowered.forge.modlauncher.Main"
                }
            }
            commonProject.sourceSets["main"].resources
                    .filter { it.name.endsWith("_at.cfg") }
                    .files
                    .forEach {
                        accessTransformer(it)
                    }

            forgeProject.sourceSets["main"].resources
                    .filter { it.name.endsWith("_at.cfg") }
                    .files
                    .forEach { accessTransformer(it) }
        }

        dependencies {
            minecraft("$forgeOrg:$forgeDep:$minecraftVersion-$forgeVersion")

            api(launch.get().output)
            implementation(accessors.get().output)
            implementation(project(commonProject.path)) {
                exclude(group = "net.minecraft", module = "$minecraftDep")
            }
            forgeMixinsImplementation(project(commonProject.path)) {
                exclude(group = "net.minecraft", module = "$minecraftDep")
            }
            add(forgeLaunch.implementationConfigurationName, project(":SpongeAPI"))

            forgeLaunchConfig("org.spongepowered:mixin:0.8")
            forgeLaunchConfig("org.ow2.asm:asm-util:6.2")
            forgeLaunchConfig("org.ow2.asm:asm-tree:6.2")
            forgeLaunchConfig("org.spongepowered:plugin-spi:0.1.1-SNAPSHOT") {
                exclude(group = "com.google.inject", module = "guice") // We use our own version
            }
            forgeLaunchConfig("org.apache.logging.log4j:log4j-api:2.11.2")
            forgeLaunchConfig("org.apache.logging.log4j:log4j-core:2.11.2")
            runtime("com.zaxxer:HikariCP:2.6.3")
            runtime("org.apache.logging.log4j:log4j-slf4j-impl:2.11.2")

            // Launch Dependencies - Needed to bootstrap the engine(s)
            // The ModLauncher compatibility launch layer
            forgeMixinsImplementation(forgeLaunchConfig)
            forgeAccessorsImplementation(forgeLaunchConfig)

            // Annotation Processor
            forgeAccessorsAnnotationProcessor("org.spongepowered:mixin:0.8")
            forgeMixinsAnnotationProcessor("org.spongepowered:mixin:0.8")

            testplugins?.apply {
                add(forgeLaunch.runtimeConfigurationName, project(testplugins.path)) {
                    exclude(group = "org.spongepowered")
                }
            }
        }

        tasks {
            jar {
                manifest {
                    attributes(mapOf(
                            "Specification-Title" to "SpongeForge",
                            "Specification-Vendor" to "SpongePowered",
                            "Specification-Version" to apiProject.version,
                            "Implementation-Title" to project.name,
                            "Implementation-Version" to generateImplementationVersionString(apiProject.version as String, minecraftVersion, recommendedVersion, forgeVersion),
                            "Implementation-Vendor" to "SpongePowered"
//                            "Implementation-Timestamp" to Instant.now().format("yyyy-MM-dd'T'HH:mm:ssZ")
                    ))
                }
            }
        }


        license {
            (this as ExtensionAware).extra.apply {
                this["name"] = "Sponge"
                this["organization"] = organization
                this["url"] = projectUrl
            }
            header = apiProject.file("HEADER.txt")

            include("**/*.java")
            newLine = false
        }
    }
}
