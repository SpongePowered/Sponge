plugins {
    id("org.spongepowered.gradle.vanilla")
}

val apiVersion: String by project
val organization: String by project
val projectUrl: String by project

description = "Code generator for automatically producing API catalog classes based off of Vanilla MC data"

minecraft {
    rootProject.sourceSets["main"].resources
            .filter { it.name.endsWith(".accesswidener") }
            .files
            .forEach {
                accessWideners(it)
            }
}

configurations.configureEach {
    exclude(group = "org.apache.logging.log4j", module = "log4j-slf4j18-impl")
}

dependencies {
    val forgeFlowerVersion: String by project
    val tinyLogVersion: String by project
    implementation("com.squareup:javapoet:1.13.0")
    implementation("com.github.javaparser:javaparser-core:3.22.1")
    implementation("org.tinylog:tinylog-api:$tinyLogVersion")
    runtimeOnly("org.tinylog:tinylog-impl:$tinyLogVersion")
    runtimeOnly("org.tinylog:slf4j-tinylog:$tinyLogVersion") // todo: doesn't work for some reason?

    // Decompiler
    forgeFlower("net.minecraftforge:forgeflower:$forgeFlowerVersion")
}

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

val apiBase = rootProject.file("SpongeAPI/src/main/java/")
val temporaryLicenseHeader = project.buildDir.resolve("api-gen-license-header.txt")
tasks.register("generateApiData", JavaExec::class) {
    group = "sponge"
    description = "Generate API Catalog classes"
    javaLauncher.set(project.javaToolchains.launcherFor(java.toolchain))

    classpath(sourceSets.main.map { it.output }, sourceSets.main.map { it.runtimeClasspath })
    mainClass.set("org.spongepowered.vanilla.generator.GeneratorMain")
    args(apiBase.canonicalPath, temporaryLicenseHeader.canonicalPath)

    doFirst {
        // Write a template-expanded license header to the temporary file
        license.header.get().asReader().buffered().use { reader ->
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
