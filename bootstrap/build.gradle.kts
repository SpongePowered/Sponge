plugins {
    id("implementation-structure")
}

val main by sourceSets.named("main")

val forge by sourceSets.register("forge") {
    spongeImpl.addDependencyToImplementation(main, this)
}

val neoforge by sourceSets.register("neoforge") {
    spongeImpl.addDependencyToImplementation(main, this)
}

dependencies {
    val forgeLib = forge.implementationConfigurationName
    forgeLib(libs.securemodules)

    val neoforgeLib = neoforge.implementationConfigurationName
    neoforgeLib(libs.securejarhandler)
}
