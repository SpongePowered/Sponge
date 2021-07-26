# Sponge-Fabric

A support of Fabric MC for Sponge server

## How to build

### Tiny-Remapper setup

1. Clone the tiny-remapper repo from [here](https://github.com/LogicFan/tiny-remapper)
```
git clone https://github.com/LogicFan/tiny-remapper.git
```

2. Checkout to `mixinRemap`
```
git checkout mixinRemap
```

3. Build the Tiny-Remapper

```
./gradlew clean build publishToMavenLocal
```

### Fabric-Loom Setup

1. Clone the [Fabric-Loom](https://github.com/LogicFan/fabric-loom) to local

```access transformers
git clone https://github.com/LogicFan/fabric-loom.git
```

2. Checkout to `mixinRemap` branch

```access transformers
git checkout mixinRemap
```

5. We build the loom project:

```access transformers
./gradlew build -x test -x checkstyleMain publishToMavenLocal
```

### Build the Project

1. Clone the sponge repository:

```access transformers
git clone --recursive https://github.com/LogicFan/Sponge.git
```

2. Change to fabric branch and checkout:

```access transformers
git checkout fabric
```

3. Final steps:

- ```git pull```
- ```git submodule update --recursive```
- ```./gradlew build --refresh-dependencies```