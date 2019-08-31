plugins {
    id("org.spongepowered.gradle.sponge.common")
    id("net.minecraftforge.gradle")
}

minecraft {
    mappings("snapshot", project.properties["mcpMappings"]!! as String)
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
}

val testPlugins = project("testplugins")

dependencies {
    // Minecraft... duh
    // TODO - migrate this to the plugin, from the gradle properties
    minecraft("net.minecraft:server:1.14.4")

    // For unit testing... not really going to work.
    testImplementation("org.spongepowered:lwts:1.0.0")

    implementation("net.minecraft:launchwrapper:1.11")
    runtime(project(testPlugins.path)) {
        exclude(module="spongeapi")
    }

    api("org.spongepowered:mixin:0.7.11-SNAPSHOT") {
        // we do need launchwrapper at the moment, only to allow us to depend on Launch for SpongeCore modifiers
//        exclude(module="launchwrapper")
    }

    runtime("org.apache.logging.log4j:log4j-slf4j-impl:2.8.1")

    // Database stuffs... likely needs to be looked at
    runtime("com.zaxxer:HikariCP:2.6.3")
    runtime("org.mariadb.jdbc:mariadb-java-client:2.0.3")
    runtime("com.h2database:h2:1.4.196")
    runtime("org.xerial:sqlite-jdbc:3.20.0")

}

tasks.test {
    systemProperties("lwts.tweaker" to "org.spongepowered.common.launch.TestTweaker")
    reports.html.setEnabled(false)
}

