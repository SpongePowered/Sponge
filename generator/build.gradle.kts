plugins {
    id("org.spongepowered.gradle.vanilla")
}

val apiVersion: String by project
val organization: String by project
val projectUrl: String by project

description = "Code generator for automatically producing API catalog classes based off of Vanilla MC data"

java {
    // generator is non-API, we can use Java 16 just fine
    if (JavaVersion.current() < JavaVersion.VERSION_16) {
        toolchain { languageVersion.set(JavaLanguageVersion.of(16)) }
    }
}

tasks.withType(JavaCompile::class) {
    options.release.set(16)
}

dependencies {
    implementation("com.squareup:javapoet:1.13.0")
    implementation("com.github.javaparser:javaparser-core:3.22.1")
    implementation("org.tinylog:tinylog-api:2.2.1")
    runtimeOnly("org.tinylog:tinylog-impl:2.2.1")
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
