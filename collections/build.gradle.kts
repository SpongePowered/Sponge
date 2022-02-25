
plugins {
    `java-library`
    id("org.cadixdev.licenser")
    `maven-publish`
    id("com.github.ben-manes.versions")
}

val organization: String by project
val projectUrl: String by project
val guavaVersion: String by project
val fastUtilVersion: String by project
val junitVersion: String by project
val mockitoVersion: String by project

dependencies {
    api("com.google.guava:guava:$guavaVersion") {
        exclude(group ="com.google.code.findbugs", module = "jsr305") // We don't want to use jsr305, use checkerframework
        exclude(group = "org.checkerframework", module = "checker-qual") // We use our own version
        exclude(group = "com.google.j2objc", module = "j2objc-annotations")
        exclude(group = "org.codehaus.mojo", module = "animal-sniffer-annotations")
    }
    api("it.unimi.dsi:fastutil:$fastUtilVersion")
    // Tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")
    testImplementation("org.mockito:mockito-inline:$mockitoVersion")
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