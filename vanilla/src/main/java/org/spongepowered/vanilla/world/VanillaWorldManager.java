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
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.resources.IResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.codec.DatapackCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.util.registry.WorldSettingsImport;
import net.minecraft.village.VillageSiege;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.ForcedChunksSaveData;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.gen.ChunkGenerator;
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
import net.minecraft.world.storage.SaveFormat;
import net.minecraft.world.storage.ServerWorldInfo;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.server.ServerWorldProperties;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeServer;
import org.spongepowered.common.accessor.server.MinecraftServerAccessor;
import org.spongepowered.common.accessor.world.storage.SaveFormat_LevelSaveAccessor;
import org.spongepowered.common.accessor.world.storage.ServerWorldInfoAccessor;
import org.spongepowered.common.bridge.ResourceKeyBridge;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.bridge.world.WorldSettingsBridge;
import org.spongepowered.common.bridge.world.gen.DimensionGeneratorSettingsBridge;
import org.spongepowered.common.bridge.world.storage.IServerWorldInfoBridge;
import org.spongepowered.common.config.SpongeGameConfigs;
import org.spongepowered.common.config.inheritable.InheritableConfigHandle;
import org.spongepowered.common.config.inheritable.WorldConfig;
import org.spongepowered.common.event.lifecycle.RegisterWorldEventImpl;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.user.SpongeUserManager;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.FutureUtil;
import org.spongepowered.common.world.server.SpongeWorldManager;
import org.spongepowered.common.world.server.WorldRegistration;
import org.spongepowered.vanilla.accessor.server.MinecraftServerAccessor_Vanilla;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class VanillaWorldManager implements SpongeWorldManager {

    private final MinecraftServer server;
    private final Path defaultWorldDirectory;
    private final Path customWorldsDirectory;
    private final Map<RegistryKey<World>, ServerWorld> worlds;
    private final Map<RegistryKey<World>, ServerWorldInfo> loadedWorldInfos;
    private final Map<ResourceKey, WorldRegistration> pendingWorlds;
    private final Map<ResourceKey, ServerWorldInfo> allInfos;

    private static final TicketType<ResourceLocation> SPAWN_CHUNKS = TicketType.create("spawn_chunks", (i, o) -> i.compareTo(o));

    public VanillaWorldManager(final MinecraftServer server) {
        this.server = server;
        this.defaultWorldDirectory = ((SaveFormat_LevelSaveAccessor) ((MinecraftServerAccessor) this.server).accessor$storageSource()).accessor$levelPath();
        this.customWorldsDirectory = this.defaultWorldDirectory.resolve("dimensions");
        this.worlds = ((MinecraftServerAccessor) this.server).accessor$levels();
        this.loadedWorldInfos = new Object2ObjectOpenHashMap<>();
        this.pendingWorlds = new LinkedHashMap<>();
        this.allInfos = new LinkedHashMap<>();

        this.registerPendingWorld0((ResourceKey) (Object) World.OVERWORLD.location(), this.defaultWorldDirectory, null);
        this.registerPendingWorld0((ResourceKey) (Object) World.NETHER.location(), this.defaultWorldDirectory.resolve(this.getDirectoryName((ResourceKey) (Object) World.NETHER.location())), null);
        this.registerPendingWorld0((ResourceKey) (Object) World.END.location(), this.defaultWorldDirectory.resolve(this.getDirectoryName((ResourceKey) (Object) World.END.location())), null);
    }

    @Override
    public Server getServer() {
        return (Server) this.server;
    }

    @Override
    public Path getDefaultWorldDirectory() {
        return this.defaultWorldDirectory;
    }

    @Override
    public Path getCustomWorldsDirectory() {
        return this.customWorldsDirectory;
    }

    @Override
    public Optional<org.spongepowered.api.world.server.ServerWorld> getWorld(final ResourceKey key) {
        Objects.requireNonNull(key, "key");

        return (Optional<org.spongepowered.api.world.server.ServerWorld>) (Object) Optional.ofNullable(this.worlds.get(SpongeWorldManager.createRegistryKey(key)));
    }

    @Override
    public Collection<org.spongepowered.api.world.server.ServerWorld> getWorlds() {
        return Collections.unmodifiableCollection((Collection<org.spongepowered.api.world.server.ServerWorld>) (Object) this.worlds.values());
    }

    @Override
    public CompletableFuture<ServerWorldProperties> createProperties(final ResourceKey key, final WorldArchetype archetype) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(archetype, "archetype");

        final ServerWorldInfo worldInfo = new ServerWorldInfo((WorldSettings) (Object) archetype, (DimensionGeneratorSettings) archetype.getWorldGeneratorSettings(), Lifecycle.stable());
        ((ResourceKeyBridge) worldInfo).bridge$setKey(key);
        ((IServerWorldInfoBridge) worldInfo).bridge$setUniqueId(UUID.randomUUID());

        SpongeCommon.postEvent(SpongeEventFactory.createConstructWorldPropertiesEvent(PhaseTracker.getCauseStackManager().getCurrentCause(),
                archetype, (WorldProperties) worldInfo));

        return CompletableFuture.completedFuture((ServerWorldProperties) worldInfo);
    }

    @Override
    public CompletableFuture<org.spongepowered.api.world.server.ServerWorld> loadWorld(final ResourceKey key) {
        Objects.requireNonNull(key, "key");

        ServerWorld world = this.worlds.get(key);
        if (world != null) {
            return CompletableFuture.completedFuture((org.spongepowered.api.world.server.ServerWorld) world);
        }

//        final boolean isSinglePlayer = this.server.isSinglePlayer();
//        final Path savesDirectory = ((SaveFormatAccessor_Vanilla) this.server.getActiveAnvilConverter()).accessor$getBaseDir();
//        final Path gameDirectory = !isSinglePlayer ? savesDirectory : savesDirectory.getParent();
//        final Path levelDirectory = savesDirectory.resolve(this.server.getFolderName());
//
//        final String directoryName = this.getDirectoryName(key);
//        final boolean isVanillaSubWorld = this.isVanillaSubWorld(directoryName);
//
//        final Path worldDirectory = isVanillaSubWorld ? levelDirectory.resolve(directoryName) :
//                levelDirectory.resolve("dimensions").resolve(key.getNamespace()).resolve(key.getValue());
//
//        if (Files.notExists(worldDirectory)) {
//            return FutureUtil.completedWithException(new IOException(String.format("World '%s' has no directory in '%s'.", key,
//                    worldDirectory.getParent().toAbsolutePath())));
//        }
//
//        if (Files.notExists(worldDirectory.resolve(Constants.Sponge.World.LEVEL_SPONGE_DAT))) {
//            return FutureUtil.completedWithException(new IOException(String.format("World '%s has no Sponge level data ('%s').",
//                    key, Constants.Sponge.World.LEVEL_SPONGE_DAT)));
//        }
//
//        final SaveFormat saveFormat = new SaveFormat(worldDirectory.getParent(), gameDirectory.resolve("backups"), this.server.getDataFixer());
//        final SaveHandler saveHandler = saveFormat.getSaveLoader(directoryName, this.server);
//
//        final WorldInfo worldInfo = saveHandler.loadWorldInfo();
//
//        final ResourceKey existingKey = ((ResourceKeyBridge) worldInfo).bridge$getKey();
//        if (existingKey != null && !existingKey.equals(key)) {
//            return FutureUtil.completedWithException(new IOException(String.format("World '%s' is keyed as '%s' in the level data.", key,
//                    existingKey)));
//        }
//
//        ((ResourceKeyBridge) worldInfo).bridge$setKey(key);
//
//        final SpongeDimensionType logicType = ((IServerWorldInfoBridge) worldInfo).bridge$getDimensionType();
//
//        final DimensionType dimensionType = Registry.DIMENSION_TYPE.getValue((ResourceLocation) (Object) key).orElseGet(() -> this.
//                createDimensionType(key, logicType, worldDirectory.getFileName().toString(), ((SimpleRegistryAccessor) Registry.DIMENSION_TYPE)
//                        .accessor$nextId()));
//
//        ((DimensionTypeBridge) dimensionType).bridge$setSpongeDimensionType(logicType);
//
//        MinecraftServerAccessor.accessor$LOGGER().info("Loading World '{}' ({})", key, logicType.getKey().getFormatted());
//
//        final InheritableConfigHandle<WorldConfig> configAdapter = SpongeGameConfigs.createWorld(logicType, key);
//        ((IServerWorldInfoBridge) worldInfo).bridge$setConfigAdapter(configAdapter);
//
//        final IChunkStatusListener chunkStatusListener = ((MinecraftServerAccessor_Vanilla) this.server).accessor$getProgressListenerFactory()
//                .create(11);
//
//        world = new ServerWorld(this.server, this.server.getBackgroundExecutor(), saveHandler, worldInfo, dimensionType, this.server.getProfiler(),
//                chunkStatusListener);
//
//        this.loadedWorldInfos.put(key, worldInfo);
//        this.infoByType.put(dimensionType, worldInfo);
//        this.allInfos.put(key, worldInfo);
//        this.worlds.put(key, world);
//        this.worldsByType.put(dimensionType, world);
//
//        this.performPostLoadWorldLogic(world, this.createDefaultSettings(null, false, worldInfo.getSeed(), worldInfo
//                .getGenerator(), null), worldDirectory, chunkStatusListener);

        return CompletableFuture.completedFuture((org.spongepowered.api.world.server.ServerWorld) world);
    }

    @Override
    public CompletableFuture<org.spongepowered.api.world.server.ServerWorld> loadWorld(ServerWorldProperties properties) {
        Objects.requireNonNull(properties, "properties");

        ServerWorld world = this.worlds.get(properties.getKey());
//        if (world != null) {
//            if (world.getWorldInfo() != properties) {
//                return FutureUtil.completedWithException(new IOException(String.format("While '%s' is already a loaded world, the "
//                        + "properties does not match the one given", properties.getKey())));
//            }
//
//            return CompletableFuture.completedFuture((org.spongepowered.api.world.server.ServerWorld) world);
//        }
//
//        final boolean isSinglePlayer = this.server.isSinglePlayer();
//        final Path savesDirectory = ((SaveFormatAccessor_Vanilla) this.server.getActiveAnvilConverter()).accessor$getBaseDir();
//        final Path gameDirectory = !isSinglePlayer ? savesDirectory : savesDirectory.getParent();
//        final Path levelDirectory = savesDirectory.resolve(this.server.getFolderName());
//
//        final String directoryName = this.getDirectoryName(properties.getKey());
//        final boolean isVanillaSubWorld = this.isVanillaSubWorld(directoryName);
//
//        final Path worldDirectory = isVanillaSubWorld ? levelDirectory.resolve(directoryName) : levelDirectory.resolve(Constants.Sponge.World
//                .DIMENSIONS_DIRECTORY).resolve(properties.getKey().getNamespace()).resolve(properties.getKey().getValue());
//
//        // If there is a folder on disk but we have no properties, this is likely because creating a properties in Vanilla inherently
//        // writes the directory (such in the case of createProperties) but this properties has never been saved. It is dangerous to patch
//        // out the creation of that directory so this hackfix will work for now
//        final boolean isOnDisk = Files.exists(worldDirectory) && Files.exists(worldDirectory.resolve(Constants.World.LEVEL_DAT));
//
//        final SaveFormat saveFormat = new SaveFormat(worldDirectory.getParent(), gameDirectory.resolve("backups"), this.server.getDataFixer());
//        final SaveHandler saveHandler = saveFormat.getSaveLoader(this.getDirectoryName(properties.getKey()), this.server);
//
//        if (isOnDisk) {
//            properties = (WorldProperties) saveHandler.loadWorldInfo();
//        }
//
//        DimensionType dimensionType = Registry.DIMENSION_TYPE.getValue((ResourceLocation) (Object) properties.getKey()).orElse(null);
//        if (dimensionType != null) {
//            // Validation checks
//            if (isOnDisk && !dimensionType.getDirectory(worldDirectory.getParent().toFile()).equals(worldDirectory.toFile())) {
//                return FutureUtil.completedWithException(new IOException(String.format("World '%s' was registered with a different directory on "
//                        + "this server instance before. Aborting...", properties.getKey())));
//            }
//        } else {
//            dimensionType = this.createDimensionType(properties.getKey(), (SpongeDimensionType) properties.getDimensionType(),
//                    worldDirectory.getFileName().toString(), ((SimpleRegistryAccessor) Registry.DIMENSION_TYPE).accessor$nextId());
//        }
//
//        final WorldInfo worldInfo = (WorldInfo) properties;
//
//        final SpongeDimensionType logicType = ((IServerWorldInfoBridge) properties).bridge$getDimensionType();
//
//        ((DimensionTypeBridge) dimensionType).bridge$setSpongeDimensionType(logicType);
//
//        MinecraftServerAccessor.accessor$LOGGER().info("Loading World '{}' ({})", properties.getKey(), logicType.getKey()
//                .getFormatted());
//
//        final InheritableConfigHandle<WorldConfig> adapter = SpongeGameConfigs.createWorld(logicType, properties.getKey());
//        ((IServerWorldInfoBridge) properties).bridge$setConfigAdapter(adapter);
//
//        final IChunkStatusListener chunkStatusListener = ((MinecraftServerAccessor_Vanilla) this.server).accessor$getProgressListenerFactory().create(11);
//
//        world = new ServerWorld(this.server, this.server.getBackgroundExecutor(), saveHandler, worldInfo,
//                dimensionType, this.server.getProfiler(), chunkStatusListener);
//
//        this.loadedWorldInfos.put(properties.getKey(), worldInfo);
//        this.infoByType.put(dimensionType, worldInfo);
//        this.allInfos.put(properties.getKey(), worldInfo);
//        this.worlds.put(properties.getKey(), world);
//        this.worldsByType.put(dimensionType, world);
//
//        this.performPostLoadWorldLogic(world, this.createDefaultSettings(null, false, worldInfo.getSeed(), worldInfo.getGenerator(), null),
//                worldDirectory, chunkStatusListener);

        return CompletableFuture.completedFuture((org.spongepowered.api.world.server.ServerWorld) world);
    }

    @Override
    public CompletableFuture<Boolean> unloadWorld(final ResourceKey key) {
        Objects.requireNonNull(key, "key");

        final RegistryKey<World> registryKey = SpongeWorldManager.createRegistryKey(key);

        if (World.OVERWORLD.equals(registryKey)) {
            return FutureUtil.completedWithException(new IOException("The default world can not be unloaded"));
        }

        final ServerWorld world = this.worlds.get(registryKey);
        if (world == null) {
            return CompletableFuture.completedFuture(false);
        }

        return this.unloadWorld((org.spongepowered.api.world.server.ServerWorld) world);
    }

    @Override
    public CompletableFuture<Boolean> unloadWorld(final org.spongepowered.api.world.server.ServerWorld world) {
        Objects.requireNonNull(world, "world");

        final RegistryKey<World> registryKey = SpongeWorldManager.createRegistryKey(world.getKey());

        if (World.OVERWORLD.equals(registryKey)) {
            return FutureUtil.completedWithException(new IOException("The default world can not be unloaded."));
        }

        if (world != this.worlds.get(registryKey)) {
            return FutureUtil.completedWithException(new IOException(String.format("World '%s' was told to unload but does not match the actual "
                    + "world loaded.", world.getKey())));
        }

        try {
            this.unloadWorld0((ServerWorld) world);
        } catch (final IOException e) {
            return FutureUtil.completedWithException(e);
        }

        return CompletableFuture.completedFuture(true);
    }

    @Override
    public Optional<ServerWorldProperties> getProperties(final ResourceKey key) {
        Objects.requireNonNull(key, "key");

        return (Optional<ServerWorldProperties>) (Object) Optional.ofNullable(this.allInfos.get(SpongeWorldManager.createRegistryKey(key)));
    }

    @Override
    public Collection<ServerWorldProperties> getUnloadedProperties() {
        final List<ServerWorldProperties> unloadedProperties = new ArrayList<>();
        for (final ServerWorldInfo worldInfo : this.allInfos.values()) {
            boolean unloaded = true;
            for (final ServerWorld value : this.worlds.values()) {
                if (value.getLevelData() == worldInfo) {
                    unloaded = false;
                    break;
                }
            }

            if (unloaded) {
                unloadedProperties.add((ServerWorldProperties) worldInfo);
            }
        }

        return unloadedProperties;
    }

    @Override
    public Collection<ServerWorldProperties> getAllProperties() {
        return (Collection<ServerWorldProperties>) (Object) Collections.unmodifiableCollection(this.allInfos.values());
    }

    @Override
    public CompletableFuture<Boolean> saveProperties(final ServerWorldProperties properties) {
        Objects.requireNonNull(properties, "properties");

        ServerWorld loadedWorld = (ServerWorld) properties.getWorld().orElse(null);

        final ResourceKey key = properties.getKey();
        final boolean isVanillaWorld = this.isVanillaWorld(key);
        final boolean isDefaultWorld = this.isDefaultWorld(key);

        final SaveFormat.LevelSave levelSave;
        try {
            levelSave = isDefaultWorld ? ((MinecraftServerAccessor) SpongeCommon.getServer()).accessor$storageSource() : (isVanillaWorld ?
                    SaveFormat.createDefault(this.defaultWorldDirectory).createAccess(this.getDirectoryName(key)) :
                    SaveFormat.createDefault(this.customWorldsDirectory).createAccess(key.getNamespace() + File.separator + key.getValue()));
        } catch (final IOException e) {
            return FutureUtil.completedWithException(e);
        }

        levelSave.saveDataTag(loadedWorld == null ? SpongeCommon.getServer().registryAccess() : loadedWorld.registryAccess(),
                (ServerWorldInfo) properties);

        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<ServerWorldProperties> copyWorld(final ResourceKey key, final ResourceKey copyKey) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(copyKey, "copyKey");

        final RegistryKey<World> registryKey = SpongeWorldManager.createRegistryKey(key);
        final RegistryKey<World> copyRegistryKey = SpongeWorldManager.createRegistryKey(copyKey);

        if (World.OVERWORLD.equals(copyRegistryKey)) {
            throw new IllegalArgumentException(String.format("The copy key '%s' cannot be the default world!", copyKey));
        }

        if (this.worlds.containsKey(copyRegistryKey)) {
            return FutureUtil.completedWithException(new IllegalStateException(String.format("The copy key '%s' is a currently loaded world!",
                    copyKey)));
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

        final Path configFile = SpongeCommon.getSpongeConfigDirectory().resolve(SpongeCommon.ECOSYSTEM_ID).resolve("worlds").resolve(key
                .getNamespace()).resolve(key.getValue() + ".conf");

        final Path copiedConfigFile = SpongeCommon.getSpongeConfigDirectory().resolve(SpongeCommon.ECOSYSTEM_ID).resolve("worlds")
                .resolve(copyKey.getNamespace()).resolve(copyKey.getValue() + ".conf");

        try {
            Files.createDirectories(copiedConfigFile.getParent());
            Files.copy(configFile, copiedConfigFile, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            return FutureUtil.completedWithException(e);
        }

        // Strip UUID
        final Path copiedSpongeLevelFile = copyDirectory.resolve(Constants.Sponge.World.LEVEL_SPONGE_DAT);
        if (Files.exists(copiedSpongeLevelFile)) {
            final CompoundNBT spongeLevelCompound;
            try {
                spongeLevelCompound = CompressedStreamTools.readCompressed(Files.newInputStream(copiedSpongeLevelFile));

                if (!spongeLevelCompound.isEmpty()) {
                    final CompoundNBT spongeDataCompound = spongeLevelCompound.getCompound(Constants.Sponge.SPONGE_DATA);
                    spongeDataCompound.remove(Constants.Sponge.World.UNIQUE_ID);
                    CompressedStreamTools.writeCompressed(spongeLevelCompound, Files.newOutputStream(copiedSpongeLevelFile));
                }
            } catch (final IOException e) {
                return FutureUtil.completedWithException(e);
            }
        }

        final SaveFormat.LevelSave levelSave;
        try {
            levelSave = (isVanillaCopyWorld ? SaveFormat.createDefault(this.defaultWorldDirectory).createAccess(this.getDirectoryName(copyKey)) :
                    SaveFormat.createDefault(this.customWorldsDirectory).createAccess(copyKey.getNamespace() + File.separator + copyKey.getValue()));
        } catch (final IOException e) {
            return FutureUtil.completedWithException(e);
        }

        final WorldSettingsImport<INBT> worldSettingsImport = WorldSettingsImport.create(NBTDynamicOps.INSTANCE, IResourceManager.Instance.INSTANCE,
                DynamicRegistries.builtin());

        return CompletableFuture.completedFuture((ServerWorldProperties) levelSave.getDataTag(worldSettingsImport, DatapackCodec.DEFAULT));
    }

    @Override
    public CompletableFuture<ServerWorldProperties> moveWorld(final ResourceKey key, final ResourceKey movedKey) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(movedKey, "movedKey");

        final RegistryKey<World> registryKey = SpongeWorldManager.createRegistryKey(key);
        final RegistryKey<World> copyRegistryKey = SpongeWorldManager.createRegistryKey(movedKey);

        if (World.OVERWORLD.equals(registryKey)) {
            return CompletableFuture.completedFuture(null);
        }

        if (this.worlds.containsKey(copyRegistryKey)) {
            return CompletableFuture.completedFuture(null);
        }

        ServerWorld loadedWorld = this.worlds.get(registryKey);
        if (loadedWorld != null) {
            try {
                this.unloadWorld0(loadedWorld);
            } catch (final IOException e) {
                return FutureUtil.completedWithException(e);
            }
        }

        this.allInfos.remove(key);

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

        final SaveFormat.LevelSave levelSave;
        try {
            levelSave = (isVanillaMoveWorld ? SaveFormat.createDefault(this.defaultWorldDirectory).createAccess(this.getDirectoryName(movedKey)) :
                    SaveFormat.createDefault(this.customWorldsDirectory).createAccess(movedKey.getNamespace() + File.separator + movedKey.getValue()));
        } catch (final IOException e) {
            return FutureUtil.completedWithException(e);
        }

        final WorldSettingsImport<INBT> worldSettingsImport = WorldSettingsImport.create(NBTDynamicOps.INSTANCE, IResourceManager.Instance.INSTANCE,
                DynamicRegistries.builtin());

        final ServerWorldInfo levelData = (ServerWorldInfo) levelSave.getDataTag(worldSettingsImport, DatapackCodec.DEFAULT);

        this.allInfos.put(movedKey, levelData);

        return CompletableFuture.completedFuture((ServerWorldProperties) levelData);
    }

    @Override
    public CompletableFuture<Boolean> deleteWorld(final ResourceKey key) {
        Objects.requireNonNull(key, "key");

        final RegistryKey<World> registryKey = SpongeWorldManager.createRegistryKey(key);

        if (World.OVERWORLD.equals(registryKey)) {
            return CompletableFuture.completedFuture(false);
        }

        final ServerWorld loadedWorld = this.worlds.get(key);
        if (loadedWorld != null) {
            boolean disableLevelSaving = loadedWorld.noSave;
            loadedWorld.noSave = true;
            try {
                this.unloadWorld0(loadedWorld);
            } catch (final IOException e) {
                loadedWorld.noSave = disableLevelSaving;
                return FutureUtil.completedWithException(e);
            }
        }

        this.allInfos.remove(key);

        final boolean isVanillaWorld = this.isVanillaWorld(key);
        final String directoryName = this.getDirectoryName(key);

        final Path directory = isVanillaWorld ? this.defaultWorldDirectory.resolve(directoryName) : this.customWorldsDirectory.resolve(key
                .getNamespace()).resolve(key.getValue());

        try {
            Files.walk(directory)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (final IOException e) {
            return FutureUtil.completedWithException(e);
        }

        final Path configFile = SpongeCommon.getSpongeConfigDirectory().resolve(SpongeCommon.ECOSYSTEM_ID).resolve("worlds").resolve(key
                .getNamespace()).resolve(key.getValue() + ".conf");

        try {
            Files.deleteIfExists(configFile);
        } catch (final IOException e) {
            return FutureUtil.completedWithException(e);
        }

        return CompletableFuture.completedFuture(true);
    }

    @Override
    public boolean registerPendingWorld0(final ResourceKey key, final Path directory, @Nullable final WorldArchetype archetype) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(directory, "directory");

        if (this.pendingWorlds.containsKey(key)) {
            return false;
        }

        this.pendingWorlds.put(key, new WorldRegistration(key, directory, (WorldSettings) (Object) archetype));
        return true;
    }

    @Override
    public void unloadWorld0(final ServerWorld world) throws IOException {
        final RegistryKey<World> registryKey = world.dimension();

        if (world.getPlayers(p -> true).size() != 0) {
            throw new IOException(String.format("World '%s' was told to unload but players remain.", registryKey.location()));
        }

        SpongeCommon.getLogger().info("Unloading World '{}' ({})", registryKey.location(), SpongeCommon.getServer().registryAccess()
                .dimensionTypes().getKey(world.dimensionType()));

        final BlockPos spawnPoint = world.getSharedSpawnPos();
        world.getChunkSource().removeRegionTicket(VanillaWorldManager.SPAWN_CHUNKS, new ChunkPos(spawnPoint), 11, registryKey.location());

        ((IServerWorldInfoBridge) world.getLevelData()).bridge$getConfigAdapter().save();
        ((ServerWorldBridge) world).bridge$setManualSave(true);

        try {
            world.save(null, true, world.noSave);
            world.close();
        } catch (final Exception ex) {
            throw new IOException(ex);
        }

        this.loadedWorldInfos.remove(registryKey);
        this.worlds.remove(registryKey);

        SpongeCommon.postEvent(SpongeEventFactory.createUnloadWorldEvent(PhaseTracker.getCauseStackManager().getCurrentCause(),
                (org.spongepowered.api.world.server.ServerWorld) world));
    }

    @Override
    @Nullable
    public ServerWorld getDefaultWorld() {
        return this.server.overworld();
    }

    @Override
    public void adjustWorldForDifficulty(final ServerWorld world, final Difficulty newDifficulty, final boolean forceDifficulty) {
        if (world.getLevelData().isDifficultyLocked() && !forceDifficulty) {
            return;
        }

        if (forceDifficulty) {
            // Don't allow vanilla forcing the difficulty at launch set ours if we have a custom one
            if (!((IServerWorldInfoBridge) world.getLevelData()).bridge$hasCustomDifficulty()) {
                ((IServerWorldInfoBridge) world.getLevelData()).bridge$forceSetDifficulty(newDifficulty);
            }
        } else {
            ((ServerWorldInfo) world.getLevelData()).setDifficulty(newDifficulty);
        }
    }

    @Override
    public void loadLevel() {
        final ServerWorldInfo defaultLevelData = (ServerWorldInfo) this.server.getWorldData();
        final WorldSettings defaultLevelSettings = ((ServerWorldInfoAccessor) defaultLevelData).accessor$settings();

        SpongeCommon.postEvent(new RegisterWorldEventImpl(PhaseTracker.getCauseStackManager().getCurrentCause(), SpongeCommon.getGame(), this));

        if (!this.server.isSingleplayer()) {
            if (!this.server.isNetherEnabled()) {
                SpongeCommon.getLogger().warn("The option 'allow-nether' has been set to 'false' in the server.properties. "
                        + "Multi-World support has been disabled and no worlds besides the default world will be loaded.");
            }
        }

        try {
            Files.createDirectories(this.customWorldsDirectory);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        try {
            this.loadExistingWorldRegistrations();
        } catch (final IOException e) {
            throw new RuntimeException("Exception caught registering existing Sponge worlds!", e);
        }

        for (final Map.Entry<ResourceKey, WorldRegistration> entry : this.pendingWorlds.entrySet()) {
            final ResourceKey key = entry.getKey();
            final WorldRegistration worldRegistration = entry.getValue();

            final boolean isDefaultWorld = this.isDefaultWorld(key);

            if (!isDefaultWorld && !this.server.isSingleplayer() && !this.server.isNetherEnabled()) {
                continue;
            }

            DimensionType dimensionType;
            WorldSettings settings = worldRegistration.getDefaultSettings();

            final Path spongeLevelFile = worldRegistration.getDirectory().resolve(Constants.Sponge.World.LEVEL_SPONGE_DAT);
            if (Files.exists(spongeLevelFile)) {
                try (final InputStream stream = Files.newInputStream(spongeLevelFile)) {
                    final CompoundNBT compound = CompressedStreamTools.readCompressed(stream);
                    final CompoundNBT spongeDataCompound = compound.getCompound(Constants.Sponge.SPONGE_DATA);

                    final String rawDimensionType = spongeDataCompound.getString(Constants.Sponge.World.DIMENSION_TYPE);
                    dimensionType = SpongeCommon.getServer().registryAccess().dimensionTypes().get(new ResourceLocation(rawDimensionType));
                    if (dimensionType == null) {
                        DimensionType overworldType =
                                SpongeCommon.getServer().registryAccess().dimensionTypes().get(DimensionType.OVERWORLD_LOCATION);
                        MinecraftServerAccessor.accessor$LOGGER().warn("World '{}' has an unknown dimension type '{}'. Defaulting to '{}'...", key,
                                rawDimensionType, overworldType);
                        dimensionType = overworldType;
                    }
                } catch (final IOException e) {
                    throw new RuntimeException(String.format("Failed to read Sponge level data for '%s'!", key), e);
                }
            } else {
                if (settings != null) {
                    dimensionType = ((WorldSettingsBridge) (Object) settings).bridge$getDimensionType();
                } else {
                    // Dealing with a fresh run of Vanilla worlds at this point...
                    dimensionType = this.getVanillaDimensionType(key);
                }
            }

            final ResourceLocation dimensionTypeKey = SpongeCommon.getServer().registryAccess().dimensionTypes().getKey(dimensionType);

            MinecraftServerAccessor.accessor$LOGGER().info("Loading World '{}' ({})", key, dimensionTypeKey);

            final boolean configExists = SpongeGameConfigs.doesWorldConfigExist(key);

            final InheritableConfigHandle<WorldConfig> configAdapter = SpongeGameConfigs.createWorld((ResourceKey) (Object) dimensionTypeKey, key);
            if (!isDefaultWorld) {
                if (!configAdapter.get().world.enabled) {
                    SpongeCommon.getLogger().warn("World '{}' ({}) has been disabled in the configuration. World will not be loaded...", key,
                            dimensionTypeKey);
                    continue;
                }
            }

            final boolean isVanillaWorld = this.isVanillaWorld(key);

            if (!isDefaultWorld && !configAdapter.get().world.loadOnStartup) {
                SpongeCommon.getLogger().warn("World '{}' ({}) has been set to not load on startup in the configuration. Skipping...", key,
                        dimensionTypeKey);
                continue;
            }

            final SaveFormat.LevelSave levelSave;
            try {
                levelSave = isDefaultWorld ? ((MinecraftServerAccessor) SpongeCommon.getServer()).accessor$storageSource() : (isVanillaWorld ?
                        SaveFormat.createDefault(this.defaultWorldDirectory).createAccess(this.getDirectoryName(key)) :
                        SaveFormat.createDefault(this.customWorldsDirectory).createAccess(key.getNamespace() + File.separator + key.getValue()));
            } catch (final IOException e) {
                throw new RuntimeException(String.format("Failed to create level data adapter for world '%s'!", key), e);
            }

            ServerWorldInfo levelData;

            boolean isNewLevelData = false;

            if (isDefaultWorld) {
                levelData = (ServerWorldInfo) this.server.getWorldData();
                settings = defaultLevelSettings;

                ((IServerWorldInfoBridge) levelData).bridge$setDimensionType(dimensionType, false);
                ((IServerWorldInfoBridge) levelData).bridge$setConfigAdapter(configAdapter);
                ((ResourceKeyBridge) levelData).bridge$setKey(key);
            } else {
                final ServerWorld defaultWorld = this.worlds.get(World.OVERWORLD);
                final WorldSettingsImport<INBT> worldSettingsImport = WorldSettingsImport.create(NBTDynamicOps.INSTANCE,
                        ((MinecraftServerAccessor) this.server).accessor$dataPackRegistries().getResourceManager(),
                        (DynamicRegistries.Impl) defaultWorld.registryAccess());
                levelData = (ServerWorldInfo) levelSave.getDataTag(worldSettingsImport, defaultLevelSettings.getDataPackConfig());
                if (levelData == null) {
                    isNewLevelData = true;
                    DimensionGeneratorSettings dimensionGeneratorSettings;
                    boolean populateLevelDataFromSettings = false;

                    if (this.server.isDemo()) {
                        if (settings == null) {
                            settings = MinecraftServer.DEMO_SETTINGS;

                            ((WorldSettingsBridge) (Object) settings).bridge$setConfigExists(configExists);
                            ((WorldSettingsBridge) (Object) settings).bridge$setInfoConfigAdapter(configAdapter);
                            ((WorldSettingsBridge) (Object) settings).bridge$setDimensionType(dimensionType);
                            populateLevelDataFromSettings = true;
                        }
                        dimensionGeneratorSettings = DimensionGeneratorSettings.demoSettings(DynamicRegistries.builtin());
                    } else {
                        if (settings == null) {
                            settings = new WorldSettings(this.getDirectoryName(key), defaultLevelData.getGameType(), defaultLevelData.isHardcore(),
                                    defaultLevelData.getDifficulty(), false, new GameRules(), defaultLevelData.getDataPackConfig());

                            ((WorldSettingsBridge) (Object) settings).bridge$setConfigExists(configExists);
                            ((WorldSettingsBridge) (Object) settings).bridge$setInfoConfigAdapter(configAdapter);
                            ((WorldSettingsBridge) (Object) settings).bridge$setDimensionType(dimensionType);
                            populateLevelDataFromSettings = true;
                        }

                        dimensionGeneratorSettings = defaultLevelData.worldGenSettings().generateBonusChest() ? defaultLevelData.worldGenSettings()
                                .withBonusChest() : ((DimensionGeneratorSettingsBridge) defaultLevelData.worldGenSettings()).bridge$copy();
                    }

                    levelData = new ServerWorldInfo(settings, dimensionGeneratorSettings, Lifecycle.stable());
                    if (populateLevelDataFromSettings) {
                        ((WorldSettingsBridge) (Object) settings).bridge$populateInfo((IServerWorldInfoBridge) levelData);
                    }
                } else if (!isNewLevelData) {
                    dimensionType = ((IServerWorldInfoBridge) levelData).bridge$getDimensionType();
                }
            }

            ((IServerWorldInfoBridge) levelData).bridge$setConfigAdapter(configAdapter);
            ((ResourceKeyBridge) levelData).bridge$setKey(worldRegistration.getKey());

            if (isNewLevelData) {
                ((IServerWorldInfoBridge) levelData).bridge$setUniqueId(UUID.randomUUID());

                SpongeCommon.postEvent(SpongeEventFactory.createConstructWorldPropertiesEvent(
                        PhaseTracker.getCauseStackManager().getCurrentCause(),
                        (WorldArchetype) (Object) settings, (ServerWorldProperties) levelData));
            } else if (((ResourceKeyBridge) levelData).bridge$getKey() == null) {
                ((IServerWorldInfoBridge) levelData).bridge$setUniqueId(UUID.randomUUID());
                ((ResourceKeyBridge) levelData).bridge$setKey(key);
                ((IServerWorldInfoBridge) levelData).bridge$setDimensionType(dimensionType, false);
            }

            levelData.setModdedInfo(this.server.getServerModName(), this.server.getModdedStatus().isPresent());

            final boolean isDebugGeneration = levelData.worldGenSettings().isDebug();
            final long seed = BiomeManager.obfuscateSeed(levelData.worldGenSettings().seed());
            final List<ISpecialSpawner> spawners = ImmutableList.of(new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege()
                    , new WanderingTraderSpawner(levelData));
            final SimpleRegistry<Dimension> levelStemRegistry = levelData.worldGenSettings().dimensions();

            Dimension levelStem = null;
            ChunkGenerator chunkGenerator;

            if (isDefaultWorld) {
                levelStem = levelStemRegistry.get(Dimension.OVERWORLD);
                if (levelStem == null) {
                    chunkGenerator = DimensionGeneratorSettings.makeDefaultOverworld(this.server.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY),
                            this.server.registryAccess().registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY), (new Random()).nextLong());
                } else {
                    chunkGenerator = levelStem.generator();
                }
            } else {
                for (final Dimension ls : levelStemRegistry) {
                    if (ls.type().equals(dimensionType)) {
                        levelStem = ls;
                        break;
                    }
                }

                if (levelStem == null) {
                    throw new RuntimeException(String.format("Failed to find a level stem for dimension type '%s'", dimensionTypeKey));
                }

                chunkGenerator = levelStem.generator();
            }

            final RegistryKey<World> registryKey = SpongeWorldManager.createRegistryKey(key);
            this.loadedWorldInfos.put(registryKey, levelData);
            this.allInfos.put(key, levelData);

            final IChunkStatusListener chunkStatusListener = ((MinecraftServerAccessor_Vanilla) this.server).accessor$getProgressListenerFactory().create(11);

            final ServerWorld serverWorld = new ServerWorld(this.server, ((MinecraftServerAccessor) this.server).accessor$executor(), levelSave, levelData,
                    registryKey, dimensionType, chunkStatusListener, chunkGenerator, isDebugGeneration, seed, isDefaultWorld ? spawners :
                    ImmutableList.of(), true);

            this.worlds.put(registryKey, serverWorld);

            this.performPostLoadWorldLogic(serverWorld, isDebugGeneration, chunkStatusListener);
        }

        this.pendingWorlds.clear();

        ((SpongeServer) SpongeCommon.getServer()).getPlayerDataManager().load();
    }

    private void loadSpawnChunks(final ServerWorld serverWorld, final IChunkStatusListener chunkStatusListener) {
        MinecraftServerAccessor.accessor$LOGGER().info("Preparing start region for world '{}' ({})", serverWorld.dimension().location(),
                SpongeCommon.getServer().registryAccess().dimensionTypes().getKey(serverWorld.dimensionType()));
        final BlockPos spawnPoint = serverWorld.getSharedSpawnPos();
        final ChunkPos chunkPos = new ChunkPos(spawnPoint);
        chunkStatusListener.updateSpawnPos(chunkPos);
        final ServerChunkProvider serverChunkProvider = serverWorld.getChunkSource();
        serverChunkProvider.getLightEngine().setTaskPerBatch(500);
        ((MinecraftServerAccessor_Vanilla) this.server).accessor$setNextTickTime(Util.getMillis());
        serverChunkProvider.addRegionTicket(VanillaWorldManager.SPAWN_CHUNKS, chunkPos, 11, serverWorld.dimension().location());

        while (serverChunkProvider.getTickingGenerated() != 441) {
            ((MinecraftServerAccessor_Vanilla) this.server).accessor$setNextTickTime(Util.getMillis() + 10L);
            ((MinecraftServerAccessor_Vanilla) this.server).accessor$waitUntilNextTick();
        }

        ((MinecraftServerAccessor_Vanilla) this.server).accessor$setNextTickTime(Util.getMillis() + 10L);
        ((MinecraftServerAccessor_Vanilla) this.server).accessor$waitUntilNextTick();

        final ForcedChunksSaveData forcedChunksSaveData = serverWorld.getDataStorage().get(ForcedChunksSaveData::new, "chunks");
        if (forcedChunksSaveData != null) {
            final LongIterator longIterator = forcedChunksSaveData.getChunks().iterator();

            while (longIterator.hasNext()) {
                final long i = longIterator.nextLong();
                final ChunkPos forceChunkPos = new ChunkPos(i);
                serverChunkProvider.updateChunkForced(forceChunkPos, true);
            }
        }

        ((MinecraftServerAccessor_Vanilla) this.server).accessor$setNextTickTime(Util.getMillis() + 10L);
        ((MinecraftServerAccessor_Vanilla) this.server).accessor$waitUntilNextTick();
        chunkStatusListener.stop();
        serverChunkProvider.getLightEngine().setTaskPerBatch(5);

        // Sponge Start - Release the chunk ticket if spawn is not set to be kept loaded...
        if (!((IServerWorldInfoBridge) serverWorld.getLevelData()).bridge$getConfigAdapter().get().world.keepSpawnLoaded) {
            serverChunkProvider.removeRegionTicket(VanillaWorldManager.SPAWN_CHUNKS, chunkPos, 11, serverWorld.dimension().location());
        }
    }

    private void loadExistingWorldRegistrations() throws IOException {
        for (final Path valueDirectory :
                Files
                        .walk(this.customWorldsDirectory, 2)
                        .filter(o -> !this.customWorldsDirectory.equals(o) && Files.isDirectory(o) && Files.exists(o.resolve(Constants.Sponge.World.LEVEL_SPONGE_DAT)))
                        .collect(Collectors.toList())) {
            final Path namespacedDirectory = valueDirectory.getParent();

            final ResourceKey key = ResourceKey.of(namespacedDirectory.getFileName().toString(), valueDirectory.getFileName().toString());
            this.pendingWorlds.put(key, new WorldRegistration(key, valueDirectory, null));
        }
    }

    private void performPostLoadWorldLogic(final ServerWorld serverWorld, final boolean isDebugGeneration, final IChunkStatusListener listener) {

        final boolean isDefaultWorld = World.OVERWORLD.equals(serverWorld.dimension());
        final ServerWorldInfo levelData = (ServerWorldInfo) serverWorld.getLevelData();

        if (isDefaultWorld) {
            // Initialize scoreboard data. This will hook to the ServerScoreboard, needs to be made multi-world aware
            ((MinecraftServerAccessor_Vanilla) this.server).accessor$readScoreboard(serverWorld.getDataStorage());

            ((MinecraftServerAccessor) this.server).accessor$commandStorage(new CommandStorage(serverWorld.getDataStorage()));
        }

        serverWorld.getWorldBorder().applySettings(levelData.getWorldBorder());
        if (!levelData.isInitialized()) {
            try {
                MinecraftServerAccessor.invoker$setInitialSpawn(serverWorld, levelData, levelData.worldGenSettings().generateBonusChest(),
                        isDebugGeneration, true);
                levelData.setInitialized(true);
                if (isDebugGeneration) {
                    ((MinecraftServerAccessor) this.server).invoker$setDebugLevel(levelData);
                }
            } catch (final Throwable throwable) {
                final CrashReport crashReport = CrashReport.forThrowable(throwable, "Exception initializing world '" + serverWorld.dimension().location()
                        + "'");
                try {
                    serverWorld.fillReportDetails(crashReport);
                } catch (Throwable ignore) {
                }

                throw new ReportedException(crashReport);
            }

            levelData.setInitialized(true);
        }

        // Initialize PlayerData in PlayerList, add WorldBorder listener. We change the method in PlayerList to handle per-world border
        this.server.getPlayerList().setLevel(serverWorld);
        if (isDefaultWorld) {
            ((SpongeUserManager) ((Server) this.server).getUserManager()).init();
        }

        if (levelData.getCustomBossEvents() != null) {
            ((ServerWorldBridge) serverWorld).bridge$getBossBarManager().load(levelData.getCustomBossEvents());
        }

        final org.spongepowered.api.world.server.ServerWorld apiWorld = (org.spongepowered.api.world.server.ServerWorld) serverWorld;
        SpongeCommon.postEvent(SpongeEventFactory.createLoadWorldEvent(PhaseTracker.getCauseStackManager().getCurrentCause(),
                apiWorld));

        final boolean generateSpawnOnLoad = ((IServerWorldInfoBridge) levelData).bridge$getConfigAdapter().get().world.generateSpawnOnLoad || isDefaultWorld;

        if (generateSpawnOnLoad) {
            this.loadSpawnChunks(serverWorld, listener);
        } else {
            serverWorld.getChunkSource().addRegionTicket(VanillaWorldManager.SPAWN_CHUNKS, new ChunkPos(apiWorld.getProperties().getSpawnPosition()
                            .getX(), apiWorld.getProperties().getSpawnPosition().getZ()), 11, (ResourceLocation) (Object) apiWorld.getKey());
        }
    }
}
