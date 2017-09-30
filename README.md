SpongeCommon [![Build Status](https://travis-ci.org/SpongePowered/SpongeCommon.svg?branch=master)](https://travis-ci.org/SpongePowered/SpongeCommon)
=============

**Currently not stable and under heavy development!**  

Common code for official SpongeAPI implementations. It is licensed under the [MIT License]. 

* [Homepage]
* [Source]
* [Issues]
* [Documentation]
* [Community Chat]: [#sponge on irc.esper.net]
* [Development Chat]: [#spongedev on irc.esper.net]

## Prerequisites
* [Java] 8

## Clone
The following steps will ensure your project is cloned properly.

1. `git clone --recursive https://github.com/SpongePowered/SpongeCommon.git`
2. `cd SpongeCommon`
3. `cp scripts/pre-commit .git/hooks`

## Setup
**Note**: SpongeCommon uses [Gradle] as its build system. The repo includes the Gradle wrapper that will automatically download the correct Gradle 
version. Local installations of Gradle may work but are untested. To execute the Gradle wrapper, run the `./gradlew` script on Unix systems or only
`gradlew` on Windows systems.

Before you are able to build SpongeCommon, you must first prepare the environment:

  - Run `./gradlew setupDecompWorkspace --refresh-dependencies`

### IDE Setup
__For [Eclipse]__
  1. Run `./gradlew eclipse`
  2. Import SpongeCommon as an existing project (File > Import > General)
  3. Select the root folder for SpongeCommon and make sure `Search for nested projects` is enabled
  4. Check SpongeCommon when it finishes building and click **Finish**

__For [IntelliJ]__
  1. Make sure you have the Gradle plugin enabled (File > Settings > Plugins).  
  2. Click File > New > Project from Existing Sources > Gradle and select the root folder for SpongeCommon.
  3. Make sure _Use default gradle wrapper_ is selected. Older/newer Gradle versions may work but we only test using the wrapper.

## Building
__Note:__ You must [Setup the environment](#setup) before you can build SpongeCommon.

In order to build SpongeCommon you simply need to run the `gradlew` command. On Windows systems you should run `gradlew` instead of `./gradlew` to
invoke the Gradle wrapper. You can find the compiled JAR files in `./build/libs`.

## Updating your Clone
The following steps will update your clone with the official repo.

1. `git pull`
2. `git submodule update --recursive`
3. `./gradlew setupDecompWorkspace --refresh-dependencies`

## Contributing
Are you a talented programmer looking to contribute some code? We'd love the help!
* Open a pull request with your changes, following our [guidelines](.github/CONTRIBUTING.md).
* Please follow the above guidelines for your pull request(s) to be accepted.

[Eclipse]: https://eclipse.org/
[Gradle]: https://gradle.org/
[Homepage]: https://spongepowered.org/
[IntelliJ]: http://www.jetbrains.com/idea/
[Issues]: https://github.com/SpongePowered/SpongeCommon/issues
[Documentation]: https://docs.spongepowered.org/
[Java]: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
[Source]: https://github.com/SpongePowered/SpongeCommon/
[MIT License]: http://www.tldrlegal.com/license/mit-license
[Community Chat]: https://webchat.esper.net/?channels=sponge
[Development Chat]: https://webchat.esper.net/?channels=spongedev
[Jenkins]: https://jenkins-ci.org/
