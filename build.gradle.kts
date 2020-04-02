import org.spongepowered.gradle.dev.SpongeDevExtension

// TODO - Add mixin gradle when that comes around.
plugins {
    id("org.spongepowered.gradle.sponge.common")
    id("net.minecraftforge.gradle")
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
    // TODO - migrate this to the plugin, from the gradle properties
    minecraft("net.minecraft:" + project.properties["minecraftDep"] + ":" + project.properties["minecraftVersion"])

    runtime("org.apache.logging.log4j:log4j-slf4j-impl:2.8.1")

    // Database stuffs... likely needs to be looked atå
    implementation("com.zaxxer:HikariCP:2.6.3")
    runtime("org.mariadb.jdbc:mariadb-java-client:2.0.3")
    runtime("com.h2database:h2:1.4.196")
    runtime("org.xerial:sqlite-jdbc:3.20.0")
}

tasks.test {
    systemProperties("lwts.tweaker" to "org.spongepowered.common.launch.TestTweaker")
    reports.html.setEnabled(false)
}

// Configure the TestPlugins project.
val api = spongeDev.api!!
//val testPlugins = project(":testplugins") {
//    apply(plugin = "org.spongepowered.gradle.sponge.dev")
//    apply(plugin = "java-library")
//    configure<SpongeDevExtension> {
//        licenseProject = "Sponge"
//    }
//    dependencies {
//        implementation(project(api.path))
//    }
//}
val launch by sourceSets.creating
val launchWrapper by sourceSets.creating {
    compileClasspath += launch.compileClasspath
}
val mixins by sourceSets.creating {
    compileClasspath += launch.compileClasspath
}
val accessors by sourceSets.creating {
    compileClasspath += launch.compileClasspath
}

configurations {
    val accessorsImplementation by getting
    val mixinsImplementation by getting
    val minecraft by getting {
        accessorsImplementation.extendsFrom(this)
    }
    implementation {
        extendsFrom(accessorsImplementation)
        mixinsImplementation.extendsFrom(this)
    }
    val launchImplementation by getting
    devOutput {
        extendsFrom(launchImplementation)
        extendsFrom(accessorsImplementation)
        extendsFrom(mixinsImplementation)
    }

}

sourceSets {
    // TODO - once invalid is cleaned up, it can be be removed
    val invalid by creating {
        java {
            srcDir("invalid/main/java")
        }
    }
    main {
        compileClasspath += launch.compileClasspath
        runtimeClasspath += launch.runtimeClasspath
        invalid.compileClasspath += compileClasspath + output
        mixins.compileClasspath += compileClasspath
    }
}

dependencies {
    // For unit testing... not really going to work.
//    runtime(project(testPlugins.path)) {
//        exclude(module="spongeapi")
//    }
    "launchCompile"("org.spongepowered:mixin:0.8")
    "launchCompile"("org.ow2.asm:asm-tree:6.2")
    "launchCompile"("org.ow2.asm:asm-util:6.2")
    "launchCompile"("org.apache.logging.log4j:log4j-api:2.8.1")
    implementation(launch.output)

    "launchWrapperCompile"(launch.output)
    "launchWrapperCompile"("net.minecraft:launchwrapper:1.11") {
        exclude(module="lwjgl")
    }
    implementation(accessors.output)
    "mixinsCompile"(sourceSets.main.get().output)
    implementation(launchWrapper.output)

}