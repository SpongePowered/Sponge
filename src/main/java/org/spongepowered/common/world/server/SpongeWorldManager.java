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
import net.minecraft.core.RegistryAccess;
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
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.datapack.DataPack;
import org.spongepowered.api.datapack.DataPackTypes;
import org.spongepowered.api.datapack.DataPacks;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.file.DeleteFileVisitor;
import org.spongepowered.api.world.DefaultWorldKeys;
import org.spongepowered.api.world.WorldType;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.WorldManager;
import org.spongepowered.api.world.server.WorldTemplate;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.server.MinecraftServerAccessor;
import org.spongepowered.common.accessor.server.level.ServerLevelAccessor;
import org.spongepowered.common.accessor.world.level.storage.LevelStorageSource_LevelStorageAccessAccessor;
import org.spongepowered.common.bridge.core.MappedRegistryBridge;
import org.spongepowered.common.bridge.server.level.ServerLevelBridge;
import org.spongepowered.common.bridge.world.level.chunk.storage.IOWorkerBridge;
import org.spongepowered.common.bridge.world.level.dimension.LevelStemBridge;
import org.spongepowered.common.bridge.world.level.storage.PrimaryLevelDataBridge;
import org.spongepowered.common.bridge.world.level.storage.ServerLevelDataBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.hooks.PlatformHooks;
import org.spongepowered.common.launch.Launch;
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
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SpongeWorldManager implements WorldManager {

    private final MinecraftServer server;
    private final Path defaultWorldDirectory, customWorldsDirectory;
    private final Map<net.minecraft.resources.ResourceKey<Level>, ServerLevel> worlds;

    public SpongeWorldManager(final MinecraftServer server) {
        this.server = server;
        this.defaultWorldDirectory = ((LevelStorageSource_LevelStorageAccessAccessor) ((MinecraftServerAccessor) this.server).accessor$storageSource()).accessor$levelDirectory().path();
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
    public Optional<Path> worldDirectory(final ResourceKey key) {
        Objects.requireNonNull(key, "key");

        Path directory = this.getDirectory(key);
        if (Files.notExists(directory)) {
            return Optional.empty();
        }
        return Optional.of(directory);
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
    public CompletableFuture<ServerWorld> loadWorld(final WorldTemplate template) {
        final ResourceKey key = Objects.requireNonNull(template, "template").key();
        final net.minecraft.resources.ResourceKey<Level> registryKey = SpongeWorldManager.createRegistryKey(key);
        if (Level.OVERWORLD.equals(registryKey)) {
            return FutureUtil.completedWithException(new IllegalArgumentException("The default world cannot be told to load!"));
        }

        final ServerLevel serverWorld = this.worlds.get(registryKey);
        if (serverWorld != null) {
            return CompletableFuture.completedFuture((ServerWorld) serverWorld);
        }

        this.saveTemplate(template);

        return this.loadWorld0(registryKey, ((SpongeWorldTemplate) template).levelStem());
    }

    @Override
    public CompletableFuture<ServerWorld> loadWorld(final ResourceKey key) {
        if (DefaultWorldKeys.DEFAULT.equals(key)) {
            return FutureUtil.completedWithException(new IllegalArgumentException("The default world cannot be told to load!"));
        }

        final net.minecraft.resources.ResourceKey<Level> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(key, "key"));
        final ServerLevel world = this.worlds.get(registryKey);
        if (world != null) {
            return CompletableFuture.completedFuture((ServerWorld) world);
        }

        // First find a loaded level-stem / To load based on a datapack load using the WorldTemplate instead

        final net.minecraft.resources.ResourceKey<LevelStem> stemKey = net.minecraft.resources.ResourceKey.create(Registries.LEVEL_STEM, (ResourceLocation) (Object) key);
        final @Nullable LevelStem levelStem = SpongeCommon.vanillaRegistry(Registries.LEVEL_STEM).get(stemKey);
        if (levelStem != null) {
            return this.loadWorld0(registryKey, levelStem);
        }

        // Then attempt to load from data pack
        final DataPack<WorldTemplate> pack = this.findPack(key);
        return this.loadTemplate(pack, key).thenCompose(template -> {
            if (template.isEmpty()) {
                return FutureUtil.completedWithException(new IOException(String.format("Failed to load a template for '%s'!", key)));
            }
            return this.loadWorld0(registryKey, ((SpongeWorldTemplate) template.get()).levelStem());
        });
    }

    private CompletableFuture<ServerWorld> loadWorld0(final net.minecraft.resources.ResourceKey<Level> registryKey, final LevelStem levelStem) {
        final ResourceKey worldKey = (ResourceKey) (Object) registryKey.location();

        MinecraftServerAccessor.accessor$LOGGER().info("Loading world '{}'", worldKey);

        final ServerLevel level;
        try {
            level = this.createNonDefaultLevel(registryKey, levelStem, worldKey);
        } catch (final IOException e) {
            return FutureUtil.completedWithException(new RuntimeException(String.format("Failed to create level data for world '%s'!", worldKey), e));
        }

        return SpongeCommon.asyncScheduler().submit(() -> this.prepareLevel(level)).thenApply(w -> {
                    ((MinecraftServerAccessor) this.server).invoker$forceDifficulty();
                    return w;
                }).thenCompose(w -> this.loadSpawnChunksAsync(level))
                  .thenApply(w -> (ServerWorld) w);
    }

    private LevelStorageSource.LevelStorageAccess getLevelStorageAccess(final ResourceKey worldKey) throws IOException {
        if (this.isVanillaWorld(worldKey)) {
            final String directoryName = this.getDirectoryName(worldKey);
            return LevelStorageSource.createDefault(this.defaultWorldDirectory).createAccess(directoryName);
        }
        final String name = worldKey.namespace() + File.separator + worldKey.value();
        return LevelStorageSource.createDefault(this.customWorldsDirectory).createAccess(name);
    }

    private LevelSettings createLevelSettings(final PrimaryLevelData defaultLevelData, final LevelStem levelStem, final String directoryName) {
        final LevelStemBridge levelStemBridge = (LevelStemBridge) (Object) levelStem;
        final GameType gameType = levelStemBridge.bridge$gameMode();
        final Boolean hardcore = levelStemBridge.bridge$hardcore();
        final Difficulty difficulty = levelStemBridge.bridge$difficulty();
        final Boolean allowCommands = levelStemBridge.bridge$allowCommands();
        return new LevelSettings(
                directoryName,
                gameType == null ? defaultLevelData.getGameType() : gameType,
                hardcore == null ? defaultLevelData.isHardcore() : hardcore,
                difficulty == null ? defaultLevelData.getDifficulty() : difficulty,
                allowCommands == null ? defaultLevelData.isAllowCommands() : allowCommands,
                defaultLevelData.getGameRules().copy(),
                defaultLevelData.getDataConfiguration());
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

        try {
            this.unloadWorld0((ServerLevel) world);
            return CompletableFuture.completedFuture(true);
        } catch (final IOException e) {
            return FutureUtil.completedWithException(e);
        }
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

        final PrimaryLevelData levelData;
        try (var storageSource = this.getLevelStorageAccess(key)) {
            final PrimaryLevelData defaultLevelData = (PrimaryLevelData) this.server.getWorldData();
            levelData = this.loadLevelData(defaultLevelData.getDataConfiguration(), storageSource.getDataTag());
        } catch (final Exception e) {
            return FutureUtil.completedWithException(e);
        }

        final DataPack<WorldTemplate> pack = this.findPack(key);
        return this.loadTemplate(pack, key).thenCompose(template -> {
            if (template.isPresent()) {
                final LevelStem scratch = ((SpongeWorldTemplate) template.get()).levelStem();
                ((PrimaryLevelDataBridge) levelData).bridge$populateFromLevelStem(scratch);
            }

            ((PrimaryLevelDataBridge) levelData).bridge$spongeData().setKey(key);
            return CompletableFuture.completedFuture(Optional.of((ServerWorldProperties) levelData));
        });
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
        final DataPack<WorldTemplate> pack = this.findPack(properties.key());
        return this.loadTemplate(pack, properties.key()).thenCompose(r -> {
            final WorldTemplate template = r.orElse(null);
            if (template != null) {
                return this.saveTemplate(WorldTemplate.builder().from(template).from(properties).build());
            }

            return CompletableFuture.completedFuture(true);
        });
    }


    private void saveLevelDat(final WorldData worldData, final ResourceKey key) throws IOException {
        try (var storageSource = this.getLevelStorageAccess(key)) {
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
        }).thenApplyAsync($ -> {
            try {
                this.server().dataPackManager().copy(this.findPack(key), key, copyKey);
            } catch (final IOException e) {
                throw new CompletionException(e);
            }

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
            try {
                this.unloadWorld0(loadedWorld);
            } catch (final IOException e) {
                return FutureUtil.completedWithException(e);
            }
        }

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
        }).thenApplyAsync($ -> {
            try {
                this.server().dataPackManager().move(this.findPack(key), key, movedKey);
            } catch (final IOException e) {
                throw new CompletionException(e);
            }

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
            final boolean disableLevelSaving = loadedWorld.noSave;
            loadedWorld.noSave = true;
            ((IOWorkerBridge) loadedWorld.getChunkSource().chunkMap.chunkScanner()).bridge$forciblyClear();
            try {
                this.unloadWorld0(loadedWorld);
            } catch (final IOException e) {
                loadedWorld.noSave = disableLevelSaving;
                return FutureUtil.completedWithException(e);
            }
        }

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
            try {
                this.server().dataPackManager().delete(this.findPack(key), key);
            } catch (final IOException e) {
                throw new CompletionException(e);
            }

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

    private DataPack<WorldTemplate> findPack(ResourceKey key) {
        return this.server().dataPackManager().findPack(DataPackTypes.WORLD, key).orElse(DataPacks.WORLD);
    }

    private void unloadWorld0(final ServerLevel level) throws IOException {
        final net.minecraft.resources.ResourceKey<Level> registryKey = level.dimension();

        if (!level.getPlayers(p -> true).isEmpty()) {
            throw new IOException(String.format("World '%s' was told to unload but players remain.", registryKey.location()));
        }

        SpongeCommon.logger().info("Unloading world '{}'", registryKey.location());

        final UnloadWorldEvent unloadWorldEvent = SpongeEventFactory.createUnloadWorldEvent(PhaseTracker.getInstance().currentCause(), (ServerWorld) level);
        SpongeCommon.post(unloadWorldEvent);

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
            throw new IOException(ex);
        }

        this.worlds.remove(registryKey);
    }

    public void createNonDefaultLevels() {
        final Registry<LevelStem> registry = SpongeCommon.vanillaRegistry(Registries.LEVEL_STEM);
        for (LevelStem levelStem : registry) {
            final ResourceKey worldKey = (ResourceKey) (Object) registry.getKey(levelStem);
            if (DefaultWorldKeys.DEFAULT.equals(worldKey)) {
                continue;
            }

            final LevelStemBridge bridge = (LevelStemBridge) (Object) levelStem;
            if (!bridge.bridge$loadOnStartup()) {
                SpongeCommon.logger().warn("World '{}' has been disabled from loading at startup. Skipping...", worldKey);
                continue;
            }

            MinecraftServerAccessor.accessor$LOGGER().info("Loading world '{}'", worldKey);
            final net.minecraft.resources.ResourceKey<Level> registryKey = SpongeWorldManager.createRegistryKey(worldKey);

            try {
                final ServerLevel level = this.createNonDefaultLevel(registryKey, levelStem, worldKey);
                this.prepareLevel(level);
            } catch (final Exception e) {
                throw new RuntimeException(String.format("Failed to create level data for world '%s'!", worldKey), e);
            }
        }
    }

    public void prepareLevels() {
        for (final Map.Entry<net.minecraft.resources.ResourceKey<Level>, ServerLevel> entry : this.worlds.entrySet()) {
            this.loadSpawnChunks(entry.getValue());
        }

        ((SpongeUserManager) Sponge.server().userManager()).init();
    }

    private PrimaryLevelData getOrCreateLevelData(@Nullable final Dynamic<?> dynamicLevelData, final LevelStem levelStem, final String directoryName) {
        final PrimaryLevelData defaultLevelData = (PrimaryLevelData) this.server.getWorldData();
        if (dynamicLevelData != null) {
            try {
                return this.loadLevelData(defaultLevelData.getDataConfiguration(), dynamicLevelData);
            } catch (Exception e) {
                throw new RuntimeException("Failed to load level data from " + directoryName, e);
            }
        }

        if (this.server.isDemo()) {
            return new PrimaryLevelData(MinecraftServer.DEMO_SETTINGS, WorldOptions.DEMO_OPTIONS, SpongeWorldManager.specialWorldProperty(levelStem), Lifecycle.stable());
        }

        final LevelSettings levelSettings = this.createLevelSettings(defaultLevelData, levelStem, directoryName);
        WorldOptions worldGenOptions = defaultLevelData.worldGenOptions();
        final Long customSeed = ((LevelStemBridge) (Object) levelStem).bridge$seed();
        if (customSeed != null) {
            worldGenOptions = worldGenOptions.withSeed(OptionalLong.of(customSeed));
        }
        return new PrimaryLevelData(levelSettings, worldGenOptions, SpongeWorldManager.specialWorldProperty(levelStem), Lifecycle.stable());
    }

    private PrimaryLevelData loadLevelData(final WorldDataConfiguration datapackConfig, final Dynamic<?> dataTag) {
        final RegistryAccess.Frozen access = this.server.registryAccess();
        final LevelDataAndDimensions levelData = LevelStorageSource.getLevelDataAndDimensions(dataTag, datapackConfig, access.registryOrThrow(Registries.LEVEL_STEM), access);
        return (PrimaryLevelData) levelData.worldData();
    }

    private ServerLevel createNonDefaultLevel(final net.minecraft.resources.ResourceKey<Level> registryKey, final LevelStem levelStem, final ResourceKey worldKey) throws IOException {
        if (DefaultWorldKeys.DEFAULT.equals(worldKey)) {
            throw new IllegalArgumentException();
        }

        final String directoryName = this.getDirectoryName(worldKey);
        final LevelStorageSource.LevelStorageAccess storageSource = this.getLevelStorageAccess(worldKey);

        @Nullable Dynamic<?> dataTag;
        try {
            dataTag = storageSource.getDataTag();
        } catch (IOException e) {
            dataTag = null;
        }

        final PrimaryLevelData levelData = this.getOrCreateLevelData(dataTag, levelStem, directoryName);
        levelData.setModdedInfo(this.server.getServerModName(), this.server.getModdedStatus().shouldReportAsModified());

        ((PrimaryLevelDataBridge) levelData).bridge$populateFromLevelStem(levelStem);
        ((PrimaryLevelDataBridge) levelData).bridge$spongeData().setKey(worldKey);

        final List<CustomSpawner> spawners;
        if (levelStem.type().is(BuiltinDimensionTypes.OVERWORLD) || levelStem.type().is(BuiltinDimensionTypes.OVERWORLD_CAVES)) {
            spawners = ImmutableList.of(new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(levelData));
        } else {
            spawners = ImmutableList.of();
        }

        final long seed = BiomeManager.obfuscateSeed(levelData.worldGenOptions().seed());
        final Executor executor = ((MinecraftServerAccessor) this.server).accessor$executor();
        final ChunkProgressListener progressListener = ((MinecraftServerAccessor) this.server).accessor$progressListenerFactory().create(SpongeWorldManager.getSpawnRadius(levelData));

        final ServerLevel level = new ServerLevel(this.server, executor, storageSource, levelData, registryKey, levelStem, progressListener, levelData.isDebugWorld(), seed, spawners, true, null);
        this.worlds.put(registryKey, level);
        PlatformHooks.INSTANCE.getWorldHooks().postLoadWorld(level);
        return level;
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

        level.getWorldBorder().applySettings(levelData.getWorldBorder());

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
                        final ConfiguredFeature<?, ?> bonusChestFeature = SpongeCommon.vanillaRegistry(Registries.CONFIGURED_FEATURE).get(MiscOverworldFeatures.BONUS_CHEST);
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

        final int spawnRadius = SpongeWorldManager.getSpawnRadius(level.getLevelData());
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

        final int spawnRadius = SpongeWorldManager.getSpawnRadius(level.getLevelData());
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

    private static int getSpawnRadius(final LevelData levelData) {
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

    private CompletionStage<Boolean> saveTemplate(final WorldTemplate template) {
        return this.server().dataPackManager().save(template).thenApply(b -> true);
    }

    private CompletableFuture<Optional<WorldTemplate>> loadTemplate(final DataPack<WorldTemplate> pack, final ResourceKey key) {
        if (this.server().dataPackManager().exists(pack, key)) {
            return this.server().dataPackManager().load(pack, key).exceptionally(e -> {
                e.printStackTrace();
                return Optional.empty();
            });
        }
        return CompletableFuture.completedFuture(Optional.empty());
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
        return SpongeCommon.spongeConfigDirectory().resolve(Launch.instance().id()).resolve("worlds").resolve(key.namespace())
                .resolve(key.value() + ".conf");
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
}
