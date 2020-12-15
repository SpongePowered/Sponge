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

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.ForcedChunksSaveData;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.ColumnFuzzedBiomeMagnifier;
import net.minecraft.world.biome.FuzzedBiomeMagnifier;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;
import net.minecraft.world.storage.CommandStorage;
import net.minecraft.world.storage.SaveFormat;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.SessionLockException;
import net.minecraft.world.storage.WorldInfo;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.dimension.DimensionTypes;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeServer;
import org.spongepowered.common.accessor.server.MinecraftServerAccessor;
import org.spongepowered.common.accessor.util.registry.SimpleRegistryAccessor;
import org.spongepowered.common.bridge.ResourceKeyBridge;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.bridge.world.WorldSettingsBridge;
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
import org.spongepowered.vanilla.accessor.world.storage.SaveFormatAccessor_Vanilla;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
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
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class VanillaWorldManager implements SpongeWorldManager {

    private final MinecraftServer server;
    private final Path savesDirectory;
    private final Map<ResourceKey, ServerWorld> worlds;
    private final Map<DimensionType, ServerWorld> worldsByType;
    private final Map<ResourceKey, WorldInfo> loadedWorldInfos;
    private final Map<DimensionType, WorldInfo> infoByType;
    private final Map<ResourceKey, WorldRegistration> pendingWorlds;
    private final Map<ResourceKey, WorldInfo> allInfos;

    private static final TicketType<ResourceLocation> SPAWN_CHUNKS = TicketType.create("spawn_chunks", (i, o) -> i.compareTo(o));

    public VanillaWorldManager(final MinecraftServer server) {
        this.server = server;
        this.savesDirectory = ((MinecraftServerAccessor_Vanilla) server).accessor$getAnvilFile().toPath().resolve(server.getFolderName());
        this.worlds = new Object2ObjectOpenHashMap<>();
        this.worldsByType = ((MinecraftServerAccessor_Vanilla) server).accessor$getLevels();
        this.loadedWorldInfos = new Object2ObjectOpenHashMap<>();
        this.infoByType = new IdentityHashMap<>();
        this.pendingWorlds = new LinkedHashMap<>();
        this.allInfos = new LinkedHashMap<>();

        this.clearCustomWorldDimensions();

        this.registerPendingWorld(SpongeWorldManager.VANILLA_OVERWORLD, null);
        this.registerPendingWorld(SpongeWorldManager.VANILLA_THE_NETHER, null);
        this.registerPendingWorld(SpongeWorldManager.VANILLA_THE_END, null);
    }

    @Override
    public Path getSavesDirectory() {
        return this.savesDirectory;
    }

    @Override
    public Optional<org.spongepowered.api.world.server.ServerWorld> getWorld(final ResourceKey key) {
        Objects.requireNonNull(key);

        return (Optional<org.spongepowered.api.world.server.ServerWorld>) (Object) Optional.ofNullable(this.worlds.get(key));
    }

    @Override
    public Collection<org.spongepowered.api.world.server.ServerWorld> getWorlds() {
        return Collections.unmodifiableCollection((Collection<org.spongepowered.api.world.server.ServerWorld>) (Object) this.worldsByType.values());
    }

    @Override
    public ResourceKey getDefaultPropertiesKey() {
        return VanillaWorldManager.VANILLA_OVERWORLD;
    }

    @Override
    public Optional<WorldProperties> getDefaultProperties() {
        final ServerWorld defaultWorld = this.getDefaultWorld();
        if (defaultWorld == null) {
            return Optional.empty();
        }
        return Optional.of((WorldProperties) defaultWorld.getWorldInfo());
    }

    @Override
    public CompletableFuture<WorldProperties> createProperties(final ResourceKey key, final WorldArchetype archetype) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(archetype);

        final WorldInfo worldInfo = new WorldInfo((WorldSettings) (Object) archetype, key.getValue());
        ((ResourceKeyBridge) worldInfo).bridge$setKey(key);
        ((IServerWorldInfoBridge) worldInfo).bridge$setUniqueId(UUID.randomUUID());
        ((IServerWorldInfoBridge) worldInfo).bridge$setModCreated(true);

        SpongeCommon.postEvent(SpongeEventFactory.createConstructWorldPropertiesEvent(PhaseTracker.getCauseStackManager().getCurrentCause(),
                archetype, (WorldProperties) worldInfo));

        return CompletableFuture.completedFuture((WorldProperties) worldInfo);
    }

    @Override
    public CompletableFuture<org.spongepowered.api.world.server.ServerWorld> loadWorld(final ResourceKey key) {
        Objects.requireNonNull(key);

        ServerWorld world = this.worlds.get(key);
        if (world != null) {
            return CompletableFuture.completedFuture((org.spongepowered.api.world.server.ServerWorld) world);
        }

        final boolean isSinglePlayer = this.server.isSinglePlayer();
        final Path savesDirectory = ((SaveFormatAccessor_Vanilla) this.server.getActiveAnvilConverter()).accessor$getBaseDir();
        final Path gameDirectory = !isSinglePlayer ? savesDirectory : savesDirectory.getParent();
        final Path levelDirectory = savesDirectory.resolve(this.server.getFolderName());

        final String directoryName = this.getDirectoryName(key);
        final boolean isVanillaSubWorld = this.isVanillaSubWorld(directoryName);

        final Path worldDirectory = isVanillaSubWorld ? levelDirectory.resolve(directoryName) :
                levelDirectory.resolve("dimensions").resolve(key.getNamespace()).resolve(key.getValue());

        if (Files.notExists(worldDirectory)) {
            return FutureUtil.completedWithException(new IOException(String.format("World '%s' has no directory in '%s'.", key,
                    worldDirectory.getParent().toAbsolutePath())));
        }

        if (Files.notExists(worldDirectory.resolve(Constants.Sponge.World.LEVEL_SPONGE_DAT))) {
            return FutureUtil.completedWithException(new IOException(String.format("World '%s has no Sponge level data ('%s').",
                    key, Constants.Sponge.World.LEVEL_SPONGE_DAT)));
        }

        final SaveFormat saveFormat = new SaveFormat(worldDirectory.getParent(), gameDirectory.resolve("backups"), this.server.getDataFixer());
        final SaveHandler saveHandler = saveFormat.getSaveLoader(directoryName, this.server);

        final WorldInfo worldInfo = saveHandler.loadWorldInfo();

        final ResourceKey existingKey = ((ResourceKeyBridge) worldInfo).bridge$getKey();
        if (existingKey != null && !existingKey.equals(key)) {
            return FutureUtil.completedWithException(new IOException(String.format("World '%s' is keyed as '%s' in the level data.", key,
                    existingKey)));
        }

        ((ResourceKeyBridge) worldInfo).bridge$setKey(key);

        final SpongeDimensionType logicType = ((IServerWorldInfoBridge) worldInfo).bridge$getDimensionType();

        final DimensionType dimensionType = Registry.DIMENSION_TYPE.getValue((ResourceLocation) (Object) key).orElseGet(() -> this.
                createDimensionType(key, logicType, worldDirectory.getFileName().toString(), ((SimpleRegistryAccessor) Registry.DIMENSION_TYPE)
                        .accessor$nextId()));

        ((DimensionTypeBridge) dimensionType).bridge$setSpongeDimensionType(logicType);

        MinecraftServerAccessor.accessor$LOGGER().info("Loading World '{}' ({})", key, logicType.getKey().getFormatted());

        final InheritableConfigHandle<WorldConfig> configAdapter = SpongeGameConfigs.createWorld(logicType, key);
        ((IServerWorldInfoBridge) worldInfo).bridge$setConfigAdapter(configAdapter);

        final IChunkStatusListener chunkStatusListener = ((MinecraftServerAccessor_Vanilla) this.server).accessor$getProgressListenerFactory()
                .create(11);

        world = new ServerWorld(this.server, this.server.getBackgroundExecutor(), saveHandler, worldInfo, dimensionType, this.server.getProfiler(),
                chunkStatusListener);

        this.loadedWorldInfos.put(key, worldInfo);
        this.infoByType.put(dimensionType, worldInfo);
        this.allInfos.put(key, worldInfo);
        this.worlds.put(key, world);
        this.worldsByType.put(dimensionType, world);

        this.performPostLoadWorldLogic(world, this.createDefaultSettings(null, false, worldInfo.getSeed(), worldInfo
                .getGenerator(), null), worldDirectory, chunkStatusListener);

        return CompletableFuture.completedFuture((org.spongepowered.api.world.server.ServerWorld) world);
    }

    @Override
    public CompletableFuture<org.spongepowered.api.world.server.ServerWorld> loadWorld(WorldProperties properties) {
        Objects.requireNonNull(properties);

        ServerWorld world = this.worlds.get(properties.getKey());
        if (world != null) {
            if (world.getWorldInfo() != properties) {
                return FutureUtil.completedWithException(new IOException(String.format("While '%s' is already a loaded world, the "
                        + "properties does not match the one given", properties.getKey())));
            }

            return CompletableFuture.completedFuture((org.spongepowered.api.world.server.ServerWorld) world);
        }

        final boolean isSinglePlayer = this.server.isSinglePlayer();
        final Path savesDirectory = ((SaveFormatAccessor_Vanilla) this.server.getActiveAnvilConverter()).accessor$getBaseDir();
        final Path gameDirectory = !isSinglePlayer ? savesDirectory : savesDirectory.getParent();
        final Path levelDirectory = savesDirectory.resolve(this.server.getFolderName());

        final String directoryName = this.getDirectoryName(properties.getKey());
        final boolean isVanillaSubWorld = this.isVanillaSubWorld(directoryName);

        final Path worldDirectory = isVanillaSubWorld ? levelDirectory.resolve(directoryName) : levelDirectory.resolve(Constants.Sponge.World
                .DIMENSIONS_DIRECTORY).resolve(properties.getKey().getNamespace()).resolve(properties.getKey().getValue());

        // If there is a folder on disk but we have no properties, this is likely because creating a properties in Vanilla inherently
        // writes the directory (such in the case of createProperties) but this properties has never been saved. It is dangerous to patch
        // out the creation of that directory so this hackfix will work for now
        final boolean isOnDisk = Files.exists(worldDirectory) && Files.exists(worldDirectory.resolve(Constants.World.LEVEL_DAT));

        final SaveFormat saveFormat = new SaveFormat(worldDirectory.getParent(), gameDirectory.resolve("backups"), this.server.getDataFixer());
        final SaveHandler saveHandler = saveFormat.getSaveLoader(this.getDirectoryName(properties.getKey()), this.server);

        if (isOnDisk) {
            properties = (WorldProperties) saveHandler.loadWorldInfo();
        }

        DimensionType dimensionType = Registry.DIMENSION_TYPE.getValue((ResourceLocation) (Object) properties.getKey()).orElse(null);
        if (dimensionType != null) {
            // Validation checks
            if (isOnDisk && !dimensionType.getDirectory(worldDirectory.getParent().toFile()).equals(worldDirectory.toFile())) {
                return FutureUtil.completedWithException(new IOException(String.format("World '%s' was registered with a different directory on "
                        + "this server instance before. Aborting...", properties.getKey())));
            }
        } else {
            dimensionType = this.createDimensionType(properties.getKey(), (SpongeDimensionType) properties.getDimensionType(),
                    worldDirectory.getFileName().toString(), ((SimpleRegistryAccessor) Registry.DIMENSION_TYPE).accessor$nextId());
        }

        final WorldInfo worldInfo = (WorldInfo) properties;

        final SpongeDimensionType logicType = ((IServerWorldInfoBridge) properties).bridge$getDimensionType();

        ((DimensionTypeBridge) dimensionType).bridge$setSpongeDimensionType(logicType);

        MinecraftServerAccessor.accessor$LOGGER().info("Loading World '{}' ({})", properties.getKey(), logicType.getKey()
                .getFormatted());

        final InheritableConfigHandle<WorldConfig> adapter = SpongeGameConfigs.createWorld(logicType, properties.getKey());
        ((IServerWorldInfoBridge) properties).bridge$setConfigAdapter(adapter);

        final IChunkStatusListener chunkStatusListener = ((MinecraftServerAccessor_Vanilla) this.server).accessor$getProgressListenerFactory().create(11);

        world = new ServerWorld(this.server, this.server.getBackgroundExecutor(), saveHandler, worldInfo,
                dimensionType, this.server.getProfiler(), chunkStatusListener);

        this.loadedWorldInfos.put(properties.getKey(), worldInfo);
        this.infoByType.put(dimensionType, worldInfo);
        this.allInfos.put(properties.getKey(), worldInfo);
        this.worlds.put(properties.getKey(), world);
        this.worldsByType.put(dimensionType, world);

        this.performPostLoadWorldLogic(world, this.createDefaultSettings(null, false, worldInfo.getSeed(), worldInfo.getGenerator(), null),
                worldDirectory, chunkStatusListener);

        return CompletableFuture.completedFuture((org.spongepowered.api.world.server.ServerWorld) world);
    }

    @Override
    public CompletableFuture<Boolean> unloadWorld(final ResourceKey key) {
        Objects.requireNonNull(key);

        if (key.equals(VanillaWorldManager.VANILLA_OVERWORLD)) {
            return FutureUtil.completedWithException(new IOException("The default world can not be unloaded"));
        }

        final ServerWorld world = this.worlds.get(key);
        if (world == null) {
            return CompletableFuture.completedFuture(false);
        }

        return this.unloadWorld((org.spongepowered.api.world.server.ServerWorld) world);
    }

    @Override
    public CompletableFuture<Boolean> unloadWorld(final org.spongepowered.api.world.server.ServerWorld world) {
        Objects.requireNonNull(world);

        if (world.getKey().equals(VanillaWorldManager.VANILLA_OVERWORLD)) {
            return FutureUtil.completedWithException(new IOException("The default world can not be unloaded."));
        }

        if (world != this.worlds.get(world.getKey())) {
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
    public Optional<WorldProperties> getProperties(final ResourceKey key) {
        Objects.requireNonNull(key);

        return (Optional<WorldProperties>) (Object) Optional.ofNullable(this.allInfos.get(key));
    }

    @Override
    public Collection<WorldProperties> getUnloadedProperties() {
        final List<WorldProperties> unloadedProperties = new ArrayList<>();
        for (final WorldInfo worldInfo : this.allInfos.values()) {
            boolean unloaded = true;
            for (final ServerWorld value : this.worlds.values()) {
                if (value.getWorldInfo() == worldInfo) {
                    unloaded = false;
                    break;
                }
            }

            if (unloaded) {
                unloadedProperties.add((WorldProperties) worldInfo);
            }
        }

        return unloadedProperties;
    }

    @Override
    public Collection<WorldProperties> getAllProperties() {
        return (Collection<WorldProperties>) (Object) Collections.unmodifiableCollection(this.allInfos.values());
    }

    @Override
    public CompletableFuture<Boolean> saveProperties(final WorldProperties properties) {
        Objects.requireNonNull(properties);

        final Path worldDirectory =
                ((SaveFormatAccessor_Vanilla) this.server.getActiveAnvilConverter()).accessor$getBaseDir().resolve(this.server.getFolderName())
                        .resolve(properties.getKey().getValue());

        final SaveFormat saveFormat = new SaveFormat(worldDirectory.getParent(), worldDirectory.getParent().getParent().resolve("backups"),
                this.server.getDataFixer());
        final SaveHandler saveHandler = saveFormat.getSaveLoader(this.getDirectoryName(properties.getKey()), this.server);
        saveHandler.saveWorldInfo((WorldInfo) properties);

        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<WorldProperties> copyWorld(final ResourceKey key, final ResourceKey copyKey) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(copyKey, "copyKey");

        if (VanillaWorldManager.VANILLA_OVERWORLD.equals(copyKey)) {
            throw new IllegalArgumentException(String.format("The copy key '%s' cannot be the default world!", copyKey));
        }

        if (this.worlds.containsKey(copyKey)) {
            return FutureUtil.completedWithException(new IllegalStateException(String.format("The copy key '%s' is a currently loaded world!",
                    copyKey)));
        }

        final ServerWorld loadedWorld = this.worlds.get(key);
        boolean disableLevelSaving = false;

        if (loadedWorld != null) {
            try {
                disableLevelSaving = loadedWorld.disableLevelSaving;
                loadedWorld.save(null, true, loadedWorld.disableLevelSaving);
            } catch (final SessionLockException e) {
                FutureUtil.completedWithException(e);
            }
            loadedWorld.disableLevelSaving = true;
        }

        final boolean isSinglePlayer = this.server.isSinglePlayer();
        final Path savesDirectory = ((SaveFormatAccessor_Vanilla) this.server.getActiveAnvilConverter()).accessor$getBaseDir();
        final Path gameDirectory = !isSinglePlayer ? savesDirectory : savesDirectory.getParent();
        final Path levelDirectory = savesDirectory.resolve(this.server.getFolderName());

        final String directoryName = this.getDirectoryName(key);
        final boolean isDefaultWorld = VanillaWorldManager.VANILLA_OVERWORLD.equals(key);
        final boolean isVanillaSubWorld = this.isVanillaSubWorld(directoryName);

        final Path originalDirectory = isDefaultWorld ? levelDirectory : isVanillaSubWorld ? levelDirectory.resolve(directoryName) :
                levelDirectory.resolve(Constants.Sponge.World.DIMENSIONS_DIRECTORY).resolve(key.getNamespace()).resolve(key.getValue());

        final Path copyDirectory = levelDirectory.resolve(Constants.Sponge.World.DIMENSIONS_DIRECTORY).resolve(copyKey.getNamespace()).resolve(copyKey.getValue());

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
            loadedWorld.disableLevelSaving = disableLevelSaving;
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
                    spongeDataCompound.removeUniqueId(Constants.Sponge.World.UNIQUE_ID);
                    CompressedStreamTools.writeCompressed(spongeLevelCompound, Files.newOutputStream(copiedSpongeLevelFile));
                }
            } catch (final IOException e) {
                return FutureUtil.completedWithException(e);
            }
        }

        final SaveFormat saveFormat = new SaveFormat(copyDirectory.getParent(), gameDirectory.resolve("backups"), this.server.getDataFixer());
        final SaveHandler saveHandler = saveFormat.getSaveLoader(this.getDirectoryName(copyKey), this.server);

        return CompletableFuture.completedFuture((WorldProperties) saveHandler.loadWorldInfo());
    }

    @Override
    public CompletableFuture<WorldProperties> renameWorld(final ResourceKey key, final String newValue) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(newValue, "newValue");

        if (VanillaWorldManager.VANILLA_OVERWORLD.equals(key)) {
            throw new IllegalArgumentException("The default world cannot be renamed!");
        }

        final ResourceKey renamedKey = ResourceKey.of(key.getNamespace(), newValue);
        if (this.worlds.containsKey(renamedKey)) {
            return CompletableFuture.completedFuture(null);
        }

        ServerWorld loadedWorld = this.worlds.get(key);
        if (loadedWorld != null) {
            try {
                this.unloadWorld0(loadedWorld);
            } catch (final IOException e) {
                return FutureUtil.completedWithException(e);
            }
        }

        this.allInfos.remove(key);

        final boolean isSinglePlayer = this.server.isSinglePlayer();
        final Path savesDirectory = ((SaveFormatAccessor_Vanilla) this.server.getActiveAnvilConverter()).accessor$getBaseDir();
        final Path gameDirectory = !isSinglePlayer ? savesDirectory : savesDirectory.getParent();
        final Path levelDirectory = savesDirectory.resolve(this.server.getFolderName());

        final String directoryName = this.getDirectoryName(key);
        final boolean isDefaultWorld = VanillaWorldManager.VANILLA_OVERWORLD.equals(key);
        final boolean isVanillaSubWorld = this.isVanillaSubWorld(directoryName);

        final Path originalDirectory = isDefaultWorld ? levelDirectory : isVanillaSubWorld ? levelDirectory.resolve(directoryName) :
                levelDirectory.resolve(Constants.Sponge.World.DIMENSIONS_DIRECTORY).resolve(key.getNamespace()).resolve(key.getValue());

        final Path renamedDirectory =
                levelDirectory.resolve(Constants.Sponge.World.DIMENSIONS_DIRECTORY).resolve(renamedKey.getNamespace()).resolve(renamedKey.getValue());

        try {
            Files.createDirectories(renamedDirectory);
            Files.move(originalDirectory, renamedDirectory, StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            return FutureUtil.completedWithException(e);
        }

        final Path configFile = SpongeCommon.getSpongeConfigDirectory().resolve(SpongeCommon.ECOSYSTEM_ID).resolve("worlds").resolve(key
                .getNamespace()).resolve(key.getValue() + ".conf");

        final Path renamedConfigFile = SpongeCommon.getSpongeConfigDirectory().resolve(SpongeCommon.ECOSYSTEM_ID).resolve("worlds")
                .resolve(renamedKey.getNamespace()).resolve(renamedKey.getValue() + ".conf");

        try {
            Files.createDirectories(renamedConfigFile.getParent());
            Files.move(configFile, renamedConfigFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            return FutureUtil.completedWithException(e);
        }

        final SaveFormat saveFormat = new SaveFormat(renamedDirectory.getParent(), gameDirectory.resolve("backups"), this.server.getDataFixer());
        final SaveHandler saveHandler = saveFormat.getSaveLoader(this.getDirectoryName(renamedKey), this.server);

        return CompletableFuture.completedFuture((WorldProperties) saveHandler.loadWorldInfo());
    }

    @Override
    public CompletableFuture<Boolean> deleteWorld(final ResourceKey key) {
        Objects.requireNonNull(key);

        if (VanillaWorldManager.VANILLA_OVERWORLD.equals(key)) {
            return CompletableFuture.completedFuture(false);
        }

        final ServerWorld loadedWorld = this.worlds.get(key);
        if (loadedWorld != null) {
            boolean disableLevelSaving = loadedWorld.disableLevelSaving;
            loadedWorld.disableLevelSaving = true;
            try {
                this.unloadWorld0(loadedWorld);
            } catch (final IOException e) {
                loadedWorld.disableLevelSaving = disableLevelSaving;
                return FutureUtil.completedWithException(e);
            }
        }

        this.allInfos.remove(key);

        final Path savesDirectory = ((SaveFormatAccessor_Vanilla) this.server.getActiveAnvilConverter()).accessor$getBaseDir();
        final Path levelDirectory = savesDirectory.resolve(this.server.getFolderName());

        final String directoryName = this.getDirectoryName(key);
        final boolean isDefaultWorld = VanillaWorldManager.VANILLA_OVERWORLD.equals(key);
        final boolean isVanillaSubWorld = this.isVanillaSubWorld(directoryName);

        final Path worldDirectory = isDefaultWorld ? levelDirectory : isVanillaSubWorld ? levelDirectory.resolve(directoryName) :
                levelDirectory.resolve(Constants.Sponge.World.DIMENSIONS_DIRECTORY).resolve(key.getNamespace()).resolve(key.getValue());

        try {
            Files.walk(worldDirectory)
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
    public boolean registerPendingWorld(final ResourceKey key, @Nullable final WorldArchetype archetype) {
        Objects.requireNonNull(key);

        if (this.pendingWorlds.containsKey(key)) {
            return false;
        }

        this.pendingWorlds.put(key, new WorldRegistration(key, null, (WorldSettings) (Object) archetype));
        return true;
    }

    @Override
    @Nullable
    public ServerWorld getWorld(final DimensionType dimensionType) {
        Objects.requireNonNull(dimensionType);

        return this.worldsByType.get(dimensionType);
    }

    @Override
    @Nullable
    public ServerWorld getWorld0(final ResourceKey key) {
        if (key == null) {
            return null;
        }

        return this.worlds.get(key);
    }

    @Override
    public void unloadWorld0(final ServerWorld world) throws IOException {
        final ResourceLocation key = (ResourceLocation) (Object) ((ResourceKeyBridge) world.getWorldInfo()).bridge$getKey();

        if (world.getPlayers().size() != 0) {
            throw new IOException(String.format("World '%s' was told to unload but players remain.", key));
        }

        SpongeCommon.getLogger().info("Unloading World '{}' ({})", key, ((ResourceKeyBridge) world.dimension.getType()).bridge$getKey());

        final BlockPos spawnPoint = world.getSpawnPoint();
        world.getChunkProvider().releaseTicket(VanillaWorldManager.SPAWN_CHUNKS, new ChunkPos(spawnPoint), 11, key);

        ((IServerWorldInfoBridge) world.getWorldInfo()).bridge$getConfigAdapter().save();
        ((ServerWorldBridge) world).bridge$setManualSave(true);

        try {
            world.save(null, true, world.disableLevelSaving);
            world.close();
        } catch (final Exception ex) {
            throw new IOException(ex);
        }

        this.loadedWorldInfos.remove(key);
        this.infoByType.remove(world.dimension.getType());
        this.worlds.remove(key);
        this.worldsByType.remove(world.dimension.getType());

        SpongeCommon.postEvent(SpongeEventFactory.createUnloadWorldEvent(PhaseTracker.getCauseStackManager().getCurrentCause(),
                (org.spongepowered.api.world.server.ServerWorld) world));
    }

    @Override
    @Nullable
    public ServerWorld getDefaultWorld() {
        return this.worlds.get(VanillaWorldManager.VANILLA_OVERWORLD);
    }

    @Override
    public void adjustWorldForDifficulty(final ServerWorld world, final Difficulty newDifficulty, final boolean forceDifficulty) {
        if (world.getWorldInfo().isDifficultyLocked() && !forceDifficulty) {
            return;
        }

        if (forceDifficulty) {
            // Don't allow vanilla forcing the difficulty at launch set ours if we have a custom one
            if (!((IServerWorldInfoBridge) world.getWorldInfo()).bridge$hasCustomDifficulty()) {
                ((IServerWorldInfoBridge) world.getWorldInfo()).bridge$forceSetDifficulty(newDifficulty);
            }
        } else {
            world.getWorldInfo().setDifficulty(newDifficulty);
        }
    }

    @Override
    public void loadAllWorlds(final String saveName, final String levelName, final long seed, final WorldType type, final JsonElement generatorOptions,
            final boolean isSinglePlayer, @Nullable WorldSettings defaultSettings, final Difficulty defaultDifficulty) {

        ((MinecraftServerAccessor_Vanilla) this.server).accessor$convertMapIfNeeded(saveName);

        SpongeCommon.postEvent(new RegisterWorldEventImpl(PhaseTracker.getCauseStackManager().getCurrentCause(), SpongeCommon.getGame(), this));

        final Path savesDirectory = ((SaveFormatAccessor_Vanilla) this.server.getActiveAnvilConverter()).accessor$getBaseDir();
        final Path gameDirectory = !isSinglePlayer ? savesDirectory : savesDirectory.getParent();
        final Path levelDirectory = savesDirectory.resolve(saveName);

        if (!isSinglePlayer) {
            // Symlink needs special handling
            try {
                if (Files.isSymbolicLink(levelDirectory)) {
                    final Path actualPathLink = Files.readSymbolicLink(levelDirectory);
                    if (Files.notExists(actualPathLink)) {
                        Files.createDirectories(actualPathLink);
                    } else if (!Files.isDirectory(actualPathLink)) {
                        throw new IOException(String.format("Worlds directory '%s' symlink to '%s' is not a directory!", levelDirectory,
                                actualPathLink));
                    }
                } else {
                    Files.createDirectories(levelDirectory);
                }
            } catch (final IOException ex) {
                throw new RuntimeException(String.format("Could not process symlink for world container '%s'!", levelName), ex);
            }

            if (!this.server.getAllowNether()) {
                SpongeCommon.getLogger().warn("The option 'allow-nether' has been set to 'false' in the server.properties. "
                        + "Multi-World support has been disabled and no worlds besides the default world will be loaded.");
            }
        }

        final Path dimensionsDirectory = levelDirectory.resolve("dimensions");

        try {
            Files.createDirectories(dimensionsDirectory);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        try {
            this.loadExistingWorldRegistrations(dimensionsDirectory);
        } catch (final IOException e) {
            throw new RuntimeException("Exception caught registering existing Sponge worlds!", e);
        }

        ((MinecraftServerAccessor_Vanilla) this.server).accessor$setUserMessage(new TranslationTextComponent("menu.loadingLevel"));

        for (final Map.Entry<ResourceKey, WorldRegistration> entry : this.pendingWorlds.entrySet()) {
            final ResourceKey key = entry.getKey();
            final WorldRegistration worldRegistration = entry.getValue();

            final boolean isDefaultWorld = VanillaWorldManager.VANILLA_OVERWORLD.equals(key);

            if (!isDefaultWorld && !isSinglePlayer && !this.server.getAllowNether()) {
                continue;
            }

            final String directoryName = this.getDirectoryName(key);
            final boolean isVanillaSubWorld = this.isVanillaSubWorld(directoryName);

            final Path worldDirectory = isDefaultWorld ? levelDirectory : isVanillaSubWorld ? levelDirectory.resolve(directoryName) :
                    dimensionsDirectory.resolve(key.getNamespace()).resolve(directoryName);

            DimensionType dimensionType = Registry.DIMENSION_TYPE.getValue((ResourceLocation) (Object) key).orElse(null);
            if (dimensionType == null) {
                dimensionType = this.createDimensionType(worldRegistration.getKey(),
                        (SpongeDimensionType) ((WorldArchetype) (Object) worldRegistration.getDefaultSettings()).getDimensionType(),
                        worldDirectory.getFileName().toString(), ((SimpleRegistryAccessor) Registry.DIMENSION_TYPE).accessor$nextId());
            }

            final SpongeDimensionType logicType = ((DimensionTypeBridge) dimensionType).bridge$getSpongeDimensionType();

            MinecraftServerAccessor.accessor$LOGGER().info("Loading World '{}' ({})", key, logicType.getKey().getFormatted());

            final boolean configExists = SpongeGameConfigs.doesWorldConfigExist(key);

            final InheritableConfigHandle<WorldConfig> configAdapter = SpongeGameConfigs.createWorld(logicType, key);
            if (!isDefaultWorld) {
                if (!configAdapter.get().getWorld().isWorldEnabled()) {
                    SpongeCommon.getLogger().warn("World '{}' ({}) has been disabled in the configuration. World will not be loaded...", key,
                            logicType.getKey().getFormatted());
                    continue;
                }
            }

            final SaveFormat saveFormat;
            final SaveHandler saveHandler;

            if (isDefaultWorld) {
                saveFormat = this.server.getActiveAnvilConverter();
                saveHandler = saveFormat.getSaveLoader(saveName, this.server);
            } else {
                saveFormat = new SaveFormat(worldDirectory.getParent(), gameDirectory.resolve("backups"), this.server.getDataFixer());
                saveHandler = saveFormat.getSaveLoader(directoryName, this.server);
            }

            WorldInfo worldInfo = saveHandler.loadWorldInfo();

            if (worldInfo == null) {

                if (worldRegistration.getDefaultSettings() != null) {
                    defaultSettings = worldRegistration.getDefaultSettings();
                }

                // Demo code does not run on SinglePlayer

                // The pass off of the config adapter here is done because creation of a WorldInfo that isn't from disk will use the
                // WorldSettings in the constructor to set the values. Which will then trigger the creation of the config handle
                // which will be a different handle than the above adapter. Easiest is to store the reference on the settings and set it
                // during population of the WorldInfo from the WorldSettings

                // Additionally, if we have no WorldInfo but we do have a config file, it is likely the admin decided to regenerate their
                // world. We want to respect that file. Setting this flag will cause WorldSettings to skip populating the WorldInfo with
                // these Sponge-specific default settings

                if (isSinglePlayer) {
                    ((WorldSettingsBridge) (Object) defaultSettings).bridge$setInfoConfigAdapter(configAdapter);
                    ((WorldSettingsBridge) (Object) defaultSettings).bridge$setConfigExists(configExists);
                    worldInfo = new WorldInfo(defaultSettings, isDefaultWorld ? levelName : this.getDirectoryName(key));
                } else {
                    if (defaultSettings == null) {
                        defaultSettings = this.createDefaultSettings(defaultSettings, isDefaultWorld, seed, type, generatorOptions);
                    }

                    ((WorldSettingsBridge) (Object) defaultSettings).bridge$setInfoConfigAdapter(configAdapter);
                    ((WorldSettingsBridge) (Object) defaultSettings).bridge$setConfigExists(configExists);
                    worldInfo = new WorldInfo(defaultSettings, isDefaultWorld ? levelName : this.getDirectoryName(key));
                }
                ((ResourceKeyBridge) worldInfo).bridge$setKey(worldRegistration.getKey());

                ((IServerWorldInfoBridge) worldInfo).bridge$setConfigAdapter(configAdapter);

                ((IServerWorldInfoBridge) worldInfo).bridge$setUniqueId(UUID.randomUUID());

                SpongeCommon.postEvent(SpongeEventFactory.createConstructWorldPropertiesEvent(
                        PhaseTracker.getCauseStackManager().getCurrentCause(),
                        (WorldArchetype) (Object) defaultSettings, (WorldProperties) worldInfo));
            } else {
                ((IServerWorldInfoBridge) worldInfo).bridge$setConfigAdapter(configAdapter);
                worldInfo.setWorldName(isDefaultWorld ? saveName : this.getDirectoryName(key));

                // This may be an existing world created before Sponge was installed, handle accordingly
                if (((ResourceKeyBridge) worldInfo).bridge$getKey() == null) {
                    ((ResourceKeyBridge) worldInfo).bridge$setKey(worldRegistration.getKey());
                    ((IServerWorldInfoBridge) worldInfo).bridge$setUniqueId(UUID.randomUUID());
                }

                defaultSettings = new WorldSettings(worldInfo);
            }

            if (((IServerWorldInfoBridge) worldInfo).bridge$getDimensionType() != null) {
                ((DimensionTypeBridge) dimensionType).bridge$setSpongeDimensionType(((IServerWorldInfoBridge) worldInfo).bridge$getDimensionType());
            } else {
                ((IServerWorldInfoBridge) worldInfo).bridge$setDimensionType(((DimensionTypeBridge) dimensionType).bridge$getSpongeDimensionType(), false);
            }

            if (isDefaultWorld) {
                ((MinecraftServerAccessor_Vanilla) this.server).accessor$loadDataPacks(worldDirectory.toFile(), worldInfo);
            }

            this.loadedWorldInfos.put(key, worldInfo);
            this.infoByType.put(dimensionType, worldInfo);
            this.allInfos.put(key, worldInfo);

            if (!isDefaultWorld && !((WorldProperties) worldInfo).doesLoadOnStartup()) {
                SpongeCommon.getLogger().warn("World '{}' ({}) has been set to not load on startup in the configuration. Skipping...", key,
                        logicType.getKey().getFormatted());
                continue;
            }

            final IChunkStatusListener chunkStatusListener = ((MinecraftServerAccessor_Vanilla) this.server).accessor$getProgressListenerFactory().create(11);

            worldInfo.func_230145_a_(this.server.getServerModName(), this.server.func_230045_q_().isPresent());

            final ServerWorld serverWorld = new ServerWorld(this.server, this.server.getBackgroundExecutor(), saveHandler, worldInfo,
                    dimensionType, this.server.getProfiler(), chunkStatusListener);

            this.worlds.put(key, serverWorld);
            this.worldsByType.put(dimensionType, serverWorld);

            this.performPostLoadWorldLogic(serverWorld, defaultSettings, worldDirectory, chunkStatusListener);
        }

        this.pendingWorlds.clear();

        if (this.server.isSinglePlayer()) {
            this.server.setDifficultyForAllWorlds(defaultDifficulty, true);
        } else {
            this.server.setDifficultyForAllWorlds(this.server.getDifficulty(), true);
        }

        ((SpongeServer) SpongeCommon.getServer()).getPlayerDataManager().load();
    }

    private void clearCustomWorldDimensions() {
        final List<DimensionType> customDimensions = new ArrayList<>();
        for (DimensionType next : Registry.DIMENSION_TYPE) {
            if ((next.getId() + 1) > 2) {
                customDimensions.add(next);
            }
        }

        ((SimpleRegistryBridge) Registry.DIMENSION_TYPE).bridge$removeAll(customDimensions);
    }

    private void loadSpawnChunks(final ServerWorld serverWorld, final IChunkStatusListener chunkStatusListener) {
        ((MinecraftServerAccessor_Vanilla) this.server).accessor$setUserMessage(new TranslationTextComponent("menu.generatingTerrain"));
        final org.spongepowered.api.world.server.ServerWorld apiWorld = (org.spongepowered.api.world.server.ServerWorld) serverWorld;
        MinecraftServerAccessor.accessor$LOGGER().info("Preparing start region for world '{}' ({})", apiWorld.getKey(),
                apiWorld.getProperties().getDimensionType().getKey());
        final BlockPos spawnPoint = serverWorld.getSpawnPoint();
        final ChunkPos spawnChunkPos = new ChunkPos(spawnPoint);
        chunkStatusListener.start(spawnChunkPos);
        final ServerChunkProvider chunkProvider = serverWorld.getChunkProvider();
        chunkProvider.getLightManager().func_215598_a(500);
        ((MinecraftServerAccessor_Vanilla) this.server).accessor$setNextTickTime(Util.milliTime());
        chunkProvider.registerTicket(VanillaWorldManager.SPAWN_CHUNKS, spawnChunkPos, 11, (ResourceLocation) (Object) apiWorld.getKey());

        while (chunkProvider.getLoadedChunksCount() != 441) {
            ((MinecraftServerAccessor_Vanilla) this.server).accessor$setNextTickTime(Util.milliTime() + 10L);
            ((MinecraftServerAccessor_Vanilla) this.server).accessor$waitUntilNextTick();
        }

        ((MinecraftServerAccessor_Vanilla) this.server).accessor$setNextTickTime(Util.milliTime() + 10L);
        ((MinecraftServerAccessor_Vanilla) this.server).accessor$waitUntilNextTick();

        ForcedChunksSaveData forcedChunksSaveData = serverWorld.getSavedData().get(ForcedChunksSaveData::new, "chunks");
        if (forcedChunksSaveData != null) {
            LongIterator longIterator = forcedChunksSaveData.getChunks().iterator();

            while (longIterator.hasNext()) {
                final long i = longIterator.nextLong();
                final ChunkPos chunkpos = new ChunkPos(i);
                serverWorld.getChunkProvider().forceChunk(chunkpos, true);
            }
        }

        ((MinecraftServerAccessor_Vanilla) this.server).accessor$setNextTickTime(Util.milliTime() + 10L);
        ((MinecraftServerAccessor_Vanilla) this.server).accessor$waitUntilNextTick();
        chunkStatusListener.stop();
        chunkProvider.getLightManager().func_215598_a(5);

        // Sponge Start - Release the chunk ticket if spawn is not set to be kept loaded...
        if (!((IServerWorldInfoBridge) serverWorld.getWorldInfo()).bridge$doesKeepSpawnLoaded()) {
            chunkProvider.releaseTicket(VanillaWorldManager.SPAWN_CHUNKS, spawnChunkPos, 11, (ResourceLocation) (Object) apiWorld.getKey());
        }
    }

    private DimensionType createDimensionType(final ResourceKey key, final SpongeDimensionType logicType, final String worldDirectory,
            final int dimensionId) {
        final DimensionType registeredType = DimensionTypeAccessor.accessor$construct(dimensionId, "", worldDirectory, logicType.getDimensionFactory(),
                logicType.hasSkylight(), logicType == DimensionTypes.OVERWORLD.get() ? ColumnFuzzedBiomeMagnifier.INSTANCE : FuzzedBiomeMagnifier.INSTANCE);
        DimensionTypeAccessor.accessor$register(key.getFormatted(), registeredType);

        ((DimensionTypeBridge) registeredType).bridge$setSpongeDimensionType(logicType);
        return registeredType;
    }

    private void loadExistingWorldRegistrations(final Path dimensionsDirectory) throws IOException {
        for (final Path namespacedDirectory : Files.walk(dimensionsDirectory, 1).filter(o -> !dimensionsDirectory.equals(o) && Files.isDirectory(o)).collect(Collectors.toList())) {
            for (final Path valueDirectory : Files.walk(namespacedDirectory, 1).filter(i -> !namespacedDirectory.equals(i) && Files.exists(i.resolve(Constants.Sponge.World.LEVEL_SPONGE_DAT))).collect(Collectors.toList()))
            {
                final ResourceKey key = ResourceKey.of(namespacedDirectory.getFileName().toString(), valueDirectory.getFileName().toString());

                final CompoundNBT spongeCompound =
                        CompressedStreamTools.readCompressed(Files.newInputStream(valueDirectory.resolve(Constants.Sponge.World.LEVEL_SPONGE_DAT)));

                final CompoundNBT spongeDataCompound = spongeCompound.getCompound(Constants.Sponge.SPONGE_DATA);

                final String rawLogicType = spongeDataCompound.getString(Constants.Sponge.World.DIMENSION_TYPE);

                SpongeDimensionType logicType = (SpongeDimensionType) SpongeCommon.getRegistry().getCatalogRegistry().get(org.spongepowered
                        .api.world.dimension.DimensionType.class, ResourceKey.resolve(rawLogicType)).orElse(null);

                if (logicType == null) {
                    SpongeCommon.getLogger().warn("World '{}' has an unknown dimension type '{}'. Falling back to '{}'.",
                            valueDirectory.getFileName().toString(), rawLogicType, DimensionTypes.OVERWORLD.get().getKey());
                    logicType = (SpongeDimensionType) DimensionTypes.OVERWORLD.get();
                }

                final DimensionType registeredType = this.createDimensionType(key, logicType, valueDirectory.getFileName().toString(),
                        ((SimpleRegistryAccessor) Registry.DIMENSION_TYPE).accessor$nextId());

                final WorldRegistration existingRegistration = this.pendingWorlds.get(key);
                if (existingRegistration != null) {
                    existingRegistration.setDimensionType(registeredType);
                } else {
                    this.pendingWorlds.put(key, new WorldRegistration(key, registeredType, null));
                }
            }
        }
    }

    private boolean isVanillaSubWorld(final String directoryName) {
        return "DIM-1".equals(directoryName) || "DIM1".equals(directoryName);
    }

    private WorldSettings createDefaultSettings(@Nullable WorldSettings providedSettings, final boolean isDefaultWorld, final long seed,
                                                final WorldType worldType, @Nullable final JsonElement generatorOptions) {
        // Pure fresh Vanilla worlds situation (plugins registering worlds to load before now *must* give us an archetype)
        if (providedSettings == null) {
            if (this.server.isDemo()) {
                providedSettings = MinecraftServer.DEMO_WORLD_SETTINGS;
            } else {
                providedSettings =
                        new WorldSettings(seed, this.server.getGameType(), this.server.canStructuresSpawn(), this.server.isHardcore(), worldType);
                if (isDefaultWorld) {
                    providedSettings.setGeneratorOptions(generatorOptions);
                    if (((MinecraftServerAccessor_Vanilla) this.server).accessor$getEnableBonusChest()) {
                        providedSettings.enableBonusChest();
                    }
                }
            }
        }

        return providedSettings;
    }

    private void performPostLoadWorldLogic(final ServerWorld serverWorld, final WorldSettings defaultSettings, final Path worldDirectory,
            final IChunkStatusListener listener) {

        final boolean isDefaultWorld = serverWorld.getDimension().getType() == DimensionType.OVERWORLD;

        if (serverWorld.getWorldInfo().getGameType() == GameType.NOT_SET) {
            serverWorld.getWorldInfo().setGameType(this.server.getGameType());
        }

        if (isDefaultWorld) {
            // Initialize scoreboard data. This will hook to the ServerScoreboard, needs to be made multi-world aware
            ((MinecraftServerAccessor_Vanilla) this.server).accessor$readScoreboard(serverWorld.getSavedData());

            ((MinecraftServerAccessor) this.server).accessor$commandStorage(new CommandStorage(serverWorld.getSavedData()));
        }

        serverWorld.getWorldBorder().copyFrom(serverWorld.getWorldInfo());
        if (!serverWorld.getWorldInfo().isInitialized()) {
            try {
                serverWorld.createSpawnPosition(defaultSettings);
                if (serverWorld.getWorldInfo().getGenerator() == WorldType.DEBUG_ALL_BLOCK_STATES) {
                    ((MinecraftServerAccessor_Vanilla) this.server).accessor$applyDebugWorldInfo(serverWorld.getWorldInfo());
                }
            } catch (final Throwable throwable) {
                final CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Exception initializing world '" + worldDirectory
                        + "'");
                try {
                    serverWorld.fillCrashReport(crashReport);
                } catch (Throwable ignore) {
                }

                throw new ReportedException(crashReport);
            } finally {
                serverWorld.getWorldInfo().setInitialized(true);
            }
        }

        // Initialize PlayerData in PlayerList, add WorldBorder listener. We change the method in PlayerList to handle per-world border
        this.server.getPlayerList().func_212504_a(serverWorld);
        if (isDefaultWorld) {
            ((SpongeUserManager) ((Server) this.server).getUserManager()).init();
        }

        if (serverWorld.getWorldInfo().getCustomBossEvents() != null) {
            ((ServerWorldBridge) serverWorld).bridge$getBossBarManager().read(serverWorld.getWorldInfo().getCustomBossEvents());
        }

        final org.spongepowered.api.world.server.ServerWorld apiWorld = (org.spongepowered.api.world.server.ServerWorld) serverWorld;
        SpongeCommon.postEvent(SpongeEventFactory.createLoadWorldEvent(PhaseTracker.getCauseStackManager().getCurrentCause(),
                apiWorld));

        final boolean generateSpawnOnLoad = ((IServerWorldInfoBridge) serverWorld.getWorldInfo()).bridge$doesGenerateSpawnOnLoad() || isDefaultWorld;

        if (generateSpawnOnLoad) {
            this.loadSpawnChunks(serverWorld, listener);
        } else {
            serverWorld.getChunkProvider().registerTicket(VanillaWorldManager.SPAWN_CHUNKS, new ChunkPos(apiWorld.getProperties().getSpawnPosition()
                            .getX(), apiWorld.getProperties().getSpawnPosition().getZ()), 11, (ResourceLocation) (Object) apiWorld.getKey());
        }
    }

    private boolean isDirectoryEmpty(final Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            return false;
        }

        try (final DirectoryStream<Path> dir = Files.newDirectoryStream(directory)) {
            return !dir.iterator().hasNext();
        }
    }
}
