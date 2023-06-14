plugins {
    id("sponge-impl.base-convention")
}

val apiVersion: String by project

dependencies {
    annotationProcessor(implementation("org.spongepowered:spongeapi:$apiVersion")!!)
}
