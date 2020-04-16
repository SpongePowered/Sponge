import org.spongepowered.gradle.dev.SourceType

buildscript {
    repositories {
        maven("https://repo.spongepowered.org/maven")
    }
    dependencies {
        classpath("org.spongepowered:mixingradle:0.7-SNAPSHOT")
    }
}

plugins {
    id("org.spongepowered.gradle.sponge.impl")
    id("net.minecraftforge.gradle")
}

apply {
    plugin("org.spongepowered.mixin")
}
tasks {
    compileJava {
        options.compilerArgs.addAll(listOf("-Xmaxerrs", "1000"))
    }
}
spongeDev {
    common(project.parent ?: project(":SpongeCommon"))
    api(common.map { it.project("SpongeAPI") })
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
            configurations += "launch"
        }
        register("modlauncher") {
            dependsOn += "launch"
            configurations += "launch"
        }
        register("invalid") {
            sourceType.set(SourceType.Invalid)
        }
    }
}

val common by spongeDev.common
val launch by configurations.creating

dependencies {
    minecraft("net.minecraft:" + common.properties["minecraftDep"] + ":" + common.properties["minecraftVersion"])
    implementation(common)

    launch("net.sf.jopt-simple:jopt-simple:5.0.4")
    launch(group = "org.spongepowered", name = "plugin-meta", version = "0.4.1")
}

minecraft {
    evaluationDependsOnChildren()
    mappings(common.properties["mcpType"]!! as String, common.properties["mcpMappings"]!! as String)
    runs {
        create("server") {
            workingDirectory( project.file("./run"))
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
            .forEach { accessTransformer(it) }
}

