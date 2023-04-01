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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.longs.LongIterator;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.data.worldgen.features.MiscOverworldFeatures;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
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
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.file.DeleteFileVisitor;
import org.spongepowered.api.world.DefaultWorldKeys;
import org.spongepowered.api.world.WorldType;
import org.spongepowered.api.world.WorldTypes;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.WorldManager;
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
import org.spongepowered.common.hooks.PlatformHooks;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.server.BootstrapProperties;
import org.spongepowered.common.user.SpongeUserManager;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.FutureUtil;

import java.io.BufferedReader;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class SpongeWorldManager implements WorldManager {

    private final MinecraftServer server;
    private final Path dimensionsDataPackDirectory, defaultWorldDirectory, customWorldsDirectory;
    private final Map<net.minecraft.resources.ResourceKey<Level>, ServerLevel> worlds;

    private static final TicketType<ResourceLocation> SPAWN_CHUNKS = TicketType.create("spawn_chunks", ResourceLocation::compareTo);

    public SpongeWorldManager(final MinecraftServer server) {
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
    public Server server() {
        return (Server) this.server;
    }

    public Path getDefaultWorldDirectory() {
        return this.defaultWorldDirectory;
    }

    public Path getDimensionDataPackDirectory() {
        return this.dimensionsDataPackDirectory;
    }

    @Override
    public Optional<org.spongepowered.api.world.server.ServerWorld> world(final ResourceKey key) {
        return Optional.ofNullable((org.spongepowered.api.world.server.ServerWorld) this.worlds.get(SpongeWorldManager.createRegistryKey(Objects
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
    public Collection<org.spongepowered.api.world.server.ServerWorld> worlds() {
        return Collections.unmodifiableCollection((Collection<org.spongepowered.api.world.server.ServerWorld>) (Object) this.worlds.values());
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
    public List<ResourceKey> templateKeys() {
        final List<ResourceKey> templateKeys = new ArrayList<>();
        // Treat Vanilla ones as template keys
        templateKeys.add(WorldTypes.OVERWORLD.location());
        templateKeys.add(WorldTypes.THE_NETHER.location());
        templateKeys.add(WorldTypes.THE_END.location());

        try (final Stream<Path> pluginDirectories = Files.list(this.getDimensionDataPackDirectory())) {
            pluginDirectories
                    .filter(Files::isDirectory)
                    .forEach(pluginDirectory -> {
                                final Path dimensionPath = pluginDirectory.resolve("dimension");
                                if (Files.isDirectory(dimensionPath)) {
                                    try (final Stream<Path> pluginTemplates = Files.list(dimensionPath)) {
                                        pluginTemplates
                                                .filter(template -> template.toString().endsWith(".json"))
                                                .forEach(template -> templateKeys.add((ResourceKey) (Object) new ResourceLocation(pluginDirectory.getFileName().toString(),
                                                        FilenameUtils.removeExtension(template.getFileName().toString()))));
                                    } catch (final IOException e) {
                                        throw new RuntimeException(e);
                                    }
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

        return Files.exists(this.getDirectory(key));
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
    public Collection<ServerWorld> worldsOfType(final WorldType type) {
        Objects.requireNonNull(type, "type");

        return this.worlds().stream().filter(w -> w.worldType() == type).collect(Collectors.toList());
    }

    @Override
    public CompletableFuture<org.spongepowered.api.world.server.ServerWorld> loadWorld(final WorldTemplate template) {
        final ResourceKey key = Objects.requireNonNull(template, "template").key();
        final net.minecraft.resources.ResourceKey<Level> registryKey = SpongeWorldManager.createRegistryKey(key);
        if (Level.OVERWORLD.equals(registryKey)) {
            return FutureUtil.completedWithException(new IllegalArgumentException("The default world cannot be told to load!"));
        }

        final ServerLevel serverWorld = this.worlds.get(registryKey);
        if (serverWorld != null) {
            return CompletableFuture.completedFuture((org.spongepowered.api.world.server.ServerWorld) serverWorld);
        }

        this.saveTemplate(template);

        return this.loadWorld0(registryKey, ((SpongeWorldTemplate) template).asLevelStem(), ((WorldGenSettings) template.generationConfig()));
    }

    @Override
    public CompletableFuture<org.spongepowered.api.world.server.ServerWorld> loadWorld(final ResourceKey key) {
        final net.minecraft.resources.ResourceKey<Level> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(key, "key"));
        if (Level.OVERWORLD.equals(registryKey)) {
            return FutureUtil.completedWithException(new IllegalArgumentException("The default world cannot be told to load!"));
        }

        final ServerLevel world = this.worlds.get(registryKey);
        if (world != null) {
            return CompletableFuture.completedFuture((org.spongepowered.api.world.server.ServerWorld) world);
        }

        return this.loadTemplate(key).thenCompose(r -> {
            WorldTemplate loadedTemplate = r.orElse(null);
            if (loadedTemplate == null) {
                final LevelStem scratch = BootstrapProperties.worldGenSettings.dimensions().get(SpongeWorldManager.createStemKey(key));
                if (scratch != null) {
                    ((ResourceKeyBridge) (Object) scratch).bridge$setKey(key);
                    loadedTemplate = new SpongeWorldTemplate(scratch);
                }

                if (loadedTemplate == null) {
                    return FutureUtil.completedWithException(new IOException(String.format("Failed to load a template for '%s'!", key)));
                }

                this.saveTemplate(loadedTemplate);
            }

            return this.loadWorld0(registryKey, ((SpongeWorldTemplate) loadedTemplate).asLevelStem(), ((WorldGenSettings) loadedTemplate.generationConfig()));
        });
    }

    private CompletableFuture<org.spongepowered.api.world.server.ServerWorld> loadWorld0(final net.minecraft.resources.ResourceKey<Level> registryKey,
            final LevelStem template, final WorldGenSettings generatorSettings) {
        final PrimaryLevelData defaultLevelData = (PrimaryLevelData) this.server.getWorldData();
        final LevelSettings defaultLevelSettings = ((PrimaryLevelDataAccessor) defaultLevelData).accessor$settings();
        final LevelStemBridge templateBridge = (LevelStemBridge) (Object) template;
        final ResourceKey worldKey = ((ResourceKeyBridge) templateBridge).bridge$getKey();

        final Holder<DimensionType> dimensionTypeHolder = template.typeHolder();
        final WorldType worldType = (WorldType) dimensionTypeHolder.value();
        final Optional<ResourceKey> worldTypeKey = RegistryTypes.WORLD_TYPE.get().findValueKey(worldType);

        MinecraftServerAccessor.accessor$LOGGER().info("Loading world '{}' ({})", worldKey, worldTypeKey.map(ResourceKey::toString).orElse("inline"));
        final String directoryName = this.getDirectoryName(worldKey);
        final LevelStorageSource.LevelStorageAccess storageSource;

        try {
            storageSource = this.createStorageSource(worldKey);
        } catch (final IOException e) {
            return FutureUtil.completedWithException(new RuntimeException(String.format("Failed to create level data for world '%s'!", worldKey), e));
        }

        PrimaryLevelData levelData;

        levelData = (PrimaryLevelData) storageSource.getDataTag((DynamicOps<Tag>) BootstrapProperties.worldSettingsAdapter, defaultLevelSettings.getDataPackConfig(), Lifecycle.stable());
        if (levelData == null) {
            final LevelSettings levelSettings;
            final WorldGenSettings generationSettings;

            if (this.server.isDemo()) {
                levelSettings = MinecraftServer.DEMO_SETTINGS;
                generationSettings = WorldGenSettings.demoSettings(BootstrapProperties.registries);
            } else {
                levelSettings = new LevelSettings(directoryName, (GameType) (Object) BootstrapProperties.gameMode.get(Sponge.game()),
                        templateBridge.bridge$hardcore().orElse(BootstrapProperties.hardcore), (Difficulty) (Object) BootstrapProperties.difficulty
                        .get(Sponge.game()), templateBridge.bridge$commands().orElse(BootstrapProperties.commands), new GameRules(),
                    defaultLevelData.getDataPackConfig());
                generationSettings = generatorSettings;
            }

            levelData = new PrimaryLevelData(levelSettings, generationSettings, Lifecycle.stable());
        }

        ((PrimaryLevelDataBridge) levelData).bridge$populateFromDimension(template);

        final InheritableConfigHandle<WorldConfig> configAdapter = SpongeGameConfigs.createWorld(worldTypeKey.orElse(null), worldKey);
        ((PrimaryLevelDataBridge) levelData).bridge$configAdapter(configAdapter);

        levelData.setModdedInfo(this.server.getServerModName(), this.server.getModdedStatus().shouldReportAsModified());
        final boolean isDebugGeneration = levelData.worldGenSettings().isDebug();
        final long seed = BiomeManager.obfuscateSeed(levelData.worldGenSettings().seed());

        final ChunkProgressListener chunkStatusListener = ((MinecraftServerAccessor) this.server).accessor$progressListenerFactory().create(11);

        final ServerLevel world = new ServerLevel(this.server, ((MinecraftServerAccessor) this.server).accessor$executor(), storageSource, levelData,
                registryKey, dimensionTypeHolder, chunkStatusListener, template.generator(), isDebugGeneration, seed, ImmutableList.of(), true);
        this.worlds.put(registryKey, world);

        this.prepareWorld(world, isDebugGeneration);
        ((MinecraftServerAccessor) this.server).invoker$forceDifficulty();
        return this.postWorldLoad(world, false).thenApply(w -> (org.spongepowered.api.world.server.ServerWorld) w);
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
            return CompletableFuture.completedFuture(true);
        } catch (final IOException e) {
            return FutureUtil.completedWithException(e);
        }
    }

    @Override
    public boolean templateExists(final ResourceKey key) {
        return Files.exists(this.getDataPackFile(Objects.requireNonNull(key, "key")));
    }

    @Override
    public CompletableFuture<Optional<WorldTemplate>> loadTemplate(final ResourceKey key) {
        final Path dataPackFile = this.getDataPackFile(Objects.requireNonNull(key, "key"));
        if (Files.exists(dataPackFile)) {
            try (final BufferedReader reader = Files.newBufferedReader(dataPackFile)) {
                final LevelStem template = SpongeWorldManager.stemFromJson(key, JsonParser.parseReader(reader));
                return CompletableFuture.completedFuture(Optional.of(((LevelStemBridge) (Object) template).bridge$asTemplate()));
            } catch (final IOException e) {
                return FutureUtil.completedWithException(e);
            }
        }

        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    public CompletableFuture<Boolean> saveTemplate(final WorldTemplate template) {
        final LevelStem scratch = ((SpongeWorldTemplate) Objects.requireNonNull(template, "template")).asLevelStem();
        try {
            final JsonElement element = SpongeWorldManager.stemToJson(scratch);
            this.writeTemplate(element, template.key());
        } catch (final Exception ex) {
            return FutureUtil.completedWithException(ex);
        }
        return CompletableFuture.completedFuture(true);
    }

    private void writeTemplate(final JsonElement element, final ResourceKey key) throws IOException {
        final Path dataPackFile = this.getDataPackFile(key);
        Files.createDirectories(dataPackFile.getParent());
        DataPackSerializer.writeFile(dataPackFile, element);
        DataPackSerializer.writePackMetadata("World", this.dimensionsDataPackDirectory.getParent());
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

        final WorldData levelData;
        try (final LevelStorageSource.LevelStorageAccess storageSource = this.createStorageSource(key)) {
            final PrimaryLevelData defaultLevelData = (PrimaryLevelData) this.server.getWorldData();
            final LevelSettings defaultLevelSettings = ((PrimaryLevelDataAccessor) defaultLevelData).accessor$settings();

            levelData = storageSource.getDataTag((DynamicOps<Tag>) BootstrapProperties.worldSettingsAdapter, defaultLevelSettings.getDataPackConfig(), Lifecycle.stable());
        } catch (final Exception ex) {
            return FutureUtil.completedWithException(ex);
        }

        if (levelData == null) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        return this.loadTemplate(key).thenCompose(template -> {
            if (template.isPresent()) {
                final LevelStem scratch = ((SpongeWorldTemplate) template.get()).asLevelStem();
                ((PrimaryLevelDataBridge) levelData).bridge$populateFromDimension(scratch);
            } else {
                ((ResourceKeyBridge) levelData).bridge$setKey(key);
            }

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

        try (final LevelStorageSource.LevelStorageAccess storageSource = this.createStorageSource(key)) {
            storageSource.saveDataTag(BootstrapProperties.registries, (WorldData) properties, null);
        } catch (final Exception ex) {
            return FutureUtil.completedWithException(ex);
        }

        // Properties doesn't have everything we need...namely the generator, load the template and set values we actually got
        return this.loadTemplate(key).thenCompose(r -> {
            final WorldTemplate template = r.orElse(null);
            if (template != null) {
                final LevelStem scratch = ((SpongeWorldTemplate) template).asLevelStem();
                ((LevelStemBridge) (Object) scratch).bridge$populateFromLevelData((PrimaryLevelData) properties);

                return this.saveTemplate(((LevelStemBridge) (Object) scratch).bridge$asTemplate());
            }

            return CompletableFuture.completedFuture(true);
        });
    }

    @Override
    public CompletableFuture<Boolean> copyWorld(final ResourceKey key, final ResourceKey copyKey) {
        final net.minecraft.resources.ResourceKey<Level> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(key, "key"));

        if (DefaultWorldKeys.DEFAULT.equals(copyKey)) {
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

        final boolean isDefaultWorld = DefaultWorldKeys.DEFAULT.equals(key);

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

            return FutureUtil.completedWithException(e);
        }

        if (loadedWorld != null) {
            loadedWorld.noSave = disableLevelSaving;
        }

        final Path configFile = this.getConfigFile(key);
        final Path copiedConfigFile = this.getConfigFile(copyKey);

        try {
            Files.createDirectories(copiedConfigFile.getParent());
            Files.copy(configFile, copiedConfigFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            return FutureUtil.completedWithException(e);
        }

        final JsonElement template;
        if (this.isVanillaWorld(key)) {
            final LevelStem stem = this.server.getWorldData().worldGenSettings().dimensions().get(SpongeWorldManager.createStemKey(key));
            template = SpongeWorldManager.stemToJson(stem);
        } else {
            try (final BufferedReader reader = Files.newBufferedReader(this.getDataPackFile(key))) {
                template = JsonParser.parseReader(reader);
            } catch (final IOException e) {
                return FutureUtil.completedWithException(e);
            }
        }

        final JsonObject spongeData = template.getAsJsonObject().getAsJsonObject("#sponge");
        spongeData.remove("unique_id");

        try {
            this.writeTemplate(template, copyKey);
        } catch (final IOException e) {
            return FutureUtil.completedWithException(e);
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

        final Path originalDirectory = this.getDirectory(key);
        final Path movedDirectory = this.getDirectory(movedKey);

        try {
            Files.createDirectories(movedDirectory);
            Files.move(originalDirectory, movedDirectory, StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            return FutureUtil.completedWithException(e);
        }

        final Path configFile = this.getConfigFile(key);
        final Path movedConfigFile = this.getConfigFile(movedKey);

        try {
            Files.createDirectories(movedConfigFile.getParent());
            Files.move(configFile, movedConfigFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            return FutureUtil.completedWithException(e);
        }

        if (this.isVanillaWorld(key)) {
            final LevelStem stem = this.server.getWorldData().worldGenSettings().dimensions().get(SpongeWorldManager.createStemKey(key));
            final JsonElement template = SpongeWorldManager.stemToJson(stem);

            try {
                this.writeTemplate(template, movedKey);
            } catch (final IOException e) {
                return FutureUtil.completedWithException(e);
            }
        } else {
            final Path dimensionTemplate = this.getDataPackFile(key);
            final Path movedDimensionTemplate = this.getDataPackFile(movedKey);

            try {
                Files.createDirectories(movedDimensionTemplate.getParent());
                Files.move(dimensionTemplate, movedDimensionTemplate, StandardCopyOption.REPLACE_EXISTING);
            } catch (final IOException e) {
                return FutureUtil.completedWithException(e);
            }
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

        final Path directory = this.getDirectory(key);
        if (Files.exists(directory)) {
            try {
                Files.walkFileTree(directory, DeleteFileVisitor.INSTANCE);
            } catch (final IOException e) {
                return FutureUtil.completedWithException(e);
            }
        }

        final Path configFile = this.getConfigFile(key);
        try {
            Files.deleteIfExists(configFile);
        } catch (final IOException e) {
            return FutureUtil.completedWithException(e);
        }

        final Path dimensionTemplate = this.getDataPackFile(key);
        try {
            Files.deleteIfExists(dimensionTemplate);
        } catch (final IOException e) {
            return FutureUtil.completedWithException(e);
        }

        return CompletableFuture.completedFuture(true);
    }

    private void unloadWorld0(final ServerLevel world) throws IOException {
        final net.minecraft.resources.ResourceKey<Level> registryKey = world.dimension();

        if (world.getPlayers(p -> true).size() != 0) {
            throw new IOException(String.format("World '%s' was told to unload but players remain.", registryKey.location()));
        }

        final Optional<ResourceKey> worldTypeKey = RegistryTypes.WORLD_TYPE.get().findValueKey((WorldType) world.dimensionType());
        SpongeCommon.logger().info("Unloading world '{}' ({})", registryKey.location(), worldTypeKey.map(ResourceKey::toString).orElse("inline"));

        final UnloadWorldEvent unloadWorldEvent = SpongeEventFactory.createUnloadWorldEvent(PhaseTracker.getCauseStackManager().currentCause(), (ServerWorld) world);
        SpongeCommon.post(unloadWorldEvent);

        final BlockPos spawnPoint = world.getSharedSpawnPos();
        world.getChunkSource().removeRegionTicket(SpongeWorldManager.SPAWN_CHUNKS, new ChunkPos(spawnPoint), 11, registryKey.location());

        ((PrimaryLevelDataBridge) world.getLevelData()).bridge$configAdapter().save();

        try {
            world.save(null, true, world.noSave);
            world.close();
            ((ServerLevelBridge) world).bridge$getLevelSave().close();
        } catch (final Exception ex) {
            throw new IOException(ex);
        }

        this.worlds.remove(registryKey);
    }

    public void loadLevel() {
        final PrimaryLevelData defaultLevelData = (PrimaryLevelData) this.server.getWorldData();
        final WorldGenSettings defaultGenerationSettings = defaultLevelData.worldGenSettings();
        final LevelSettings defaultLevelSettings = ((PrimaryLevelDataAccessor) defaultLevelData).accessor$settings();

        final net.minecraft.core.Registry<LevelStem> templates = defaultGenerationSettings.dimensions();

        final boolean multiworldEnabled = this.server.isSingleplayer() || this.server.isNetherEnabled();
        if (!multiworldEnabled) {
            SpongeCommon.logger().warn("The option 'allow-nether' has been set to 'false' in the server.properties. "
                    + "Multi-World support has been disabled and no worlds besides the default world will be loaded.");
        }

        for (final RegistryEntry<LevelStem> entry : ((Registry<LevelStem>) templates).streamEntries().collect(Collectors.toList())) {
            final ResourceKey worldKey = entry.key();
            final LevelStem template = entry.value();
            final LevelStemBridge templateBridge = (LevelStemBridge) (Object) template;
            ((ResourceKeyBridge) templateBridge).bridge$setKey(worldKey);

            final boolean isDefaultWorld = DefaultWorldKeys.DEFAULT.equals(worldKey);
            if (!isDefaultWorld && !multiworldEnabled) {
                continue;
            }

            final Holder<DimensionType> dimensionTypeHolder = template.typeHolder();
            final WorldType worldType = (WorldType) dimensionTypeHolder.value();
            final Optional<ResourceKey> worldTypeKey = RegistryTypes.WORLD_TYPE.get().findValueKey(worldType);

            MinecraftServerAccessor.accessor$LOGGER().info("Loading world '{}' ({})", worldKey, worldTypeKey.map(ResourceKey::toString).orElse("inline"));
            if (!isDefaultWorld && !templateBridge.bridge$loadOnStartup()) {
                SpongeCommon.logger().warn("World '{}' has been disabled from loading at startup. Skipping...", worldKey);
                continue;
            }

            final String directoryName = this.getDirectoryName(worldKey);
            final LevelStorageSource.LevelStorageAccess storageSource;

            if (isDefaultWorld) {
                storageSource = ((MinecraftServerAccessor) this.server).accessor$storageSource();
            } else {
                try {
                    storageSource = this.createStorageSource(worldKey);
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
                        .getDataTag((DynamicOps<Tag>) BootstrapProperties.worldSettingsAdapter, defaultLevelSettings.getDataPackConfig(), Lifecycle.stable());
                if (levelData == null) {
                    final LevelSettings levelSettings;
                    final WorldGenSettings generationSettings;

                    if (this.server.isDemo()) {
                        levelSettings = MinecraftServer.DEMO_SETTINGS;
                        generationSettings = WorldGenSettings.demoSettings(BootstrapProperties.registries);
                    } else {
                        levelSettings = new LevelSettings(directoryName,
                                (GameType) (Object) BootstrapProperties.gameMode.get(Sponge.game()),
                                templateBridge.bridge$hardcore().orElse(BootstrapProperties.hardcore),
                                (Difficulty) (Object) BootstrapProperties.difficulty.get(Sponge.game()),
                                templateBridge.bridge$commands().orElse(BootstrapProperties.commands), new GameRules(), defaultLevelData.getDataPackConfig());
                        generationSettings = ((WorldGenSettingsBridge) defaultLevelData.worldGenSettings()).bridge$copy();
                    }

                    isDebugGeneration = generationSettings.isDebug();

                    ((DimensionGeneratorSettingsAccessor) generationSettings).accessor$dimensions(new MappedRegistry<>(
                            net.minecraft.core.Registry.LEVEL_STEM_REGISTRY, Lifecycle.stable(), null));

                    levelData = new PrimaryLevelData(levelSettings, generationSettings, Lifecycle.stable());
                } else {
                    isDebugGeneration = levelData.worldGenSettings().isDebug();
                }
            }

            ((PrimaryLevelDataBridge) levelData).bridge$populateFromDimension(template);

            final InheritableConfigHandle<WorldConfig> configAdapter = SpongeGameConfigs.createWorld(worldTypeKey.orElse(null), worldKey);
            ((PrimaryLevelDataBridge) levelData).bridge$configAdapter(configAdapter);

            levelData.setModdedInfo(this.server.getServerModName(), this.server.getModdedStatus().shouldReportAsModified());
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
                    registryKey, dimensionTypeHolder, chunkStatusListener, template.generator(), isDebugGeneration, seed, spawners, true);
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
        ((SpongeServer) SpongeCommon.server()).getPlayerDataManager().load();
    }

    private void prepareWorld(final ServerLevel world, final boolean isDebugGeneration) {
        final boolean isDefaultWorld = Level.OVERWORLD.equals(world.dimension());
        final PrimaryLevelData levelData = (PrimaryLevelData) world.getLevelData();

        if (isDefaultWorld) {
            // Initialize scoreboard data. This will hook to the ServerScoreboard, needs to be made multi-world aware
            ((MinecraftServerAccessor) this.server).accessor$readScoreboard(world.getDataStorage());

            ((MinecraftServerAccessor) this.server).accessor$commandStorage(new CommandStorage(world.getDataStorage()));
        }

        final boolean isInitialized = levelData.isInitialized();

        final LoadWorldEvent loadWorldEvent = SpongeEventFactory.createLoadWorldEvent(PhaseTracker.getCauseStackManager().currentCause(), (ServerWorld) world, isInitialized);
        SpongeCommon.post(loadWorldEvent);
        PlatformHooks.INSTANCE.getWorldHooks().postLoadWorld(world);

        // Set the view distance back on it's self to trigger the logic
        ((PrimaryLevelDataBridge) world.getLevelData()).bridge$viewDistance().ifPresent(v -> ((PrimaryLevelDataBridge) world.getLevelData()).bridge$setViewDistance(v));

        world.getWorldBorder().applySettings(levelData.getWorldBorder());

        if (!isInitialized) {
            try {
                final boolean hasSpawnAlready = ((PrimaryLevelDataBridge) world.getLevelData()).bridge$customSpawnPosition();
                if (!hasSpawnAlready) {
                    if (isDefaultWorld || ((ServerWorldProperties) world.getLevelData()).performsSpawnLogic()) {
                        MinecraftServerAccessor.invoker$setInitialSpawn(world, levelData, levelData.worldGenSettings().generateBonusChest(), !isDebugGeneration);
                    } else if (Level.END.equals(world.dimension())) {
                        ((PrimaryLevelData) world.getLevelData()).setSpawn(ServerLevel.END_SPAWN_POINT, 0);
                    }
                } else if (levelData.worldGenSettings().generateBonusChest()) {
                    MiscOverworldFeatures.BONUS_CHEST.value().place(world, world.getChunkSource().getGenerator(), world.random, new BlockPos(levelData.getXSpawn(),
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
        this.server.getPlayerList().addWorldborderListener(world);

        if (levelData.getCustomBossEvents() != null) {
            ((ServerLevelBridge) world).bridge$getBossBarManager().load(levelData.getCustomBossEvents());
        }
    }

    private CompletableFuture<ServerLevel> postWorldLoad(final ServerLevel world, final boolean blocking) {
        final PrimaryLevelData levelData = (PrimaryLevelData) world.getLevelData();
        final PrimaryLevelDataBridge levelBridge = (PrimaryLevelDataBridge) levelData;
        if (Level.OVERWORLD.equals(world.dimension()) || levelBridge.bridge$performsSpawnLogic()) {
            final Optional<ResourceKey> worldTypeKey = RegistryTypes.WORLD_TYPE.get().findValueKey((WorldType) world.dimensionType());
            MinecraftServerAccessor.accessor$LOGGER().info("Preparing start region for world '{}' ({})", world.dimension().location(),
                    worldTypeKey.map(ResourceKey::toString).orElse("inline"));
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

        serverChunkProvider.addRegionTicket(SpongeWorldManager.SPAWN_CHUNKS, chunkPos, borderRadius, world.dimension().location());
        final CompletableFuture<ServerLevel> generationFuture = new CompletableFuture<>();
        Sponge.asyncScheduler().submit(
                Task.builder().plugin(Launch.instance().platformPlugin())
                        .execute(task -> {
                            if (serverChunkProvider.getTickingGenerated() >= spawnChunks) {
                                Sponge.server().scheduler().submit(Task.builder().plugin(Launch.instance().platformPlugin()).execute(() -> generationFuture.complete(world)).build());
                                // Notify the future that we are done
                                task.cancel(); // And cancel this task
                                final Optional<ResourceKey> worldTypeKey = RegistryTypes.WORLD_TYPE.get().findValueKey((WorldType) world.dimensionType());
                                MinecraftServerAccessor.accessor$LOGGER().info("Done preparing start region for world '{}' ({})", world
                                        .dimension().location(), worldTypeKey.map(ResourceKey::toString).orElse("inline"));
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
                serverChunkProvider.removeRegionTicket(SpongeWorldManager.SPAWN_CHUNKS, chunkPos, 11, world.dimension().location());
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
        serverChunkProvider.addRegionTicket(SpongeWorldManager.SPAWN_CHUNKS, chunkPos, 11, world.dimension().location());

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
            serverChunkProvider.removeRegionTicket(SpongeWorldManager.SPAWN_CHUNKS, chunkPos, 11, world.dimension().location());
        }
    }

    private void updateForcedChunks(final ServerLevel world, final ServerChunkCache serverChunkProvider) {
        final ForcedChunksSavedData forcedChunksSaveData = world.getDataStorage().get(ForcedChunksSavedData::load, "chunks");
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
        return net.minecraft.resources.ResourceKey.create(net.minecraft.core.Registry.DIMENSION_REGISTRY, (ResourceLocation) (Object) key);
    }

    public static net.minecraft.resources.ResourceKey<LevelStem> createStemKey(final ResourceKey key) {
        return net.minecraft.resources.ResourceKey.create(net.minecraft.core.Registry.LEVEL_STEM_REGISTRY, (ResourceLocation) (Object) key);
    }

    private static LevelStem stemFromJson(final ResourceKey key, final JsonElement element) throws IOException {
        final LevelStem stem = LevelStem.CODEC.parse(
                new Dynamic<>(RegistryOps.create(JsonOps.INSTANCE, BootstrapProperties.registries), element))
                .getOrThrow(true, s -> {});
        ((LevelStemBridge) (Object) stem).bridge$setFromSettings(false);
        ((ResourceKeyBridge) (Object) stem).bridge$setKey(key);
        return stem;
    }

    private static JsonElement stemToJson(final LevelStem stem) {
        return LevelStem.CODEC.encodeStart(
                RegistryOps.create(JsonOps.INSTANCE, BootstrapProperties.registries),
                stem).getOrThrow(true, s -> {});
    }

    private LevelStorageSource.LevelStorageAccess createStorageSource(final ResourceKey key) throws IOException {
        if (DefaultWorldKeys.DEFAULT.equals(key)) {
            LevelStorageSource.createDefault(this.defaultWorldDirectory.getParent()).createAccess(this.defaultWorldDirectory.getFileName().toString());
        }
        if (DefaultWorldKeys.THE_NETHER.equals(key)) {
            return LevelStorageSource.createDefault(this.defaultWorldDirectory).createAccess("DIM-1");
        }
        if (DefaultWorldKeys.THE_END.equals(key)) {
            return LevelStorageSource.createDefault(this.defaultWorldDirectory).createAccess("DIM1");
        }
        return LevelStorageSource.createDefault(this.customWorldsDirectory).createAccess(key.namespace() + File.separator + key.value());
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

    private Path getDataPackFile(final ResourceKey key) {
        return this.getDimensionDataPackDirectory().resolve(key.namespace()).resolve("dimension").resolve(key.value() + ".json");
    }

    private Path getConfigFile(final ResourceKey key) {
        return SpongeCommon.spongeConfigDirectory().resolve(Launch.instance().id()).resolve("worlds").resolve(key.namespace())
                .resolve(key.value() + ".conf");
    }
}
