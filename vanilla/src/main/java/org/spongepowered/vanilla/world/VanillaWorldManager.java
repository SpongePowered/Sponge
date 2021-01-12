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
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.nbt.INBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.util.registry.WorldGenSettingsExport;
import net.minecraft.util.registry.WorldSettingsImport;
import net.minecraft.village.VillageSiege;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.ForcedChunksSaveData;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.gen.feature.Features;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;
import net.minecraft.world.spawner.CatSpawner;
import net.minecraft.world.spawner.ISpecialSpawner;
import net.minecraft.world.spawner.PatrolSpawner;
import net.minecraft.world.spawner.PhantomSpawner;
import net.minecraft.world.spawner.WanderingTraderSpawner;
import net.minecraft.world.storage.CommandStorage;
import net.minecraft.world.storage.FolderName;
import net.minecraft.world.storage.IServerConfiguration;
import net.minecraft.world.storage.SaveFormat;
import net.minecraft.world.storage.ServerWorldInfo;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.WorldType;
import org.spongepowered.api.world.server.WorldTemplate;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeServer;
import org.spongepowered.common.accessor.server.MinecraftServerAccessor;
import org.spongepowered.common.accessor.world.gen.DimensionGeneratorSettingsAccessor;
import org.spongepowered.common.accessor.world.storage.SaveFormat_LevelSaveAccessor;
import org.spongepowered.common.accessor.world.storage.ServerWorldInfoAccessor;
import org.spongepowered.common.bridge.ResourceKeyBridge;
import org.spongepowered.common.bridge.world.DimensionBridge;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.bridge.world.gen.DimensionGeneratorSettingsBridge;
import org.spongepowered.common.bridge.world.storage.ServerWorldInfoBridge;
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

public final class VanillaWorldManager implements SpongeWorldManager {

    private final MinecraftServer server;
    private final Path dimensionsDataPackDirectory, defaultWorldDirectory, customWorldsDirectory;
    private final Map<RegistryKey<World>, ServerWorld> worlds;

    private static final TicketType<ResourceLocation> SPAWN_CHUNKS = TicketType.create("spawn_chunks", (i, o) -> i.compareTo(o));

    public VanillaWorldManager(final MinecraftServer server) {
        this.server = server;
        this.dimensionsDataPackDirectory = ((MinecraftServerAccessor) server).accessor$storageSource().getLevelPath(FolderName.DATAPACK_DIR).resolve("plugin_dimension").resolve("data");
        try {
            Files.createDirectories(this.dimensionsDataPackDirectory);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        this.defaultWorldDirectory = ((SaveFormat_LevelSaveAccessor) ((MinecraftServerAccessor) this.server).accessor$storageSource()).accessor$levelPath();
        this.customWorldsDirectory = this.defaultWorldDirectory.resolve("dimensions");
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
        final ServerWorld world = this.server.overworld();
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
    public Collection<org.spongepowered.api.world.server.ServerWorld> worlds() {
        return Collections.unmodifiableCollection((Collection<org.spongepowered.api.world.server.ServerWorld>) (Object) this.worlds.values());
    }

    @Override
    public List<ResourceKey> worldKeys() {
        final List<ResourceKey> worldKeys = new ArrayList<>();
        worldKeys.add((ResourceKey) (Object) Dimension.OVERWORLD.location());
        worldKeys.add((ResourceKey) (Object) Dimension.NETHER.location());
        worldKeys.add((ResourceKey) (Object) Dimension.END.location());

        // TODO May be wise to consider looking at other data packs to grab their keys as well
        try {
            for (final Path namespacedDirectory : Files.walk(this.dimensionsDataPackDirectory, 1).collect(Collectors.toList())) {
                final Path dimensionDirectory = namespacedDirectory.resolve("dimension");
                if (Files.exists(dimensionDirectory)) {
                    for (final Path valueDirectory : Files.walk(dimensionDirectory, 1).collect(Collectors.toList())) {
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
        final RegistryKey<World> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(key, "key"));

        if (World.OVERWORLD.equals(registryKey)) {
            return true;
        }

        if (this.worlds.get(registryKey) != null) {
            return true;
        }

        final boolean isVanillaSubLevel = World.NETHER.equals(registryKey) || World.END.equals(registryKey);
        final Path levelDirectory = isVanillaSubLevel ? this.defaultWorldDirectory.resolve(this.getDirectoryName(key)) :
                this.customWorldsDirectory.resolve(key.getNamespace()).resolve(key.getValue());
        return Files.exists(levelDirectory);
    }

    @Override
    public Optional<ResourceKey> worldKey(final UUID uniqueId) {
        Objects.requireNonNull(uniqueId, "uniqueId");
        return this.worlds
                .values()
                .stream()
                .filter(w -> ((org.spongepowered.api.world.server.ServerWorld) w).getUniqueId().equals(uniqueId))
                .map(w -> (org.spongepowered.api.world.server.ServerWorld) w)
                .map(org.spongepowered.api.world.server.ServerWorld::getKey)
                .findAny();
    }

    @Override
    public CompletableFuture<org.spongepowered.api.world.server.ServerWorld> loadWorld(final WorldTemplate template) {
        final ResourceKey key = Objects.requireNonNull(template, "template").getKey();
        final RegistryKey<World> registryKey = SpongeWorldManager.createRegistryKey(key);
        if (World.OVERWORLD.equals(registryKey)) {
            FutureUtil.completedWithException(new IllegalArgumentException("The default world cannot be told to load!"));
        }
        final ServerWorld serverWorld = this.worlds.get(registryKey);
        if (serverWorld != null) {
            return CompletableFuture.completedFuture((org.spongepowered.api.world.server.ServerWorld) serverWorld);
        }

        this.saveTemplate(template);

        return this.loadWorld0(registryKey, ((SpongeWorldTemplate) template).asDimension());
    }

    @Override
    public CompletableFuture<org.spongepowered.api.world.server.ServerWorld> loadWorld(final ResourceKey key) {
        final RegistryKey<World> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(key, "key"));

        if (World.OVERWORLD.equals(registryKey)) {
            FutureUtil.completedWithException(new IllegalArgumentException("The default world cannot be told to load!"));
        }

        final ServerWorld world = this.worlds.get(registryKey);
        if (world != null) {
            return CompletableFuture.completedFuture((org.spongepowered.api.world.server.ServerWorld) world);
        }

        return this.loadTemplate(key).thenCompose(r -> {
            WorldTemplate loadedTemplate = r.orElse(null);
            if (loadedTemplate == null) {
                final Dimension scratch = BootstrapProperties.dimensionGeneratorSettings.dimensions().get(RegistryKey.create(
                    net.minecraft.util.registry.Registry.LEVEL_STEM_REGISTRY, (ResourceLocation) (Object) key));
                if (scratch != null) {
                    ((ResourceKeyBridge) (Object) scratch).bridge$setKey(key);
                    loadedTemplate = new SpongeWorldTemplate(scratch);
                }

                if (loadedTemplate == null) {
                    return CompletableFuture.completedFuture(null);
                }

                this.saveTemplate(loadedTemplate);
            }

            return this.loadWorld0(registryKey, ((SpongeWorldTemplate) loadedTemplate).asDimension());
        });
    }

    private CompletableFuture<org.spongepowered.api.world.server.ServerWorld> loadWorld0(final RegistryKey<World> registryKey,
            final Dimension template) {
        final ServerWorldInfo defaultLevelData = (ServerWorldInfo) this.server.getWorldData();
        final WorldSettings defaultLevelSettings = ((ServerWorldInfoAccessor) defaultLevelData).accessor$settings();
        final DimensionBridge templateBridge = (DimensionBridge) (Object) template;
        final ResourceKey worldKey = ((ResourceKeyBridge) templateBridge).bridge$getKey();

        final WorldType worldType = (WorldType) template.type();
        final ResourceKey worldTypeKey = RegistryTypes.WORLD_TYPE.get().valueKey((WorldType) template.type());

        MinecraftServerAccessor.accessor$LOGGER().info("Loading World '{}' ({})", worldKey, worldTypeKey);
        final String directoryName = this.getDirectoryName(worldKey);
        final boolean isVanillaSubLevel = this.isVanillaSubWorld(directoryName);
        final SaveFormat.LevelSave storageSource;

        try {
            if (isVanillaSubLevel) {
                storageSource = SaveFormat.createDefault(this.defaultWorldDirectory).createAccess(directoryName);
            } else {
                storageSource = SaveFormat.createDefault(this.customWorldsDirectory).createAccess(worldKey.getNamespace() + File.separator + worldKey.getValue());
            }
        } catch (final IOException e) {
            e.printStackTrace();
            return FutureUtil.completedWithException(new RuntimeException(String.format("Failed to create level data for world '%s'!", worldKey), e));
        }

        ServerWorldInfo levelData;

        levelData = (ServerWorldInfo) storageSource.getDataTag((DynamicOps<INBT>) BootstrapProperties.worldSettingsAdapter, defaultLevelSettings.getDataPackConfig());
        if (levelData == null) {
            final WorldSettings levelSettings;
            final DimensionGeneratorSettings generationSettings;

            if (this.server.isDemo()) {
                levelSettings = MinecraftServer.DEMO_SETTINGS;
                generationSettings = DimensionGeneratorSettings.demoSettings(BootstrapProperties.registries);
            } else {
                levelSettings = new WorldSettings(directoryName, (GameType) (Object) BootstrapProperties.gameMode.get(Sponge.getGame().registries()),
                        templateBridge.bridge$hardcore().orElse(BootstrapProperties.hardcore), (Difficulty) (Object) BootstrapProperties.difficulty
                        .get(Sponge.getGame().registries()), templateBridge.bridge$commands().orElse(BootstrapProperties.commands), new GameRules(),
                    defaultLevelData.getDataPackConfig());
                generationSettings = ((DimensionGeneratorSettingsBridge) defaultLevelData.worldGenSettings()).bridge$copy();
            }

            levelData = new ServerWorldInfo(levelSettings, generationSettings, Lifecycle.stable());
        }

        ((ServerWorldInfoBridge) levelData).bridge$populateFromDimension(template);

        final InheritableConfigHandle<WorldConfig> configAdapter = SpongeGameConfigs.createWorld(worldTypeKey, worldKey);
        ((ServerWorldInfoBridge) levelData).bridge$configAdapter(configAdapter);

        levelData.setModdedInfo(this.server.getServerModName(), this.server.getModdedStatus().isPresent());
        final boolean isDebugGeneration = levelData.worldGenSettings().isDebug();
        final long seed = BiomeManager.obfuscateSeed(levelData.worldGenSettings().seed());

        final IChunkStatusListener chunkStatusListener = ((MinecraftServerAccessor) this.server).accessor$getProgressListenerFactory().create(11);

        final ServerWorld world = new ServerWorld(this.server, ((MinecraftServerAccessor) this.server).accessor$executor(), storageSource, levelData,
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
        final RegistryKey<World> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(key, "key"));

        if (World.OVERWORLD.equals(registryKey)) {
            return CompletableFuture.completedFuture(false);
        }

        final ServerWorld world = this.worlds.get(registryKey);
        if (world == null) {
            return CompletableFuture.completedFuture(false);
        }

        return this.unloadWorld((org.spongepowered.api.world.server.ServerWorld) world);
    }

    @Override
    public CompletableFuture<Boolean> unloadWorld(final org.spongepowered.api.world.server.ServerWorld world) {
        final RegistryKey<World> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(world, "world").getKey());

        if (World.OVERWORLD.equals(registryKey)) {
            return CompletableFuture.completedFuture(false);
        }

        if (world != this.worlds.get(registryKey)) {
            return CompletableFuture.completedFuture(false);
        }

        try {
            this.unloadWorld0((ServerWorld) world);
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
                final Dimension template = this.loadTemplate0(SpongeWorldManager.createRegistryKey(key), dataPackFile);
                ((ResourceKeyBridge) (Object) template).bridge$setKey(key);
                return CompletableFuture.completedFuture(Optional.of(((DimensionBridge) (Object) template).bridge$asTemplate()));
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    public CompletableFuture<Boolean> saveTemplate(final WorldTemplate template) {
        final Dimension scratch = ((SpongeWorldTemplate) Objects.requireNonNull(template, "template")).asDimension();
        try {
            final JsonElement element = SpongeWorldTemplate.DIRECT_CODEC.encodeStart(WorldGenSettingsExport.create(JsonOps.INSTANCE, BootstrapProperties.registries), scratch).getOrThrow(true, s -> { });
            final Path dataPackFile = this.getDataPackFile(template.getKey());
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
        final RegistryKey<World> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(key, "key"));

        if (this.worlds.get(registryKey) != null) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        if (!this.worldExists(key)) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        final boolean isVanillaWorld = this.isVanillaWorld(key);
        final String directoryName = this.getDirectoryName(key);
        final SaveFormat.LevelSave storageSource;

        try {
            if (isVanillaWorld) {
                storageSource = SaveFormat.createDefault(this.defaultWorldDirectory).createAccess(directoryName);
            } else {
                storageSource = SaveFormat.createDefault(this.customWorldsDirectory).createAccess(key.getNamespace() + File.separator +
                        key.getValue());
            }
        } catch (final IOException e) {
            return FutureUtil.completedWithException(e);
        }

        final ServerWorldInfo defaultLevelData = (ServerWorldInfo) this.server.getWorldData();
        final WorldSettings defaultLevelSettings = ((ServerWorldInfoAccessor) defaultLevelData).accessor$settings();

        final IServerConfiguration levelData;
        try {
            levelData = storageSource.getDataTag((DynamicOps<INBT>) BootstrapProperties.worldSettingsAdapter, defaultLevelSettings.getDataPackConfig());
        } catch (final Exception ex) {
            return FutureUtil.completedWithException(ex);
        }

        return this.loadTemplate(key).thenCompose(r -> {
            r.ifPresent(template -> {
                final Dimension scratch = ((SpongeWorldTemplate) template).asDimension();
                ((ServerWorldInfoBridge) levelData).bridge$populateFromDimension(scratch);
            });

            return CompletableFuture.completedFuture(Optional.of((ServerWorldProperties) levelData));
        });
    }

    @Override
    public CompletableFuture<Boolean> saveProperties(final ServerWorldProperties properties) {
        final RegistryKey<World> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(properties, "properties").getKey());

        if (this.worlds.get(registryKey) != null) {
            return CompletableFuture.completedFuture(false);
        }

        final ResourceKey key = properties.getKey();

        final boolean isVanillaWorld = this.isVanillaWorld(key);
        final String directoryName = this.getDirectoryName(key);
        final SaveFormat.LevelSave storageSource;

        try {
            if (isVanillaWorld) {
                storageSource = SaveFormat.createDefault(this.defaultWorldDirectory).createAccess(directoryName);
            } else {
                storageSource = SaveFormat.createDefault(this.customWorldsDirectory).createAccess(key.getNamespace() + File.separator +
                        key.getValue());
            }
        } catch (final IOException e) {
            return FutureUtil.completedWithException(e);
        }

        try {
            storageSource.saveDataTag(BootstrapProperties.registries, (IServerConfiguration) properties, null);
        } catch (final Exception ex) {
            return FutureUtil.completedWithException(ex);
        }

        // Properties doesn't have everything we need...namely the generator, load the template and set values we actually got
        return this.loadTemplate(key).thenCompose(r -> {
            final WorldTemplate template = r.orElse(null);
            if (template != null) {
                final Dimension scratch = ((SpongeWorldTemplate) template).asDimension();
                ((DimensionBridge) (Object) scratch).bridge$populateFromLevelData((ServerWorldInfo) properties);

                return this.saveTemplate(((DimensionBridge) (Object) scratch).bridge$asTemplate());
            }

            return CompletableFuture.completedFuture(true);
        });
    }

    @Override
    public CompletableFuture<Boolean> copyWorld(final ResourceKey key, final ResourceKey copyKey) {
        final RegistryKey<World> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(key, "key"));
        final RegistryKey<World> copyRegistryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(copyKey, "copyKey"));

        if (World.OVERWORLD.equals(copyRegistryKey)) {
            return CompletableFuture.completedFuture(false);
        }

        if (!this.worldExists(key)) {
            return CompletableFuture.completedFuture(false);
        }

        if (this.worldExists(copyKey)) {
            return CompletableFuture.completedFuture(false);
        }

        final ServerWorld loadedWorld = this.worlds.get(registryKey);
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
                .resolve(directoryName) : this.customWorldsDirectory.resolve(key.getNamespace()).resolve(key.getValue());

        final boolean isVanillaCopyWorld = this.isVanillaWorld(copyKey);
        final String copyDirectoryName = this.getDirectoryName(copyKey);

        final Path copyDirectory = isVanillaCopyWorld ? this.defaultWorldDirectory
                .resolve(copyDirectoryName) : this.customWorldsDirectory.resolve(key.getNamespace()).resolve(key.getValue());

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
                writer.write(fixedObject.getAsString());
            } catch (final IOException e) {
                FutureUtil.completedWithException(e);
            }
        }

        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> moveWorld(final ResourceKey key, final ResourceKey movedKey) {
        final RegistryKey<World> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(key, "key"));

        if (World.OVERWORLD.equals(registryKey)) {
            return CompletableFuture.completedFuture(false);
        }

        if (!this.worldExists(key)) {
            return CompletableFuture.completedFuture(false);
        }

        if (this.worldExists(movedKey)) {
            return CompletableFuture.completedFuture(false);
        }

        final ServerWorld loadedWorld = this.worlds.get(registryKey);
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
                .resolve(directoryName) : this.customWorldsDirectory.resolve(key.getNamespace()).resolve(key.getValue());

        final boolean isVanillaMoveWorld = this.isVanillaWorld(movedKey);
        final String moveDirectoryName = this.getDirectoryName(movedKey);

        final Path moveDirectory = isVanillaMoveWorld ? this.defaultWorldDirectory
                .resolve(moveDirectoryName) : this.customWorldsDirectory.resolve(key.getNamespace()).resolve(key.getValue());

        try {
            Files.createDirectories(moveDirectory);
            Files.move(originalDirectory, moveDirectory, StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            return FutureUtil.completedWithException(e);
        }

        final Path configFile = SpongeCommon.getSpongeConfigDirectory().resolve(SpongeCommon.ECOSYSTEM_ID).resolve("worlds").resolve(key
                .getNamespace()).resolve(key.getValue() + ".conf");

        final Path copiedConfigFile = SpongeCommon.getSpongeConfigDirectory().resolve(SpongeCommon.ECOSYSTEM_ID).resolve("worlds")
                .resolve(movedKey.getNamespace()).resolve(movedKey.getValue() + ".conf");

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
        final RegistryKey<World> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(key, "key"));

        if (World.OVERWORLD.equals(registryKey)) {
            return CompletableFuture.completedFuture(false);
        }

        if (!this.worldExists(key)) {
            return CompletableFuture.completedFuture(false);
        }

        final ServerWorld loadedWorld = this.worlds.get(registryKey);
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

        final Path directory = isVanillaWorld ? this.defaultWorldDirectory.resolve(directoryName) : this.customWorldsDirectory.resolve(key.getNamespace()).resolve(key.getValue());

        if (Files.exists(directory)) {
            try {
                for (final Path path : Files.walk(directory).sorted(Comparator.reverseOrder()).collect(Collectors.toList())) {
                    Files.deleteIfExists(path);
                }
            } catch (final IOException e) {
                return FutureUtil.completedWithException(e);
            }
        }

        final Path configFile = SpongeCommon.getSpongeConfigDirectory().resolve(SpongeCommon.ECOSYSTEM_ID).resolve("worlds").resolve(key.getNamespace()).resolve(key.getValue() + ".conf");

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
    public void unloadWorld0(final ServerWorld world) throws IOException {
        final RegistryKey<World> registryKey = world.dimension();

        if (world.getPlayers(p -> true).size() != 0) {
            throw new IOException(String.format("World '%s' was told to unload but players remain.", registryKey.location()));
        }

        SpongeCommon.getLogger().info("Unloading World '{}' ({})", registryKey.location(), RegistryTypes.WORLD_TYPE.get().valueKey((WorldType) world.dimensionType()));

        final BlockPos spawnPoint = world.getSharedSpawnPos();
        world.getChunkSource().removeRegionTicket(VanillaWorldManager.SPAWN_CHUNKS, new ChunkPos(spawnPoint), 11, registryKey.location());

        ((ServerWorldInfoBridge) world.getLevelData()).bridge$configAdapter().save();
        ((ServerWorldBridge) world).bridge$setManualSave(true);

        try {
            world.save(null, true, world.noSave);
            world.close();
            ((ServerWorldBridge) world).bridge$getLevelSave().close();
        } catch (final Exception ex) {
            throw new IOException(ex);
        }

        this.worlds.remove(registryKey);

        SpongeCommon.postEvent(SpongeEventFactory.createUnloadWorldEvent(PhaseTracker.getCauseStackManager().getCurrentCause(), (org.spongepowered.api.world.server.ServerWorld) world));
    }

    @Override
    public void loadLevel() {
        final ServerWorldInfo defaultLevelData = (ServerWorldInfo) this.server.getWorldData();
        final DimensionGeneratorSettings defaultGenerationSettings = defaultLevelData.worldGenSettings();
        final WorldSettings defaultLevelSettings = ((ServerWorldInfoAccessor) defaultLevelData).accessor$settings();

        final SimpleRegistry<Dimension> templates = defaultGenerationSettings.dimensions();

        final boolean multiworldEnabled = this.server.isSingleplayer() || this.server.isNetherEnabled();
        if (!multiworldEnabled) {
            SpongeCommon.getLogger().warn("The option 'allow-nether' has been set to 'false' in the server.properties. "
                    + "Multi-World support has been disabled and no worlds besides the default world will be loaded.");
        }

        for (final RegistryEntry<Dimension> entry : ((Registry<Dimension>) (Object) templates).streamEntries().collect(Collectors.toList())) {
            final ResourceKey worldKey = entry.key();
            final Dimension template = entry.value();
            final DimensionBridge templateBridge = (DimensionBridge) (Object) template;
            ((ResourceKeyBridge) templateBridge).bridge$setKey(worldKey);

            final boolean isDefaultWorld = this.isDefaultWorld(worldKey);
            if (!isDefaultWorld && !multiworldEnabled) {
                continue;
            }

            final WorldType worldType = (WorldType) template.type();
            final ResourceKey worldTypeKey = RegistryTypes.WORLD_TYPE.get().valueKey((WorldType) template.type());

            MinecraftServerAccessor.accessor$LOGGER().info("Loading World '{}' ({})", worldKey, worldTypeKey);
            if (!isDefaultWorld && !templateBridge.bridge$loadOnStartup()) {
                SpongeCommon.getLogger().warn("World '{}' has been disabled from loading at startup. Skipping...", worldKey);
                continue;
            }

            final String directoryName = this.getDirectoryName(worldKey);
            final boolean isVanillaSubLevel = this.isVanillaSubWorld(directoryName);
            final SaveFormat.LevelSave storageSource;

            if (isDefaultWorld) {
                storageSource = ((MinecraftServerAccessor) this.server).accessor$storageSource();
            } else {
                try {
                    if (isVanillaSubLevel) {
                        storageSource = SaveFormat.createDefault(this.defaultWorldDirectory).createAccess(directoryName);
                    } else {
                        storageSource = SaveFormat.createDefault(this.customWorldsDirectory).createAccess(worldKey.getNamespace() + File.separator + worldKey.getValue());
                    }
                } catch (final IOException e) {
                    throw new RuntimeException(String.format("Failed to create level data for world '%s'!", worldKey), e);
                }
            }

            ServerWorldInfo levelData;
            final boolean isDebugGeneration;

            if (isDefaultWorld) {
                levelData = defaultLevelData;
                isDebugGeneration = defaultGenerationSettings.isDebug();
            } else {
                levelData = (ServerWorldInfo) storageSource
                        .getDataTag((DynamicOps<INBT>) BootstrapProperties.worldSettingsAdapter, defaultLevelSettings.getDataPackConfig());
                if (levelData == null) {
                    final WorldSettings levelSettings;
                    final DimensionGeneratorSettings generationSettings;

                    if (this.server.isDemo()) {
                        levelSettings = MinecraftServer.DEMO_SETTINGS;
                        generationSettings = DimensionGeneratorSettings.demoSettings(BootstrapProperties.registries);
                    } else {
                        levelSettings = new WorldSettings(directoryName,
                                (GameType) (Object) BootstrapProperties.gameMode.get(Sponge.getGame().registries()),
                                templateBridge.bridge$hardcore().orElse(BootstrapProperties.hardcore),
                                (Difficulty) (Object) BootstrapProperties.difficulty.get(Sponge.getGame().registries()),
                                templateBridge.bridge$commands().orElse(BootstrapProperties.commands), new GameRules(), defaultLevelData.getDataPackConfig());
                        generationSettings = ((DimensionGeneratorSettingsBridge) defaultLevelData.worldGenSettings()).bridge$copy();
                    }

                    isDebugGeneration = generationSettings.isDebug();

                    ((DimensionGeneratorSettingsAccessor) generationSettings).accessor$dimensions(new SimpleRegistry<>(
                            net.minecraft.util.registry.Registry.LEVEL_STEM_REGISTRY, Lifecycle.stable()));

                    levelData = new ServerWorldInfo(levelSettings, generationSettings, Lifecycle.stable());
                } else {
                    isDebugGeneration = levelData.worldGenSettings().isDebug();
                }
            }

            ((ServerWorldInfoBridge) levelData).bridge$populateFromDimension(template);

            final InheritableConfigHandle<WorldConfig> configAdapter = SpongeGameConfigs.createWorld(worldTypeKey, worldKey);
            ((ServerWorldInfoBridge) levelData).bridge$configAdapter(configAdapter);

            levelData.setModdedInfo(this.server.getServerModName(), this.server.getModdedStatus().isPresent());
            final long seed = BiomeManager.obfuscateSeed(levelData.worldGenSettings().seed());

            final RegistryKey<World> registryKey = SpongeWorldManager.createRegistryKey(worldKey);
            final IChunkStatusListener chunkStatusListener = ((MinecraftServerAccessor) this.server).accessor$getProgressListenerFactory().create(11);
            final List<ISpecialSpawner> spawners;
            if (isDefaultWorld) {
                spawners = ImmutableList.of(new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(levelData));
            } else {
                spawners = ImmutableList.of();
            }

            final ServerWorld world = new ServerWorld(this.server, ((MinecraftServerAccessor) this.server).accessor$executor(), storageSource, levelData,
                    registryKey, (DimensionType) worldType, chunkStatusListener, template.generator(), isDebugGeneration, seed, spawners, true);
            this.worlds.put(registryKey, world);

            this.prepareWorld(world, isDebugGeneration);
        }

        ((MinecraftServerAccessor) this.server).invoker$forceDifficulty();

        for (final Map.Entry<RegistryKey<World>, ServerWorld> entry : this.worlds.entrySet()) {
            try {
                this.postWorldLoad(entry.getValue(), true).get();
            } catch (final InterruptedException | ExecutionException e) {
                throw new IllegalStateException(e);
            }
        }

        ((SpongeUserManager) Sponge.getServer().getUserManager()).init();
        ((SpongeServer) SpongeCommon.getServer()).getPlayerDataManager().load();
    }

    private ServerWorld prepareWorld(final ServerWorld world, final boolean isDebugGeneration) {
        final boolean isDefaultWorld = World.OVERWORLD.equals(world.dimension());
        final ServerWorldInfo levelData = (ServerWorldInfo) world.getLevelData();

        if (isDefaultWorld) {
            // Initialize scoreboard data. This will hook to the ServerScoreboard, needs to be made multi-world aware
            ((MinecraftServerAccessor) this.server).accessor$readScoreboard(world.getDataStorage());

            ((MinecraftServerAccessor) this.server).accessor$commandStorage(new CommandStorage(world.getDataStorage()));
        }

        final boolean isInitialized = levelData.isInitialized();

        SpongeCommon.postEvent(SpongeEventFactory.createLoadWorldEvent(PhaseTracker.getCauseStackManager().getCurrentCause(),
            (org.spongepowered.api.world.server.ServerWorld) world, isInitialized));

        // Set the view distance back on it's self to trigger the logic
        ((ServerWorldInfoBridge) world.getLevelData()).bridge$viewDistance().ifPresent(v -> ((ServerWorldInfoBridge) world.getLevelData()).bridge$setViewDistance(v));

        world.getWorldBorder().applySettings(levelData.getWorldBorder());

        if (!isInitialized) {
            try {
                final boolean hasSpawnAlready = ((ServerWorldInfoBridge) world.getLevelData()).bridge$customSpawnPosition();
                if (!hasSpawnAlready) {
                    if (isDefaultWorld || ((ServerWorldProperties) world.getLevelData()).performsSpawnLogic()) {
                        MinecraftServerAccessor.invoker$setInitialSpawn(world, levelData, levelData.worldGenSettings().generateBonusChest(), isDebugGeneration, !isDebugGeneration);
                    } else if (World.END.equals(world.dimension())) {
                        ((ServerWorldInfo) world.getLevelData()).setSpawn(ServerWorld.END_SPAWN_POINT, 0);
                    }
                } else {
                    Features.BONUS_CHEST.place(world, world.getChunkSource().getGenerator(), world.random, new BlockPos(levelData.getXSpawn(),
                            levelData.getYSpawn(),levelData.getZSpawn()));
                }
                levelData.setInitialized(true);
                if (isDebugGeneration) {
                    ((MinecraftServerAccessor) this.server).invoker$setDebugLevel(levelData);
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
            ((ServerWorldBridge) world).bridge$getBossBarManager().load(levelData.getCustomBossEvents());
        }

        return world;
    }

    private CompletableFuture<ServerWorld> postWorldLoad(final ServerWorld world, final boolean blocking) {
        final ServerWorldInfo levelData = (ServerWorldInfo) world.getLevelData();
        final ServerWorldInfoBridge levelBridge = (ServerWorldInfoBridge) levelData;
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

    private CompletableFuture<ServerWorld> loadSpawnChunksAsync(final ServerWorld world) {

        final BlockPos spawnPoint = world.getSharedSpawnPos();
        final ChunkPos chunkPos = new ChunkPos(spawnPoint);
        final ServerChunkProvider serverChunkProvider = world.getChunkSource();
        serverChunkProvider.getLightEngine().setTaskPerBatch(500);

        final int borderRadius = 11;
        final int diameter = ((borderRadius - 1) * 2) + 1;
        final int spawnChunks = diameter * diameter;

        serverChunkProvider.addRegionTicket(VanillaWorldManager.SPAWN_CHUNKS, chunkPos, borderRadius, world.dimension().location());
        final CompletableFuture<ServerWorld> generationFuture = new CompletableFuture<>();
        Sponge.getAsyncScheduler().submit(
                Task.builder().plugin(Launch.getInstance().getPlatformPlugin())
                        .execute(task -> {
                            if (serverChunkProvider.getTickingGenerated() >= spawnChunks) {
                                Sponge.getServer().getScheduler().submit(Task.builder().plugin(Launch.getInstance().getPlatformPlugin()).execute(() -> generationFuture.complete(world)).build());
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
            if (!((ServerWorldInfoBridge) world.getLevelData()).bridge$performsSpawnLogic()) {
                serverChunkProvider.removeRegionTicket(VanillaWorldManager.SPAWN_CHUNKS, chunkPos, 11, world.dimension().location());
            }
            return world;
        });
    }

    private void loadSpawnChunks(final ServerWorld world) {
        final BlockPos spawnPoint = world.getSharedSpawnPos();
        final ChunkPos chunkPos = new ChunkPos(spawnPoint);
        final IChunkStatusListener chunkStatusListener = ((ServerWorldBridge) world).bridge$getChunkStatusListener();
        chunkStatusListener.updateSpawnPos(chunkPos);
        final ServerChunkProvider serverChunkProvider = world.getChunkSource();
        serverChunkProvider.getLightEngine().setTaskPerBatch(500);
        ((MinecraftServerAccessor) this.server).accessor$setNextTickTime(Util.getMillis());
        serverChunkProvider.addRegionTicket(VanillaWorldManager.SPAWN_CHUNKS, chunkPos, 11, world.dimension().location());

        while (serverChunkProvider.getTickingGenerated() != 441) {
            ((MinecraftServerAccessor) this.server).accessor$setNextTickTime(Util.getMillis() + 10L);
            ((MinecraftServerAccessor) this.server).accessor$waitUntilNextTick();
        }

        ((MinecraftServerAccessor) this.server).accessor$setNextTickTime(Util.getMillis() + 10L);
        ((MinecraftServerAccessor) this.server).accessor$waitUntilNextTick();

        this.updateForcedChunks(world, serverChunkProvider);

        ((MinecraftServerAccessor) this.server).accessor$setNextTickTime(Util.getMillis() + 10L);
        ((MinecraftServerAccessor) this.server).accessor$waitUntilNextTick();
        chunkStatusListener.stop();
        serverChunkProvider.getLightEngine().setTaskPerBatch(5);

        // Sponge Start - Release the chunk ticket if spawn is not set to be kept loaded...
        if (!((ServerWorldInfoBridge) world.getLevelData()).bridge$performsSpawnLogic()) {
            serverChunkProvider.removeRegionTicket(VanillaWorldManager.SPAWN_CHUNKS, chunkPos, 11, world.dimension().location());
        }
    }

    private void updateForcedChunks(final ServerWorld world, final ServerChunkProvider serverChunkProvider) {
        final ForcedChunksSaveData forcedChunksSaveData = world.getDataStorage().get(ForcedChunksSaveData::new, "chunks");
        if (forcedChunksSaveData != null) {
            final LongIterator longIterator = forcedChunksSaveData.getChunks().iterator();

            while (longIterator.hasNext()) {
                final long i = longIterator.nextLong();
                final ChunkPos forceChunkPos = new ChunkPos(i);
                serverChunkProvider.updateChunkForced(forceChunkPos, true);
            }
        }
    }

    private Dimension loadTemplate0(final RegistryKey<World> registryKey, final Path file) throws IOException {
        try (final InputStream stream = Files.newInputStream(file); final InputStreamReader reader = new InputStreamReader(stream)) {
            final JsonParser parser = new JsonParser();
            final JsonElement element = parser.parse(reader);
            final SingleTemplateAccess singleTemplateAccess = new SingleTemplateAccess(registryKey, element);
            final WorldSettingsImport<JsonElement> settingsAdapter = WorldSettingsImport.create(JsonOps.INSTANCE, singleTemplateAccess, (DynamicRegistries.Impl) BootstrapProperties.registries);
            final SimpleRegistry<Dimension> registry = new SimpleRegistry<>(net.minecraft.util.registry.Registry.LEVEL_STEM_REGISTRY, Lifecycle.stable());
            settingsAdapter.decodeElements(registry, net.minecraft.util.registry.Registry.LEVEL_STEM_REGISTRY, Dimension.CODEC);
            return registry.stream().findAny().orElse(null);
        }
    }

    private static final class SingleTemplateAccess implements WorldSettingsImport.IResourceAccess {

        private final RegistryKey<?> key;
        private final JsonElement element;

        public SingleTemplateAccess(final RegistryKey<?> key, final JsonElement element) {
            this.key = key;
            this.element = element;
        }

        @Override
        public Collection<ResourceLocation> listResources(final RegistryKey<? extends net.minecraft.util.registry.Registry<?>> registryKey) {
            if (this.key.isFor(registryKey)) {
                return Collections.singletonList(new ResourceLocation(this.key.location().getNamespace(),
                        registryKey.location().getPath() + "/" + this.key.location().getPath() + ".json"));
            }
            return Collections.emptyList();
        }

        @Override
        public <E> DataResult<Pair<E, OptionalInt>> parseElement(final DynamicOps<JsonElement> ops, final RegistryKey<? extends net.minecraft.util.registry.Registry<E>> registryKey, final RegistryKey<E> elementKey, final Decoder<E> decoder) {
            final DataResult<E> result = decoder.parse(ops, this.element);
            return result.map(t -> Pair.of(t, OptionalInt.empty()));
        }
    }
}
