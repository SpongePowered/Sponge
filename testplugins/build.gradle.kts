val apiVersion: String by project
val organization: String by project
val projectUrl: String by project

dependencies {
    annotationProcessor(implementation("org.spongepowered:spongeapi:$apiVersion")!!)
}

indraSpotlessLicenser {
    licenseHeaderFile(rootProject.file("HEADER.txt"))

    property("name", "Sponge")
    property("organization", organization)
    property("url", projectUrl)
}
