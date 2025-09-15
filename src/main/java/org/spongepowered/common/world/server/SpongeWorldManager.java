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
package org.spongepowered.common.world.server;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.longs.LongIterator;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.MiscOverworldFeatures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.WanderingTraderSpawner;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.file.DeleteFileVisitor;
import org.spongepowered.api.world.DefaultWorldKeys;
import org.spongepowered.api.world.WorldType;
import org.spongepowered.api.world.generation.config.WorldGenerationConfig;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.WorldArchetype;
import org.spongepowered.api.world.server.WorldArchetypeType;
import org.spongepowered.api.world.server.WorldManager;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.server.MinecraftServerAccessor;
import org.spongepowered.common.accessor.server.level.ServerLevelAccessor;
import org.spongepowered.common.accessor.world.level.storage.PrimaryLevelDataAccessor;
import org.spongepowered.common.bridge.core.MappedRegistryBridge;
import org.spongepowered.common.bridge.server.level.ServerLevelBridge;
import org.spongepowered.common.bridge.world.level.chunk.storage.IOWorkerBridge;
import org.spongepowered.common.bridge.world.level.dimension.LevelStemBridge;
import org.spongepowered.common.bridge.world.level.storage.LevelStorageAccessBridge;
import org.spongepowered.common.bridge.world.level.storage.PrimaryLevelDataBridge;
import org.spongepowered.common.bridge.world.level.storage.ServerLevelDataBridge;
import org.spongepowered.common.config.SpongeGameConfigs;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.hooks.PlatformHooks;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.launch.config.core.SpongeConfigs;
import org.spongepowered.common.user.SpongeUserManager;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.FutureUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SpongeWorldManager implements WorldManager {

    private final MinecraftServer server;
    private final Path defaultWorldDirectory, customWorldsDirectory;
    private final Map<net.minecraft.resources.ResourceKey<Level>, ServerLevel> worlds;

    public SpongeWorldManager(final MinecraftServer server) {
        this.server = server;
        this.defaultWorldDirectory = ((MinecraftServerAccessor) this.server).accessor$storageSource().getLevelDirectory().path();
        this.customWorldsDirectory = this.defaultWorldDirectory.resolve("dimensions");
        try {
            Files.createDirectories(this.customWorldsDirectory);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        this.worlds = ((MinecraftServerAccessor) this.server).accessor$levels();
    }

    @Override
    public Server server() {
        return (Server) this.server;
    }

    public Path getDefaultWorldDirectory() {
        return this.defaultWorldDirectory;
    }

    @Override
    public Optional<ServerWorld> world(final ResourceKey key) {
        return Optional.ofNullable((ServerWorld) this.worlds.get(SpongeWorldManager.createRegistryKey(Objects
                .requireNonNull(key, "key"))));
    }

    @Override
    public Path worldDirectory(final ResourceKey key) {
        Objects.requireNonNull(key, "key");
        return this.getDirectory(key);
    }

    @Override
    public Collection<ServerWorld> worlds() {
        return Collections.unmodifiableCollection((Collection<ServerWorld>) (Object) this.worlds.values());
    }

    @Override
    public List<ResourceKey> worldKeys() {
        final List<ResourceKey> worldKeys = new ArrayList<>();
        worldKeys.add(DefaultWorldKeys.DEFAULT);

        if (Files.exists(this.getDirectory(DefaultWorldKeys.THE_NETHER))) {
            worldKeys.add(DefaultWorldKeys.THE_NETHER);
        }

        if (Files.exists(this.getDirectory(DefaultWorldKeys.THE_END))) {
            worldKeys.add(DefaultWorldKeys.THE_END);
        }

        try {
            for (final Path namespacedDirectory : Files.list(this.customWorldsDirectory).toList()) {
                if (this.customWorldsDirectory.equals(namespacedDirectory)) {
                    continue;
                }

                for (final Path valueDirectory : Files.list(namespacedDirectory).toList()) {
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
    public boolean worldExists(final ResourceKey key) {
        final net.minecraft.resources.ResourceKey<Level> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(key, "key"));

        if (Level.OVERWORLD.equals(registryKey)) {
            return true;
        }

        if (this.worlds.get(registryKey) != null) {
            return true;
        }

        return Files.exists(this.getDirectory(key));
    }

    @Override
    public Optional<ResourceKey> worldKey(final UUID uniqueId) {
        Objects.requireNonNull(uniqueId, "uniqueId");
        return this.worlds
            .values()
            .stream()
            .filter(w -> ((ServerWorld) w).uniqueId().equals(uniqueId))
            .map(w -> (ServerWorld) w)
            .map(ServerWorld::key)
            .findAny();
    }

    @Override
    public Collection<ServerWorld> worldsOfType(final WorldType type) {
        Objects.requireNonNull(type, "type");

        return this.worlds().stream().filter(w -> w.worldType() == type).collect(Collectors.toList());
    }

    @Override
    public CompletableFuture<Optional<ServerWorld>> loadWorld(final ResourceKey key, ServerWorldProperties.LoadOptions propertiesLoadOption) {
        final net.minecraft.resources.ResourceKey<Level> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(key, "key"));

        final ServerLevel world = this.worlds.get(registryKey);
        if (world != null) {
            final ServerWorldProperties.LoadOptions.@Nullable GetOperation getOperation = propertiesLoadOption.getOperation().orElse(null);
            if (getOperation != null) {
                getOperation.getCallback().ifPresent(c -> c.accept((ServerWorldProperties) world.getLevelData()));
                return CompletableFuture.completedFuture(Optional.of((ServerWorld) world));
            } else if (propertiesLoadOption.loadOperation().isPresent() || propertiesLoadOption.createOperation().isPresent()) {
                return FutureUtil.completedWithException(new IllegalArgumentException("World is already loaded!"));
            }
        }

        if (Level.OVERWORLD.equals(registryKey) && (propertiesLoadOption.loadOperation().isPresent() || propertiesLoadOption.createOperation().isPresent())) {
            return FutureUtil.completedWithException(new IllegalArgumentException("The default world cannot be told to load!"));
        }

        final ServerWorldProperties.LoadOptions.@Nullable LoadOperation loadOperation = propertiesLoadOption.loadOperation().orElse(null);
        if (loadOperation != null && this.worldExists(key)) {
            try {
                final LevelStorageSource.LevelStorageAccess storageSource = this.createLevelStorageAccess(key);
                try {
                    final Optional<LevelDataLoadResult> result = this.loadLevelData(storageSource, registryKey, loadOperation);
                    if (result.isPresent()) {
                        loadOperation.loadCallback().ifPresent(c -> c.accept((ServerWorldProperties) result.get().data()));
                        return this.loadWorld0(registryKey, result.get(), storageSource);
                    } else {
                        storageSource.close();
                    }
                } catch (final Exception e) {
                    storageSource.close();
                    return CompletableFuture.failedFuture(e);
                }
            } catch (final IOException e) {
                return CompletableFuture.failedFuture(e);
            }
        }

        final ServerWorldProperties.LoadOptions.@Nullable CreateOperation createOperation = propertiesLoadOption.createOperation().orElse(null);
        if (createOperation != null) {
            try {
                final LevelStorageSource.LevelStorageAccess storageSource = this.createLevelStorageAccess(key);
                try {
                    final LevelDataLoadResult result = this.initializeLevelData(key, () -> this.createLevelData(registryKey, createOperation.worldArchetype()));
                    createOperation.createCallback().ifPresent(c -> c.accept((ServerWorldProperties) result.data()));
                    return this.loadWorld0(registryKey, result, storageSource);
                } catch (final Exception e) {
                    storageSource.close();
                    return CompletableFuture.failedFuture(e);
                }
            } catch (final IOException e) {
                return CompletableFuture.failedFuture(e);
            }
        }

        return CompletableFuture.completedFuture(Optional.empty());
    }

    private CompletableFuture<Optional<ServerWorld>> loadWorld0(final net.minecraft.resources.ResourceKey<Level> registryKey,
            final LevelDataLoadResult levelData, final LevelStorageSource.LevelStorageAccess storageSource) {
        final ServerLevel world;
        try {
            world = this.createLevel(registryKey, levelData.stem(), storageSource, levelData.data());
        } catch (final Exception e) {
            return FutureUtil.completedWithException(new RuntimeException(String.format("Failed to create level data for world '%s'!", registryKey.location()), e));
        }

        return SpongeCommon.asyncScheduler().submit(() -> this.prepareLevel(world)).thenApply(w -> {
                ((MinecraftServerAccessor) this.server).invoker$forceDifficulty();
                return w;
            }).thenCompose(w -> this.loadSpawnChunksAsync(world))
            .thenApply(w -> Optional.of((ServerWorld) w));
    }

    private LevelStorageSource.LevelStorageAccess createLevelStorageAccess(final ResourceKey worldKey) throws IOException {
        LevelStorageSource.LevelStorageAccess storageAccess;
        if (this.isVanillaWorld(worldKey)) {
            final String directoryName = this.getDirectoryName(worldKey);
            storageAccess = LevelStorageSource.createDefault(this.defaultWorldDirectory).createAccess(directoryName);
        } else {
            final String name = worldKey.namespace() + File.separator + worldKey.value();
            storageAccess = LevelStorageSource.createDefault(this.customWorldsDirectory).createAccess(name);
        }
        ((LevelStorageAccessBridge) storageAccess).bridge$setDedicated(true);
        return storageAccess;
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

        return this.unloadWorld((ServerWorld) world);
    }

    @Override
    public CompletableFuture<Boolean> unloadWorld(final ServerWorld world) {
        final net.minecraft.resources.ResourceKey<Level> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(world, "world").key());

        if (Level.OVERWORLD.equals(registryKey)) {
            return CompletableFuture.completedFuture(false);
        }

        if (world != this.worlds.get(registryKey)) {
            return CompletableFuture.completedFuture(false);
        }

        return this.unloadWorld0((ServerLevel) world);
    }

    @Override
    public CompletableFuture<Optional<ServerWorldProperties>> loadProperties(final ResourceKey key, ServerWorldProperties.LoadOptions propertiesLoadOptions) {
        final net.minecraft.resources.ResourceKey<Level> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(key, "key"));

        final @Nullable ServerLevel level = this.worlds.get(registryKey);
        if (level != null) {
            final ServerWorldProperties.LoadOptions.@Nullable GetOperation getOperation = propertiesLoadOptions.getOperation().orElse(null);
            if (getOperation != null) {
                final ServerWorldProperties properties = (ServerWorldProperties) level.getLevelData();
                getOperation.getCallback().ifPresent(c -> c.accept(properties));
                return CompletableFuture.completedFuture(Optional.of(properties));
            } else if (propertiesLoadOptions.loadOperation().isPresent() || propertiesLoadOptions.createOperation().isPresent()) {
                return FutureUtil.completedWithException(new IllegalArgumentException("World is already loaded!"));
            }
        }

        final ServerWorldProperties.LoadOptions.@Nullable LoadOperation loadOperation = propertiesLoadOptions.loadOperation().orElse(null);
        if (loadOperation != null && this.worldExists(key)) {
            try {
                final Optional<SpongeWorldManager.LevelDataLoadResult> result = this.loadLevelData(registryKey, loadOperation);
                if (result.isPresent()) {
                    final ServerWorldProperties properties = (ServerWorldProperties) result.get().data();
                    loadOperation.loadCallback().ifPresent(c -> c.accept(properties));
                    return CompletableFuture.completedFuture(Optional.of(properties));
                }
            } catch (final IOException e) {
                return FutureUtil.completedWithException(e);
            }
        }

        final ServerWorldProperties.LoadOptions.@Nullable CreateOperation createOperation = propertiesLoadOptions.createOperation().orElse(null);
        if (createOperation != null) {
            final ServerWorldProperties properties = (ServerWorldProperties) this.initializeLevelData(key, () -> this.createLevelData(
                registryKey, createOperation.worldArchetype())).data();
            createOperation.createCallback().ifPresent(c -> c.accept(properties));
            return CompletableFuture.completedFuture(Optional.of(properties));
        }

        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    public CompletableFuture<Boolean> saveProperties(final ServerWorldProperties properties) {
        final net.minecraft.resources.ResourceKey<Level> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(properties, "properties").key());

        if (this.worlds.get(registryKey) != null) {
            return CompletableFuture.completedFuture(false);
        }

        if (properties instanceof WorldData worldData) {
            try {
                this.saveLevelDat(worldData, properties.key());
            } catch (Exception ex) {
                return FutureUtil.completedWithException(ex);
            }
        }
        // TODO else: what about DerivedDataLevel?

        // Properties doesn't have everything we need...namely the generator, load the template and set values we actually got
        return FutureUtil.completedWithException(new IllegalArgumentException("TODO!"));
    }


    private void saveLevelDat(final WorldData worldData, final ResourceKey key) throws IOException {
        try (var storageSource = this.createLevelStorageAccess(key)) {
            storageSource.saveDataTag(this.server.registryAccess(), worldData, null);
        }
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
        final boolean disableLevelSaving;

        if (loadedWorld != null) {
            disableLevelSaving = loadedWorld.noSave;
            loadedWorld.save(null, true, loadedWorld.noSave);
            loadedWorld.noSave = true;
        } else {
            disableLevelSaving = false;
        }

        final boolean isDefaultWorld = DefaultWorldKeys.DEFAULT.equals(key);

        return CompletableFuture.runAsync(() -> {
            final Path originalDirectory = this.getDirectory(key);
            final Path copyDirectory = this.getDirectory(copyKey);

            try {
                Files.walkFileTree(originalDirectory, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                        // Silly recursion if the default world is being copied
                        if (dir.getFileName().toString().equals(Constants.Sponge.World.DIMENSIONS_DIRECTORY)) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }

                        // Silly copying of vanilla sub worlds if the default world is being copied
                        if (isDefaultWorld && SpongeWorldManager.this.isVanillaSubWorld(dir.getFileName().toString())) {
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
                    Files.walkFileTree(copyDirectory, DeleteFileVisitor.INSTANCE);
                } catch (final IOException ignore) {
                }

                throw new CompletionException(e);
            }

            if (loadedWorld != null) {
                loadedWorld.noSave = disableLevelSaving;
            }

            final Path configFile = this.getConfigFile(key);
            final Path copyConfigFile = this.getConfigFile(copyKey);

            try {
                Files.createDirectories(copyConfigFile.getParent());
                Files.copy(configFile, copyConfigFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (final IOException e) {
                throw new CompletionException(e);
            }

            try {
                final Optional<SpongeWorldManager.LevelDataLoadResult> levelData = this.loadLevelData(copyRegistryKey, null);
                if (levelData.isPresent()) {
                    this.saveLevelDat(levelData.get().data(), copyKey);
                }
            } catch (final IOException e) {
                throw new CompletionException(e);
            }
        }).thenApplyAsync($ -> {
            return true;
        }, SpongeCommon.server());
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
            return this.unloadWorld0(loadedWorld).thenCompose($ -> this.moveWorld0(key, movedKey));
        }

        return this.moveWorld0(key, movedKey);
    }

    private CompletableFuture<Boolean> moveWorld0(final ResourceKey key, final ResourceKey movedKey) {
        final net.minecraft.resources.ResourceKey<Level> movedKeyKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(movedKey, "movedKey"));

        return CompletableFuture.runAsync(() -> {
            final Path originalDirectory = this.getDirectory(key);
            final Path movedDirectory = this.getDirectory(movedKey);

            try {
                Files.createDirectories(movedDirectory);
                Files.move(originalDirectory, movedDirectory, StandardCopyOption.REPLACE_EXISTING);
            } catch (final IOException e) {
                throw new CompletionException(e);
            }

            final Path configFile = this.getConfigFile(key);
            final Path movedConfigFile = this.getConfigFile(movedKey);

            try {
                Files.createDirectories(movedConfigFile.getParent());
                Files.move(configFile, movedConfigFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (final IOException e) {
                throw new CompletionException(e);
            }

            try {
                final Optional<SpongeWorldManager.LevelDataLoadResult> levelData = this.loadLevelData(movedKeyKey, null);
                if (levelData.isPresent()) {
                    this.saveLevelDat(levelData.get().data(), movedKey);
                }
            } catch (final IOException e) {
                throw new CompletionException(e);
            }
        }).thenApplyAsync($ -> {
            return true;
        }, SpongeCommon.server());
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
            if (!loadedWorld.getPlayers(p -> true).isEmpty()) {
                return CompletableFuture.failedFuture(new IOException(String.format("World '%s' was told to unload but players remain.", registryKey.location())));
            }

            final boolean disableLevelSaving = loadedWorld.noSave;
            loadedWorld.noSave = true;
            ((IOWorkerBridge) loadedWorld.getChunkSource().chunkMap.chunkScanner()).bridge$haltStore(true);
            return this.unloadWorld0(loadedWorld)
                .thenCompose($ -> this.deleteWorld0(key))
                .whenComplete(($, e) -> {
                    if (e != null) {
                        loadedWorld.noSave = disableLevelSaving;
                        ((IOWorkerBridge) loadedWorld.getChunkSource().chunkMap.chunkScanner()).bridge$haltStore(false);
                    }
                });
        }

        return this.deleteWorld0(key);
    }

    private CompletableFuture<Boolean> deleteWorld0(final ResourceKey key) {
        final net.minecraft.resources.ResourceKey<Level> registryKey = SpongeWorldManager.createRegistryKey(key);

        return CompletableFuture.runAsync(() -> {
            final Path directory = this.getDirectory(key);
            if (Files.exists(directory)) {
                try {
                    Files.walkFileTree(directory, DeleteFileVisitor.INSTANCE);
                } catch (final IOException e) {
                    throw new CompletionException(e);
                }
            }

            final Path configFile = this.getConfigFile(key);
            try {
                Files.deleteIfExists(configFile);
            } catch (final IOException e) {
                throw new CompletionException(e);
            }
        }).thenApplyAsync($ -> {
            //After vanilla has detected a new dimension from a data pack it "promotes" it
            //to the overworld's level data where the level persist even when the data pack is removed.
            //This forcible removes it from there too.
            final Registry<LevelStem> levelStemRegistry = SpongeCommon.vanillaRegistry(Registries.LEVEL_STEM);
            final net.minecraft.resources.ResourceKey<net.minecraft.world.level.dimension.LevelStem> levelStemKey = Registries.levelToLevelStem(registryKey);
            if (levelStemRegistry.containsKey(levelStemKey)) {
                ((MappedRegistryBridge<LevelStem>) levelStemRegistry).bridge$forceRemoveValue(Registries.levelToLevelStem(registryKey));
            }

            final LevelStorageSource.LevelStorageAccess storageSource = ((MinecraftServerAccessor) this.server).accessor$storageSource();
            final PrimaryLevelData levelData = (PrimaryLevelData) this.server.getWorldData();
            storageSource.saveDataTag(SpongeCommon.server().registryAccess(), levelData, null);

            return true;
        }, SpongeCommon.server());
    }

    private CompletableFuture<Boolean> unloadWorld0(final ServerLevel level) {
        final net.minecraft.resources.ResourceKey<Level> registryKey = level.dimension();

        if (!level.getPlayers(p -> true).isEmpty()) {
            return CompletableFuture.failedFuture(new IOException(String.format("World '%s' was told to unload but players remain.", registryKey.location())));
        }

        // We first tell the world to save without flushing
        // and wait for the callback when I/O queue is empty.
        level.save(null, false, level.noSave);

        return ((IOWorkerBridge) level.getChunkSource().chunkMap.chunkScanner()).bridge$onIdle().thenComposeAsync($ -> {
            if (!level.getPlayers(p -> true).isEmpty()) {
                return CompletableFuture.failedFuture(new IOException(String.format("World '%s' was told to unload but players remain.", registryKey.location())));
            }

            SpongeCommon.logger().info("Unloading world '{}'", registryKey.location());

            final UnloadWorldEvent unloadWorldEvent = SpongeEventFactory.createUnloadWorldEvent(PhaseTracker.getInstance().currentCause(), (ServerWorld) level);
            SpongeCommon.post(unloadWorldEvent);

            PlatformHooks.INSTANCE.getWorldHooks().preUnloadWorld(level);

            final int lastSpawnChunkRadius = ((ServerLevelAccessor) level).accessor$lastSpawnChunkRadius();
            if (lastSpawnChunkRadius > 1) {
                level.getChunkSource().removeRegionTicket(TicketType.START, new ChunkPos(level.getSharedSpawnPos()), lastSpawnChunkRadius, Unit.INSTANCE);
                ((ServerLevelAccessor) level).accessor$setLastSpawnChunkRadius(1);
            }

            final var configAdapter = ((ServerLevelDataBridge) level.getLevelData()).bridge$spongeData().configAdapter();
            if (configAdapter != null) {
                configAdapter.save();
            }

            try {
                level.save(null, true, level.noSave);
                level.close();
                ((ServerLevelBridge) level).bridge$getLevelSave().close();
            } catch (final Exception ex) {
                return CompletableFuture.failedFuture(new IOException(ex));
            }

            this.worlds.remove(registryKey);

            return CompletableFuture.completedFuture(true);
        }, SpongeCommon.server());
    }

    public void createNonDefaultLevels() {
        final Registry<LevelStem> registry = SpongeCommon.vanillaRegistry(Registries.LEVEL_STEM);
        for (final LevelStem levelStem : registry) {
            final ResourceKey worldKey = (ResourceKey) (Object) registry.getKey(levelStem);
            if (DefaultWorldKeys.DEFAULT.equals(worldKey)) {
                continue;
            }

            final LevelStemBridge templateBridge = (LevelStemBridge) (Object) levelStem;
            if (templateBridge.bridge$hasLegacyData()) {
                ((MappedRegistryBridge<LevelStem>) registry).bridge$forceRemoveValue(Registries.levelToLevelStem(SpongeWorldManager.createRegistryKey(worldKey)));
                continue;
            }

            final net.minecraft.resources.ResourceKey<Level> registryKey = SpongeWorldManager.createRegistryKey(worldKey);

            try {
                final LevelStorageSource.LevelStorageAccess storageSource = this.createLevelStorageAccess(worldKey);
                try {
                    final LevelDataLoadResult levelData = this.loadLevelData(storageSource, registryKey, () ->
                        this.createLevelData(registryKey, WorldArchetype.of((WorldArchetypeType) (Object) levelStem)));
                    if (!((PrimaryLevelDataBridge) levelData.data()).bridge$loadOnStartup()) {
                        SpongeCommon.logger().warn("World '{}' has been disabled from loading at startup. Skipping...", worldKey);
                        continue;
                    }

                    final ServerLevel world = this.createLevel(registryKey, levelData.stem(), storageSource, levelData.data());
                    this.prepareLevel(world);
                } catch (final Exception e) {
                    storageSource.close();
                    throw e;
                }
            } catch (final IOException e) {
                throw new RuntimeException(String.format("Failed to create level data for world '%s'!", worldKey), e);
            } catch (final Exception e) {
                throw new IllegalStateException(String.format("Failed to create level data for world '%s'!", worldKey), e);
            }
        }
    }

    public void prepareLevels() {
        for (final Map.Entry<net.minecraft.resources.ResourceKey<Level>, ServerLevel> entry : this.worlds.entrySet()) {
            this.loadSpawnChunks(entry.getValue());
        }

        ((SpongeUserManager) Sponge.server().userManager()).init();
    }

    private LevelDataLoadResult initializeLevelData(final ResourceKey key, final Supplier<LevelDataLoadResult> loader) {
        return this.initializeLevelData(key, loader, Optional::of);
    }

    private Optional<LevelDataLoadResult> initializeLevelDataOptional(final ResourceKey key, final Supplier<Optional<LevelDataLoadResult>> loader) {
        return this.initializeLevelData(key, loader, Function.identity());
    }

    private <T> T initializeLevelData(final ResourceKey key, final Supplier<T> loader, final Function<T, Optional<LevelDataLoadResult>> mapper) {
        final T result = loader.get();
        mapper.apply(result).ifPresent(r -> {
            final DimensionType dimensionType = r.stem().type().value();
            ((PrimaryLevelDataBridge) r.data()).bridge$spongeData().setKey(key);
            ((PrimaryLevelDataBridge) r.data()).bridge$spongeData().setConfigAdapter(SpongeGameConfigs.load(dimensionType, key));
            ((PrimaryLevelDataBridge) r.data()).bridge$populateFromLevelStem(r.stem());
        });
        return result;
    }

    private Optional<LevelDataLoadResult> loadLevelData(final net.minecraft.resources.ResourceKey<Level> registryKey,
            final ServerWorldProperties.LoadOptions.@Nullable LoadOperation loadOperation) throws IOException {
        try (final LevelStorageSource.LevelStorageAccess storageSource = this.createLevelStorageAccess((ResourceKey) (Object) registryKey.location())) {
            return this.loadLevelData(storageSource, registryKey, loadOperation);
        }
    }

    private Optional<LevelDataLoadResult> loadLevelData(final LevelStorageSource.LevelStorageAccess storageSource,
            final net.minecraft.resources.ResourceKey<Level> registryKey, final ServerWorldProperties.LoadOptions.@Nullable LoadOperation loadOperation) {
        return this.initializeLevelDataOptional((ResourceKey) (Object) registryKey.location(), () ->
            Optional.ofNullable(this.loadLevelTag(storageSource))
                .map(t -> this.readLevelData(registryKey, t, loadOperation)));
    }

    private LevelDataLoadResult loadLevelData(final LevelStorageSource.LevelStorageAccess storageSource,
            final net.minecraft.resources.ResourceKey<Level> registryKey, final Supplier<LevelDataLoadResult> defaultSupplier) {
        return this.initializeLevelData((ResourceKey) (Object) registryKey.location(), () ->
            Optional.ofNullable(this.loadLevelTag(storageSource))
                .map(t -> this.readLevelData(registryKey, t, null))
                .orElseGet(defaultSupplier));
    }

    private @Nullable Dynamic<?> loadLevelTag(final LevelStorageSource.LevelStorageAccess storageSource) {
        try {
            return storageSource.getDataTag();
        } catch (final IOException e) {
            try {
                final Dynamic<?> dataTag = storageSource.getDataTagFallback();
                storageSource.restoreLevelDataFromOld();
                return dataTag;
            } catch (final IOException ex) {
                return null;
            }
        }
    }

    private LevelDataLoadResult readLevelData(final net.minecraft.resources.ResourceKey<Level> registryKey, final Dynamic<?> dataTag,
            final ServerWorldProperties.LoadOptions.@Nullable LoadOperation loadOperation) {
        final PrimaryLevelData defaultLevelData = (PrimaryLevelData) this.server.getWorldData();
        final net.minecraft.core.RegistryAccess.Frozen access = this.server.registryAccess();
        final LevelDataAndDimensions levelData = LevelStorageSource.getLevelDataAndDimensions(
            dataTag, defaultLevelData.getDataConfiguration(), access.lookupOrThrow(Registries.LEVEL_STEM), access);
        final WorldArchetype worldArchetype = SpongeWorldManager.resolveWorldArchetype(levelData, registryKey, loadOperation);
        final LevelStem levelStem = (LevelStem) (Object) worldArchetype.type();
        final PrimaryLevelData worldData = (PrimaryLevelData) levelData.worldData();
        worldArchetype.generationConfig().ifPresent(o ->
            ((PrimaryLevelDataAccessor) worldData).accessor$worldOptions((WorldOptions) o));
        ((PrimaryLevelDataAccessor) worldData).accessor$specialWorldProperty(SpongeWorldManager.specialWorldProperty(levelStem));
        return new LevelDataLoadResult(worldData, levelStem);
    }

    private static WorldArchetype resolveWorldArchetype(final LevelDataAndDimensions levelData, final net.minecraft.resources.ResourceKey<Level> registryKey,
            final ServerWorldProperties.LoadOptions.@Nullable LoadOperation loadOperation) {
        if (loadOperation != null && loadOperation.overrideWorldArchetype().isPresent()) {
            return loadOperation.overrideWorldArchetype().get();
        }

        // Worlds that were loaded with Sponge know the associated world archetype.
        final Optional<LevelStem> spongeWorldKeyStem = Optional.ofNullable(((PrimaryLevelDataBridge) levelData.worldData()).bridge$spongeData().key())
            .flatMap(worldKey -> levelData.dimensions().dimensions().getOptional(
                Registries.levelToLevelStem(SpongeWorldManager.createRegistryKey(worldKey))));
        if (spongeWorldKeyStem.isPresent()) {
            return WorldArchetype.of((WorldArchetypeType) (Object) spongeWorldKeyStem.get());
        }

        // Fallback to best guess.
        // TODO: Expose API to iterate the candidates or allow to specify the lookup key?
        final Optional<LevelStem> worldKeyStem = levelData.dimensions().dimensions().getOptional(Registries.levelToLevelStem(registryKey));
        if (worldKeyStem.isPresent()) {
            return WorldArchetype.of((WorldArchetypeType) (Object) worldKeyStem.get());
        }

        if (loadOperation != null && loadOperation.fallbackWorldArchetype().isPresent()) {
            return loadOperation.fallbackWorldArchetype().get();
        }

        throw new IllegalStateException("The world has no valid WorldArchetype!");
    }

    private LevelDataLoadResult createLevelData(final net.minecraft.resources.ResourceKey<Level> registryKey, final WorldArchetype archetype) {
        final LevelStem levelStem = (LevelStem) (Object) archetype.type();
        final PrimaryLevelData defaultLevelData = (PrimaryLevelData) this.server.getWorldData();
        final LevelSettings levelSettings = this.createLevelSettings(defaultLevelData, this.getDirectoryName((ResourceKey) (Object) registryKey.location()));
        return new LevelDataLoadResult(
            new PrimaryLevelData(levelSettings, (WorldOptions) archetype.generationConfig().orElse((WorldGenerationConfig) defaultLevelData.worldGenOptions()),
                SpongeWorldManager.specialWorldProperty(levelStem), Lifecycle.stable()), levelStem);
    }

    private LevelSettings createLevelSettings(final PrimaryLevelData defaultLevelData, final String directoryName) {
        return new LevelSettings(
            directoryName,
            defaultLevelData.getGameType(),
            defaultLevelData.isHardcore(),
            defaultLevelData.getDifficulty(),
            defaultLevelData.isAllowCommands(),
            defaultLevelData.getGameRules().copy(defaultLevelData.enabledFeatures()),
            defaultLevelData.getDataConfiguration());
    }

    private ServerLevel createLevel(
        final net.minecraft.resources.ResourceKey<Level> registryKey,
        final LevelStem levelStem,
        final LevelStorageSource.LevelStorageAccess storageSource,
        final PrimaryLevelData levelData) {

        final ResourceKey worldKey = (ResourceKey) (Object) registryKey.location();

        MinecraftServerAccessor.accessor$LOGGER().info("Loading world '{}'", worldKey);

        final List<CustomSpawner> spawners;
        if (levelStem.type().is(BuiltinDimensionTypes.OVERWORLD) || levelStem.type().is(BuiltinDimensionTypes.OVERWORLD_CAVES)) {
            spawners = ImmutableList.of(new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(levelData));
        } else {
            spawners = ImmutableList.of();
        }

        levelData.setModdedInfo(this.server.getServerModName(), this.server.getModdedStatus().shouldReportAsModified());

        final long seed = BiomeManager.obfuscateSeed(levelData.worldGenOptions().seed());
        final Executor executor = ((MinecraftServerAccessor) this.server).accessor$executor();
        final ChunkProgressListener progressListener = ((MinecraftServerAccessor) this.server).accessor$progressListenerFactory().create(SpongeWorldManager.getSpawnRadius(levelData));

        final ServerLevel world = new ServerLevel(this.server, executor, storageSource, levelData,
            registryKey, levelStem, progressListener, levelData.isDebugWorld(), seed, spawners, true, null);
        this.worlds.put(registryKey, world);

        // Ensure that the world border is registered.
        world.getWorldBorder().applySettings(levelData.getWorldBorder());
        PlatformHooks.INSTANCE.getWorldHooks().postLoadWorld(world);
        return world;
    }

    private ServerLevel prepareLevel(final ServerLevel level) {
        if (Level.OVERWORLD.equals(level.dimension())) {
            throw new IllegalArgumentException();
        }

        final ServerLevelData levelData = (ServerLevelData) level.getLevelData();
        final ServerLevelDataBridge levelDataBridge = (ServerLevelDataBridge) levelData;

        final boolean initialized = levelData.isInitialized();
        final LoadWorldEvent loadWorldEvent = SpongeEventFactory.createLoadWorldEvent(PhaseTracker.getInstance().currentCause(), (ServerWorld) level, initialized);
        SpongeCommon.post(loadWorldEvent);

        levelDataBridge.bridge$triggerViewDistanceLogic();

        if (!initialized) {
            if (levelData instanceof WorldData worldData) {
                try {
                    final boolean isDebugGeneration = worldData.isDebugWorld();
                    final boolean hasSpawnAlready = levelDataBridge.bridge$customSpawnPosition();
                    if (!hasSpawnAlready) {
                        if (levelDataBridge.bridge$performsSpawnLogic()) {
                            MinecraftServerAccessor.invoker$setInitialSpawn(level, levelData, worldData.worldGenOptions().generateBonusChest(), isDebugGeneration);
                        } else if (Level.END.equals(level.dimension())) {
                            levelData.setSpawn(ServerLevel.END_SPAWN_POINT, 0);
                        }
                    } else if (worldData.worldGenOptions().generateBonusChest()) {
                        final BlockPos pos = levelData.getSpawnPos();
                        final ConfiguredFeature<?, ?> bonusChestFeature = SpongeCommon.vanillaRegistry(Registries.CONFIGURED_FEATURE).getValue(MiscOverworldFeatures.BONUS_CHEST);
                        bonusChestFeature.place(level, level.getChunkSource().getGenerator(), level.random, pos);
                    }
                    levelData.setInitialized(true);
                    if (isDebugGeneration) {
                        ((MinecraftServerAccessor) this.server).invoker$setupDebugLevel(worldData);
                    }
                } catch (final Throwable throwable) {
                    final CrashReport crashReport = CrashReport.forThrowable(throwable, "Exception initializing world '" + level.dimension().location()  + "'");
                    try {
                        level.fillReportDetails(crashReport);
                    } catch (final Throwable ignore) {
                    }

                    throw new ReportedException(crashReport);
                }
            }

            levelData.setInitialized(true);
        }

        // Initialize PlayerData in PlayerList, add WorldBorder listener. We change the method in PlayerList to handle per-world border
        this.server.getPlayerList().addWorldborderListener(level);

        if (levelData instanceof WorldData worldData && worldData.getCustomBossEvents() != null) {
            ((ServerLevelBridge) level).bridge$getBossBarManager().load(worldData.getCustomBossEvents(), level.registryAccess());
        }

        return level;
    }

    /**
     * Same as loadSpawnChunks but async and without listener.
     */
    private CompletableFuture<ServerLevel> loadSpawnChunksAsync(final ServerLevel level) {
        MinecraftServerAccessor.accessor$LOGGER().info("Preparing start region for dimension {}", level.dimension().location());

        final ServerChunkCache chunkSource = level.getChunkSource();
        level.setDefaultSpawnPos(level.getSharedSpawnPos(), level.getSharedSpawnAngle());

        final int spawnRadius = SpongeWorldManager.getSpawnRadius((ServerLevelData) level.getLevelData());
        final int spawnSize = spawnRadius > 0 ? Mth.square(ChunkProgressListener.calculateDiameter(spawnRadius)) : 0;

        final CompletableFuture<ServerLevel> generationFuture = new CompletableFuture<>();
        Sponge.asyncScheduler().submit(
            Task.builder().plugin(Launch.instance().platformPlugin()).execute(task -> {
                if (chunkSource.getTickingGenerated() >= spawnSize) {
                    Sponge.server().scheduler().submit(Task.builder().plugin(Launch.instance().platformPlugin()).execute(() -> generationFuture.complete(level)).build());
                    // Notify the future that we are done
                    task.cancel(); // And cancel this task
                    MinecraftServerAccessor.accessor$LOGGER().info("Done preparing start region for dimension {}", level.dimension().location());
                }
            }).interval(10, TimeUnit.MILLISECONDS).build()
        );

        return generationFuture.thenApply(v -> {
            SpongeWorldManager.updateForcedChunks(v, v.getChunkSource());
            return v;
        });
    }

    /**
     * Mimic MinecraftServer#prepareLevels
     */
    private void loadSpawnChunks(final ServerLevel level) {
        MinecraftServerAccessor.accessor$LOGGER().info("Preparing start region for dimension {}", level.dimension().location());

        final BlockPos spawnPoint = level.getSharedSpawnPos();
        final ChunkPos chunkPos = new ChunkPos(spawnPoint);
        final ChunkProgressListener progressListener = ((ServerLevelBridge) level).bridge$getChunkProgressListener();
        progressListener.updateSpawnPos(chunkPos);
        final ServerChunkCache chunkSource = level.getChunkSource();
        ((MinecraftServerAccessor) this.server).accessor$nextTickTimeNanos(Util.getNanos());
        level.setDefaultSpawnPos(spawnPoint, level.getSharedSpawnAngle());

        final int spawnRadius = SpongeWorldManager.getSpawnRadius((ServerLevelData) level.getLevelData());
        final int spawnSize = spawnRadius > 0 ? Mth.square(ChunkProgressListener.calculateDiameter(spawnRadius)) : 0;

        while (chunkSource.getTickingGenerated() < spawnSize) {
            ((MinecraftServerAccessor) this.server).accessor$nextTickTimeNanos(Util.getNanos() + 10L * TimeUtil.NANOSECONDS_PER_MILLISECOND);
            ((MinecraftServerAccessor) this.server).accessor$waitUntilNextTick();
        }

        ((MinecraftServerAccessor) this.server).accessor$nextTickTimeNanos(Util.getNanos() + 10L * TimeUtil.NANOSECONDS_PER_MILLISECOND);
        ((MinecraftServerAccessor) this.server).accessor$waitUntilNextTick();

        SpongeWorldManager.updateForcedChunks(level, chunkSource);

        ((MinecraftServerAccessor) this.server).accessor$nextTickTimeNanos(Util.getNanos() + 10L * TimeUtil.NANOSECONDS_PER_MILLISECOND);
        ((MinecraftServerAccessor) this.server).accessor$waitUntilNextTick();
        progressListener.stop();
    }

    private static int getSpawnRadius(final ServerLevelData levelData) {
        return ((ServerLevelDataBridge) levelData).bridge$performsSpawnLogic() ? levelData.getGameRules().getInt(GameRules.RULE_SPAWN_CHUNK_RADIUS) : 0;
    }

    private static void updateForcedChunks(final ServerLevel level, final ServerChunkCache serverChunkProvider) {
        final ForcedChunksSavedData forcedChunksSaveData = level.getDataStorage().get(ForcedChunksSavedData.factory(), "chunks");
        if (forcedChunksSaveData != null) {
            final LongIterator longIterator = forcedChunksSaveData.getChunks().iterator();

            while (longIterator.hasNext()) {
                final long i = longIterator.nextLong();
                final ChunkPos forceChunkPos = new ChunkPos(i);
                serverChunkProvider.updateChunkForced(forceChunkPos, true);
            }
        }
    }

    public static net.minecraft.resources.ResourceKey<Level> createRegistryKey(final ResourceKey key) {
        return net.minecraft.resources.ResourceKey.create(Registries.DIMENSION, (ResourceLocation) (Object) key);
    }

    private String getDirectoryName(final ResourceKey key) {
        if (DefaultWorldKeys.DEFAULT.equals(key)) {
            return "";
        }
        if (DefaultWorldKeys.THE_NETHER.equals(key)) {
            return "DIM-1";
        }
        if (DefaultWorldKeys.THE_END.equals(key)) {
            return "DIM1";
        }
        return key.value();
    }

    private Path getDirectory(final ResourceKey key) {
        if (DefaultWorldKeys.DEFAULT.equals(key)) {
            return this.defaultWorldDirectory;
        }
        if (DefaultWorldKeys.THE_NETHER.equals(key)) {
            return this.defaultWorldDirectory.resolve("DIM-1");
        }
        if (DefaultWorldKeys.THE_END.equals(key)) {
            return this.defaultWorldDirectory.resolve("DIM1");
        }
        return this.customWorldsDirectory.resolve(key.namespace()).resolve(key.value());
    }

    private boolean isVanillaWorld(final ResourceKey key) {
        return DefaultWorldKeys.DEFAULT.equals(key) || DefaultWorldKeys.THE_NETHER.equals(key) || DefaultWorldKeys.THE_END.equals(key);
    }

    private boolean isVanillaSubWorld(final String directoryName) {
        return "DIM-1".equals(directoryName) || "DIM1".equals(directoryName);
    }

    private Path getConfigFile(final ResourceKey key) {
        return SpongeConfigs.getDirectory().resolve("worlds").resolve(key.namespace()).resolve(key.value() + ".conf");
    }

    private static PrimaryLevelData.SpecialWorldProperty specialWorldProperty(final LevelStem stem) {
        // Copied from WorldDimensions#specialWorldProperty
        final ChunkGenerator generator = stem.generator();
        if (generator instanceof DebugLevelSource) {
            return PrimaryLevelData.SpecialWorldProperty.DEBUG;
        } else {
            return generator instanceof FlatLevelSource ? PrimaryLevelData.SpecialWorldProperty.FLAT : PrimaryLevelData.SpecialWorldProperty.NONE;
        }
    }

    private record LevelDataLoadResult(PrimaryLevelData data, LevelStem stem)
    {
    }
}
