plugins {
    id("templated-resources")
    id("sponge-impl.base-convention")
    id("com.github.johnrengelman.shadow")
    eclipse
}

sourceSets {
    main {
        multirelease.alternateVersions(9)
    }
}

// Installer source sets
/*
val minecraftVersion: String by project
dependencies {
    val asmVersion: String by project
    val forgeAutoRenamingToolVersion: String by project
    val tinyLogVersion: String by project

    implementation("com.google.code.gson:gson:2.8.0")
    implementation("org.spongepowered:configurate-hocon:$apiConfigurateVersion")
    implementation("org.spongepowered:configurate-core:$apiConfigurateVersion")
    implementation("org.spongepowered:configurate-jackson:$apiConfigurateVersion")
    implementation("net.sf.jopt-simple:jopt-simple:5.0.3")
    implementation("org.tinylog:tinylog-api:$tinyLogVersion")
    implementation("org.tinylog:tinylog-impl:$tinyLogVersion")
    // Override ASM versions, and explicitly declare dependencies so ASM is excluded from the manifest.
    val asmExclusions = sequenceOf("-commons", "-tree", "-analysis", "")
            .map { "asm$it" }
            .onEach {
                implementation("org.ow2.asm:$it:$asmVersion")
            }.toSet()
    implementation("net.minecraftforge:ForgeAutoRenamingTool:$forgeAutoRenamingToolVersion") {
        exclude(group = "net.sf.jopt-simple")
        asmExclusions.forEach { exclude(group = "org.ow2.asm", module = it) } // Use our own ASM version
    }
}

val vanillaInstallerJar by registering(Jar::class) {
    archiveClassifier.set("installer")
    manifest{
        from(vanillaManifest)
        attributes(
                "Premain-Class" to "org.spongepowered.vanilla.installer.Agent",
                "Agent-Class" to "org.spongepowered.vanilla.installer.Agent",
                "Launcher-Agent-Class" to "org.spongepowered.vanilla.installer.Agent",
                "Multi-Release" to true
        )
    }
    from(vanillaInstaller.output)
    into("META-INF/versions/9/") {
        from(vanillaInstallerJava9.output)
    }
}
tasks {
    val installerTemplateSource = project.file("src/main/templates")
    val installerTemplateDest = project.layout.buildDirectory.dir("generated/sources/templates")
    val generateInstallerTemplates by registering(Copy::class) {
        group = "sponge"
        description = "Generate classes from templates for the SpongeVanilla installer"
        val properties = mutableMapOf(
                "minecraftVersion" to minecraftVersion
        )
        inputs.properties(properties)

        // Copy template
        from(installerTemplateSource)
        into(installerTemplateDest)
        expand(properties)
    }
    vanillaInstaller.java.srcDir(generateInstallerTemplates.map { it.outputs })

// Generate templates on IDE import as well
    (rootProject.idea.project as? ExtensionAware)?.also {
        (it.extensions["settings"] as ExtensionAware).extensions.getByType(TaskTriggersConfig::class).afterSync(generateInstallerTemplates)
    }
    project.eclipse {
        synchronizationTasks(generateInstallerTemplates)
    }

    val installerResources = project.layout.buildDirectory.dir("generated/resources/installer")
    vanillaInstaller.resources.srcDir(installerResources)

    val downloadNotNeeded = configurations.register("downloadNotNeeded") {
        extendsFrom(configurations.minecraft.get())
        extendsFrom(vanillaInstallerConfig.get())
    }

    val emitDependencies by registering(org.spongepowered.gradle.impl.OutputDependenciesToJson::class) {
        group = "sponge"
        // everything in applaunch goes -> bootstrap, everything in libraries goes -> main (MC is inserted here too)
        this.dependencies("bootstrap", vanillaAppLaunchConfig)
        this.dependencies("main", vanillaLibrariesConfig)
        // except what we're providing through the installer
        this.excludedDependencies(downloadNotNeeded)

        outputFile.set(installerResources.map { it.file("libraries.json") })
    }
    named(vanillaInstaller.processResourcesTaskName).configure {
        dependsOn(emitDependencies)
    }

    shadowJar {
        mergeServiceFiles()

        configurations = listOf(project.configurations.getByName(vanillaInstaller.runtimeClasspathConfigurationName))

        archiveClassifier.set("universal")
        manifest {
            attributes(mapOf(
                    "Superclass-Transformer" to "common.superclasschange,vanilla.superclasschange",
                    "Access-Widener" to "common.accesswidener",
                    "MixinConfigs" to mixinConfigs.joinToString(","),
                    "Main-Class" to "org.spongepowered.vanilla.installer.InstallerMain",
                    "Launch-Target" to "sponge_server_prod",
                    "Multi-Release" to true,
                    "Premain-Class" to "org.spongepowered.vanilla.installer.Agent",
                    "Agent-Class" to "org.spongepowered.vanilla.installer.Agent",
                    "Launcher-Agent-Class" to "org.spongepowered.vanilla.installer.Agent"
            ))
            from(vanillaManifest)
        }
        from(commonProject.sourceSets.main.map { it.output })
        from(commonProject.sourceSets.named("mixins").map { it.output })
        from(commonProject.sourceSets.named("accessors").map { it.output })
        from(commonProject.sourceSets.named("launch").map { it.output })
        from(commonProject.sourceSets.named("applaunch").map { it.output })
        from(sourceSets.main.map { it.output })
        from(vanillaInstaller.output)
        from(vanillaInstallerJava9.output) {
            into("META-INF/versions/9/")
        }
        from(vanillaAppLaunch.output)
        from(vanillaLaunch.output)
        from(vanillaMixins.output)
        /*dependencies {
        // include(project(":"))
        include("org.spongepowered:spongeapi:$apiVersion")
    } */

        // We cannot have modules in a shaded jar
        exclude("META-INF/versions/*/module-info.class")
        exclude("module-info.class")
    }
    assemble {
        dependsOn(shadowJar)
    }
}
*/