Sponge ![Java CI with Gradle](https://github.com/SpongePowered/Sponge/workflows/Java%20CI%20with%20Gradle/badge.svg?branch=api-8&event=push)
=============

The SpongeAPI implementation targeting vanilla Minecraft and 3rd party platforms. It is licensed under the [MIT License].

* [Homepage]
* [Source]
* [Issues]
* [Documentation]
* [Discord] `#sponge`

## Latest Builds

### Sponge

**1.16.5**: ![Snapshots](https://img.shields.io/nexus/maven-snapshots/org.spongepowered/sponge?label=Sponge&server=https%3A%2F%2Frepo-new.spongepowered.org%2F)

### SpongeVanilla

**1.16.5**: ![Snapshots](https://img.shields.io/nexus/maven-releases/org.spongepowered/spongevanilla?label=SpongeVanilla&server=https%3A%2F%2Frepo-new.spongepowered.org%2F)


## Prerequisites
* [Java] 8

## Clone
The following steps will ensure your project is cloned properly.

1. `git clone --recursive https://github.com/SpongePowered/Sponge.git`
2. `cd Sponge`
3. `cp scripts/pre-commit .git/hooks`

## Setup
**Note**: Sponge uses [Gradle] as its build system. The repo includes the Gradle wrapper that will automatically download the correct Gradle 
version. Local installations of Gradle may work (as long as they are using Gradle 6.8+) but are untested. To execute the Gradle wrapper, run the 
`./gradlew` script on Unix systems or only `gradlew` on Windows systems.

To have browsable sources for use in-IDE, run `./gradlew :decompile`. This command will need to be re-ran after any change to
Minecraft version or to `.accesswidener` files. If sources are not appearing properly, an IDE refresh should fix things.

### IDE Setup
__For [Eclipse]__
 1. Make sure the Buildship plugin is installed (available on the [Eclipse Marketplace])
 2. Import the project as an *Existing Gradle Project* (via File > Import > Gradle)

While we do our best to support any IDE, most of our developers use IntelliJ, so issues may pop up with Eclipse from time to time.
We'll be happy to work to resolve those issues if reported via [our issues page][Issues] or fixed via PR.

__For [IntelliJ]__
  1. Make sure you have the Gradle plugin enabled (File > Settings > Plugins).  
  2. Click File > New > Project from Existing Sources > Gradle and select the root folder for Sponge.
  3. Make sure _Use default gradle wrapper_ is selected. Older/newer Gradle versions may work but we only test using the wrapper.

For both Eclipse and IntelliJ, a variety of run configurations will be generated which allow running the client and server in development. These run 
configurations will be re-generated on each project import, so any desired modifications should be done on copies of the configurations.

While these run configurations have Java versions attached to them, be aware that IntelliJ ignores that information entirely, and Eclipse will 
only be able to align those java versions with whatever JREs it is aware of.

## Building

In order to build Sponge you simply need to run the `gradlew build` command. On Windows systems you should run `gradlew build` instead 
of `./gradlew build` to invoke the Gradle wrapper. You can find the compiled JAR files in `./build/libs` and `./vanilla/build/libs`.

## Updating your Clone
The following steps will update your clone with the official repo.

1. `git pull`
2. `git submodule update --recursive`
3. `./gradlew build --refresh-dependencies`

## Contributing
Are you a talented programmer looking to contribute some code? We'd love the help!
* Open a pull request with your changes, following our [guidelines](.github/CONTRIBUTING.md).
* Please follow the above guidelines for your pull request(s) to be accepted.

[Eclipse]: https://eclipse.org/
[Eclipse Marketplace]: http://marketplace.eclipse.org/content/buildship-gradle-integration
[Gradle]: https://gradle.org/
[Homepage]: https://spongepowered.org/
[IntelliJ]: http://www.jetbrains.com/idea/
[Issues]: https://github.com/SpongePowered/Sponge/issues
[Documentation]: https://docs.spongepowered.org/
[Java]: https://adoptopenjdk.net/?variant=openjdk8&jvmVariant=hotspot
[Source]: https://github.com/SpongePowered/Sponge/
[MIT License]: http://www.tldrlegal.com/license/mit-license
[Discord]: https://discord.gg/sponge
