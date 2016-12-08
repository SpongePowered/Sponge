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
__Note:__ If you do not have [Gradle] installed then use `./gradlew` for Unix systems or Git Bash and `gradlew.bat` for Windows systems in place of any `gradle` command.

Before you are able to build SpongeCommon, you must first prepare the environment:

  - Run `gradle setupDecompWorkspace --refresh-dependencies`

**Note**: You may substitute `setupDecompWorkspace` for `setupCIWorkspace` when building on a CI such as [Jenkins].

### IDE Setup
__For [Eclipse]__
  1. Run `gradle eclipse`
  2. Import SpongeCommon as an existing project (File > Import > General)
  3. Select the root folder for SpongeCommon and make sure `Search for nested projects` is enabled
  4. Check SpongeCommon when it finishes building and click **Finish**

__For [IntelliJ]__
  1. Make sure you have the Gradle plugin enabled (File > Settings > Plugins).  
  2. Click File > New > Project from Existing Sources > Gradle and select the root folder for SpongeCommon.
  3. Select _Use customizable gradle wrapper_ if you do not have Gradle installed.

## Building
__Note:__ If you do not have [Gradle] installed then use ./gradlew for Unix systems or Git Bash and gradlew.bat for Windows systems in place of any 'gradle' command.

__Note:__ You must [Setup the environment](#setup) before you can build SpongeCommon.

In order to build SpongeCommon you simply need to run the `gradle` command. You can find the compiled JAR files in `./build/libs`.

## Updating your Clone
The following steps will update your clone with the official repo.

1. `git pull`
2. `git submodule update --recursive`
3. `gradle setupDecompWorkspace --refresh-dependencies`

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
