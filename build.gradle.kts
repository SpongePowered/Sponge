import org.spongepowered.gradle.dev.SourceType

plugins {
    id("org.spongepowered.gradle.sponge.common")
    id("net.minecraftforge.gradle")
}

spongeDev {
    addedSourceSets {
        register("mixins") {
            sourceType.set(SourceType.Mixin)
        }
        register("accessors") {
            sourceType.set(SourceType.Accessor)
        }
        register("launch") {
            sourceType.set(SourceType.Launch)
        }
        register("launchWrapper") {
            dependsOn += "launch"
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

dependencies {
    // Minecraft... duh
    minecraft("net.minecraft:" + project.properties["minecraftDep"] + ":" + project.properties["minecraftVersion"])

    runtime("org.apache.logging.log4j:log4j-slf4j-impl:2.8.1")

    // Database stuffs... likely needs to be looked atå
    implementation("com.zaxxer:HikariCP:2.6.3")
    runtime("org.mariadb.jdbc:mariadb-java-client:2.0.3")
    runtime("com.h2database:h2:1.4.196")
    runtime("org.xerial:sqlite-jdbc:3.20.0")
//    implementation("org.ow2.asm:asm:6.2")
    implementation("org.ow2.asm:asm-util:6.2")
    implementation("org.ow2.asm:asm-tree:6.2")
    "launchCompile"("org.checkerframework:checker-qual:2.8.1")
    "launchCompile"("com.google.guava:guava:25.1-jre") {
        exclude(group ="com.google.code.findbugs", module = "jsr305") // We don't want to use jsr305, use checkerframework
        exclude(group = "org.checkerframework", module = "checker-qual") // We use our own version
        exclude(group = "com.google.j2objc", module = "j2objc-annotations")
        exclude(group = "org.codehaus.mojo", module = "animal-sniffer-annotations")

    }
    "launchCompile"("org.spongepowered:mixin:0.8")
    "launchCompile"("org.ow2.asm:asm-tree:6.2")
    "launchCompile"("org.ow2.asm:asm-util:6.2")
    "launchCompile"("org.apache.logging.log4j:log4j-api:2.8.1")

    "launchWrapperCompile"("net.minecraft:launchwrapper:1.11") {
        exclude(module="lwjgl")
    }
}

configurations {
    val minecraft by getting
    val accessorsImplementation by getting
    accessorsImplementation.extendsFrom(minecraft)
}

sourceSets {
    // TODO - once invalid is cleaned up, it can be be removed
    val invalid by creating {
        java {
            srcDir("invalid/main/java")
        }
    }
    main {
        invalid.compileClasspath += compileClasspath + output
    }
}
