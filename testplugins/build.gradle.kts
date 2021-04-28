val apiVersion: String by project
val organization: String by project
val projectUrl: String by project

dependencies {
    annotationProcessor(implementation("org.spongepowered:spongeapi:$apiVersion")!!)
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
