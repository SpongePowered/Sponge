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

spongeDev {
    api(project.project("SpongeAPI"))
    common(project)
    addedSourceSets {
        register("mixins") {
            sourceType.set(SourceType.Mixin)
            configurations += arrayOf("launch", "mixins", "minecraft")
        }
        register("accessors") {
            sourceType.set(SourceType.Accessor)
            configurations += arrayOf("launch", "mixins", "minecraft")

        }
        register("launch") {
            sourceType.set(SourceType.Launch)
            configurations += "launch"
        }
        register("modlauncher") {
            dependsOn += "launch"
            configurations += arrayOf("launch", "modlauncher")
        }
        register("invalid") {
            sourceType.set(SourceType.Invalid)
            configurations += arrayOf("launch", "mixins")
        }
    }
}

minecraft {
    mappings(project.properties["mcpType"]!! as String, project.properties["mcpMappings"]!! as String)
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
    minecraft("net.minecraft:" + project.properties["minecraftDep"] + ":" + project.properties["minecraftVersion"])

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
}