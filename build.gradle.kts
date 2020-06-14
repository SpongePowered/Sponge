import org.spongepowered.gradle.dev.SourceType

buildscript {
    repositories {
        maven("https://repo.spongepowered.org/maven")
    }
    dependencies {
        classpath("org.spongepowered:mixingradle:0.7-SNAPSHOT")
    }
}

repositories {
    maven("https://files.minecraftforge.net/maven")
}
plugins {
    id("org.spongepowered.gradle.sponge.common")
    id("net.minecraftforge.gradle")
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

spongeDev {
    api(apiProject)
    common(commonProject)
    addedSourceSets {
        register("launch") {
            sourceType.set(SourceType.Launch)
            configurations += "launch"
        }
        register("accessors") {
            sourceType.set(SourceType.Accessor)
            configurations += arrayOf("launch", "mixins", "minecraft")
        }
        register("modlauncher") {
            dependsOn += "launch"
            configurations += arrayOf("launch", "modlauncher")
        }
        register("mixins") {
            sourceType.set(SourceType.Mixin)
            configurations += arrayOf("launch", "mixins", "minecraft")
        }

        register("invalid") {
            sourceType.set(SourceType.Invalid)
            configurations += arrayOf("launch", "mixins", "minecraft")
        }
    }
}

minecraft {
    mappings(mcpType, mcpMappings)
    runs {
        create("server") {
            workingDirectory( project.file("../run"))
            mods {
                create("sponge") {
                    source(project.sourceSets["main"])
                }
            }
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

val launch by configurations.creating
val mixins by configurations.creating
val modlauncher by configurations.creating
mixins.extendsFrom(launch)

configure<org.spongepowered.asm.gradle.plugins.MixinExtension>() {
    add(sourceSets["mixins"], "mixins.common.refmap.json")
    add(sourceSets["accessors"], "mixins.common.accessors.refmap.json")
}
dependencies {
    minecraft("net.minecraft:$minecraftDep:$minecraftVersion")

    runtime("org.apache.logging.log4j:log4j-slf4j-impl:2.8.1")

    // Database stuffs... likely needs to be looked at
    implementation("com.zaxxer:HikariCP:2.6.3")
    implementation("org.mariadb.jdbc:mariadb-java-client:2.0.3")
    implementation("com.h2database:h2:1.4.196")
    implementation("org.xerial:sqlite-jdbc:3.20.0")

    // ASM - required for generating event listeners
    implementation("org.ow2.asm:asm-util:6.2")
    implementation("org.ow2.asm:asm-tree:6.2")

    // Launch Dependencies - Needed to bootstrap the engine(s)
    launch("org.spongepowered:mixin:0.8")
    launch("org.checkerframework:checker-qual:2.8.1")
    launch("com.google.guava:guava:25.1-jre") {
        exclude(group ="com.google.code.findbugs", module = "jsr305") // We don't want to use jsr305, use checkerframework
        exclude(group = "org.checkerframework", module = "checker-qual") // We use our own version
        exclude(group = "com.google.j2objc", module = "j2objc-annotations")
        exclude(group = "org.codehaus.mojo", module = "animal-sniffer-annotations")
        exclude(group = "com.google.errorprone", module = "error_prone_annotations")
    }
    launch("com.google.code.gson:gson:2.2.4")
    launch("org.ow2.asm:asm-tree:6.2")
    launch("org.ow2.asm:asm-util:6.2")
    launch("org.apache.logging.log4j:log4j-api:2.8.1")
    launch("org.spongepowered:configurate-core:3.6.1")
    launch("org.spongepowered:configurate-hocon:3.6.1")

    // Mixins needs to be able to target api classes
    mixins(spongeDev.api.get())

    // The ModLauncher compatibility launch layer
    modlauncher("cpw.mods:modlauncher:4.1.+")
    modlauncher("org.ow2.asm:asm-commons:6.2")
    modlauncher("cpw.mods:grossjava9hacks:1.1.+")
    modlauncher("net.minecraftforge:accesstransformers:1.0.+:shadowed")
    // Annotation Processor
    "accessorsAnnotationProcessor"(launch)
    "mixinsAnnotationProcessor"(launch)
    "accessorsAnnotationProcessor"("org.spongepowered:mixin:0.8")
    "mixinsAnnotationProcessor"("org.spongepowered:mixin:0.8")
}

allprojects {

    afterEvaluate {
        tasks {
            compileJava {
                options.compilerArgs.addAll(listOf("-Xmaxerrs", "1000"))
            }
        }
    }
}

val accessors by sourceSets

project("SpongeVanilla") {
    val vanillaProject = this
    apply {
        plugin("org.spongepowered.gradle.sponge.impl")
        plugin("net.minecraftforge.gradle")
        plugin("org.spongepowered.mixin")
    }

    description = "The SpongeAPI implementation for Vanilla Minecraft"

    configure<org.spongepowered.gradle.dev.SpongeImpl> {
        common(commonProject)
        api(apiProject)
        addForgeFlower.set(true)
        addedSourceSets {
            register("mixins") {
                sourceType.set(SourceType.Mixin)
            }
            register("accessors") {
                sourceType.set(SourceType.Accessor)
            }
            register("launch") {
                sourceType.set(SourceType.Launch)
                configurations += "vanillaLaunch"
            }
            register("modlauncher") {
                dependsOn += "launch"
                configurations += "vanillaLaunch"
            }
            register("invalid") {
                sourceType.set(SourceType.Invalid)
            }
        }
    }

    val vanillaLaunch by vanillaProject.configurations.creating
    vanillaLaunch.extendsFrom(launch)

    configure<net.minecraftforge.gradle.userdev.UserDevExtension> {
        mappings(mcpType, mcpMappings)
        runs {
            create("server") {
                workingDirectory(vanillaProject.file("./run"))
                mods {
                    create("sponge") {
                        source(vanillaProject.sourceSets["main"])
                    }
                }
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
        "invalidImplementation"(project(commonProject.path)) {
            exclude(group = "net.minecraft", module = "server")
        }

        // Launch Dependencies - Needed to bootstrap the engine(s)
        vanillaLaunch("org.spongepowered:mixin:0.8")
        vanillaLaunch("org.checkerframework:checker-qual:2.8.1")
        vanillaLaunch("com.google.guava:guava:25.1-jre") {
            exclude(group ="com.google.code.findbugs", module = "jsr305") // We don't want to use jsr305, use checkerframework
            exclude(group = "org.checkerframework", module = "checker-qual") // We use our own version
            exclude(group = "com.google.j2objc", module = "j2objc-annotations")
            exclude(group = "org.codehaus.mojo", module = "animal-sniffer-annotations")
            exclude(group = "com.google.errorprone", module = "error_prone_annotations")
        }
        vanillaLaunch("com.google.code.gson:gson:2.2.4")
        vanillaLaunch("org.ow2.asm:asm-tree:6.2")
        vanillaLaunch("org.ow2.asm:asm-util:6.2")
        vanillaLaunch("org.apache.logging.log4j:log4j-api:2.8.1")
        vanillaLaunch("org.spongepowered:configurate-core:3.6.1")
        vanillaLaunch("org.spongepowered:configurate-hocon:3.6.1")
        vanillaLaunch("net.sf.jopt-simple:jopt-simple:5.0.4")
        "mixinsImplementation"(commonProject)


        // Annotation Processor
        "accessorsAnnotationProcessor"(vanillaLaunch)
        "mixinsAnnotationProcessor"(vanillaLaunch)
        "accessorsAnnotationProcessor"("org.spongepowered:mixin:0.8")
        "mixinsAnnotationProcessor"("org.spongepowered:mixin:0.8")
    }

}


