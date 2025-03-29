val apiVersion: String by project

dependencies {
    annotationProcessor(implementation("org.spongepowered:spongeapi:$apiVersion")!!)
    val userPlugins = file("userPlugins")
    if (userPlugins.exists()) {
        userPlugins.readLines().filter { !it.startsWith("#") && it.isNotBlank() }.forEach {
            implementation(it)
        }
    }
}
