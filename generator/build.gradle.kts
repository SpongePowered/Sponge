plugins {
    id("org.spongepowered.gradle.vanilla")
    id("sponge-impl.base-convention")
}

description = "Code generator for automatically producing API catalog classes based off of Vanilla MC data"

minecraft {
    rootProject.sourceSets["main"].resources
            .filter { it.name.endsWith(".accesswidener") }
            .files
            .forEach {
                accessWideners(it)
            }
}

indra {
    javaVersions().target(16)
}

dependencies {
    implementation(libs.javapoet)
    implementation(libs.javaparser)
    implementation(libs.tinylog.api)
    runtimeOnly(libs.tinylog.impl)
}

val apiBase = rootProject.file("SpongeAPI/src/main/java/")
tasks.register("generateApiData", JavaExec::class) {
    group = "sponge"
    description = "Generate API Catalog classes"
    javaLauncher.set(project.javaToolchains.launcherFor(java.toolchain))

    val temporaryLicenseHeader = temporaryDir.resolve("api-gen-license-header.txt")

    classpath(sourceSets.main.map { it.output }, sourceSets.main.map { it.runtimeClasspath })
    mainClass.set("org.spongepowered.vanilla.generator.GeneratorMain")
    args(apiBase.canonicalPath, temporaryLicenseHeader.canonicalPath)

    doFirst {
        // Write a template-expanded license header to the temporary file
        indraSpotlessLicenser.licenseHeaderFile().get().asReader().buffered().use { reader ->
            val template = groovy.text.GStringTemplateEngine().createTemplate(reader)

            val propertyMap = indraSpotlessLicenser.properties().get().toMutableMap()
            propertyMap["name"] = "SpongeAPI"
            val out = template.make(propertyMap)

            temporaryLicenseHeader.bufferedWriter(Charsets.UTF_8).use { writer ->
                out.writeTo(writer)
            }
        }
    }
}
