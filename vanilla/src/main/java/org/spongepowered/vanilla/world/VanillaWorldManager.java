/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.vanilla.world;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.longs.LongIterator;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.worldgen.Features;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.RegistryWriteOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.WanderingTraderSpawner;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.CommandStorage;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.apache.commons.io.FilenameUtils;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.WorldType;
import org.spongepowered.api.world.WorldTypes;
import org.spongepowered.api.world.server.WorldTemplate;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeServer;
import org.spongepowered.common.accessor.server.MinecraftServerAccessor;
import org.spongepowered.common.accessor.world.gen.DimensionGeneratorSettingsAccessor;
import org.spongepowered.common.accessor.world.level.storage.LevelStorageSource_LevelStorageAccessAccessor;
import org.spongepowered.common.accessor.world.level.storage.PrimaryLevelDataAccessor;
import org.spongepowered.common.bridge.ResourceKeyBridge;
import org.spongepowered.common.bridge.server.level.ServerLevelBridge;
import org.spongepowered.common.bridge.world.level.dimension.LevelStemBridge;
import org.spongepowered.common.bridge.world.level.levelgen.WorldGenSettingsBridge;
import org.spongepowered.common.bridge.world.level.storage.PrimaryLevelDataBridge;
import org.spongepowered.common.config.SpongeGameConfigs;
import org.spongepowered.common.config.inheritable.InheritableConfigHandle;
import org.spongepowered.common.config.inheritable.WorldConfig;
import org.spongepowered.common.datapack.DataPackSerializer;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.server.BootstrapProperties;
import org.spongepowered.common.user.SpongeUserManager;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.FutureUtil;
import org.spongepowered.common.world.server.SpongeWorldManager;
import org.spongepowered.common.world.server.SpongeWorldTemplate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class VanillaWorldManager implements SpongeWorldManager {

    private final MinecraftServer server;
    private final Path dimensionsDataPackDirectory, defaultWorldDirectory, customWorldsDirectory;
    private final Map<net.minecraft.resources.ResourceKey<Level>, ServerLevel> worlds;

    private static final TicketType<ResourceLocation> SPAWN_CHUNKS = TicketType.create("spawn_chunks", (i, o) -> i.compareTo(o));

    public VanillaWorldManager(final MinecraftServer server) {
        this.server = server;
        this.dimensionsDataPackDirectory = ((MinecraftServerAccessor) server).accessor$storageSource().getLevelPath(LevelResource.DATAPACK_DIR).resolve("plugin_dimension").resolve("data");
        try {
            Files.createDirectories(this.dimensionsDataPackDirectory);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        this.defaultWorldDirectory = ((LevelStorageSource_LevelStorageAccessAccessor) ((MinecraftServerAccessor) this.server).accessor$storageSource()).accessor$levelPath();
        this.customWorldsDirectory = this.defaultWorldDirectory.resolve("dimensions");
        try {
            Files.createDirectories(this.customWorldsDirectory);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        this.worlds = ((MinecraftServerAccessor) this.server).accessor$levels();
    }

    @Override
    public Path getDefaultWorldDirectory() {
        return this.defaultWorldDirectory;
    }

    @Override
    public Path getDimensionDataPackDirectory() {
        return this.dimensionsDataPackDirectory;
    }

    @Override
    public Server server() {
        return (Server) this.server;
    }

    @Override
    public org.spongepowered.api.world.server.ServerWorld defaultWorld() {
        final ServerLevel world = this.server.overworld();
        if (world == null) {
            throw new IllegalStateException("The default world has not been loaded yet! (Did you call this too early in the lifecycle?");
        }
        return (org.spongepowered.api.world.server.ServerWorld) world;
    }

    @Override
    public Optional<org.spongepowered.api.world.server.ServerWorld> world(final ResourceKey key) {
        return Optional.ofNullable((org.spongepowered.api.world.server.ServerWorld) this.worlds.get(SpongeWorldManager.createRegistryKey(Objects.requireNonNull(key, "key"))));
    }

    @Override
    public Optional<Path> worldDirectory(final ResourceKey key) {
        Objects.requireNonNull(key, "key");
        
        Path directory;
        if (Level.OVERWORLD.location().equals(key)) {
            directory = this.defaultWorldDirectory;
        } else if (Level.NETHER.location().equals(key)) {
            directory = this.defaultWorldDirectory.resolve("DIM-1");
        } else if (Level.END.location().equals(key)) {
            directory = this.defaultWorldDirectory.resolve("DIM1");
        } else {
            directory = this.customWorldsDirectory.resolve(key.namespace()).resolve(key.value());
        }
        if (Files.notExists(directory)) {
            return Optional.empty();
        }
        return Optional.of(directory);
    }

    @Override
    public Collection<org.spongepowered.api.world.server.ServerWorld> worlds() {
        return Collections.unmodifiableCollection((Collection<org.spongepowered.api.world.server.ServerWorld>) (Object) this.worlds.values());
    }

    @Override
    public List<ResourceKey> worldKeys() {
        final List<ResourceKey> worldKeys = new ArrayList<>();
        worldKeys.add((ResourceKey) (Object) Level.OVERWORLD.location());

        if (Files.exists(this.defaultWorldDirectory.resolve(this.getDirectoryName((ResourceKey) (Object) Level.NETHER.location())))) {
            worldKeys.add((ResourceKey) (Object) Level.NETHER.location());
        }

        if (Files.exists(this.defaultWorldDirectory.resolve(this.getDirectoryName((ResourceKey) (Object) Level.END.location())))) {
            worldKeys.add((ResourceKey) (Object) Level.END.location());
        }

        try {
            for (final Path namespacedDirectory : Files.walk(this.customWorldsDirectory, 1).collect(Collectors.toList())) {
                if (this.customWorldsDirectory.equals(namespacedDirectory)) {
                    continue;
                }

                for (final Path valueDirectory : Files.walk(namespacedDirectory, 1).collect(Collectors.toList())) {
                    if (namespacedDirectory.equals(valueDirectory)) {
                        continue;
                    }
                    
                    worldKeys.add(ResourceKey.of(namespacedDirectory.getFileName().toString(), valueDirectory.getFileName().toString()));
                }
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        return Collections.unmodifiableList(worldKeys);
    }

    @Override
    public List<ResourceKey> templateKeys() {
        final List<ResourceKey> templateKeys = new ArrayList<>();
        // Treat Vanilla ones as template keys
        templateKeys.add(WorldTypes.OVERWORLD.location());
        templateKeys.add(WorldTypes.THE_NETHER.location());
        templateKeys.add(WorldTypes.THE_END.location());

        try (final Stream<Path> pluginDirectories = Files.walk(this.getDimensionDataPackDirectory(), 1)) {
            pluginDirectories
                    .filter(Files::isDirectory)
                    .forEach(pluginDirectory -> {
                                try (final Stream<Path> pluginTemplates = Files.walk(pluginDirectory.resolve("dimension"), 1)) {
                                    pluginTemplates
                                            .filter(template -> template.endsWith(".json"))
                                            .forEach(template -> templateKeys.add((ResourceKey) (Object) new ResourceLocation(pluginDirectory.toString(),
                                                    FilenameUtils.removeExtension(template.getFileName().toString()))));
                                } catch (final IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        return templateKeys;
    }

    @Override
    public boolean worldExists(final ResourceKey key) {
        final net.minecraft.resources.ResourceKey<Level> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(key, "key"));

        if (Level.OVERWORLD.equals(registryKey)) {
            return true;
        }

        if (this.worlds.get(registryKey) != null) {
            return true;
        }

        final boolean isVanillaSubLevel = Level.NETHER.equals(registryKey) || Level.END.equals(registryKey);
        final Path levelDirectory = isVanillaSubLevel ? this.defaultWorldDirectory.resolve(this.getDirectoryName(key)) :
            this.customWorldsDirectory.resolve(key.namespace()).resolve(key.value());
        return Files.exists(levelDirectory);
    }

    @Override
    public Optional<ResourceKey> worldKey(final UUID uniqueId) {
        Objects.requireNonNull(uniqueId, "uniqueId");
        return this.worlds
            .values()
            .stream()
            .filter(w -> ((org.spongepowered.api.world.server.ServerWorld) w).uniqueId().equals(uniqueId))
            .map(w -> (org.spongepowered.api.world.server.ServerWorld) w)
            .map(org.spongepowered.api.world.server.ServerWorld::key)
            .findAny();
    }

    @Override
    public CompletableFuture<org.spongepowered.api.world.server.ServerWorld> loadWorld(final WorldTemplate template) {
        final ResourceKey key = Objects.requireNonNull(template, "template").key();
        final net.minecraft.resources.ResourceKey<Level> registryKey = SpongeWorldManager.createRegistryKey(key);
        if (Level.OVERWORLD.equals(registryKey)) {
            FutureUtil.completedWithException(new IllegalArgumentException("The default world cannot be told to load!"));
        }
        final ServerLevel serverWorld = this.worlds.get(registryKey);
        if (serverWorld != null) {
            return CompletableFuture.completedFuture((org.spongepowered.api.world.server.ServerWorld) serverWorld);
        }

        this.saveTemplate(template);

        return this.loadWorld0(registryKey, ((SpongeWorldTemplate) template).asDimension(), ((WorldGenSettings) template.generationConfig()));
    }

    @Override
    public CompletableFuture<org.spongepowered.api.world.server.ServerWorld> loadWorld(final ResourceKey key) {
        final net.minecraft.resources.ResourceKey<Level> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(key, "key"));

        if (Level.OVERWORLD.equals(registryKey)) {
            FutureUtil.completedWithException(new IllegalArgumentException("The default world cannot be told to load!"));
        }

        final ServerLevel world = this.worlds.get(registryKey);
        if (world != null) {
            return CompletableFuture.completedFuture((org.spongepowered.api.world.server.ServerWorld) world);
        }

        return this.loadTemplate(key).thenCompose(r -> {
            WorldTemplate loadedTemplate = r.orElse(null);
            if (loadedTemplate == null) {
                final LevelStem scratch = BootstrapProperties.dimensionGeneratorSettings.dimensions().get(net.minecraft.resources.ResourceKey.create(
                    net.minecraft.core.Registry.LEVEL_STEM_REGISTRY, (ResourceLocation) (Object) key));
                if (scratch != null) {
                    ((ResourceKeyBridge) (Object) scratch).bridge$setKey(key);
                    loadedTemplate = new SpongeWorldTemplate(scratch);
                }

                if (loadedTemplate == null) {
                    return FutureUtil.completedWithException(new IOException(String.format("Failed to load a template for '%s'!", key)));
                }

                this.saveTemplate(loadedTemplate);
            }

            return this.loadWorld0(registryKey, ((SpongeWorldTemplate) loadedTemplate).asDimension(), ((WorldGenSettings) loadedTemplate.generationConfig()));
        });
    }

    private CompletableFuture<org.spongepowered.api.world.server.ServerWorld> loadWorld0(final net.minecraft.resources.ResourceKey<Level> registryKey,
            final LevelStem template, final WorldGenSettings generatorSettings) {
        final PrimaryLevelData defaultLevelData = (PrimaryLevelData) this.server.getWorldData();
        final LevelSettings defaultLevelSettings = ((PrimaryLevelDataAccessor) defaultLevelData).accessor$settings();
        final LevelStemBridge templateBridge = (LevelStemBridge) (Object) template;
        final ResourceKey worldKey = ((ResourceKeyBridge) templateBridge).bridge$getKey();

        final WorldType worldType = (WorldType) template.type();
        final ResourceKey worldTypeKey = RegistryTypes.WORLD_TYPE.get().valueKey((WorldType) template.type());

        MinecraftServerAccessor.accessor$LOGGER().info("Loading world '{}' ({})", worldKey, worldTypeKey);
        final String directoryName = this.getDirectoryName(worldKey);
        final boolean isVanillaSubLevel = this.isVanillaSubWorld(directoryName);
        final LevelStorageSource.LevelStorageAccess storageSource;

        try {
            if (isVanillaSubLevel) {
                storageSource = LevelStorageSource.createDefault(this.defaultWorldDirectory).createAccess(directoryName);
            } else {
                storageSource = LevelStorageSource.createDefault(this.customWorldsDirectory).createAccess(worldKey.namespace() + File.separator + worldKey.value());
            }
        } catch (final IOException e) {
            e.printStackTrace();
            return FutureUtil.completedWithException(new RuntimeException(String.format("Failed to create level data for world '%s'!", worldKey), e));
        }

        PrimaryLevelData levelData;

        levelData = (PrimaryLevelData) storageSource.getDataTag((DynamicOps<Tag>) BootstrapProperties.worldSettingsAdapter, defaultLevelSettings.getDataPackConfig());
        if (levelData == null) {
            final LevelSettings levelSettings;
            final WorldGenSettings generationSettings;

            if (this.server.isDemo()) {
                levelSettings = MinecraftServer.DEMO_SETTINGS;
                generationSettings = WorldGenSettings.demoSettings(BootstrapProperties.registries);
            } else {
                levelSettings = new LevelSettings(directoryName, (GameType) (Object) BootstrapProperties.gameMode.get(Sponge.game().registries()),
                        templateBridge.bridge$hardcore().orElse(BootstrapProperties.hardcore), (Difficulty) (Object) BootstrapProperties.difficulty
                        .get(Sponge.game().registries()), templateBridge.bridge$commands().orElse(BootstrapProperties.commands), new GameRules(),
                    defaultLevelData.getDataPackConfig());
                generationSettings = generatorSettings;
            }

            levelData = new PrimaryLevelData(levelSettings, generationSettings, Lifecycle.stable());
        }

        ((PrimaryLevelDataBridge) levelData).bridge$populateFromDimension(template);

        final InheritableConfigHandle<WorldConfig> configAdapter = SpongeGameConfigs.createWorld(worldTypeKey, worldKey);
        ((PrimaryLevelDataBridge) levelData).bridge$configAdapter(configAdapter);

        levelData.setModdedInfo(this.server.getServerModName(), this.server.getModdedStatus().isPresent());
        final boolean isDebugGeneration = levelData.worldGenSettings().isDebug();
        final long seed = BiomeManager.obfuscateSeed(levelData.worldGenSettings().seed());

        final ChunkProgressListener chunkStatusListener = ((MinecraftServerAccessor) this.server).accessor$progressListenerFactory().create(11);

        final ServerLevel world = new ServerLevel(this.server, ((MinecraftServerAccessor) this.server).accessor$executor(), storageSource, levelData,
                registryKey, (DimensionType) worldType, chunkStatusListener, template.generator(), isDebugGeneration, seed, ImmutableList.of(), true);
        this.worlds.put(registryKey, world);

        return SpongeCommon.getAsyncScheduler().submit(() -> this.prepareWorld(world, isDebugGeneration)).thenApply(w -> {
            ((MinecraftServerAccessor) this.server).invoker$forceDifficulty();
            return w;
        }).thenCompose(w -> this.postWorldLoad(w, false))
          .thenApply(w -> (org.spongepowered.api.world.server.ServerWorld) w);
    }

    @Override
    public CompletableFuture<Boolean> unloadWorld(final ResourceKey key) {
        final net.minecraft.resources.ResourceKey<Level> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(key, "key"));

        if (Level.OVERWORLD.equals(registryKey)) {
            return CompletableFuture.completedFuture(false);
        }

        final ServerLevel world = this.worlds.get(registryKey);
        if (world == null) {
            return CompletableFuture.completedFuture(false);
        }

        return this.unloadWorld((org.spongepowered.api.world.server.ServerWorld) world);
    }

    @Override
    public CompletableFuture<Boolean> unloadWorld(final org.spongepowered.api.world.server.ServerWorld world) {
        final net.minecraft.resources.ResourceKey<Level> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(world, "world").key());

        if (Level.OVERWORLD.equals(registryKey)) {
            return CompletableFuture.completedFuture(false);
        }

        if (world != this.worlds.get(registryKey)) {
            return CompletableFuture.completedFuture(false);
        }

        try {
            this.unloadWorld0((ServerLevel) world);
        } catch (final IOException e) {
            return FutureUtil.completedWithException(e);
        }

        return CompletableFuture.completedFuture(true);
    }

    @Override
    public boolean templateExists(final ResourceKey key) {
        return Files.exists(this.getDataPackFile(Objects.requireNonNull(key, "key")));
    }

    @Override
    public CompletableFuture<Optional<WorldTemplate>> loadTemplate(final ResourceKey key) {
        final Path dataPackFile = this.getDataPackFile(Objects.requireNonNull(key, "key"));
        if (Files.exists(dataPackFile)) {
            try {
                final LevelStem template = this.loadTemplate0(SpongeWorldManager.createRegistryKey(key), dataPackFile);
                ((ResourceKeyBridge) (Object) template).bridge$setKey(key);
                return CompletableFuture.completedFuture(Optional.of(((LevelStemBridge) (Object) template).bridge$asTemplate()));
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    public CompletableFuture<Boolean> saveTemplate(final WorldTemplate template) {
        final LevelStem scratch = ((SpongeWorldTemplate) Objects.requireNonNull(template, "template")).asDimension();
        try {
            final JsonElement element = SpongeWorldTemplate.DIRECT_CODEC.encodeStart(RegistryWriteOps.create(JsonOps.INSTANCE, BootstrapProperties.registries), scratch).getOrThrow(true, s -> { });
            final Path dataPackFile = this.getDataPackFile(template.key());
            Files.createDirectories(dataPackFile.getParent());
            DataPackSerializer.writeFile(dataPackFile, element);
            DataPackSerializer.writePackMetadata("World", this.dimensionsDataPackDirectory.getParent());
        } catch (final Exception ex) {
            FutureUtil.completedWithException(ex);
        }
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Optional<ServerWorldProperties>> loadProperties(final ResourceKey key) {
        final net.minecraft.resources.ResourceKey<Level> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(key, "key"));

        if (this.worlds.get(registryKey) != null) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        if (!this.worldExists(key)) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        final boolean isVanillaWorld = this.isVanillaWorld(key);
        final String directoryName = this.getDirectoryName(key);
        final LevelStorageSource.LevelStorageAccess storageSource;

        try {
            if (isVanillaWorld) {
                storageSource = LevelStorageSource.createDefault(this.defaultWorldDirectory).createAccess(directoryName);
            } else {
                storageSource = LevelStorageSource.createDefault(this.customWorldsDirectory).createAccess(key.namespace() + File.separator +
                        key.value());
            }
        } catch (final IOException e) {
            return FutureUtil.completedWithException(e);
        }

        final PrimaryLevelData defaultLevelData = (PrimaryLevelData) this.server.getWorldData();
        final LevelSettings defaultLevelSettings = ((PrimaryLevelDataAccessor) defaultLevelData).accessor$settings();

        final WorldData levelData;
        try {
            levelData = storageSource.getDataTag((DynamicOps<Tag>) BootstrapProperties.worldSettingsAdapter, defaultLevelSettings.getDataPackConfig());
        } catch (final Exception ex) {
            return FutureUtil.completedWithException(ex);
        }

        return this.loadTemplate(key).thenCompose(r -> {
            r.ifPresent(template -> {
                final LevelStem scratch = ((SpongeWorldTemplate) template).asDimension();
                ((PrimaryLevelDataBridge) levelData).bridge$populateFromDimension(scratch);
            });

            return CompletableFuture.completedFuture(Optional.of((ServerWorldProperties) levelData));
        });
    }

    @Override
    public CompletableFuture<Boolean> saveProperties(final ServerWorldProperties properties) {
        final net.minecraft.resources.ResourceKey<Level> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(properties, "properties").key());

        if (this.worlds.get(registryKey) != null) {
            return CompletableFuture.completedFuture(false);
        }

        final ResourceKey key = properties.key();

        final boolean isVanillaWorld = this.isVanillaWorld(key);
        final String directoryName = this.getDirectoryName(key);
        final LevelStorageSource.LevelStorageAccess storageSource;

        try {
            if (isVanillaWorld) {
                storageSource = LevelStorageSource.createDefault(this.defaultWorldDirectory).createAccess(directoryName);
            } else {
                storageSource = LevelStorageSource.createDefault(this.customWorldsDirectory).createAccess(key.namespace() + File.separator +
                        key.value());
            }
        } catch (final IOException e) {
            return FutureUtil.completedWithException(e);
        }

        try {
            storageSource.saveDataTag(BootstrapProperties.registries, (WorldData) properties, null);
        } catch (final Exception ex) {
            return FutureUtil.completedWithException(ex);
        }

        // Properties doesn't have everything we need...namely the generator, load the template and set values we actually got
        return this.loadTemplate(key).thenCompose(r -> {
            final WorldTemplate template = r.orElse(null);
            if (template != null) {
                final LevelStem scratch = ((SpongeWorldTemplate) template).asDimension();
                ((LevelStemBridge) (Object) scratch).bridge$populateFromLevelData((PrimaryLevelData) properties);

                return this.saveTemplate(((LevelStemBridge) (Object) scratch).bridge$asTemplate());
            }

            return CompletableFuture.completedFuture(true);
        });
    }

    @Override
    public CompletableFuture<Boolean> copyWorld(final ResourceKey key, final ResourceKey copyKey) {
        final net.minecraft.resources.ResourceKey<Level> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(key, "key"));
        final net.minecraft.resources.ResourceKey<Level> copyRegistryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(copyKey, "copyKey"));

        if (Level.OVERWORLD.equals(copyRegistryKey)) {
            return CompletableFuture.completedFuture(false);
        }

        if (!this.worldExists(key)) {
            return CompletableFuture.completedFuture(false);
        }

        if (this.worldExists(copyKey)) {
            return CompletableFuture.completedFuture(false);
        }

        final ServerLevel loadedWorld = this.worlds.get(registryKey);
        boolean disableLevelSaving = false;

        if (loadedWorld != null) {
            disableLevelSaving = loadedWorld.noSave;
            loadedWorld.save(null, true, loadedWorld.noSave);
            loadedWorld.noSave = true;
        }

        final boolean isDefaultWorld = this.isDefaultWorld(key);
        final boolean isVanillaWorld = this.isVanillaWorld(key);
        final String directoryName = this.getDirectoryName(key);

        final Path originalDirectory = isDefaultWorld ? this.defaultWorldDirectory : isVanillaWorld ? this.defaultWorldDirectory
                .resolve(directoryName) : this.customWorldsDirectory.resolve(key.namespace()).resolve(key.value());

        final boolean isVanillaCopyWorld = this.isVanillaWorld(copyKey);
        final String copyDirectoryName = this.getDirectoryName(copyKey);

        final Path copyDirectory = isVanillaCopyWorld ? this.defaultWorldDirectory
                .resolve(copyDirectoryName) : this.customWorldsDirectory.resolve(copyKey.namespace()).resolve(copyKey.value());

        try {
            Files.walkFileTree(originalDirectory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                    // Silly recursion if the default world is being copied
                    if (dir.getFileName().toString().equals(Constants.Sponge.World.DIMENSIONS_DIRECTORY)) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }

                    // Silly copying of vanilla sub worlds if the default world is being copied
                    if (isDefaultWorld && VanillaWorldManager.this.isVanillaSubWorld(dir.getFileName().toString())) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }

                    final Path relativize = originalDirectory.relativize(dir);
                    final Path directory = copyDirectory.resolve(relativize);
                    Files.createDirectories(directory);

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                    final String fileName = file.getFileName().toString();
                    // Do not copy backups (not relevant anymore)
                    if (fileName.equals(Constants.Sponge.World.LEVEL_SPONGE_DAT_OLD)) {
                        return FileVisitResult.CONTINUE;
                    }
                    if (fileName.equals(Constants.World.LEVEL_DAT_OLD)) {
                        return FileVisitResult.CONTINUE;
                    }
                    Files.copy(file, copyDirectory.resolve(originalDirectory.relativize(file)), StandardCopyOption.COPY_ATTRIBUTES,
                            StandardCopyOption.REPLACE_EXISTING);

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (final IOException e) {
            // Bail the whole deal if we hit IO problems!
            try {
                Files.deleteIfExists(copyDirectory);
            } catch (final IOException ignore) {
            }

            return FutureUtil.completedWithException(e);
        }

        if (loadedWorld != null) {
            loadedWorld.noSave = disableLevelSaving;
        }

        final Path dimensionTemplate = this.getDataPackFile(key);
        final Path copiedDimensionTemplate = this.getDataPackFile(copyKey);

        try {
            Files.createDirectories(copiedDimensionTemplate.getParent());
            Files.copy(dimensionTemplate, copiedDimensionTemplate);
        } catch (final IOException e) {
            FutureUtil.completedWithException(e);
        }

        final JsonObject fixedObject;
        try (final InputStream stream = Files.newInputStream(copiedDimensionTemplate); final InputStreamReader reader = new InputStreamReader(stream)) {
            final JsonParser parser = new JsonParser();
            final JsonElement element = parser.parse(reader);

            final JsonObject root = element.getAsJsonObject();
            final JsonObject spongeData = root.getAsJsonObject("#sponge");
            spongeData.remove("unique_id");
            fixedObject = root;
        } catch (final IOException e) {
            return FutureUtil.completedWithException(e);
        }

        if (fixedObject != null) {
            try (final BufferedWriter writer = Files.newBufferedWriter(copiedDimensionTemplate)) {
                writer.write(fixedObject.toString());
            } catch (final IOException e) {
                FutureUtil.completedWithException(e);
            }
        }

        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> moveWorld(final ResourceKey key, final ResourceKey movedKey) {
        final net.minecraft.resources.ResourceKey<Level> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(key, "key"));

        if (Level.OVERWORLD.equals(registryKey)) {
            return CompletableFuture.completedFuture(false);
        }

        if (!this.worldExists(key)) {
            return CompletableFuture.completedFuture(false);
        }

        if (this.worldExists(movedKey)) {
            return CompletableFuture.completedFuture(false);
        }

        final ServerLevel loadedWorld = this.worlds.get(registryKey);
        if (loadedWorld != null) {
            try {
                this.unloadWorld0(loadedWorld);
            } catch (final IOException e) {
                return FutureUtil.completedWithException(e);
            }
        }

        final boolean isVanillaWorld = this.isVanillaWorld(key);
        final String directoryName = this.getDirectoryName(key);

        final Path originalDirectory = isVanillaWorld ? this.defaultWorldDirectory
                .resolve(directoryName) : this.customWorldsDirectory.resolve(key.namespace()).resolve(key.value());

        final boolean isVanillaMoveWorld = this.isVanillaWorld(movedKey);
        final String moveDirectoryName = this.getDirectoryName(movedKey);

        final Path moveDirectory = isVanillaMoveWorld ? this.defaultWorldDirectory
                .resolve(moveDirectoryName) : this.customWorldsDirectory.resolve(key.namespace()).resolve(key.value());

        try {
            Files.createDirectories(moveDirectory);
            Files.move(originalDirectory, moveDirectory, StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            return FutureUtil.completedWithException(e);
        }

        final Path configFile = SpongeCommon.getSpongeConfigDirectory().resolve(SpongeCommon.ECOSYSTEM_ID).resolve("worlds").resolve(key
                .namespace()).resolve(key.value() + ".conf");

        final Path copiedConfigFile = SpongeCommon.getSpongeConfigDirectory().resolve(SpongeCommon.ECOSYSTEM_ID).resolve("worlds")
                .resolve(movedKey.namespace()).resolve(movedKey.value() + ".conf");

        try {
            Files.createDirectories(copiedConfigFile.getParent());
            Files.move(configFile, copiedConfigFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            return FutureUtil.completedWithException(e);
        }

        final Path dimensionTemplate = this.getDataPackFile(key);
        final Path copiedDimensionTemplate = this.getDataPackFile(movedKey);

        try {
            Files.createDirectories(copiedDimensionTemplate.getParent());
            Files.move(dimensionTemplate, copiedDimensionTemplate, StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            FutureUtil.completedWithException(e);
        }

        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> deleteWorld(final ResourceKey key) {
        final net.minecraft.resources.ResourceKey<Level> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(key, "key"));

        if (Level.OVERWORLD.equals(registryKey)) {
            return CompletableFuture.completedFuture(false);
        }

        if (!this.worldExists(key)) {
            return CompletableFuture.completedFuture(false);
        }

        final ServerLevel loadedWorld = this.worlds.get(registryKey);
        if (loadedWorld != null) {
            final boolean disableLevelSaving = loadedWorld.noSave;
            loadedWorld.noSave = true;
            try {
                this.unloadWorld0(loadedWorld);
            } catch (final IOException e) {
                loadedWorld.noSave = disableLevelSaving;
                return FutureUtil.completedWithException(e);
            }
        }

        final boolean isVanillaWorld = this.isVanillaWorld(key);
        final String directoryName = this.getDirectoryName(key);

        final Path directory = isVanillaWorld ? this.defaultWorldDirectory.resolve(directoryName) : this.customWorldsDirectory.resolve(key.namespace()).resolve(key.value());

        if (Files.exists(directory)) {
            try {
                for (final Path path : Files.walk(directory).sorted(Comparator.reverseOrder()).collect(Collectors.toList())) {
                    Files.deleteIfExists(path);
                }
            } catch (final IOException e) {
                return FutureUtil.completedWithException(e);
            }
        }

        final Path configFile = SpongeCommon.getSpongeConfigDirectory().resolve(SpongeCommon.ECOSYSTEM_ID).resolve("worlds").resolve(key.namespace()).resolve(key.value() + ".conf");

        try {
            Files.deleteIfExists(configFile);
        } catch (final IOException e) {
            return FutureUtil.completedWithException(e);
        }

        final Path dimensionTemplate = this.getDataPackFile(key);

        try {
            Files.deleteIfExists(dimensionTemplate);
        } catch (final IOException e) {
            FutureUtil.completedWithException(e);
        }

        return CompletableFuture.completedFuture(true);
    }

    @Override
    public void unloadWorld0(final ServerLevel world) throws IOException {
        final net.minecraft.resources.ResourceKey<Level> registryKey = world.dimension();

        if (world.getPlayers(p -> true).size() != 0) {
            throw new IOException(String.format("World '%s' was told to unload but players remain.", registryKey.location()));
        }

        SpongeCommon.getLogger().info("Unloading world '{}' ({})", registryKey.location(), RegistryTypes.WORLD_TYPE.get().valueKey((WorldType) world.dimensionType()));

        final BlockPos spawnPoint = world.getSharedSpawnPos();
        world.getChunkSource().removeRegionTicket(VanillaWorldManager.SPAWN_CHUNKS, new ChunkPos(spawnPoint), 11, registryKey.location());

        ((PrimaryLevelDataBridge) world.getLevelData()).bridge$configAdapter().save();
        ((ServerLevelBridge) world).bridge$setManualSave(true);

        try {
            world.save(null, true, world.noSave);
            world.close();
            ((ServerLevelBridge) world).bridge$getLevelSave().close();
        } catch (final Exception ex) {
            throw new IOException(ex);
        }

        this.worlds.remove(registryKey);

        SpongeCommon.postEvent(SpongeEventFactory.createUnloadWorldEvent(PhaseTracker.getCauseStackManager().currentCause(), (org.spongepowered.api.world.server.ServerWorld) world));
    }

    @Override
    public void loadLevel() {
        final PrimaryLevelData defaultLevelData = (PrimaryLevelData) this.server.getWorldData();
        final WorldGenSettings defaultGenerationSettings = defaultLevelData.worldGenSettings();
        final LevelSettings defaultLevelSettings = ((PrimaryLevelDataAccessor) defaultLevelData).accessor$settings();

        final MappedRegistry<LevelStem> templates = defaultGenerationSettings.dimensions();

        final boolean multiworldEnabled = this.server.isSingleplayer() || this.server.isNetherEnabled();
        if (!multiworldEnabled) {
            SpongeCommon.getLogger().warn("The option 'allow-nether' has been set to 'false' in the server.properties. "
                    + "Multi-World support has been disabled and no worlds besides the default world will be loaded.");
        }

        for (final RegistryEntry<LevelStem> entry : ((Registry<LevelStem>) (Object) templates).streamEntries().collect(Collectors.toList())) {
            final ResourceKey worldKey = entry.key();
            final LevelStem template = entry.value();
            final LevelStemBridge templateBridge = (LevelStemBridge) (Object) template;
            ((ResourceKeyBridge) templateBridge).bridge$setKey(worldKey);

            final boolean isDefaultWorld = this.isDefaultWorld(worldKey);
            if (!isDefaultWorld && !multiworldEnabled) {
                continue;
            }

            final WorldType worldType = (WorldType) template.type();
            final ResourceKey worldTypeKey = RegistryTypes.WORLD_TYPE.get().valueKey((WorldType) template.type());

            MinecraftServerAccessor.accessor$LOGGER().info("Loading world '{}' ({})", worldKey, worldTypeKey);
            if (!isDefaultWorld && !templateBridge.bridge$loadOnStartup()) {
                SpongeCommon.getLogger().warn("World '{}' has been disabled from loading at startup. Skipping...", worldKey);
                continue;
            }

            final String directoryName = this.getDirectoryName(worldKey);
            final boolean isVanillaSubLevel = this.isVanillaSubWorld(directoryName);
            final LevelStorageSource.LevelStorageAccess storageSource;

            if (isDefaultWorld) {
                storageSource = ((MinecraftServerAccessor) this.server).accessor$storageSource();
            } else {
                try {
                    if (isVanillaSubLevel) {
                        storageSource = LevelStorageSource.createDefault(this.defaultWorldDirectory).createAccess(directoryName);
                    } else {
                        storageSource = LevelStorageSource.createDefault(this.customWorldsDirectory).createAccess(worldKey.namespace() + File.separator + worldKey.value());
                    }
                } catch (final IOException e) {
                    throw new RuntimeException(String.format("Failed to create level data for world '%s'!", worldKey), e);
                }
            }

            PrimaryLevelData levelData;
            final boolean isDebugGeneration;

            if (isDefaultWorld) {
                levelData = defaultLevelData;
                isDebugGeneration = defaultGenerationSettings.isDebug();
            } else {
                levelData = (PrimaryLevelData) storageSource
                        .getDataTag((DynamicOps<Tag>) BootstrapProperties.worldSettingsAdapter, defaultLevelSettings.getDataPackConfig());
                if (levelData == null) {
                    final LevelSettings levelSettings;
                    final WorldGenSettings generationSettings;

                    if (this.server.isDemo()) {
                        levelSettings = MinecraftServer.DEMO_SETTINGS;
                        generationSettings = WorldGenSettings.demoSettings(BootstrapProperties.registries);
                    } else {
                        levelSettings = new LevelSettings(directoryName,
                                (GameType) (Object) BootstrapProperties.gameMode.get(Sponge.game().registries()),
                                templateBridge.bridge$hardcore().orElse(BootstrapProperties.hardcore),
                                (Difficulty) (Object) BootstrapProperties.difficulty.get(Sponge.game().registries()),
                                templateBridge.bridge$commands().orElse(BootstrapProperties.commands), new GameRules(), defaultLevelData.getDataPackConfig());
                        generationSettings = ((WorldGenSettingsBridge) defaultLevelData.worldGenSettings()).bridge$copy();
                    }

                    isDebugGeneration = generationSettings.isDebug();

                    ((DimensionGeneratorSettingsAccessor) generationSettings).accessor$dimensions(new MappedRegistry<>(
                            net.minecraft.core.Registry.LEVEL_STEM_REGISTRY, Lifecycle.stable()));

                    levelData = new PrimaryLevelData(levelSettings, generationSettings, Lifecycle.stable());
                } else {
                    isDebugGeneration = levelData.worldGenSettings().isDebug();
                }
            }

            ((PrimaryLevelDataBridge) levelData).bridge$populateFromDimension(template);

            final InheritableConfigHandle<WorldConfig> configAdapter = SpongeGameConfigs.createWorld(worldTypeKey, worldKey);
            ((PrimaryLevelDataBridge) levelData).bridge$configAdapter(configAdapter);

            levelData.setModdedInfo(this.server.getServerModName(), this.server.getModdedStatus().isPresent());
            final long seed = BiomeManager.obfuscateSeed(levelData.worldGenSettings().seed());

            final net.minecraft.resources.ResourceKey<Level> registryKey = SpongeWorldManager.createRegistryKey(worldKey);
            final ChunkProgressListener chunkStatusListener = ((MinecraftServerAccessor) this.server).accessor$progressListenerFactory().create(11);
            final List<CustomSpawner> spawners;
            if (isDefaultWorld) {
                spawners = ImmutableList.of(new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(levelData));
            } else {
                spawners = ImmutableList.of();
            }

            final ServerLevel world = new ServerLevel(this.server, ((MinecraftServerAccessor) this.server).accessor$executor(), storageSource, levelData,
                    registryKey, (DimensionType) worldType, chunkStatusListener, template.generator(), isDebugGeneration, seed, spawners, true);
            // Ensure that the world border is registered.
            world.getWorldBorder().applySettings(levelData.getWorldBorder());

            this.worlds.put(registryKey, world);

            this.prepareWorld(world, isDebugGeneration);
        }

        ((MinecraftServerAccessor) this.server).invoker$forceDifficulty();

        for (final Map.Entry<net.minecraft.resources.ResourceKey<Level>, ServerLevel> entry : this.worlds.entrySet()) {
            try {
                this.postWorldLoad(entry.getValue(), true).get();
            } catch (final InterruptedException | ExecutionException e) {
                throw new IllegalStateException(e);
            }
        }

        ((SpongeUserManager) Sponge.server().userManager()).init();
        ((SpongeServer) SpongeCommon.getServer()).getPlayerDataManager().load();
    }

    private ServerLevel prepareWorld(final ServerLevel world, final boolean isDebugGeneration) {
        final boolean isDefaultWorld = Level.OVERWORLD.equals(world.dimension());
        final PrimaryLevelData levelData = (PrimaryLevelData) world.getLevelData();

        if (isDefaultWorld) {
            // Initialize scoreboard data. This will hook to the ServerScoreboard, needs to be made multi-world aware
            ((MinecraftServerAccessor) this.server).accessor$readScoreboard(world.getDataStorage());

            ((MinecraftServerAccessor) this.server).accessor$commandStorage(new CommandStorage(world.getDataStorage()));
        }

        final boolean isInitialized = levelData.isInitialized();

        SpongeCommon.postEvent(SpongeEventFactory.createLoadWorldEvent(PhaseTracker.getCauseStackManager().currentCause(),
            (org.spongepowered.api.world.server.ServerWorld) world, isInitialized));

        // Set the view distance back on it's self to trigger the logic
        ((PrimaryLevelDataBridge) world.getLevelData()).bridge$viewDistance().ifPresent(v -> ((PrimaryLevelDataBridge) world.getLevelData()).bridge$setViewDistance(v));

        world.getWorldBorder().applySettings(levelData.getWorldBorder());

        if (!isInitialized) {
            try {
                final boolean hasSpawnAlready = ((PrimaryLevelDataBridge) world.getLevelData()).bridge$customSpawnPosition();
                if (!hasSpawnAlready) {
                    if (isDefaultWorld || ((ServerWorldProperties) world.getLevelData()).performsSpawnLogic()) {
                        MinecraftServerAccessor.invoker$setInitialSpawn(world, levelData, levelData.worldGenSettings().generateBonusChest(), isDebugGeneration, !isDebugGeneration);
                    } else if (Level.END.equals(world.dimension())) {
                        ((PrimaryLevelData) world.getLevelData()).setSpawn(ServerLevel.END_SPAWN_POINT, 0);
                    }
                } else {
                    Features.BONUS_CHEST.place(world, world.getChunkSource().getGenerator(), world.random, new BlockPos(levelData.getXSpawn(),
                            levelData.getYSpawn(),levelData.getZSpawn()));
                }
                levelData.setInitialized(true);
                if (isDebugGeneration) {
                    ((MinecraftServerAccessor) this.server).invoker$setupDebugLevel(levelData);
                }
            } catch (final Throwable throwable) {
                final CrashReport crashReport = CrashReport.forThrowable(throwable, "Exception initializing world '" + world.dimension().location()  + "'");
                try {
                    world.fillReportDetails(crashReport);
                } catch (final Throwable ignore) {
                }

                throw new ReportedException(crashReport);
            }

            levelData.setInitialized(true);
        }

        // Initialize PlayerData in PlayerList, add WorldBorder listener. We change the method in PlayerList to handle per-world border
        this.server.getPlayerList().setLevel(world);

        if (levelData.getCustomBossEvents() != null) {
            ((ServerLevelBridge) world).bridge$getBossBarManager().load(levelData.getCustomBossEvents());
        }

        return world;
    }

    private CompletableFuture<ServerLevel> postWorldLoad(final ServerLevel world, final boolean blocking) {
        final PrimaryLevelData levelData = (PrimaryLevelData) world.getLevelData();
        final PrimaryLevelDataBridge levelBridge = (PrimaryLevelDataBridge) levelData;
        final boolean isDefaultWorld = this.isDefaultWorld((ResourceKey) (Object) world.dimension().location());
        if (isDefaultWorld || levelBridge.bridge$performsSpawnLogic()) {
            MinecraftServerAccessor.accessor$LOGGER().info("Preparing start region for world '{}' ({})", world.dimension().location(),
                    RegistryTypes.WORLD_TYPE.get().valueKey((WorldType) world.dimensionType()));
            if (blocking) {
                this.loadSpawnChunks(world);
                return CompletableFuture.completedFuture(world); // Chunk are generated
            } else {
                return this.loadSpawnChunksAsync(world); // Chunks are NOT generated yet BUT will be when the future returns
            }
        }
        return CompletableFuture.completedFuture(world); // Chunks are NOT generated AND will not generate unless prompted
    }

    private CompletableFuture<ServerLevel> loadSpawnChunksAsync(final ServerLevel world) {

        final BlockPos spawnPoint = world.getSharedSpawnPos();
        final ChunkPos chunkPos = new ChunkPos(spawnPoint);
        final ServerChunkCache serverChunkProvider = world.getChunkSource();
        serverChunkProvider.getLightEngine().setTaskPerBatch(500);

        final int borderRadius = 11;
        final int diameter = ((borderRadius - 1) * 2) + 1;
        final int spawnChunks = diameter * diameter;

        serverChunkProvider.addRegionTicket(VanillaWorldManager.SPAWN_CHUNKS, chunkPos, borderRadius, world.dimension().location());
        final CompletableFuture<ServerLevel> generationFuture = new CompletableFuture<>();
        Sponge.asyncScheduler().submit(
                Task.builder().plugin(Launch.getInstance().getPlatformPlugin())
                        .execute(task -> {
                            if (serverChunkProvider.getTickingGenerated() >= spawnChunks) {
                                Sponge.server().scheduler().submit(Task.builder().plugin(Launch.getInstance().getPlatformPlugin()).execute(() -> generationFuture.complete(world)).build());
                                // Notify the future that we are done
                                task.cancel(); // And cancel this task
                                MinecraftServerAccessor.accessor$LOGGER().info("Done preparing start region for world '{}' ({})", world
                                        .dimension().location(), RegistryTypes.WORLD_TYPE.get().valueKey((WorldType) world.dimensionType()));
                            }
                        })
                        .interval(10, TimeUnit.MILLISECONDS)
                        .build()
        );
        return generationFuture.thenApply(v -> {
            this.updateForcedChunks(world, serverChunkProvider);
            serverChunkProvider.getLightEngine().setTaskPerBatch(5);

            // Sponge Start - Release the chunk ticket if spawn is not set to be kept loaded...
            if (!((PrimaryLevelDataBridge) world.getLevelData()).bridge$performsSpawnLogic()) {
                serverChunkProvider.removeRegionTicket(VanillaWorldManager.SPAWN_CHUNKS, chunkPos, 11, world.dimension().location());
            }
            return world;
        });
    }

    private void loadSpawnChunks(final ServerLevel world) {
        final BlockPos spawnPoint = world.getSharedSpawnPos();
        final ChunkPos chunkPos = new ChunkPos(spawnPoint);
        final ChunkProgressListener chunkStatusListener = ((ServerLevelBridge) world).bridge$getChunkStatusListener();
        chunkStatusListener.updateSpawnPos(chunkPos);
        final ServerChunkCache serverChunkProvider = world.getChunkSource();
        serverChunkProvider.getLightEngine().setTaskPerBatch(500);
        ((MinecraftServerAccessor) this.server).accessor$nextTickTime(Util.getMillis());
        serverChunkProvider.addRegionTicket(VanillaWorldManager.SPAWN_CHUNKS, chunkPos, 11, world.dimension().location());

        while (serverChunkProvider.getTickingGenerated() != 441) {
            ((MinecraftServerAccessor) this.server).accessor$nextTickTime(Util.getMillis() + 10L);
            ((MinecraftServerAccessor) this.server).accessor$waitUntilNextTick();
        }

        ((MinecraftServerAccessor) this.server).accessor$nextTickTime(Util.getMillis() + 10L);
        ((MinecraftServerAccessor) this.server).accessor$waitUntilNextTick();

        this.updateForcedChunks(world, serverChunkProvider);

        ((MinecraftServerAccessor) this.server).accessor$nextTickTime(Util.getMillis() + 10L);
        ((MinecraftServerAccessor) this.server).accessor$waitUntilNextTick();
        chunkStatusListener.stop();
        serverChunkProvider.getLightEngine().setTaskPerBatch(5);

        // Sponge Start - Release the chunk ticket if spawn is not set to be kept loaded...
        if (!((PrimaryLevelDataBridge) world.getLevelData()).bridge$performsSpawnLogic()) {
            serverChunkProvider.removeRegionTicket(VanillaWorldManager.SPAWN_CHUNKS, chunkPos, 11, world.dimension().location());
        }
    }

    private void updateForcedChunks(final ServerLevel world, final ServerChunkCache serverChunkProvider) {
        final ForcedChunksSavedData forcedChunksSaveData = world.getDataStorage().get(ForcedChunksSavedData::new, "chunks");
        if (forcedChunksSaveData != null) {
            final LongIterator longIterator = forcedChunksSaveData.getChunks().iterator();

            while (longIterator.hasNext()) {
                final long i = longIterator.nextLong();
                final ChunkPos forceChunkPos = new ChunkPos(i);
                serverChunkProvider.updateChunkForced(forceChunkPos, true);
            }
        }
    }

    private LevelStem loadTemplate0(final net.minecraft.resources.ResourceKey<Level> registryKey, final Path file) throws IOException {
        try (final InputStream stream = Files.newInputStream(file); final InputStreamReader reader = new InputStreamReader(stream)) {
            final JsonParser parser = new JsonParser();
            final JsonElement element = parser.parse(reader);
            final SingleTemplateAccess singleTemplateAccess = new SingleTemplateAccess(registryKey, element);
            final RegistryReadOps<JsonElement> settingsAdapter = RegistryReadOps.create(JsonOps.INSTANCE, singleTemplateAccess, (RegistryAccess.RegistryHolder) BootstrapProperties.registries);
            final MappedRegistry<LevelStem> registry = new MappedRegistry<>(net.minecraft.core.Registry.LEVEL_STEM_REGISTRY, Lifecycle.stable());
            settingsAdapter.decodeElements(registry, net.minecraft.core.Registry.LEVEL_STEM_REGISTRY, LevelStem.CODEC);
            final LevelStem template = registry.stream().findAny().orElse(null);
            if (template != null) {
                ((LevelStemBridge) (Object) template).bridge$setFromSettings(false);
            }
            return template;
        }
    }

    private static final class SingleTemplateAccess implements RegistryReadOps.ResourceAccess {

        private final net.minecraft.resources.ResourceKey<?> key;
        private final JsonElement element;

        public SingleTemplateAccess(final net.minecraft.resources.ResourceKey<?> key, final JsonElement element) {
            this.key = key;
            this.element = element;
        }

        @Override
        public Collection<ResourceLocation> listResources(final net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<?>> registryKey) {
            if (this.key.isFor(registryKey)) {
                return Collections.singletonList(new ResourceLocation(this.key.location().getNamespace(),
                        registryKey.location().getPath() + "/" + this.key.location().getPath() + ".json"));
            }
            return Collections.emptyList();
        }

        @Override
        public <E> DataResult<Pair<E, OptionalInt>> parseElement(final DynamicOps<JsonElement> ops, final net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<E>> registryKey, final net.minecraft.resources.ResourceKey<E> elementKey, final Decoder<E> decoder) {
            final DataResult<E> result = decoder.parse(ops, this.element);
            return result.map(t -> Pair.of(t, OptionalInt.empty()));
        }
    }
}
