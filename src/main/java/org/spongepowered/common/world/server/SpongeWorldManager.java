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
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.MiscOverworldFeatures;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkLoadCounter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.server.level.progress.LoggingLevelLoadListener;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.filefix.FileFixException;
import net.minecraft.util.worldupdate.UpgradeProgress;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTraderSpawner;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.TicketStorage;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.storage.*;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
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
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.WorldArchetype;
import org.spongepowered.api.world.server.WorldArchetypeType;
import org.spongepowered.api.world.server.WorldManager;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.server.MinecraftServerAccessor;
import org.spongepowered.common.accessor.world.level.storage.PrimaryLevelDataAccessor;
import org.spongepowered.common.bridge.core.MappedRegistryBridge;
import org.spongepowered.common.bridge.world.level.chunk.storage.IOWorkerBridge;
import org.spongepowered.common.bridge.world.level.dimension.LevelStemBridge;
import org.spongepowered.common.bridge.world.level.storage.PrimaryLevelDataBridge;
import org.spongepowered.common.bridge.world.level.storage.ServerLevelDataBridge;
import org.spongepowered.common.config.SpongeGameConfigs;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.hooks.PlatformHooks;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.launch.config.core.SpongeConfigs;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.ExecutorUtil;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SpongeWorldManager implements WorldManager {

    private final MinecraftServer server;
    private final Path defaultWorldDirectory, customWorldsDirectory;
    private final Map<net.minecraft.resources.ResourceKey<Level>, ServerLevel> worlds;
    private final Map<net.minecraft.resources.ResourceKey<Level>, WorldOperationTask> worldOperations = new HashMap<>();
    private final LevelLoadListener levelLoadListener;

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
        this.levelLoadListener = LoggingLevelLoadListener.forDedicatedServer();
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

        // All dimensions are stored under dimensions/{namespace}/{value}/
        try {
            if (Files.exists(this.customWorldsDirectory)) {
                for (final Path namespacedDirectory : Files.list(this.customWorldsDirectory).toList()) {
                    if (!Files.isDirectory(namespacedDirectory) || this.customWorldsDirectory.equals(namespacedDirectory)) {
                        continue;
                    }

                    for (final Path valueDirectory : Files.list(namespacedDirectory).toList()) {
                        if (!Files.isDirectory(valueDirectory) || namespacedDirectory.equals(valueDirectory)) {
                            continue;
                        }

                        worldKeys.add(ResourceKey.of(namespacedDirectory.getFileName().toString(), valueDirectory.getFileName().toString()));
                    }
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

        final Path dimensionDir = this.getDirectory(key);
        return Files.exists(dimensionDir);
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

        return this.performWorldOperation(registryKey, WorldOperationType.LOAD, t -> {
            if (t.isCancelled()) {
                return CompletableFuture.failedFuture(new CancellationException());
            }

            final ServerLevel world = this.worlds.get(registryKey);
            if (world != null) {
                final ServerWorldProperties.LoadOptions.@Nullable GetOperation getOperation = propertiesLoadOption.getOperation().orElse(null);
                if (getOperation != null) {
                    getOperation.getCallback().ifPresent(c -> c.accept((ServerWorldProperties) world.getLevelData()));
                    return CompletableFuture.completedFuture(Optional.of((ServerWorld) world));
                } else if (propertiesLoadOption.loadOperation().isPresent() || propertiesLoadOption.createOperation().isPresent()) {
                    return CompletableFuture.failedFuture(new IllegalArgumentException("World is already loaded!"));
                }
            }

            if (Level.OVERWORLD.equals(registryKey) && (propertiesLoadOption.loadOperation().isPresent() || propertiesLoadOption.createOperation().isPresent())) {
                return CompletableFuture.failedFuture(new IllegalArgumentException("The default world cannot be told to load!"));
            }

            return SpongeCommon.asyncScheduler().<CompletableFuture<Optional<ServerWorld>>>submit(() -> {
                if (t.isCancelled()) {
                    return CompletableFuture.failedFuture(new CancellationException());
                }

                final LevelStorageSource.LevelStorageAccess storageSource = this.serverStorageSource();

                final ServerWorldProperties.LoadOptions.@Nullable LoadOperation loadOperation = propertiesLoadOption.loadOperation().orElse(null);
                if (loadOperation != null && this.worldExists(key)) {
                    final Optional<LevelDataLoadResult> result = this.loadLevelData(storageSource, registryKey, loadOperation);
                    if (result.isPresent()) {
                        loadOperation.loadCallback().ifPresent(c -> c.accept((ServerWorldProperties) result.get().data()));
                        return this.loadWorld0(t, registryKey, result.get(), storageSource);
                    }
                }

                final ServerWorldProperties.LoadOptions.@Nullable CreateOperation createOperation = propertiesLoadOption.createOperation().orElse(null);
                if (createOperation != null) {
                    final LevelDataLoadResult result = this.createLevelData(registryKey, createOperation.worldArchetype());
                    createOperation.createCallback().ifPresent(c -> c.accept((ServerWorldProperties) result.data()));
                    return this.loadWorld0(t, registryKey, result, storageSource);
                }

                return CompletableFuture.completedFuture(Optional.empty());
            }).thenCompose(Function.identity());
        });
    }

    private CompletableFuture<Optional<ServerWorld>> loadWorld0(final WorldOperationTask task, final net.minecraft.resources.ResourceKey<Level> registryKey,
                                                                final LevelDataLoadResult levelData, final LevelStorageSource.LevelStorageAccess storageSource) {
        return CompletableFuture.<CompletableFuture<ServerLevel>>supplyAsync(() -> {
                if (task.isCancelled()) {
                    return CompletableFuture.failedFuture(new CancellationException());
                }

                final ServerLevel world = this.createLevel(registryKey, levelData.stem(), storageSource, levelData.data());

                this.prepareLevel(world);

                ((MinecraftServerAccessor) this.server).invoker$forceDifficulty();

                return this.loadSpawnChunksAsync(task, world);
            }, SpongeCommon.server())
            .thenCompose(Function.identity())
            .thenApply(w -> Optional.of((ServerWorld) w));
    }

    private LevelStorageSource.LevelStorageAccess serverStorageSource() {
        return ((MinecraftServerAccessor) this.server).accessor$storageSource();
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

        final @Nullable WorldOperationTask pendingTask = this.worldOperations.get(registryKey);
        if (pendingTask != null && pendingTask.type() == WorldOperationType.UNLOAD) {
            return (CompletableFuture<Boolean>) pendingTask.future();
        }

        return this.performWorldOperation(registryKey, WorldOperationType.UNLOAD, t -> {
            if (world != this.worlds.get(registryKey)) {
                return CompletableFuture.completedFuture(false);
            }

            return this.unloadWorld0(t, (ServerLevel) world);
        });
    }

    @Override
    public CompletableFuture<Optional<ServerWorldProperties>> loadProperties(final ResourceKey key, ServerWorldProperties.LoadOptions propertiesLoadOptions) {
        final net.minecraft.resources.ResourceKey<Level> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(key, "key"));

        return this.performWorldOperation(registryKey, WorldOperationType.LOAD, t -> {
            if (t.isCancelled()) {
                return CompletableFuture.failedFuture(new CancellationException());
            }

            final @Nullable ServerLevel level = this.worlds.get(registryKey);
            if (level != null) {
                final ServerWorldProperties.LoadOptions.@Nullable GetOperation getOperation = propertiesLoadOptions.getOperation().orElse(null);
                if (getOperation != null) {
                    final ServerWorldProperties properties = (ServerWorldProperties) level.getLevelData();
                    getOperation.getCallback().ifPresent(c -> c.accept(properties));
                    return CompletableFuture.completedFuture(Optional.of(properties));
                } else if (propertiesLoadOptions.loadOperation().isPresent() || propertiesLoadOptions.createOperation().isPresent()) {
                    return CompletableFuture.failedFuture(new IllegalArgumentException("World is already loaded!"));
                }
            }

            return SpongeCommon.asyncScheduler().submit(() -> {
                if (t.isCancelled()) {
                    throw new CancellationException();
                }

                final ServerWorldProperties.LoadOptions.@Nullable LoadOperation loadOperation = propertiesLoadOptions.loadOperation().orElse(null);
                if (loadOperation != null && this.worldExists(key)) {
                    final Optional<LevelDataLoadResult> result = this.loadLevelData(registryKey, loadOperation);
                    if (result.isPresent()) {
                        final ServerWorldProperties properties = (ServerWorldProperties) result.get().data();
                        loadOperation.loadCallback().ifPresent(c -> c.accept(properties));
                        return Optional.of(properties);
                    }
                }

                final ServerWorldProperties.LoadOptions.@Nullable CreateOperation createOperation = propertiesLoadOptions.createOperation().orElse(null);
                if (createOperation != null) {
                    final LevelDataLoadResult result = this.createLevelData(registryKey, createOperation.worldArchetype());
                    createOperation.createCallback().ifPresent(c -> c.accept((ServerWorldProperties) result.data()));
                    return Optional.of((ServerWorldProperties) result.data());
                }

                return Optional.empty();
            });
        });
    }

    @Override
    public CompletableFuture<Boolean> saveProperties(final ServerWorldProperties properties) {
        final net.minecraft.resources.ResourceKey<Level> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(properties, "properties").key());

        return this.performWorldOperation(registryKey, WorldOperationType.SAVE, t -> {
            final ServerLevel level = this.worlds.get(registryKey);
            if (level != null) {
                // World is loaded — save it through the level's normal save mechanism
                return SpongeCommon.asyncScheduler().submit(() -> {
                    level.save(null, true, level.noSave);
                    return true;
                });
            }

            // World is not loaded — save the properties to the level.dat on disk
            return SpongeCommon.asyncScheduler().submit(() -> {
                if (properties instanceof WorldData worldData) {
                    this.saveLevelDat(worldData, properties.key());
                    return true;
                }
                return false;
            });
        });
    }


    private void saveLevelDat(final WorldData worldData, final ResourceKey key) {
        this.serverStorageSource().saveDataTag(worldData);
    }

    @Override
    public CompletableFuture<Boolean> copyWorld(final ResourceKey key, final ResourceKey copyKey) {
        final net.minecraft.resources.ResourceKey<Level> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(key, "key"));
        final net.minecraft.resources.ResourceKey<Level> copyRegistryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(copyKey, "copyKey"));

        if (Level.OVERWORLD.equals(copyRegistryKey)) {
            return CompletableFuture.completedFuture(false);
        }

        return this.performWorldOperation(registryKey, WorldOperationType.COPY, t ->
            this.performWorldOperation(copyRegistryKey, WorldOperationType.COPY, ct -> {
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

                return CompletableFuture.runAsync(() -> {
                    final Path originalDirectory = this.getDirectory(key);
                    final Path copyDirectory = this.getDirectory(copyKey);

                    try {
                        Files.walkFileTree(originalDirectory, new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
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

                    final Path configFile = this.getConfigFile(key);
                    final Path copyConfigFile = this.getConfigFile(copyKey);

                    try {
                        Files.createDirectories(copyConfigFile.getParent());
                        Files.copy(configFile, copyConfigFile, StandardCopyOption.REPLACE_EXISTING);
                    } catch (final IOException e) {
                        throw new CompletionException(e);
                    }

                    final Optional<LevelDataLoadResult> levelData = this.loadLevelData(copyRegistryKey, null);
                    if (levelData.isPresent()) {
                        this.saveLevelDat(levelData.get().data(), copyKey);
                    }
                }).thenApplyAsync($ -> {
                    if (loadedWorld != null) {
                        loadedWorld.noSave = disableLevelSaving;
                    }

                    return true;
                }, SpongeCommon.server());
            })
        );
    }

    @Override
    public CompletableFuture<Boolean> moveWorld(final ResourceKey key, final ResourceKey movedKey) {
        final net.minecraft.resources.ResourceKey<Level> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(key, "key"));
        final net.minecraft.resources.ResourceKey<Level> movedRegistryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(movedKey, "movedKey"));

        if (Level.OVERWORLD.equals(registryKey)) {
            return CompletableFuture.completedFuture(false);
        }

        return this.performWorldOperation(registryKey, WorldOperationType.MOVE, t ->
            this.performWorldOperation(movedRegistryKey, WorldOperationType.MOVE, mt -> {
                if (!this.worldExists(key)) {
                    return CompletableFuture.completedFuture(false);
                }

                if (this.worldExists(movedKey)) {
                    return CompletableFuture.completedFuture(false);
                }

                final ServerLevel loadedWorld = this.worlds.get(registryKey);
                if (loadedWorld != null) {
                    return this.unloadWorld0(t, loadedWorld).thenCompose($ -> this.moveWorld0(key, movedKey));
                }

                return this.moveWorld0(key, movedKey);
            })
        );
    }

    private CompletableFuture<Boolean> moveWorld0(final ResourceKey key, final ResourceKey movedKey) {
        final net.minecraft.resources.ResourceKey<Level> movedRegistryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(movedKey, "movedKey"));

        return CompletableFuture.supplyAsync(() -> {
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

            final Optional<LevelDataLoadResult> levelData = this.loadLevelData(movedRegistryKey, null);
            if (levelData.isPresent()) {
                this.saveLevelDat(levelData.get().data(), movedKey);
            }

            return true;
        });
    }

    @Override
    public CompletableFuture<Boolean> deleteWorld(final ResourceKey key) {
        final net.minecraft.resources.ResourceKey<Level> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(key, "key"));

        if (Level.OVERWORLD.equals(registryKey)) {
            return CompletableFuture.completedFuture(false);
        }

        return this.performWorldOperation(registryKey, WorldOperationType.DELETE, t -> {
            if (!this.worldExists(key)) {
                return CompletableFuture.completedFuture(false);
            }

            final ServerLevel loadedWorld = this.worlds.get(registryKey);
            if (loadedWorld != null) {
                if (!loadedWorld.getPlayers(p -> true).isEmpty()) {
                    return CompletableFuture.failedFuture(new IOException(String.format("World '%s' was told to unload but players remain.", registryKey.identifier())));
                }

                final boolean disableLevelSaving = loadedWorld.noSave;
                loadedWorld.noSave = true;
                ((IOWorkerBridge) loadedWorld.getChunkSource().chunkMap.chunkScanner()).bridge$haltStore(true);
                return this.unloadWorld0(t, loadedWorld)
                    .thenCompose($ -> this.deleteWorld0(key))
                    .whenCompleteAsync(($, e) -> {
                        if (e != null) {
                            loadedWorld.noSave = disableLevelSaving;
                            ((IOWorkerBridge) loadedWorld.getChunkSource().chunkMap.chunkScanner()).bridge$haltStore(false);
                        }
                    }, SpongeCommon.server());
            }

            return this.deleteWorld0(key);
        });
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
            final Registry<LevelStem> levelStemRegistry = SpongeCommon.server().registryAccess().lookupOrThrow(Registries.LEVEL_STEM);
            final net.minecraft.resources.ResourceKey<LevelStem> levelStemKey = Registries.levelToLevelStem(registryKey);
            if (levelStemRegistry.containsKey(levelStemKey)) {
                ((MappedRegistryBridge<LevelStem>) levelStemRegistry).bridge$forceRemoveValue(Registries.levelToLevelStem(registryKey));
            }

            final LevelStorageSource.LevelStorageAccess storageSource = ((MinecraftServerAccessor) this.server).accessor$storageSource();
            final PrimaryLevelData levelData = (PrimaryLevelData) this.server.getWorldData();
            storageSource.saveDataTag(levelData);

            return true;
        }, SpongeCommon.server());
    }

    private CompletableFuture<Boolean> unloadWorld0(final WorldOperationTask task, final ServerLevel level) {
        if (task.isCancelled()) {
            return CompletableFuture.failedFuture(new CancellationException());
        }

        final net.minecraft.resources.ResourceKey<Level> registryKey = level.dimension();

        if (!level.getPlayers(p -> true).isEmpty()) {
            return CompletableFuture.failedFuture(new IOException(String.format("World '%s' was told to unload but players remain.", registryKey.identifier())));
        }

        // We first tell the world to save without flushing
        // and wait for the callback when I/O queue is empty.
        level.save(null, false, level.noSave);

        return ((IOWorkerBridge) level.getChunkSource().chunkMap.chunkScanner()).bridge$onIdle().thenComposeAsync($ -> {
            if (task.isCancelled()) {
                return CompletableFuture.failedFuture(new CancellationException());
            }

            if (!level.getPlayers(p -> true).isEmpty()) {
                return CompletableFuture.failedFuture(new IOException(String.format("World '%s' was told to unload but players remain.", registryKey.identifier())));
            }

            SpongeCommon.logger().info("Unloading world '{}'", registryKey.identifier());

            final UnloadWorldEvent unloadWorldEvent = SpongeEventFactory.createUnloadWorldEvent(PhaseTracker.getInstance().currentCause(), (ServerWorld) level);
            SpongeCommon.post(unloadWorldEvent);

            PlatformHooks.INSTANCE.getWorldHooks().preUnloadWorld(level);

            final var configAdapter = ((ServerLevelDataBridge) level.getLevelData()).bridge$spongeData().configAdapter();
            if (configAdapter != null) {
                configAdapter.save();
            }

            while (level.getChunkSource().chunkMap.hasWork()) {
                level.getChunkSource().deactivateTicketsOnClosing();

                final boolean noSave = level.noSave;
                level.noSave = false;
                try {
                    level.getChunkSource().tick(() -> true, false);
                } finally {
                    level.noSave = noSave;
                }

                while (level.getChunkSource().pollTask()) {
                    // Consumes main thread task on poll
                }
            }

            try {
                level.save(null, true, level.noSave);
                level.close();
                // No longer closing per-world storageAccess -- all worlds share the server's single LevelStorageAccess
            } catch (final Exception ex) {
                return CompletableFuture.failedFuture(new IOException(ex));
            }

            this.worlds.remove(registryKey);

            return CompletableFuture.completedFuture(true);
        }, SpongeCommon.server());
    }

    public void createNonDefaultLevels() {
        // Migration from old SpongeData format is handled by FileFixerUpperMixin
        // which runs BEFORE any ServerLevel is created, preserving UUIDs in
        // per-dimension data/sponge/registry.dat files.
        // LEVEL_STEM is a dimension registry, loaded separately from worldgen registries.
        // It's in the server's composite registryAccess() but NOT in Sponge's scoped holder
        // (which explicitly excludes DIMENSION_REGISTRIES during RegistryDataLoader processing).
        final Registry<LevelStem> registry = PlatformHooks.INSTANCE.getWorldHooks().earlyRegistryAccess(Registries.LEVEL_STEM);
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
            final LevelStorageSource.LevelStorageAccess storageSource = this.serverStorageSource();

            try {
                final LevelDataLoadResult levelData = this.loadLevelData(storageSource, registryKey, null)
                    .orElseGet(() -> this.createLevelData(registryKey, WorldArchetype.of((WorldArchetypeType) (Object) levelStem)));
                if (!((PrimaryLevelDataBridge) levelData.data()).bridge$loadOnStartup()) {
                    SpongeCommon.logger().warn("World '{}' has been disabled from loading at startup. Skipping...", worldKey);
                    continue;
                }

                final var saveDataPath = storageSource.getDimensionPath(registryKey).resolve("data");
                final var saveDataStorage = new SavedDataStorage(saveDataPath, this.server.getFixerUpper(), this.server.registryAccess());
                @Nullable WorldGenSettings worldGen = saveDataStorage.get(WorldGenSettings.TYPE);

                if (worldGen != null) {
                    ((PrimaryLevelDataBridge) levelData.data()).bridge$spongeData().setWorldOptions(worldGen.options());
                }

                final ServerLevel world = this.createLevel(registryKey, levelData.stem(), storageSource, levelData.data());
                this.prepareLevel(world);
            } catch (final Exception e) {
                throw new IllegalStateException(String.format("Failed to create level data for world '%s'!", worldKey), e);
            }
        }
    }

    /**
     * Migrates old Sponge world data from previous formats.
     *
     * <p>In pre-26.1 Sponge, per-world identity (UUID, key) was stored in a {@code SpongeData}
     * compound inside each dimension's {@code level.dat}. In the new format, this data lives
     * in {@code data/sponge/registry.dat} per-dimension via {@link SpongeRegistryData}.</p>
     *
     * <p>This method handles:</p>
     * <ul>
     *   <li>Reading {@code SpongeData} from the root {@code level.dat} (overworld identity)</li>
     *   <li>Writing it to {@code dimensions/minecraft/overworld/data/sponge/registry.dat}</li>
     *   <li>Stripping {@code SpongeData} from the root {@code level.dat}</li>
     * </ul>
     *
     * <p>Note: Per-dimension {@code level.dat} files for nether/end are lost during vanilla's
     * {@code DimensionStorageFileFix} (which deletes {@code DIM-1/} and {@code DIM1/} after
     * moving region/entities/poi/data). Those dimensions get fresh UUIDs on first load.</p>
     */
    private void migrateOldSpongeData() {
        final Path rootLevelDat = this.defaultWorldDirectory.resolve("level.dat");
        if (!Files.exists(rootLevelDat)) {
            return;
        }

        try {
            final CompoundTag root = NbtIo.readCompressed(rootLevelDat, NbtAccounter.defaultQuota());
            final CompoundTag spongeData = root.getCompound("SpongeData").orElse(null);
            if (spongeData == null || spongeData.isEmpty()) {
                return; // No old SpongeData to migrate
            }

            SpongeCommon.logger().info("Migrating old SpongeData from level.dat files to per-dimension SavedData...");

            // Migrate each dimension that has SpongeData
            // Root level.dat = overworld, check dimensions/ for nether/end level.dat
            // (vanilla's DimensionStorageFileFix moves region/entities/poi/data from DIM-1/DIM1
            //  to dimensions/minecraft/the_nether|the_end but does NOT move level.dat — those are lost.
            //  However, if someone manually placed them or they survived, we handle them.)
            this.migrateOneDimension(spongeData, Level.OVERWORLD, "overworld");

            // Check for per-dimension level.dat files that may survive in the new directories
            for (final var entry : Map.of(
                Level.NETHER, "the_nether",
                Level.END, "the_end"
            ).entrySet()) {
                final Path dimLevelDat = DimensionType.getStorageFolder(entry.getKey(), this.defaultWorldDirectory)
                    .resolve("level.dat");
                if (Files.exists(dimLevelDat)) {
                    try {
                        final CompoundTag dimRoot = NbtIo.readCompressed(dimLevelDat, NbtAccounter.defaultQuota());
                        final @Nullable CompoundTag dimSpongeData = dimRoot.getCompound("SpongeData").orElse(null);
                        if (dimSpongeData != null && !dimSpongeData.isEmpty()) {
                            this.migrateOneDimension(dimSpongeData, entry.getKey(), entry.getValue());
                            dimRoot.remove("SpongeData");
                            NbtIo.writeCompressed(dimRoot, dimLevelDat);
                        }
                    } catch (final Exception e) {
                        SpongeCommon.logger().warn("  Failed to migrate SpongeData from {}", dimLevelDat, e);
                    }
                }
            }

            // Strip SpongeData from root level.dat
            root.remove("SpongeData");
            NbtIo.writeCompressed(root, rootLevelDat);
            SpongeCommon.logger().info("  Stripped SpongeData from root level.dat");

        } catch (final Exception e) {
            SpongeCommon.logger().warn("Failed to migrate old SpongeData from level.dat — worlds will get fresh UUIDs", e);
        }
    }

    private void migrateOneDimension(
        final CompoundTag spongeData,
        final net.minecraft.resources.ResourceKey<Level> dimensionKey,
        final String dimensionName
    ) {
        try {
            final Path dataDir = DimensionType.getStorageFolder(dimensionKey, this.defaultWorldDirectory)
                .resolve("data").resolve("sponge");
            final Path registryFile = dataDir.resolve("registry.dat");
            if (Files.exists(registryFile)) {
                return; // Already migrated
            }

            final int[] uuidArray = spongeData.getIntArray("UUID").orElse(null);
            if (uuidArray == null || uuidArray.length != 4) {
                return;
            }

            final UUID uuid = new UUID(
                (long) uuidArray[0] << 32 | uuidArray[1] & 0xFFFFFFFFL,
                (long) uuidArray[2] << 32 | uuidArray[3] & 0xFFFFFFFFL
            );

            Files.createDirectories(dataDir);
            final SpongeRegistryData registryData = new SpongeRegistryData(uuid, Optional.of(dimensionKey));
            final var encoded = SpongeRegistryData.CODEC.encodeStart(NbtOps.INSTANCE, registryData);
            if (encoded.isSuccess()) {
                final CompoundTag savedDataTag = new CompoundTag();
                savedDataTag.put("data", encoded.getOrThrow());
                NbtUtils.addCurrentDataVersion(savedDataTag);
                NbtIo.writeCompressed(savedDataTag, registryFile);
                SpongeCommon.logger().info("  Migrated {} UUID {} to {}", dimensionName, uuid, registryFile);
            }
        } catch (final Exception e) {
            SpongeCommon.logger().warn("  Failed to migrate SpongeData for dimension {}", dimensionName, e);
        }
    }

    private Optional<LevelDataLoadResult> loadLevelData(final net.minecraft.resources.ResourceKey<Level> registryKey,
                                                        final ServerWorldProperties.LoadOptions.@Nullable LoadOperation loadOperation) {
        return this.loadLevelData(this.serverStorageSource(), registryKey, loadOperation);
    }

    private Optional<LevelDataLoadResult> loadLevelData(
        final LevelStorageSource.LevelStorageAccess storageSource,
        final net.minecraft.resources.ResourceKey<Level> registryKey,
        final ServerWorldProperties.LoadOptions.@Nullable LoadOperation loadOperation
    ) {
        return Optional.ofNullable(this.loadLevelTag(registryKey)).map(t -> this.readLevelData(storageSource, registryKey, t, loadOperation));
    }

    private LevelDataLoadResult initializeLevelData(final ResourceKey key, final PrimaryLevelData data, final LevelStem stem) {
        final DimensionType dimensionType = stem.type().value();
        final var spongeData = ((PrimaryLevelDataBridge) data).bridge$spongeData();
        spongeData.setKey(key);
        spongeData.setConfigAdapter(SpongeGameConfigs.load(dimensionType, key));
        if (spongeData.worldGenOptions() == null) {
            spongeData.setWorldGenOptions(this.server.getWorldGenSettings());
        }
        ((PrimaryLevelDataBridge) data).bridge$populateFromLevelStem(stem);
        return new LevelDataLoadResult(data, stem);
    }

    /**
     * Attempts to load a per-dimension level.dat from the dimension's directory.
     * With the shared LevelStorageAccess, we cannot use storageSource.getUnfixedDataTag()
     * (which reads from the ROOT level.dat). Instead, check the dimension-specific path.
     *
     * <p>In the new architecture, non-overworld dimensions do NOT have their own level.dat
     * (Sponge no longer writes per-dimension level.dat files). This method returns null
     * for dimensions without one, causing loadLevelData to fall through to createLevelData.</p>
     */
    private @Nullable Dynamic<?> loadLevelTag(final net.minecraft.resources.ResourceKey<Level> registryKey) {
        final Path dimensionDir = DimensionType.getStorageFolder(registryKey, this.defaultWorldDirectory);
        final Path levelDat = dimensionDir.resolve("level.dat");
        if (!Files.exists(levelDat)) {
            return null;
        }
        try {
            final CompoundTag root = NbtIo.readCompressed(levelDat, NbtAccounter.defaultQuota());
            return new Dynamic<>(NbtOps.INSTANCE, root.getCompoundOrEmpty("Data"));
        } catch (final IOException e) {
            SpongeCommon.logger().warn("Failed to load level data from {}", levelDat, e);
            return null;
        }
    }

    private LevelDataLoadResult readLevelData(
        final LevelStorageSource.LevelStorageAccess worldAccess,
        final net.minecraft.resources.ResourceKey<Level> registryKey,
        final Dynamic<?> dataTag,
        final ServerWorldProperties.LoadOptions.@Nullable LoadOperation loadOperation
    ) {
        // Apply file-fixing and data-fixing before parsing. In 26.1, FileFixerUpper
        // requires all level data to be fixed before getLevelDataAndDimensions() can parse it.
        // Vanilla handles this for the overworld via the WorldLoader pipeline, but Sponge's
        // per-world level.dat files need the same treatment.
        final Dynamic<?> fixedDataTag;
        try {
            fixedDataTag = DataFixers.getFileFixer().fix(worldAccess, dataTag, new UpgradeProgress());
        } catch (final FileFixException e) {
            throw new RuntimeException(String.format("Failed to apply data fixes for world '%s'", registryKey.identifier()), e);
        }

        final PrimaryLevelData defaultLevelData = (PrimaryLevelData) this.server.getWorldData();
        final RegistryAccess.Frozen access = this.server.registryAccess();
        final LevelDataAndDimensions levelData = LevelStorageSource.getLevelDataAndDimensions(worldAccess,
            fixedDataTag, defaultLevelData.getDataConfiguration(), access.lookupOrThrow(Registries.LEVEL_STEM), access);
        final WorldArchetype worldArchetype = SpongeWorldManager.resolveWorldArchetype(levelData, registryKey, loadOperation);
        final LevelStem levelStem = (LevelStem) (Object) worldArchetype.type();
        final PrimaryLevelData worldData = (PrimaryLevelData) levelData.worldDataAndGenSettings().data();
        worldArchetype.generationConfig().ifPresent(o ->
            ((PrimaryLevelDataBridge) worldData).bridge$spongeData().setWorldOptions((WorldOptions) o));
        ((PrimaryLevelDataAccessor) worldData).accessor$specialWorldProperty(SpongeWorldManager.specialWorldProperty(levelStem));
        return this.initializeLevelData((ResourceKey) (Object) registryKey.identifier(), worldData, levelStem);
    }

    private static WorldArchetype resolveWorldArchetype(final LevelDataAndDimensions levelData, final net.minecraft.resources.ResourceKey<Level> registryKey,
                                                        final ServerWorldProperties.LoadOptions.@Nullable LoadOperation loadOperation) {
        if (loadOperation != null && loadOperation.overrideWorldArchetype().isPresent()) {
            return loadOperation.overrideWorldArchetype().get();
        }

        // Worlds that were loaded with Sponge know the associated world archetype.
        final Optional<LevelStem> spongeWorldKeyStem = Optional.ofNullable(((PrimaryLevelDataBridge) levelData.worldDataAndGenSettings().data()).bridge$spongeData().key())
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
        final LevelSettings levelSettings = this.createLevelSettings(defaultLevelData, registryKey.identifier().toString());
        final LevelDataLoadResult result = this.initializeLevelData(
            (ResourceKey) (Object) registryKey.identifier(),
            new PrimaryLevelData(levelSettings,
                SpongeWorldManager.specialWorldProperty(levelStem), Lifecycle.stable()),
            levelStem
        );
        // Apply custom generation config from the archetype (e.g. custom seed)
        archetype.generationConfig().ifPresent(o ->
            ((PrimaryLevelDataBridge) result.data()).bridge$spongeData().setWorldOptions((WorldOptions) o));
        return result;
    }

    private LevelSettings createLevelSettings(final PrimaryLevelData defaultLevelData, final String directoryName) {
        return new LevelSettings(
            directoryName,
            defaultLevelData.getGameType(),
            defaultLevelData.getLevelSettings().difficultySettings(),
            defaultLevelData.isAllowCommands(),
            defaultLevelData.getDataConfiguration()
        );
    }

    private ServerLevel createLevel(
        final net.minecraft.resources.ResourceKey<Level> registryKey,
        final LevelStem levelStem,
        final LevelStorageSource.LevelStorageAccess storageSource,
        final PrimaryLevelData levelData
    ) {
        final ResourceKey worldKey = (ResourceKey) (Object) registryKey.identifier();
        final Registry<DimensionType> dimensionTypes = this.server.registryAccess().lookupOrThrow(Registries.DIMENSION_TYPE);
        final var saveDataStorage = this.server.getDataStorage();

        // Resolve inline DimensionType holders to registry references.
        // When a LevelStem is deserialized from WorldGenSettings with an inline DimensionType
        // (Holder.direct), the login packet encoder fails because STREAM_CODEC expects a registry ID.
        // The registry uses IdentityHashMap so wrapAsHolder() won't match deserialized instances.
        // Instead, iterate the registry and match by record equals().
        final LevelStem resolvedStem = SpongeWorldManager.resolveInlineDimensionType(levelStem, dimensionTypes);

        MinecraftServerAccessor.accessor$LOGGER().info("Loading world '{}'", worldKey);

        final List<CustomSpawner> spawners;
        if (resolvedStem.type().value() == dimensionTypes.getValue(BuiltinDimensionTypes.OVERWORLD) || resolvedStem.type().value() == dimensionTypes.getValue(BuiltinDimensionTypes.OVERWORLD_CAVES)) {
            // TODO - 26.1-snapshot-6 figure out if we need to do anything here per world?
            spawners = ImmutableList.of(new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(saveDataStorage));
        } else {
            spawners = ImmutableList.of();
        }

        levelData.setModdedInfo(this.server.getServerModName(), this.server.getModdedStatus().shouldReportAsModified());
        final var spongeData = ((PrimaryLevelDataBridge) levelData).bridge$spongeData();
        final var seed = BiomeManager.obfuscateSeed(spongeData.worldGenOptions().seed());
        final Executor executor = ((MinecraftServerAccessor) this.server).accessor$executor();

        final ServerLevel world = new ServerLevel(this.server, executor, storageSource, levelData,
            registryKey, resolvedStem, levelData.isDebugWorld(), seed, spawners, true);
        this.worlds.put(registryKey, world);

        // Persist the LevelStem (with resolved holder) in the server's WorldGenSettings so custom
        // dimensions survive restart. Using the resolved stem ensures the DimensionType is serialized
        // as a registry reference, not inline, breaking the inline cycle for future loads.
        final net.minecraft.resources.ResourceKey<LevelStem> stemKey = Registries.levelToLevelStem(registryKey);
        final WorldGenSettings currentSettings = this.server.getWorldGenSettings();
        if (!currentSettings.dimensions().dimensions().containsKey(stemKey)) {
            final Map<net.minecraft.resources.ResourceKey<LevelStem>, LevelStem> mutableDims = new HashMap<>(currentSettings.dimensions().dimensions());
            mutableDims.put(stemKey, resolvedStem);
            final WorldGenSettings updated = new WorldGenSettings(currentSettings.options(), new WorldDimensions(mutableDims));
            this.server.getDataStorage().set(WorldGenSettings.TYPE, updated);
        }

        // Store per-dimension WorldGenSettings in the dimension's own SavedDataStorage.
        // This gives each world its own world_gen_settings.dat with the correct seed/options.
        final var levelSpongeData = ((PrimaryLevelDataBridge) levelData).bridge$spongeData();
        final WorldOptions perWorldOptions = levelSpongeData.worldGenOptions() != null
            ? levelSpongeData.worldGenOptions() : currentSettings.options();
        final WorldGenSettings perDimSettings = new WorldGenSettings(
            perWorldOptions, new WorldDimensions(Map.of(stemKey, resolvedStem)));
        world.getDataStorage().set(WorldGenSettings.TYPE, perDimSettings);

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
                    final var spongeLevelData = levelDataBridge.bridge$spongeData();
                    if (!hasSpawnAlready) {
                        if (levelDataBridge.bridge$performsSpawnLogic()) {
                            MinecraftServerAccessor.invoker$setInitialSpawn(level, levelData, spongeLevelData.worldGenOptions().generateBonusChest(), isDebugGeneration, server.getLevelLoadListener());
                        } else if (Level.END.equals(level.dimension())) {
                            levelData.setSpawn(new LevelData.RespawnData(new GlobalPos(level.dimension(), ServerLevel.END_SPAWN_POINT), 0, 0));
                        }
                    } else if (spongeLevelData.worldGenOptions().generateBonusChest()) {
                        final BlockPos pos = levelData.getRespawnData().pos();
                        final Feature bonusChestFeature = SpongeCommon.vanillaRegistry(Registries.FEATURE).getValue(MiscOverworldFeatures.BONUS_CHEST);
                        bonusChestFeature.place(level, level.getChunkSource().getGenerator(), level.getRandom(), pos);
                    }
                    levelData.setInitialized(true);
                    if (isDebugGeneration) {
                        ((MinecraftServerAccessor) this.server).invoker$setupDebugLevel(worldData);
                    }
                } catch (final Throwable throwable) {
                    final CrashReport crashReport = CrashReport.forThrowable(throwable, "Exception initializing world '" + level.dimension().identifier() + "'");
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

        final var customBossEvents = levelDataBridge.bridge$spongeData().customBossEvents();
        if (levelData instanceof WorldData worldData && customBossEvents != null) {
            // TODO - re-add load to bossbarmanager
//            ((ServerLevelBridge) level).bridge$getBossBarManager().load(customBossEvents, level.registryAccess());
        }

        return level;
    }

    /**
     * Same as loadSpawnChunks but async and without listener.
     */
    private CompletableFuture<ServerLevel> loadSpawnChunksAsync(final WorldOperationTask operationTask, final ServerLevel level) {
        MinecraftServerAccessor.accessor$LOGGER().info("Preparing start region for dimension {}", level.dimension().identifier());

        final var respawnData = level.getRespawnData();
        level.setRespawnData(level.getWorldBorderAdjustedRespawnData(respawnData));

        final ChunkLoadCounter chunkLoadCounter = new ChunkLoadCounter();
        chunkLoadCounter.track(level, () -> {
            TicketStorage $$1x = level.getDataStorage().get(TicketStorage.TYPE);
            if ($$1x != null) {
                $$1x.activateAllDeactivatedTickets();
            }
        });

        final CompletableFuture<ServerLevel> generationFuture = new CompletableFuture<>();
        Sponge.asyncScheduler().submit(
            Task.builder().plugin(Launch.instance().platformPlugin()).execute(task -> {
                if (operationTask.isCancelled()) {
                    generationFuture.cancel(false);
                    task.cancel();
                } else if (chunkLoadCounter.pendingChunks() <= 0) {
                    generationFuture.complete(level);
                    // Notify the future that we are done
                    task.cancel(); // And cancel this task
                    MinecraftServerAccessor.accessor$LOGGER().info("Done preparing start region for dimension {}", level.dimension().identifier());
                }
            }).interval(10, TimeUnit.MILLISECONDS).build()
        );

        return generationFuture;
    }

    public static net.minecraft.resources.ResourceKey<Level> createRegistryKey(final ResourceKey key) {
        return net.minecraft.resources.ResourceKey.create(Registries.DIMENSION, (Identifier) (Object) key);
    }

    private Path getDirectory(final ResourceKey key) {
        final net.minecraft.resources.ResourceKey<Level> registryKey = SpongeWorldManager.createRegistryKey(key);
        return DimensionType.getStorageFolder(registryKey, this.defaultWorldDirectory);
    }

    private Path getConfigFile(final ResourceKey key) {
        return SpongeConfigs.getDirectory().resolve("worlds").resolve(key.namespace()).resolve(key.value() + ".conf");
    }

    @SuppressWarnings("deprecation")
    private static PrimaryLevelData.SpecialWorldProperty specialWorldProperty(final LevelStem stem) {
        // Copied from WorldDimensions#specialWorldProperty
        final ChunkGenerator generator = stem.generator();
        if (generator instanceof DebugLevelSource) {
            return PrimaryLevelData.SpecialWorldProperty.DEBUG;
        } else {
            return generator instanceof FlatLevelSource ? PrimaryLevelData.SpecialWorldProperty.FLAT : PrimaryLevelData.SpecialWorldProperty.NONE;
        }
    }

    private <T> CompletableFuture<T> performWorldOperation(final net.minecraft.resources.ResourceKey<Level> worldKey, final WorldOperationType type,
                                                           final Function<WorldOperationTask, CompletableFuture<T>> function) {
        if (SpongeCommon.server().isStopped()) {
            return ExecutorUtil.serverManagedBlock(SpongeCommon.server(), function.apply(new WorldOperationTask(type, null)));
        }
        return this.worldOperations.compute(worldKey, ($, value) -> new WorldOperationTask(type, value))
            .chain(t -> function.apply(t).whenCompleteAsync(($0, $1) -> this.worldOperations.remove(worldKey, t), SpongeCommon.server()));
    }

    public CompletableFuture<Void> close() {
        return CompletableFuture.allOf(this.worldOperations.values().stream().map(t -> {
            t.cancel();

            return t.future();
        }).toArray(CompletableFuture[]::new));
    }

    private record LevelDataLoadResult(PrimaryLevelData data, LevelStem stem) {
    }

    private static final class WorldOperationTask {

        private final WorldOperationType type;

        private @MonotonicNonNull CompletableFuture<?> future;
        private @Nullable WorldOperationTask previous;

        private boolean cancelled;

        WorldOperationTask(final WorldOperationType type, final @Nullable WorldOperationTask previous) {
            this.type = type;
            this.previous = previous;

            if (previous != null) {
                previous.future.whenComplete(($0, $1) -> this.previous = null);
            }
        }

        WorldOperationType type() {
            return this.type;
        }

        CompletableFuture<?> future() {
            return this.future;
        }

        boolean isCancelled() {
            return this.cancelled;
        }

        <T> CompletableFuture<T> chain(final Function<WorldOperationTask, CompletableFuture<T>> function) {
            final @Nullable WorldOperationTask previous = this.previous;

            final CompletableFuture<T> future = previous == null
                ? function.apply(this)
                : previous.future.handleAsync(($0, $1) -> function.apply(this), SpongeCommon.server()).thenCompose(Function.identity());

            this.future = future;

            return future;
        }

        void cancel() {
            if (this.type == WorldOperationType.LOAD || this.type == WorldOperationType.UNLOAD) {
                this.cancelled = true;
            }

            final @Nullable WorldOperationTask previous = this.previous;
            if (previous != null) {
                previous.cancel();
            }
        }
    }

    /**
     * Resolves inline (Holder.direct) DimensionType holders in a LevelStem to registry references.
     *
     * <p>When a LevelStem is deserialized from WorldGenSettings with an inline DimensionType,
     * the network STREAM_CODEC (ByteBufCodecs.holderRegistry) can't find the registry ID,
     * causing login packet encoding to fail. The DimensionType registry uses IdentityHashMap
     * for value lookups, so wrapAsHolder() won't match deserialized record instances.
     * Record equals() also fails because HolderSet.Named uses identity comparison.</p>
     *
     * <p>This method matches by comparing the structural primitive/enum/TagKey fields of
     * DimensionType, which are reliably comparable across deserialized instances.</p>
     */
    static LevelStem resolveInlineDimensionType(final LevelStem stem, final Registry<DimensionType> dimensionTypes) {
        if (stem.type() instanceof Holder.Reference<?>) {
            return stem; // Already a registry reference
        }
        final DimensionType inline = stem.type().value();
        for (final var entry : dimensionTypes.entrySet()) {
            final DimensionType registered = entry.getValue();
            if (SpongeWorldManager.dimensionTypeStructurallyEquals(inline, registered)) {
                final Optional<Holder.Reference<DimensionType>> ref = dimensionTypes.get(entry.getKey().identifier());
                if (ref.isPresent()) {
                    return new LevelStem(ref.get(), stem.generator());
                }
            }
        }
        SpongeCommon.logger().warn("Could not resolve inline DimensionType to registry reference. "
            + "Login packets for worlds using this dimension type may fail.");
        return stem;
    }

    /**
     * Compares two DimensionType instances by their structural fields, ignoring Holder/HolderSet
     * fields that use identity-based equality (HolderSet.Named, Holder.Reference).
     */
    private static boolean dimensionTypeStructurallyEquals(final DimensionType a, final DimensionType b) {
        return a.hasFixedTime() == b.hasFixedTime()
            && a.hasSkyLight() == b.hasSkyLight()
            && a.hasCeiling() == b.hasCeiling()
            && a.hasEnderDragonFight() == b.hasEnderDragonFight()
            && Double.compare(a.coordinateScale(), b.coordinateScale()) == 0
            && a.minY() == b.minY()
            && a.height() == b.height()
            && a.logicalHeight() == b.logicalHeight()
            && a.infiniburn().equals(b.infiniburn())
            && Float.compare(a.ambientLight(), b.ambientLight()) == 0
            && a.monsterSettings().monsterSpawnBlockLightLimit() == b.monsterSettings().monsterSpawnBlockLightLimit()
            && a.skybox() == b.skybox()
            && a.cardinalLightType() == b.cardinalLightType();
    }

    private enum WorldOperationType {
        LOAD,
        UNLOAD,
        SAVE,
        COPY,
        MOVE,
        DELETE
    }
}
