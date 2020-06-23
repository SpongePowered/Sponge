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

minecraft {
    mappings(mcpType, mcpMappings)
    runs {
        create("server") {
            workingDirectory(project.file("../run"))

        }
    }
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
}

// create the sourcesets
val mixins by sourceSets.registering
val accessors by sourceSets.registering
val launch by sourceSets.registering
val modlauncher by sourceSets.registering
val invalid by sourceSets.registering

val launchConfig = configurations.register("launch")
val mixinsConfig = configurations.register("mixins") {
    extendsFrom(launchConfig.get())
}
val modlauncherConfig = configurations.register("modlauncher")


//configure<org.spongepowered.asm.gradle.plugins.MixinExtension>() {
////    add(sourceSet = mixins, refMapName = "mixins.common.refmap.json")
////    add(sourceSet = accessors, refMapName = "mixins.common.accessors.refmap.json")
//}
repositories {
    maven("https://files.minecraftforge.net/maven")
}
dependencies {
    minecraft("net.minecraft:$minecraftDep:$minecraftVersion")

    runtime("org.apache.logging.log4j:log4j-slf4j-impl:2.8.1")

    // api
    api("org.spongepowered:plugin-spi:0.1.1-SNAPSHOT")
    api("org.spongepowered:plugin-meta:0.6.0-SNAPSHOT")

    // Database stuffs... likely needs to be looked at
    implementation("com.zaxxer:HikariCP:2.6.3")
    implementation("org.mariadb.jdbc:mariadb-java-client:2.0.3")
    implementation("com.h2database:h2:1.4.196")
    implementation("org.xerial:sqlite-jdbc:3.20.0")

    // ASM - required for generating event listeners
    implementation("org.ow2.asm:asm-util:6.2")
    implementation("org.ow2.asm:asm-tree:6.2")

    // Launch Dependencies - Needed to bootstrap the engine(s)
    launchConfig.configure {
        add(this.name, "org.spongepowered:mixin:0.8")
        add(this.name, "org.checkerframework:checker-qual:2.8.1")
        add(this.name, "com.google.guava:guava:25.1-jre") {
            exclude(group = "com.google.code.findbugs", module = "jsr305") // We don't want to use jsr305, use checkerframework
            exclude(group = "org.checkerframework", module = "checker-qual") // We use our own version
            exclude(group = "com.google.j2objc", module = "j2objc-annotations")
            exclude(group = "org.codehaus.mojo", module = "animal-sniffer-annotations")
            exclude(group = "com.google.errorprone", module = "error_prone_annotations")
        }
        add(this.name, "com.google.code.gson:gson:2.2.4")
        add(this.name, "org.ow2.asm:asm-tree:6.2")
        add(this.name, "org.ow2.asm:asm-util:6.2")
        add(this.name, "org.apache.logging.log4j:log4j-api:2.8.1")
        add(this.name, "org.spongepowered:configurate-core:3.6.1")
        add(this.name, "org.spongepowered:configurate-hocon:3.6.1")
    }


    // Mixins needs to be able to target api classes
//    mixins(spongeDev.api.get())

    // The ModLauncher compatibility launch layer
    modlauncherConfig.configure {
        add(this.name, "cpw.mods:modlauncher:4.1.+")
        add(this.name, "org.ow2.asm:asm-commons:6.2")
        add(this.name, "cpw.mods:grossjava9hacks:1.1.+")
        add(this.name, "net.minecraftforge:accesstransformers:1.0.+:shadowed")
    }

    // Annotation Processor
    "accessorsAnnotationProcessor"(launchConfig.get())
    "mixinsAnnotationProcessor"(launchConfig.get())
    "accessorsAnnotationProcessor"("org.spongepowered:mixin:0.8")
    "mixinsAnnotationProcessor"("org.spongepowered:mixin:0.8")
}

allprojects {

    apply(plugin = "maven-publish")

    afterEvaluate {
        tasks {
            compileJava {
                options.compilerArgs.addAll(listOf("-Xmaxerrs", "1000"))
            }
        }
    }

    repositories {
        maven {
            name = "sponge v2"
            setUrl("https://repo-new.spongepowered.org/repository/maven-public/")
        }
        maven("https://repo.spongepowered.org/maven")
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
                this.setUrl(uri("https://maven.pkg.github.com/spongepowered/plugin-meta"))
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
                    setUrl(uri(this))
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

val accessors by sourceSets

project("SpongeVanilla") {
    val vanillaProject = this
    apply {
        plugin("net.minecraftforge.gradle")
        plugin("org.spongepowered.mixin")
        plugin("java-library")
        plugin("maven-publish")
        plugin("idea")
        plugin("eclipse")
    }

    description = "The SpongeAPI implementation for Vanilla Minecraft"

    val vanillaLaunch = vanillaProject.configurations.register("vanillaLaunch")
    vanillaLaunch.configure {
        extendsFrom(launchConfig.get())
    }

    val vanillaInvalid by sourceSets.register("invalid")
    val vanillaMixins by sourceSets.register("mixins")
    val vanillaAccessors by sourceSets.register("accessors")

    val vanillaInvalidImplementation by configurations.named("invalidImplementation")
    val vanillaAccessorsAnnotationProcessor by configurations.named("accessorsAnnotationProcessor")
    val vanillaMixinsImplementation by configurations.named("mixinsImplementation")
    val vanillaMixinsAnnotationProcessor by configurations.named("mixinsAnnotationProcessor")

    configure<net.minecraftforge.gradle.userdev.UserDevExtension> {
        mappings(mcpType, mcpMappings)
        runs {
            create("server") {
                workingDirectory(vanillaProject.file("./run"))
                args.addAll(listOf("nogui", "--launchTarget", "sponge_server_dev"))
                main = "org.spongepowered.modlauncher.Main"
            }

            create("client") {
                environment("target", "client")
                workingDirectory(vanillaProject.file("./run"))
                args.addAll(listOf("--launchTarget", "sponge_client_dev", "--version", "1.14.4", "--accessToken", "0"))
                main = "org.spongepowered.modlauncher.Main"
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

        implementation(project(commonProject.path)) {
            exclude(group = "net.minecraft", module = "server")
        }

        // Invalid set
        vanillaInvalidImplementation(project(commonProject.path)) {
            exclude(group = "net.minecraft", module = "server")
        }

        // Launch Dependencies - Needed to bootstrap the engine(s)
        vanillaLaunch.configure {
            add(this.name, "org.spongepowered:mixin:0.8")
            add(this.name, "org.spongepowered:mixin:0.8")
            add(this.name, "org.checkerframework:checker-qual:3.4.1")
            add(this.name, "com.google.guava:guava:25.1-jre") {
                exclude(group = "com.google.code.findbugs", module = "jsr305") // We don't want to use jsr305, use checkerframework
                exclude(group = "org.checkerframework", module = "checker-qual") // We use our own version
                exclude(group = "com.google.j2objc", module = "j2objc-annotations")
                exclude(group = "org.codehaus.mojo", module = "animal-sniffer-annotations")
                exclude(group = "com.google.errorprone", module = "error_prone_annotations")
            }
            add(this.name, "com.google.code.gson:gson:2.2.4")
            add(this.name, "org.ow2.asm:asm-tree:6.2")
            add(this.name, "org.ow2.asm:asm-util:6.2")
            add(this.name, "org.apache.logging.log4j:log4j-api:2.8.1")
            add(this.name, "org.spongepowered:configurate-core:3.6.1")
            add(this.name, "org.spongepowered:configurate-hocon:3.6.1")
            add(this.name, "net.sf.jopt-simple:jopt-simple:5.0.4")
            add(this.name, "cpw.mods:grossjava9hacks:1.1.+")
        }
        "mixinsImplementation"(commonProject)


        // Annotation Processor
        vanillaAccessorsAnnotationProcessor(vanillaLaunch.get())
        vanillaMixinsAnnotationProcessor(vanillaLaunch.get())
        vanillaAccessorsAnnotationProcessor("org.spongepowered:mixin:0.8")
        vanillaMixinsAnnotationProcessor("org.spongepowered:mixin:0.8")
    }
}
