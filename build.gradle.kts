import java.security.MessageDigest
import java.util.Locale

plugins {
    id("org.spongepowered.gradle.vanilla")
    `maven-publish`
    `java-library`
    eclipse
    id("org.cadixdev.licenser") version "0.5.0"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

val apiProject = project.project("SpongeAPI")
val commonProject = project
val minecraftVersion: String by project
val recommendedVersion: String by project

val asmVersion: String by project
val modlauncherVersion: String by project
val mixinVersion: String by project
val pluginSpiVersion: String by project
val guavaVersion: String by project
val junitVersion: String by project

minecraft {
    version(minecraftVersion)
    injectRepositories().set(false)
    project.sourceSets["main"].resources
            .filter { it.name.endsWith(".accesswidener") }
            .files
            .forEach {
                accessWidener(it)
                parent?.minecraft?.accessWidener(it)
            }
}

val commonManifest = the<JavaPluginConvention>().manifest {
    attributes(
        "Specification-Title" to "Sponge",
        "Specification-Vendor" to "SpongePowered",
        "Specification-Version" to apiProject.version,
        "Implementation-Title" to project.name,
        "Implementation-Version" to generateImplementationVersionString(apiProject.version as String, minecraftVersion, recommendedVersion),
        "Implementation-Vendor" to "SpongePowered"
    )
}

tasks {
    jar {
        manifest.from(commonManifest)
    }
    val mixinsJar by registering(Jar::class) {
        archiveClassifier.set("mixins")
        manifest.from(commonManifest)
        from(mixins.map { it.output })
    }
    val accessorsJar by registering(Jar::class) {
        archiveClassifier.set("accessors")
        manifest.from(commonManifest)
        from(accessors.map { it.output })
    }
    val launchJar by registering(Jar::class) {
        archiveClassifier.set("launch")
        manifest.from(commonManifest)
        from(launch.map { it.output })
    }
    val applaunchJar by registering(Jar::class) {
        archiveClassifier.set("applaunch")
        manifest.from(commonManifest)
        from(applaunch.map { it.output })
    }

    test {
        useJUnitPlatform()
    }

}

version = generateImplementationVersionString(apiProject.version as String, minecraftVersion, recommendedVersion)

// Configurations
val minecraftConfig by configurations.named("minecraft")
val minecraftClasspathConfig by configurations.named("minecraftClasspath")

val applaunchConfig by configurations.register("applaunch")

val launchConfig by configurations.register("launch") {
    extendsFrom(minecraftConfig)
    extendsFrom(minecraftClasspathConfig)
    extendsFrom(applaunchConfig)
}
val accessorsConfig by configurations.register("accessors") {
    extendsFrom(minecraftConfig)
    extendsFrom(launchConfig)
    extendsFrom(minecraftClasspathConfig)
}
val mixinsConfig by configurations.register("mixins") {
    extendsFrom(applaunchConfig)
    extendsFrom(launchConfig)
    extendsFrom(minecraftConfig)
    extendsFrom(minecraftClasspathConfig)
}

// create the sourcesets
val main by sourceSets

val applaunch by sourceSets.registering {
    applyNamedDependencyOnOutput(originProject = project, sourceAdding = this, targetSource = main, implProject = project, dependencyConfigName = main.implementationConfigurationName)
    project.dependencies {
        mixinsConfig(this@registering.output)
    }
    configurations.named(implementationConfigurationName) {
        extendsFrom(applaunchConfig)
    }
}
val launch by sourceSets.registering {
    applyNamedDependencyOnOutput(originProject = project, sourceAdding = applaunch.get(), targetSource = this, implProject = project, dependencyConfigName = this.implementationConfigurationName)
    project.dependencies {
        mixinsConfig(this@registering.output)
    }
    project.dependencies {
        implementation(this@registering.output)
    }

    configurations.named(implementationConfigurationName) {
        extendsFrom(launchConfig)
    }
}

val accessors by sourceSets.registering {
    applyNamedDependencyOnOutput(originProject = project, sourceAdding = launch.get(), targetSource = this, implProject = project, dependencyConfigName = this.implementationConfigurationName)
    applyNamedDependencyOnOutput(originProject = project, sourceAdding = this, targetSource = main, implProject = project, dependencyConfigName = main.implementationConfigurationName)
    configurations.named(implementationConfigurationName) {
        extendsFrom(accessorsConfig)
    }
}
val mixins by sourceSets.registering {
    applyNamedDependencyOnOutput(originProject = project, sourceAdding = launch.get(), targetSource = this, implProject = project, dependencyConfigName = this.implementationConfigurationName)
    applyNamedDependencyOnOutput(originProject = project, sourceAdding = applaunch.get(), targetSource = this, implProject = project, dependencyConfigName = this.implementationConfigurationName)
    applyNamedDependencyOnOutput(originProject = project, sourceAdding = accessors.get(), targetSource = this, implProject = project, dependencyConfigName = this.implementationConfigurationName)
    applyNamedDependencyOnOutput(originProject = project, sourceAdding = main, targetSource = this, implProject = project, dependencyConfigName = this.implementationConfigurationName)
    configurations.named(implementationConfigurationName) {
        extendsFrom(mixinsConfig)
    }
}

dependencies {
    // api
    api(project(":SpongeAPI"))

    // Database stuffs... likely needs to be looked at
    implementation("com.zaxxer:HikariCP:2.6.3")
    implementation("org.mariadb.jdbc:mariadb-java-client:2.0.3")
    implementation("com.h2database:h2:1.4.196")
    implementation("org.xerial:sqlite-jdbc:3.20.0")
    implementation("com.google.inject:guice:4.1.0")
    implementation("javax.inject:javax.inject:1")

    // ASM - required for generating event listeners
    implementation("org.ow2.asm:asm-util:$asmVersion")
    implementation("org.ow2.asm:asm-tree:$asmVersion")

    // Implementation-only Adventure
    implementation(platform("net.kyori:adventure-bom:4.7.0"))
    implementation("net.kyori:adventure-serializer-configurate4")

    // Launch Dependencies - Needed to bootstrap the engine(s)
    launchConfig(project(":SpongeAPI"))
    launchConfig("org.spongepowered:plugin-spi:$pluginSpiVersion")
    launchConfig("org.spongepowered:mixin:$mixinVersion")
    launchConfig("org.checkerframework:checker-qual:3.4.1")
    launchConfig("com.google.guava:guava:$guavaVersion") {
        exclude(group = "com.google.code.findbugs", module = "jsr305") // We don't want to use jsr305, use checkerframework
        exclude(group = "org.checkerframework", module = "checker-qual") // We use our own version
        exclude(group = "com.google.j2objc", module = "j2objc-annotations")
        exclude(group = "org.codehaus.mojo", module = "animal-sniffer-annotations")
        exclude(group = "com.google.errorprone", module = "error_prone_annotations")
    }
    launchConfig("com.google.inject:guice:4.1.0")
    launchConfig("javax.inject:javax.inject:1")
    launchConfig("com.google.code.gson:gson:2.8.0")
    launchConfig("org.ow2.asm:asm-tree:$asmVersion")
    launchConfig("org.ow2.asm:asm-util:$asmVersion")

    // Applaunch -- initialization that needs to occur without game access
    applaunchConfig("org.spongepowered:plugin-spi:$pluginSpiVersion")
    applaunchConfig("org.apache.logging.log4j:log4j-api:2.11.2")
    applaunchConfig("com.google.guava:guava:$guavaVersion")
    applaunchConfig(platform("org.spongepowered:configurate-bom:4.0.0"))
    applaunchConfig("org.spongepowered:configurate-core") {
        exclude(group = "org.checkerframework", module = "checker-qual") // We use our own version
    }
    applaunchConfig("org.spongepowered:configurate-hocon") {
        exclude(group = "org.spongepowered", module = "configurate-core")
        exclude(group = "org.checkerframework", module = "checker-qual") // We use our own version
    }
    applaunchConfig("org.spongepowered:configurate-jackson") {
        exclude(group = "org.spongepowered", module = "configurate-core")
        exclude(group = "org.checkerframework", module = "checker-qual") // We use our own version
    }
    applaunchConfig("org.apache.logging.log4j:log4j-core:2.11.2")

    mixinsConfig(sourceSets.named("main").map { it.output })
    add(mixins.get().implementationConfigurationName, project(":SpongeAPI"))

    // Tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
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
    val apiSplit = apiVersion.replace("-SNAPSHOT", "").split(".")
    val minor = if (apiSplit.size > 1) apiSplit[1] else (if (apiSplit.size > 0) apiSplit.last() else "-1")
    val apiReleaseVersion = "${apiSplit[0]}.$minor"
    return listOfNotNull(minecraftVersion, addedVersionInfo, "$apiReleaseVersion.$implRecommendedVersion").joinToString("-")
}
fun generatePlatformBuildVersionString(apiVersion: String, minecraftVersion: String, implRecommendedVersion: String, addedVersionInfo: String? = null): String {
    val isRelease = !implRecommendedVersion.endsWith("-SNAPSHOT")
    println("Detected Implementation Version $implRecommendedVersion as ${if (isRelease) "Release" else "Snapshot"}")
    val apiSplit = apiVersion.replace("-SNAPSHOT", "").split(".")
    val minor = if (apiSplit.size > 1) apiSplit[1] else (if (apiSplit.size > 0) apiSplit.last() else "-1")
    val apiReleaseVersion = "${apiSplit[0]}.$minor"
    val buildNumber = Integer.parseInt(System.getenv("BUILD_NUMBER") ?: "0")
    val implVersionAsReleaseCandidateOrRecommended: String = if (isRelease) {
        "$apiReleaseVersion.$implRecommendedVersion"
    } else {
        "$apiReleaseVersion.${implRecommendedVersion.replace("-SNAPSHOT", "")}-RC$buildNumber"
    }
    return listOfNotNull(minecraftVersion, addedVersionInfo, implVersionAsReleaseCandidateOrRecommended).joinToString("-")
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

    base {
        archivesBaseName = name.toLowerCase(Locale.ENGLISH)
    }

    tasks {
        withType(JavaCompile::class).configureEach {
            options.compilerArgs.addAll(listOf("-Xmaxerrs", "1000"))
            options.encoding = "UTF-8"
            if (JavaVersion.current().isJava10Compatible) {
                options.release.set(8)
            }
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    tasks.withType<AbstractArchiveTask> {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
    val spongeSnapshotRepo: String? by project
    val spongeReleaseRepo: String? by project
    tasks {

        withType<PublishToMavenRepository>().configureEach {
            onlyIf {
                (repository == publishing.repositories["GitHubPackages"] &&
                        !(rootProject.version as String).endsWith("-SNAPSHOT")) ||
                        (!spongeSnapshotRepo.isNullOrBlank()
                                && !spongeReleaseRepo.isNullOrBlank()
                                && repository == publishing.repositories["spongeRepo"]
                                && publication == publishing.publications["sponge"])

            }
        }
    }
    sourceSets.configureEach {
        if (project.name == "SpongeAPI" && "main" == this.name) {
            return@configureEach;
        }
        val sourceSet = this
        val sourceJarName: String = if ("main".equals(this.name)) "sourceJar" else "${this.name}SourceJar"
        tasks.register(sourceJarName, Jar::class.java) {
            group = "build"
            val classifier = if ("main".equals(sourceSet.name)) "sources" else "${sourceSet.name}sources"
            archiveClassifier.set(classifier)
            from(sourceSet.allJava)
        }
    }
    afterEvaluate {
        publishing {
            repositories {
                maven {
                    name = "GitHubPackages"
                    this.url = uri("https://maven.pkg.github.com/SpongePowered/${rootProject.name}")
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
                        username = spongeUsername ?: ""
                        password = spongePassword ?: ""
                    }
                }
            }
        }
    }
}

tasks {
    val jar by existing
    val sourceJar by existing
    val mixinsJar by existing
    val accessorsJar by existing
    val launchJar by existing
    val applaunchJar by existing
    shadowJar {
        mergeServiceFiles()
        archiveClassifier.set("dev")
        manifest {
            attributes(mapOf(
                    "Access-Widener" to "common.accesswidener",
                    "Multi-Release" to true
            ))
            from(commonManifest)
        }
        from(jar)
        from(sourceJar)
        from(mixinsJar)
        from(accessorsJar)
        from(launchJar)
        from(applaunchJar)
        dependencies {
            include(project(":"))
        }
    }
}
publishing {
    publications {
        register("sponge", MavenPublication::class) {
            from(components["java"])

            artifact(tasks["sourceJar"])
            artifact(tasks["mixinsJar"])
            artifact(tasks["accessorsJar"])
            artifact(tasks["launchJar"])
            artifact(tasks["applaunchJar"])
            artifact(tasks["applaunchSourceJar"])
            artifact(tasks["launchSourceJar"])
            artifact(tasks["mixinsSourceJar"])
            artifact(tasks["accessorsSourceJar"])
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
data class ProjectDep(val group: String, val module: String, val version: String)


val testplugins: Project? = subprojects.find { "testplugins".equals(it.name) }
if (testplugins != null) {
    project("testplugins") {
        apply {
            plugin("java-library")
            plugin("idea")
            plugin("eclipse")
            plugin("org.cadixdev.licenser")
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
        plugin("org.spongepowered.gradle.vanilla")
        plugin("java-library")
        plugin("maven-publish")
        plugin("org.cadixdev.licenser")
        plugin("com.github.johnrengelman.shadow")
    }

    description = "The SpongeAPI implementation for Vanilla Minecraft"
    version = generatePlatformBuildVersionString(apiProject.version as String, minecraftVersion, recommendedVersion)
    println("SpongeVanilla Version $version")

    val vanillaLibrariesConfig by configurations.register("libraries") {
    }
    val vanillaMinecraftConfig by configurations.named("minecraft")
    val vanillaMinecraftClasspathConfig by configurations.named("minecraftClasspath")
    val vanillaAppLaunchConfig by configurations.register("applaunch") {
        extendsFrom(vanillaLibrariesConfig)
    }
    val vanillaInstallerConfig by configurations.register("installer") {
    }

    val vanillaInstaller by sourceSets.register("installer") {
    }

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
        applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = accessors.get(), targetSource = this, implProject = vanillaProject, dependencyConfigName = this.implementationConfigurationName)
        applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = launch.get(), targetSource = this, implProject = vanillaProject, dependencyConfigName = this.implementationConfigurationName)
        applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = applaunch.get(), targetSource = this, implProject = vanillaProject, dependencyConfigName = this.implementationConfigurationName)
        configurations.named(implementationConfigurationName) {
            extendsFrom(vanillaLibrariesConfig)
        }
    }
    val vanillaLaunch by sourceSets.register("launch") {
        // implementation (compile) dependencies
        applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = launch.get(), targetSource = this, implProject = vanillaProject, dependencyConfigName = this.implementationConfigurationName)
        applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = applaunch.get(), targetSource = this, implProject = vanillaProject, dependencyConfigName = this.implementationConfigurationName)
        applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = main, targetSource = this, implProject = vanillaProject, dependencyConfigName = this.implementationConfigurationName)
        applyNamedDependencyOnOutput(originProject = vanillaProject, sourceAdding = this, targetSource = vanillaMain, implProject = vanillaProject, dependencyConfigName = vanillaMain.implementationConfigurationName)

        configurations.named(implementationConfigurationName) {
            extendsFrom(vanillaAppLaunchConfig)
            extendsFrom(vanillaMinecraftConfig)
            extendsFrom(vanillaMinecraftClasspathConfig)
        }
    }
    val vanillaMixins by sourceSets.register("mixins") {
        // implementation (compile) dependencies
        applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = mixins.get(), targetSource = this, implProject = vanillaProject, dependencyConfigName = this.implementationConfigurationName)
        applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = accessors.get(), targetSource = this, implProject = vanillaProject, dependencyConfigName = this.implementationConfigurationName)
        applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = launch.get(), targetSource = this, implProject = vanillaProject, dependencyConfigName = this.implementationConfigurationName)
        applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = applaunch.get(), targetSource = this, implProject = vanillaProject, dependencyConfigName = this.implementationConfigurationName)
        applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = main, targetSource = this, implProject = vanillaProject, dependencyConfigName = this.implementationConfigurationName)
        applyNamedDependencyOnOutput(originProject = vanillaProject, sourceAdding = vanillaMain, targetSource = this, implProject = vanillaProject, dependencyConfigName = this.implementationConfigurationName)
        applyNamedDependencyOnOutput(originProject = vanillaProject, sourceAdding = vanillaLaunch, targetSource = this, implProject = vanillaProject, dependencyConfigName = this.implementationConfigurationName)
    }
    val vanillaAppLaunch by sourceSets.register("applaunch") {
        // implementation (compile) dependencies
        applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = applaunch.get(), targetSource = this, implProject = vanillaProject, dependencyConfigName = this.implementationConfigurationName)
        applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = launch.get(), targetSource = vanillaLaunch, implProject = vanillaProject, dependencyConfigName = this.implementationConfigurationName)
        applyNamedDependencyOnOutput(originProject = vanillaProject, sourceAdding = vanillaInstaller, targetSource = this, implProject = vanillaProject, dependencyConfigName = this.implementationConfigurationName)
        applyNamedDependencyOnOutput(originProject = vanillaProject, sourceAdding = this, targetSource = vanillaLaunch, implProject = vanillaProject, dependencyConfigName = vanillaLaunch.implementationConfigurationName)
        // runtime dependencies - literally add the rest of the project, because we want to launch the game
        applyNamedDependencyOnOutput(originProject = vanillaProject, sourceAdding = vanillaMixins, targetSource = this, implProject = vanillaProject, dependencyConfigName = this.runtimeOnlyConfigurationName)
        applyNamedDependencyOnOutput(originProject = vanillaProject, sourceAdding = vanillaLaunch, targetSource = this, implProject = vanillaProject, dependencyConfigName = this.runtimeOnlyConfigurationName)
        applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = mixins.get(), targetSource = this, implProject = vanillaProject, dependencyConfigName = this.runtimeOnlyConfigurationName)
        applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = main, targetSource = this, implProject = vanillaProject, dependencyConfigName = this.runtimeOnlyConfigurationName)
        applyNamedDependencyOnOutput(originProject = commonProject, sourceAdding = accessors.get(), targetSource = this, implProject = vanillaProject, dependencyConfigName = this.runtimeOnlyConfigurationName)
        applyNamedDependencyOnOutput(originProject = vanillaProject, sourceAdding = vanillaMain, targetSource = this, implProject = vanillaProject, dependencyConfigName = this.runtimeOnlyConfigurationName)
    }
    val vanillaMixinsImplementation by configurations.named(vanillaMixins.implementationConfigurationName) {
        extendsFrom(vanillaAppLaunchConfig)
        extendsFrom(vanillaMinecraftConfig)
        extendsFrom(vanillaMinecraftClasspathConfig)
    }
    val generator by sourceSets.registering {
        tasks.named(compileJavaTaskName, JavaCompile::class) {
            options.release.set(11)
            if (!JavaVersion.current().isJava11Compatible) {
                javaCompiler.set(javaToolchains.compilerFor { languageVersion.set(JavaLanguageVersion.of(11)) })
            }
        }

        configurations.named(implementationConfigurationName) {
            extendsFrom(vanillaMinecraftConfig)
            extendsFrom(vanillaMinecraftClasspathConfig)
        }
    }

    configurations.named(vanillaMixins.annotationProcessorConfigurationName)
    configurations.named(vanillaInstaller.implementationConfigurationName) {
        extendsFrom(vanillaInstallerConfig)
    }
    configurations.named(vanillaAppLaunch.implementationConfigurationName) {
        extendsFrom(vanillaAppLaunchConfig)
        extendsFrom(launchConfig)
    }
    val vanillaAppLaunchRuntime by configurations.named(vanillaAppLaunch.runtimeOnlyConfigurationName)

    minecraft {
        version(minecraftVersion)
        injectRepositories().set(false)
        runs {
            // Full development environment
            sequenceOf(8, 11, 15).forEach {
                server("runJava${it}Server") {
                    args("--nogui", "--launchTarget", "sponge_server_dev")
                    // ideaModule("${rootProject.name}.${project.name}.applaunch")
                }
                client("runJava${it}Client") {
                    args("--launchTarget", "sponge_client_dev")
                    // ideaModule("${rootProject.name}.${project.name}.applaunch")
                }
                tasks.named("runJava${it}Server", JavaExec::class).configure {
                    javaLauncher.set(javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(it)) })
                }
                tasks.named("runJava${it}Client", JavaExec::class).configure {
                    javaLauncher.set(javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(it)) })
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
                workingDirectory().set(vanillaProject.file("run/"))
                if (System.getProperty("idea.active")?.toBoolean() == true) {
                    // IntelliJ does not properly report its compatibility
                    jvmArgs("-Dterminal.ansi=true", "-Djansi.mode=force")
                }
                jvmArgs("-Dlog4j.configurationFile=log4j2_dev.xml")
                allJvmArgumentProviders() += CommandLineArgumentProvider {
                    // Resolve the Mixin artifact for use as a reload agent
                    val mixinJar = vanillaAppLaunchConfig.resolvedConfiguration
                            .getFiles { it.name == "mixin" && it.group == "org.spongepowered" }
                            .firstOrNull()

                    val base = if (mixinJar != null) {
                        listOf("-javaagent:$mixinJar")
                    } else {
                        emptyList()
                    }

                    // Then add necessary module cracks
                    if (!this.name.contains("Java8")) {
                        base + listOf(
                                "--illegal-access=deny", // enable strict mode in prep for Java 16
                                "--add-exports=java.base/sun.security.util=ALL-UNNAMED", // ModLauncher
                                "--add-opens=java.base/java.util.jar=ALL-UNNAMED", // ModLauncher
                                "--add-opens=java.base/java.lang=ALL-UNNAMED" // Guice
                        )
                    } else {
                        base
                    }
                }
                mainClass().set("org.spongepowered.vanilla.applaunch.Main")
                classpath().from(vanillaAppLaunch.runtimeClasspath, vanillaAppLaunch.output)
            }
        }
        commonProject.sourceSets["main"].resources
                .filter { it.name.endsWith(".accesswidener") }
                .files
                .forEach {
                    accessWidener(it)
                }

        vanillaProject.sourceSets["main"].resources
                .filter { it.name.endsWith(".accesswidener") }
                .files
                .forEach { accessWidener(it) }
    }

    dependencies {
        val jlineVersion: String by project
        api(launch.map { it.output })
        implementation(accessors.map { it.output })
        implementation(project(commonProject.path))

        vanillaMixinsImplementation(project(commonProject.path))
        add(vanillaLaunch.implementationConfigurationName, project(":SpongeAPI"))

        vanillaInstallerConfig("com.google.code.gson:gson:2.8.0")
        vanillaInstallerConfig("org.spongepowered:configurate-hocon:4.0.0")
        vanillaInstallerConfig("org.spongepowered:configurate-core:4.0.0")
        vanillaInstallerConfig("net.sf.jopt-simple:jopt-simple:5.0.3")
        vanillaInstallerConfig("org.tinylog:tinylog-api:2.2.1")
        vanillaInstallerConfig("org.tinylog:tinylog-impl:2.2.1")
        // Override ASM versions, and explicitly declare dependencies so ASM is excluded from the manifest.
        val asmExclusions = sequenceOf("-commons", "-tree", "-analysis", "")
                .map { "asm$it" }
                .onEach {
            vanillaInstallerConfig("org.ow2.asm:$it:$asmVersion")
        }.toSet()
        vanillaInstallerConfig("org.cadixdev:atlas:0.2.1") {
            asmExclusions.forEach { exclude(group = "org.ow2.asm", module = it) } // Use our own ASM version
        }
        vanillaInstallerConfig("org.cadixdev:lorenz-asm:0.5.6") {
            asmExclusions.forEach { exclude(group = "org.ow2.asm", module = it) } // Use our own ASM version
        }
        vanillaInstallerConfig("org.cadixdev:lorenz-io-proguard:0.5.6")

        vanillaAppLaunchConfig(project(":SpongeAPI"))
        vanillaAppLaunchConfig(platform("net.kyori:adventure-bom:4.7.0"))
        vanillaAppLaunchConfig("net.kyori:adventure-serializer-configurate4")
        vanillaAppLaunchConfig("org.spongepowered:mixin:$mixinVersion")
        vanillaAppLaunchConfig("org.ow2.asm:asm-util:$asmVersion")
        vanillaAppLaunchConfig("org.ow2.asm:asm-tree:$asmVersion")
        vanillaAppLaunchConfig("com.google.guava:guava:$guavaVersion")
        vanillaAppLaunchConfig("org.spongepowered:plugin-spi:$pluginSpiVersion")
        vanillaAppLaunchConfig("javax.inject:javax.inject:1")
        vanillaAppLaunchConfig("org.apache.logging.log4j:log4j-api:2.11.2")
        vanillaAppLaunchConfig("org.apache.logging.log4j:log4j-core:2.11.2")
        vanillaAppLaunchConfig("com.lmax:disruptor:3.4.2")
        vanillaAppLaunchConfig("com.zaxxer:HikariCP:2.6.3")
        vanillaAppLaunchConfig("org.apache.logging.log4j:log4j-slf4j-impl:2.11.2")
        vanillaAppLaunchConfig(platform("org.spongepowered:configurate-bom:4.0.0"))
        vanillaAppLaunchConfig("org.spongepowered:configurate-core") {
            exclude(group = "org.checkerframework", module = "checker-qual")
        }
        vanillaAppLaunchConfig("org.spongepowered:configurate-hocon") {
            exclude(group = "org.spongepowered", module = "configurate-core")
            exclude(group = "org.checkerframework", module = "checker-qual")
        }
        vanillaAppLaunchConfig("org.spongepowered:configurate-jackson") {
            exclude(group = "org.spongepowered", module = "configurate-core")
            exclude(group = "org.checkerframework", module = "checker-qual")
        }

        vanillaLibrariesConfig("net.minecrell:terminalconsoleappender:1.2.0")
        vanillaLibrariesConfig("org.jline:jline-terminal:$jlineVersion")
        vanillaLibrariesConfig("org.jline:jline-reader:$jlineVersion")
        vanillaLibrariesConfig("org.jline:jline-terminal-jansi:$jlineVersion") {
            exclude("org.fusesource.jansi") // Use our own JAnsi
        }
        // A newer version is required to make log4j happy
        vanillaLibrariesConfig("org.fusesource.jansi:jansi:2.3.1")

        // Launch Dependencies - Needed to bootstrap the engine(s)
        // The ModLauncher compatibility launch layer
        vanillaAppLaunchConfig("cpw.mods:modlauncher:$modlauncherVersion") {
            exclude(group = "org.apache.logging.log4j")
        }
        vanillaAppLaunchConfig("org.ow2.asm:asm-commons:$asmVersion")
        vanillaAppLaunchConfig("cpw.mods:grossjava9hacks:1.1.+") {
            exclude(group = "org.apache.logging.log4j")
        }
        vanillaAppLaunchConfig("net.fabricmc:access-widener:1.0.2") {
            exclude(group = "org.apache.logging.log4j")
        }

        testplugins?.apply {
            vanillaAppLaunchRuntime(project(testplugins.path)) {
                exclude(group = "org.spongepowered")
            }
        }

        "generatorImplementation"("com.squareup:javapoet:1.13.0")
        "generatorImplementation"("com.github.javaparser:javaparser-core:3.19.0")
        "generatorImplementation"("org.tinylog:tinylog-api:2.2.1")
        "generatorRuntimeOnly"("org.tinylog:tinylog-impl:2.2.1")
    }

    val vanillaManifest = the<JavaPluginConvention>().manifest {
        attributes(
            "Specification-Title" to "SpongeVanilla",
            "Specification-Vendor" to "SpongePowered",
            "Specification-Version" to apiProject.version,
            "Implementation-Title" to project.name,
            "Implementation-Version" to generatePlatformBuildVersionString(apiProject.version as String, minecraftVersion, recommendedVersion),
            "Implementation-Vendor" to "SpongePowered"
        )
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

        val installerTemplateSource = vanillaProject.file("src/installer/templates")
        val installerTemplateDest = vanillaProject.layout.buildDirectory.dir("generated/sources/installerTemplates")
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

        val installerResources = vanillaProject.layout.buildDirectory.dir("generated/resources/installer")
        vanillaInstaller.resources.srcDir(installerResources)
        val emitDependencies by registering(OutputDependenciesToJson::class) {
            group = "sponge"
            // everything in applaunch
            configuration.set(vanillaAppLaunchConfig)
            // except what we're providing through the installer
            excludeConfiguration.set(vanillaInstallerConfig)
            // for accesstransformers
            allowedClassifiers.add("service")

            outputFile.set(installerResources.map { it.file("libraries.json") })
        }
        named(vanillaInstaller.processResourcesTaskName).configure {
            dependsOn(emitDependencies)
        }

        shadowJar {
            mergeServiceFiles()

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
            from(commonProject.tasks.jar)
            from(commonProject.tasks.named("mixinsJar"))
            from(commonProject.tasks.named("accessorsJar"))
            from(commonProject.tasks.named("launchJar"))
            from(commonProject.tasks.named("applaunchJar"))
            from(jar)
            from(vanillaInstallerJar)
            from(vanillaAppLaunchJar)
            from(vanillaLaunchJar)
            from(vanillaMixinsJar)
            from(vanillaInstallerConfig)
            dependencies {
                include(project(":"))
                include(project(":SpongeAPI"))
            }

            // We cannot have modules in a shaded jar
            exclude("META-INF/versions/*/module-info.class")
            exclude("module-info.class")
        }
        assemble {
            dependsOn(shadowJar)
        }

        val apiBase = apiProject.file("src/main/java/")
        val temporaryLicenseHeader = project.buildDir.resolve("api-gen-license-header.txt")
        register("generateApiData", JavaExec::class) {
            group = "sponge"
            description = "Generate API Catalog classes"
            if (!JavaVersion.current().isJava11Compatible) {
                javaLauncher.set(project.javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(11)) })
            }

            classpath(generator.map { it.output }, generator.map { it.runtimeClasspath })
            mainClass.set("org.spongepowered.vanilla.generator.GeneratorMain")
            args(apiBase.canonicalPath, temporaryLicenseHeader.canonicalPath)

            doFirst {
                // Write a template-expanded license header to the temporary file
                license.header.bufferedReader(Charsets.UTF_8).use { reader ->
                    val template = groovy.text.GStringTemplateEngine().createTemplate(reader)

                    val propertyMap = (license as ExtensionAware).extra.properties.toMutableMap()
                    propertyMap["name"] = "SpongeAPI"
                    val out = template.make(propertyMap)

                    temporaryLicenseHeader.bufferedWriter(Charsets.UTF_8).use { writer ->
                        out.writeTo(writer)
                    }
                }
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
}

abstract class OutputDependenciesToJson: DefaultTask() {

    companion object {
        val GSON: com.google.gson.Gson = com.google.gson.GsonBuilder().also {
            it.setPrettyPrinting()
        }.create()
    }

    /**
     * A single dependency
     */
    data class DependencyDescriptor(val group: String, val module: String, val version: String, val md5: String)

    /**
     * A manifest containing a list of dependencies.
     *
     * At runtime, transitive dependencies won't be traversed, so this needs to
     * include direct + transitive depends.
     */
    data class DependencyManifest(val dependencies: List<DependencyDescriptor>)

    /**
     * Configuration to gather depenency artifacts from
     */
    @get:org.gradle.api.tasks.Input
    abstract val configuration: Property<Configuration>

    /**
     * Excludes configuration, to remove certain entries from dependencies and transitive dependencies of [configuration]
     */
    @get:org.gradle.api.tasks.Input
    abstract val excludeConfiguration: Property<Configuration>

    /**
     * Classifiers to include in the dependency manifest. The empty string identifies no classifier.
     */
    @get:org.gradle.api.tasks.Input
    abstract val allowedClassifiers: SetProperty<String>

    @get:org.gradle.api.tasks.OutputFile
    abstract val outputFile: RegularFileProperty

    init {
        allowedClassifiers.add("")
    }

    @org.gradle.api.tasks.TaskAction
    fun generateDependenciesJson() {
        val foundConfig = if (this.excludeConfiguration.isPresent) {
            val config = this.configuration.get().copyRecursive()
            val excludes = this.excludeConfiguration.get()
            excludes.allDependencies.forEach {
                config.exclude(group = it.group, module = it.name)
            }
            config
        } else {
            this.configuration.get()
        }

        val manifest = foundConfig.resolvedConfiguration.firstLevelModuleDependencies.asSequence()
                .flatMap { it.allModuleArtifacts.asSequence() }
                // only jars with the allowed classifiers
                .filter { it.extension == "jar" && allowedClassifiers.get().contains(it.classifier ?: "") }
                .filter { it.moduleVersion.id.name != "SpongeAPI" }
                .distinct()
                .map { dependency ->

                    val ident = dependency.moduleVersion.id
                    val version = (dependency.id.componentIdentifier as? ModuleComponentIdentifier)?.version ?: ident.version
                    // Get file input stream for reading the file content
                    val md5hash = dependency.file.inputStream().use {
                        val hasher = MessageDigest.getInstance("MD5")
                        val buf = ByteArray(4096)
                        var read: Int = it.read(buf)
                        while (read != -1) {
                            hasher.update(buf, 0, read)
                            read = it.read(buf)
                        }

                        hasher.digest().joinToString("") { "%02x".format(it) }
                    }

                    // create descriptor
                    DependencyDescriptor(
                            group = ident.group,
                            module = ident.name,
                            version = version,
                            md5 = md5hash
                    )
                }.toList().run {
                    DependencyManifest(this)
                }

        logger.info("Writing version manifest to ${outputFile.get().asFile}")
        this.outputFile.get().asFile.bufferedWriter(Charsets.UTF_8).use {
            GSON.toJson(manifest, it)
        }
    }
}