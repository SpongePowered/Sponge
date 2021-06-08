import java.util.Locale

plugins {
    `maven-publish`
    `java-library`
    eclipse
    id("org.spongepowered.gradle.vanilla")
    id("org.cadixdev.licenser")
    id("com.github.johnrengelman.shadow")
    id("org.spongepowered.gradle.sponge.dev") apply false // for version json generation
    id("implementation-structure")
    id("org.jetbrains.gradle.plugin.idea-ext")
    id("com.github.ben-manes.versions")
}

val commonProject = project
val apiVersion: String by project
val minecraftVersion: String by project
val recommendedVersion: String by project

val apiAdventureVersion: String by project
val apiConfigurateVersion: String by project
val asmVersion: String by project
val log4jVersion: String by project
val modlauncherVersion: String by project
val mixinVersion: String by project
val pluginSpiVersion: String by project
val guavaVersion: String by project
val junitVersion: String by project
val timingsVersion: String by project

val commonManifest = the<JavaPluginConvention>().manifest {
    attributes(
        "Specification-Title" to "Sponge",
        "Specification-Vendor" to "SpongePowered",
        "Specification-Version" to apiVersion,
        "Implementation-Title" to project.name,
        "Implementation-Version" to spongeImpl.generateImplementationVersionString(apiVersion, minecraftVersion, recommendedVersion),
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

    check {
        dependsOn(gradle.includedBuild("SpongeAPI").task(":check"))
    }

    prepareWorkspace {
        dependsOn(gradle.includedBuild("SpongeAPI").task(":genEventImpl"))
    }

}

version = spongeImpl.generateImplementationVersionString(apiVersion, minecraftVersion, recommendedVersion)

// Configurations
val applaunchConfig by configurations.register("applaunch")

val launchConfig by configurations.register("launch") {
    extendsFrom(configurations.minecraft.get())
    extendsFrom(applaunchConfig)
}
val accessorsConfig by configurations.register("accessors") {
    extendsFrom(launchConfig)
}
val mixinsConfig by configurations.register("mixins") {
    extendsFrom(applaunchConfig)
    extendsFrom(launchConfig)
}

// create the sourcesets
val main by sourceSets

val applaunch by sourceSets.registering {
    spongeImpl.applyNamedDependencyOnOutput(project, this, main, project, main.implementationConfigurationName)
    project.dependencies {
        mixinsConfig(this@registering.output)
    }
    configurations.named(implementationConfigurationName) {
        extendsFrom(applaunchConfig)
    }
}
val launch by sourceSets.registering {
    spongeImpl.applyNamedDependencyOnOutput(project, applaunch.get(), this, project, this.implementationConfigurationName)
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
    spongeImpl.applyNamedDependencyOnOutput(project, launch.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(project, this, main, project, main.implementationConfigurationName)
    configurations.named(implementationConfigurationName) {
        extendsFrom(accessorsConfig)
    }
}
val mixins by sourceSets.registering {
    spongeImpl.applyNamedDependencyOnOutput(project, launch.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(project, applaunch.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(project, accessors.get(), this, project, this.implementationConfigurationName)
    spongeImpl.applyNamedDependencyOnOutput(project, main, this, project, this.implementationConfigurationName)
    configurations.named(implementationConfigurationName) {
        extendsFrom(mixinsConfig)
    }
}

dependencies {
    // api
    api("org.spongepowered:spongeapi:$apiVersion")

    // Database stuffs... likely needs to be looked at
    implementation("com.zaxxer:HikariCP:2.6.3")
    implementation("org.mariadb.jdbc:mariadb-java-client:2.0.3")
    implementation("com.h2database:h2:1.4.196")
    implementation("org.xerial:sqlite-jdbc:3.20.0")
    implementation("javax.inject:javax.inject:1")

    // Timings
    implementation("org.spongepowered:timings:$timingsVersion")

    // ASM - required for generating event listeners
    implementation("org.ow2.asm:asm-util:$asmVersion")
    implementation("org.ow2.asm:asm-tree:$asmVersion")

    // Implementation-only Adventure
    implementation(platform("net.kyori:adventure-bom:$apiAdventureVersion"))
    implementation("net.kyori:adventure-serializer-configurate4")

    // Launch Dependencies - Needed to bootstrap the engine(s)
    launchConfig("org.spongepowered:spongeapi:$apiVersion")
    launchConfig("org.spongepowered:plugin-spi:$pluginSpiVersion")
    launchConfig("org.spongepowered:mixin:$mixinVersion")
    launchConfig("org.checkerframework:checker-qual:3.13.0")
    launchConfig("com.google.guava:guava:$guavaVersion") {
        exclude(group = "com.google.code.findbugs", module = "jsr305") // We don't want to use jsr305, use checkerframework
        exclude(group = "org.checkerframework", module = "checker-qual") // We use our own version
        exclude(group = "com.google.j2objc", module = "j2objc-annotations")
        exclude(group = "org.codehaus.mojo", module = "animal-sniffer-annotations")
        exclude(group = "com.google.errorprone", module = "error_prone_annotations")
    }
    launchConfig("com.google.code.gson:gson:2.8.0")
    launchConfig("org.ow2.asm:asm-tree:$asmVersion")
    launchConfig("org.ow2.asm:asm-util:$asmVersion")

    // Applaunch -- initialization that needs to occur without game access
    applaunchConfig("org.spongepowered:plugin-spi:$pluginSpiVersion")
    applaunchConfig("org.apache.logging.log4j:log4j-api:$log4jVersion")
    applaunchConfig("com.google.guava:guava:$guavaVersion")
    applaunchConfig(platform("org.spongepowered:configurate-bom:$apiConfigurateVersion"))
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
    applaunchConfig("org.apache.logging.log4j:log4j-core:$log4jVersion")

    mixinsConfig(sourceSets.named("main").map { it.output })
    mixinsConfig("org.spongepowered:timings:$timingsVersion")
    add(mixins.get().implementationConfigurationName, "org.spongepowered:spongeapi:$apiVersion")

    // Tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

val organization: String by project
val projectUrl: String by project
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

allprojects {
    configurations.configureEach {
        resolutionStrategy.dependencySubstitution {
            // https://github.com/zml2008/guice/tree/backport/5.0.1
            substitute(module("com.google.inject:guice:5.0.1"))
                    .because("We need to run against Guava 21")
                    .using(module("ca.stellardrift.guice-backport:guice:5.0.1"))
        }
    }

    apply(plugin = "org.jetbrains.gradle.plugin.idea-ext")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "org.cadixdev.licenser")

    base {
        archivesBaseName = name.toLowerCase(Locale.ENGLISH)
    }

    plugins.withId("org.spongepowered.gradle.vanilla") {
        minecraft {
            version(minecraftVersion)
            injectRepositories(false)
            project.sourceSets["main"].resources
                .filter { it.name.endsWith(".accesswidener") }
                .files
                .forEach {
                    accessWideners(it)
                    parent?.minecraft?.accessWideners(it)
                }
        }
    }

    idea {
        if (project != null) {
            (project as ExtensionAware).extensions["settings"].run {
                (this as ExtensionAware).extensions.getByType(org.jetbrains.gradle.ext.ActionDelegationConfig::class).run {
                    delegateBuildRunToGradle = false
                    testRunner = org.jetbrains.gradle.ext.ActionDelegationConfig.TestRunner.PLATFORM
                }
                (this as ExtensionAware).extensions.getByType(org.jetbrains.gradle.ext.IdeaCompilerConfiguration::class).run {
                    addNotNullAssertions = false
                    useReleaseOption = JavaVersion.current().isJava10Compatible
                    parallelCompilation = true
                }
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
        val emptyAnnotationProcessors = objects.fileCollection()
        withType(JavaCompile::class).configureEach {
            options.compilerArgs.addAll(listOf("-Xmaxerrs", "1000"))
            options.encoding = "UTF-8"
            if (JavaVersion.current().isJava10Compatible) {
                options.release.set(8)
            }
            options.annotationProcessorPath = emptyAnnotationProcessors // hack so IntelliJ doesn't try to run Mixin AP
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