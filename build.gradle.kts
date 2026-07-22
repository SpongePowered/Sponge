import org.jetbrains.gradle.ext.compiler
import org.jetbrains.gradle.ext.delegateActions
import org.jetbrains.gradle.ext.settings
import org.spongepowered.gradle.vanilla.task.DecompileJarTask
import java.util.Locale

plugins {
    `maven-publish`
    `java-library`
    eclipse
    id("org.spongepowered.gradle.vanilla")
    alias(libs.plugins.shadow)
    alias(apiLibs.plugins.spongeGradle.convention) apply false // for version json generation
    alias(libs.plugins.indra.licenserSpotless) version apiLibs.versions.indra.get()
    id("implementation-structure")
    id(apiLibs.plugins.ideaExt.get().pluginId)
    alias(libs.plugins.versions)
    alias(libs.plugins.blossom)
}

val apiVersion: String by project
val apiJavaTarget: String by project
val minecraftVersion: String by project
val recommendedVersion: String by project
val organization: String by project
val projectUrl: String by project

version = spongeImpl.generateImplementationVersionString(apiVersion, minecraftVersion, recommendedVersion)

val commonManifest = java.manifest {
    attributes(
        "Specification-Title" to "Sponge",
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

// SpongeCommon libraries
val serviceLibrariesConfig = configurations.register("serviceLibraries")
val gameLibrariesConfig = configurations.register("gameLibraries") {
    extendsFrom(configurations.minecraft.get())
    extendsFrom(configurations.api.get())
}

// Layers
val serviceLayerConfig = configurations.register("serviceLayer") {
    extendsFrom(serviceLibrariesConfig.get())
}
val gameLayerConfig = configurations.register("gameLayer") {
    extendsFrom(serviceLayerConfig.get())
    extendsFrom(gameLibrariesConfig.get())
}

// SpongeCommon source sets

// Service layer
// applaunchConfig is also used by vanilla installer, hence the separate sourceset
val applaunchConf = sourceSets.register("applaunchConfig")
val applaunch by sourceSets.registering {
    spongeImpl.addDependencyToImplementation(applaunchConf.get(), this)

    configurations.named(implementationConfigurationName) {
        extendsFrom(serviceLayerConfig.get())
    }

    blossom.javaSources {
        property("pluginSpiVersion", apiLibs.pluginSpi.get().version)
    }
}

// Game layer
val accessors by sourceSets.registering {
    configurations.named(implementationConfigurationName) {
        extendsFrom(gameLayerConfig.get())
    }
}
val mixins by sourceSets.registering {
    spongeImpl.addDependencyToImplementation(applaunchConf.get(), this)
    spongeImpl.addDependencyToImplementation(applaunch.get(), this)
    spongeImpl.addDependencyToImplementation(accessors.get(), this)

    configurations.named(implementationConfigurationName) {
        extendsFrom(gameLayerConfig.get())
    }
}
val main by sourceSets.named("main") {
    spongeImpl.addDependencyToImplementation(applaunchConf.get(), this)
    spongeImpl.addDependencyToImplementation(applaunch.get(), this)
    spongeImpl.addDependencyToImplementation(accessors.get(), this)

    spongeImpl.addDependencyToImplementation(this, mixins.get())

    configurations.named(implementationConfigurationName) {
        extendsFrom(gameLayerConfig.get())
    }
}


dependencies {
    val service = serviceLibrariesConfig.name
    service(apiLibs.checkerQual)
    service(libs.guava) {
        exclude(group = "com.google.errorprone", module = "error_prone_annotations")
        exclude(group = "org.checkerframework", module = "checker-qual")
    }
    service(libs.log4j.api)
    service(libs.log4j.core)
    service(libs.log4j.jpl)
    service(apiLibs.pluginSpi)

    service(libs.accessWidener)
    service(libs.asm.commons)
    service(libs.asm.util)
    service(libs.asm.tree)
    service(libs.mixin)
    service(libs.mixinextras.common)

    api("org.spongepowered:spongeapi:${apiVersion}")
    val game = gameLibrariesConfig.name
    game(libs.javaxInject)
    game(platform(apiLibs.adventure.bom))
    game(libs.adventure.serializerConfigurate4)
    game(libs.adventure.serializerAnsi)

    // Optional
    val applaunchCompileOnly = applaunch.get().compileOnlyConfigurationName
    applaunchCompileOnly(libs.jacoco.core)

    // Tests
    testImplementation(platform(apiLibs.junit.bom))
    testImplementation(apiLibs.junit.api)
    testImplementation(apiLibs.junit.params)
    testRuntimeOnly(apiLibs.junit.engine)
    testRuntimeOnly(apiLibs.junit.launcher)

    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junitJupiter) {
        exclude(group = "org.junit.jupiter", module = "junit-jupiter-api")
    }
}

minecraft {
    accessWideners(main.resources.filter { it.name.endsWith(".accesswidener") })
}

idea {
    project.settings {
        delegateActions {
            delegateBuildRunToGradle = false
            testRunner = org.jetbrains.gradle.ext.ActionDelegationConfig.TestRunner.GRADLE
        }
        compiler {
            addNotNullAssertions = false
            useReleaseOption = true
            parallelCompilation = true
        }
    }
}

sourceSets {
    test {
        blossom.resources {
            property("apiVersion", apiVersion.replace("-SNAPSHOT", ""))
        }
    }
}

allprojects {
    configurations.configureEach {
        resolutionStrategy.dependencySubstitution {
            // https://github.com/zml2008/guice/tree/backport/5.0.1
            substitute(module("com.google.inject:guice:5.0.1"))
                    .because("We need to run against Guava 21")
                    .using(module("ca.stellardrift.guice-backport:guice:5.0.1"))
        }
    }

    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "net.kyori.indra.licenser.spotless")

    base {
        archivesName = name.lowercase(Locale.ENGLISH)
    }

    plugins.withId("org.spongepowered.gradle.vanilla") {
        minecraft {
            version(minecraftVersion)
            injectRepositories(false)
        }

        dependencies {
            decompiler(libs.vineflower)
        }

        tasks.named("decompile", DecompileJarTask::class) {
            extraFernFlowerArgs.put("win", "0")
        }
    }

    java {
        val targetJavaVersion = JavaVersion.toVersion(apiJavaTarget.toInt())
        sourceCompatibility = targetJavaVersion
        targetCompatibility = targetJavaVersion
        if (JavaVersion.current() < targetJavaVersion) {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(apiJavaTarget.toInt()))
            }
        }
    }

    tasks.withType<AbstractArchiveTask> {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }

    spotless {
        java {
            toggleOffOn("@formatter:off", "@formatter:on")
            endWithNewline()
            indentWithSpaces(4)
            trimTrailingWhitespace()
            removeUnusedImports()
            importOrderFile(rootProject.file("SpongeAPI/extra/eclipse/sponge_eclipse.importorder"))
            targetExclude("build/generated/**/*") // exclude generated content
        }
        kotlinGradle {
            endWithNewline()
            indentWithSpaces(4)
            trimTrailingWhitespace()
        }
    }

    indraSpotlessLicenser {
        licenseHeaderFile(rootProject.file("HEADER.txt"))

        property("name", "Sponge")
        property("organization", organization)
        property("url", projectUrl)
    }

    val spongeSnapshotRepo: String? by project
    val spongeReleaseRepo: String? by project
    tasks {
        withType(JavaCompile::class).configureEach {
            options.compilerArgs.addAll(listOf("-Xmaxerrs", "1000"))
            options.encoding = "UTF-8"
            options.release.set(apiJavaTarget.toInt())
        }

        withType(PublishToMavenRepository::class).configureEach {
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
        val sourceSet = this
        val isMain = "main" == sourceSet.name

        val sourcesJarName: String = if (isMain) "sourcesJar" else (sourceSet.name + "SourcesJar")
        tasks.register(sourcesJarName, Jar::class.java) {
            group = "build"
            val classifier = if (isMain) "sources" else (sourceSet.name + "-sources")
            archiveClassifier.set(classifier)
            from(sourceSet.allJava)
        }
    }

    tasks.register("printConfigsHierarchy") {
        group = "debug"
        doLast {
            configurations.forEach { conf: Configuration  ->
                val seen = mutableSetOf<Configuration>()
                println("Parents of ${conf.name}:")
                printParents(conf, "", seen)
            }
        }
    }

    tasks.register("printConfigsResolution") {
        group = "debug"
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

tasks {
    jar {
        manifest.from(commonManifest)
    }

    val mixinsJar by registering(Jar::class) {
        group = "build"
        archiveClassifier.set("mixins")
        manifest.from(commonManifest)
        from(mixins.map { it.output })
    }
    val accessorsJar by registering(Jar::class) {
        group = "build"
        archiveClassifier.set("accessors")
        manifest.from(commonManifest)
        from(accessors.map { it.output })
    }
    val applaunchJar by registering(Jar::class) {
        group = "build"
        archiveClassifier.set("applaunch")
        manifest.from(commonManifest)
        from(applaunch.map { it.output })
    }

    shadowJar {
        archiveClassifier.set("dev")

        mergeServiceFiles()
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        configurations = listOf()

        manifest {
            attributes(mapOf(
                "Access-Widener" to "common.accesswidener",
                "Multi-Release" to true
            ))
            from(commonManifest)
        }

        from(mixins.map { it.output })
        from(accessors.map { it.output })
        from(applaunch.map { it.output })
        from(applaunchConf.map { it.output })
    }

    test {
        // tests can only be run in subprojects
        enabled = false
    }

    check {
        dependsOn(gradle.includedBuild("SpongeAPI").task(":check"))
    }
}

publishing {
    publications {
        register("sponge", MavenPublication::class) {
            from(components["java"])
            artifact(tasks["sourcesJar"])

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
