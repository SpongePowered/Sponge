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
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Unit;
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
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.SessionLockException;
import net.minecraft.world.storage.WorldInfo;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeServer;
import org.spongepowered.common.bridge.ResourceKeyBridge;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.bridge.world.WorldSettingsBridge;
import org.spongepowered.common.bridge.world.dimension.DimensionTypeBridge;
import org.spongepowered.common.bridge.world.storage.WorldInfoBridge;
import org.spongepowered.common.config.InheritableConfigHandle;
import org.spongepowered.common.config.SpongeConfigs;
import org.spongepowered.common.accessor.util.registry.SimpleRegistryAccessor;
import org.spongepowered.common.accessor.world.dimension.DimensionTypeAccessor;
import org.spongepowered.common.config.inheritable.WorldConfig;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.user.SpongeUserManager;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.world.dimension.SpongeDimensionType;
import org.spongepowered.common.world.server.SpongeWorldManager;
import org.spongepowered.common.world.server.WorldRegistration;
import org.spongepowered.vanilla.accessor.world.storage.SaveFormatAccessor_Vanilla;
import org.spongepowered.vanilla.accessor.server.MinecraftServerAccessor_Vanilla;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

    public VanillaWorldManager(final MinecraftServer server) {
        this.server = server;
        this.savesDirectory = ((MinecraftServerAccessor_Vanilla) server).accessor$getAnvilFile().toPath().resolve(server.getFolderName());
        this.worlds = new Object2ObjectOpenHashMap<>();
        this.worldsByType = ((MinecraftServerAccessor_Vanilla) server).accessor$getWorlds();
        this.loadedWorldInfos = new Object2ObjectOpenHashMap<>();
        this.infoByType = new IdentityHashMap<>();
        this.pendingWorlds = new LinkedHashMap<>();
        this.allInfos = new LinkedHashMap<>();

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
    public CompletableFuture<Optional<WorldProperties>> createProperties(final ResourceKey key, final WorldArchetype archetype) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(archetype);

        final WorldInfo worldInfo = new WorldInfo((WorldSettings) (Object) archetype, key.getValue());
        ((ResourceKeyBridge) worldInfo).bridge$setKey(key);
        ((WorldInfoBridge) worldInfo).bridge$setUniqueId(UUID.randomUUID());
        ((WorldInfoBridge) worldInfo).bridge$setModCreated(true);

        return CompletableFuture.completedFuture(Optional.of((WorldProperties) worldInfo));
    }

    @Override
    public CompletableFuture<Optional<org.spongepowered.api.world.server.ServerWorld>> loadWorld(final ResourceKey key) {
        Objects.requireNonNull(key);

        ServerWorld world = worlds.get(key);
        if (world != null) {
            return CompletableFuture.completedFuture(Optional.of((org.spongepowered.api.world.server.ServerWorld) world));
        }

        final Path worldDirectory = ((SaveFormatAccessor_Vanilla) this.server.getActiveAnvilConverter()).accessor$getSavesDir().resolve(this.server.getFolderName()).resolve(key.getValue());

        if (Files.notExists(worldDirectory)) {
            SpongeCommon.getLogger().error("World '{}' has no directory in '{}'. Aborting...", key, worldDirectory.getParent().toAbsolutePath());
            return CompletableFuture.completedFuture(Optional.empty());
        }

        if (Files.notExists(worldDirectory.resolve(Constants.Sponge.World.LEVEL_SPONGE_DAT))) {
            SpongeCommon.getLogger().error("World '{}' has no Sponge level data ({}). Aborting...", key, Constants.Sponge.World.LEVEL_SPONGE_DAT);
            return CompletableFuture.completedFuture(Optional.empty());
        }

        final SaveHandler saveHandler = new SaveHandler(worldDirectory.getParent().toFile(), key.getValue(), this.server, this.server.getDataFixer());
        final WorldInfo worldInfo = saveHandler.loadWorldInfo();
        final Integer dimensionId = ((WorldInfoBridge) worldInfo).bridge$getDimensionId();

        if (dimensionId == null) {
            SpongeCommon.getLogger().error("World '{}' has no dimension id in the Sponge level data ({}). Aborting...", key, Constants.Sponge
                    .World.LEVEL_SPONGE_DAT);
            return CompletableFuture.completedFuture(Optional.empty());
        }

        final ResourceKey existingKey = ((ResourceKeyBridge) worldInfo).bridge$getKey();
        if (existingKey != null && !existingKey.equals(key)) {
            SpongeCommon.getLogger().error("World '{}' is keyed as '{}' in the level data. Aborting...", key, existingKey);
            return CompletableFuture.completedFuture(Optional.empty());
        }

        ((ResourceKeyBridge) worldInfo).bridge$setKey(key);

        final SpongeDimensionType logicType = ((WorldInfoBridge) worldInfo).bridge$getLogicType();

        final DimensionType dimensionType = Registry.DIMENSION_TYPE.getValue((ResourceLocation) (Object) key).orElseGet(() -> this.
                createDimensionType(key, logicType, worldDirectory.getFileName().toString(), dimensionId + 1));

        if (dimensionType.getId() != dimensionId) {
            SpongeCommon.getLogger().error("World '{}' specifies internal id '{}' which was already registered. Aborting...", key, dimensionId);
            return CompletableFuture.completedFuture(Optional.empty());
        }

        ((DimensionTypeBridge) dimensionType).bridge$setSpongeDimensionType(logicType);

        MinecraftServerAccessor_Vanilla.accessor$getLogger().info("Loading World '{}' ({}/{})", key, logicType.getKey().getFormatted(), dimensionType.getId());

        final IChunkStatusListener chunkStatusListener = ((MinecraftServerAccessor_Vanilla) this.server).accessor$getChunkStatusListenerFactory().create(11);

        world = new ServerWorld(this.server, this.server.getBackgroundExecutor(), saveHandler, worldInfo,
                dimensionType, this.server.getProfiler(), chunkStatusListener);

        this.loadedWorldInfos.put(key, worldInfo);
        this.infoByType.put(dimensionType, worldInfo);
        this.allInfos.put(key, worldInfo);
        this.worlds.put(key, world);
        this.worldsByType.put(dimensionType, world);

        this.performPostLoadWorldLogic(world, this.createDefaultSettings(null, false, worldInfo.getSeed(), worldInfo.getGenerator(), null), worldDirectory, chunkStatusListener);

        return CompletableFuture.completedFuture(Optional.of((org.spongepowered.api.world.server.ServerWorld) world));
    }

    @Override
    public CompletableFuture<Optional<org.spongepowered.api.world.server.ServerWorld>> loadWorld(WorldProperties properties) {
        Objects.requireNonNull(properties);

        ServerWorld world = this.worlds.get(properties.getKey());
        if (world != null) {
            if (world.getWorldInfo() != properties) {
                SpongeCommon.getLogger().error("While properties '{}' already is a loaded world, that world's properties does not match. Aborting.."
                        + ".", properties.getKey());
                return CompletableFuture.completedFuture(Optional.empty());
            }

            return CompletableFuture.completedFuture(Optional.of((org.spongepowered.api.world.server.ServerWorld) world));
        }

        final Path worldDirectory =
                ((SaveFormatAccessor_Vanilla) this.server.getActiveAnvilConverter()).accessor$getSavesDir().resolve(this.server.getFolderName())
                        .resolve(properties.getKey().getValue());
        final boolean isOnDisk = Files.exists(worldDirectory);

        final SaveHandler saveHandler = new SaveHandler(worldDirectory.getParent().toFile(), properties.getKey().getValue(), this.server,
                this.server.getDataFixer());

        if (isOnDisk) {
            properties = (WorldProperties) saveHandler.loadWorldInfo();
        }

        DimensionType dimensionType = Registry.DIMENSION_TYPE.getValue((ResourceLocation) (Object) properties.getKey()).orElse(null);
        if (dimensionType != null) {
            // Validation checks
            if (isOnDisk && !dimensionType.getDirectory(worldDirectory.getParent().toFile()).equals(worldDirectory.toFile())) {
                SpongeCommon.getLogger().error("World '{}' was registered with a different directory on this server instance before. Aborting...",
                        properties.getKey());
                return CompletableFuture.completedFuture(Optional.empty());
            }

            if (((WorldInfoBridge) properties).bridge$getDimensionId() != null && dimensionType.getId() != (((WorldInfoBridge) properties).bridge$getDimensionId())) {
                SpongeCommon.getLogger().error("World '{}' was registered with a different internal id on this server instance before. Aborting...",
                        properties.getKey());
                return CompletableFuture.completedFuture(Optional.empty());
            }
        } else {
            int dimensionId;
            if (((WorldInfoBridge) properties).bridge$getDimensionId() != null) {
                dimensionType = DimensionType.getById(((WorldInfoBridge) properties).bridge$getDimensionId());
                if (dimensionType != null) {
                    SpongeCommon.getLogger().error("World '{}' is already registered under a different id. Aborting...",
                            properties.getKey());
                    return CompletableFuture.completedFuture(Optional.empty());
                }
                dimensionId = ((WorldInfoBridge) properties).bridge$getDimensionId();
            } else {
                dimensionId = ((SimpleRegistryAccessor) Registry.DIMENSION_TYPE).accessor$getNextFreeId();
            }

            dimensionType = this.createDimensionType(properties.getKey(), (SpongeDimensionType) properties.getDimensionType(),
                    worldDirectory.getFileName().toString(), dimensionId);
        }

        ((WorldInfoBridge) properties).bridge$setDimensionId(dimensionType);

        final WorldInfo worldInfo = (WorldInfo) properties;

        final SpongeDimensionType logicType = ((WorldInfoBridge) properties).bridge$getLogicType();

        ((DimensionTypeBridge) dimensionType).bridge$setSpongeDimensionType(logicType);

        MinecraftServerAccessor_Vanilla.accessor$getLogger().info("Loading World '{}' ({}/{})", properties.getKey(), logicType.getKey().getFormatted(),
                dimensionType.getId());

        final IChunkStatusListener chunkStatusListener = ((MinecraftServerAccessor_Vanilla) this.server).accessor$getChunkStatusListenerFactory().create(11);

        final ServerWorld serverWorld = new ServerWorld(this.server, this.server.getBackgroundExecutor(), saveHandler, worldInfo,
                dimensionType, this.server.getProfiler(), chunkStatusListener);

        this.loadedWorldInfos.put(properties.getKey(), worldInfo);
        this.infoByType.put(dimensionType, worldInfo);
        this.allInfos.put(properties.getKey(), worldInfo);
        this.worlds.put(properties.getKey(), serverWorld);
        this.worldsByType.put(dimensionType, serverWorld);

        this.performPostLoadWorldLogic(serverWorld, this.createDefaultSettings(null, false, worldInfo.getSeed(), worldInfo.getGenerator(), null), worldDirectory, chunkStatusListener);

        return CompletableFuture.completedFuture(Optional.of((org.spongepowered.api.world.server.ServerWorld) world));
    }

    @Override
    public CompletableFuture<Boolean> unloadWorld(final org.spongepowered.api.world.server.ServerWorld world) {
        Objects.requireNonNull(world);

        if (world.getKey() == VanillaWorldManager.VANILLA_OVERWORLD) {
            SpongeCommon.getLogger().error("The default world is not allowed to be unloaded. Aborting...");
            return CompletableFuture.completedFuture(false);
        }

        final ServerWorld actualWorld = this.worlds.get(world.getKey());
        if (actualWorld != world) {
            SpongeCommon.getLogger().error("World '{}' was told to unload but does not match the actual world loaded. "
                    + "Aborting...", world.getKey());
            return CompletableFuture.completedFuture(false);
        }

        try {
            actualWorld.save(null, true, true);
        } catch (final SessionLockException e) {
            SpongeCommon.getLogger().error("Exception caught when saving world '{}' while unloading. Aborting...", world.getKey(), e);
            return CompletableFuture.completedFuture(false);
        }

        try {
            actualWorld.close();
        } catch (IOException e) {
            SpongeCommon.getLogger().error("Exception caught when closing world '{}' while unloading. Aborting...", world.getKey());
            return CompletableFuture.completedFuture(false);
        }

        this.loadedWorldInfos.remove(world.getKey());
        this.infoByType.remove(actualWorld.dimension.getType());
        this.worlds.remove(world.getKey());
        this.worldsByType.remove(actualWorld.dimension.getType());
        
        SpongeCommon.postEvent(SpongeEventFactory.createUnloadWorldEvent(PhaseTracker.getCauseStackManager().getCurrentCause(), world));

        return CompletableFuture.completedFuture(true);
    }

    @Override
    public Optional<WorldProperties> getProperties(final ResourceKey key) {
        Objects.requireNonNull(key);

        return (Optional<WorldProperties>) (Object) Optional.ofNullable(this.loadedWorldInfos.get(key));
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
                ((SaveFormatAccessor_Vanilla) this.server.getActiveAnvilConverter()).accessor$getSavesDir().resolve(this.server.getFolderName())
                        .resolve(properties.getKey().getValue());

        final SaveHandler saveHandler = new SaveHandler(worldDirectory.getParent().toFile(), worldDirectory.getFileName().toString(), this.server,
                this.server.getDataFixer());
        saveHandler.saveWorldInfo((WorldInfo) properties);

        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Optional<WorldProperties>> copyWorld(final ResourceKey key, final String copyValue) {
        return null;
    }

    @Override
    public CompletableFuture<Optional<WorldProperties>> renameWorld(final ResourceKey key, final String newValue) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> deleteWorld(final ResourceKey key) {
        return null;
    }

    @Override
    public boolean registerPendingWorld(final ResourceKey key, @Nullable final WorldArchetype archetype) {
        Objects.requireNonNull(key);

        if (this.pendingWorlds.containsKey(key)) {
            return false;
        }

        DimensionType registeredType;

        if (archetype == null) {
            // Only null for the initial Vanilla registrations but I could create generic settings for them. I'll ponder it
            registeredType = VANILLA_THE_NETHER.equals(key) ? DimensionType.THE_NETHER : VANILLA_THE_END.equals(key) ? DimensionType.THE_END : DimensionType.OVERWORLD;
        } else {
            registeredType = this.createDimensionType(key, ((WorldSettingsBridge) (Object) archetype).bridge$getLogicType(), key.getValue(),
                    ((SimpleRegistryAccessor) Registry.DIMENSION_TYPE).accessor$getNextFreeId());
        }

        this.pendingWorlds.put(key, new WorldRegistration(key, registeredType, (WorldSettings) (Object) archetype));
        return true;
    }

    @Override
    public @Nullable ServerWorld getWorld(final DimensionType dimensionType) {
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
    public @Nullable ServerWorld getDefaultWorld() {
        return this.worlds.get(VanillaWorldManager.VANILLA_OVERWORLD);
    }

    @Override
    public void adjustWorldForDifficulty(final ServerWorld world, final Difficulty newDifficulty, final boolean isCustom) {

    }

    @Override
    public void loadAllWorlds(final String saveName, final String levelName, final long seed, final WorldType type, final JsonElement generatorOptions,
            final boolean isSinglePlayer, @Nullable WorldSettings defaultSettings, final Difficulty defaultDifficulty) {

        ((MinecraftServerAccessor_Vanilla) this.server).accessor$convertMapIfNeeded(saveName);

        try {
            this.loadExistingWorldRegistrations();
        } catch (IOException e) {
            SpongeCommon.getLogger().error("Exception caught registering existing Sponge worlds!", e);
        }

        final DataFixer dataFixer = ((SaveFormatAccessor_Vanilla) this.server.getActiveAnvilConverter()).accessor$getDataFixer();
        final Path savesDirectory = ((SaveFormatAccessor_Vanilla) this.server.getActiveAnvilConverter()).accessor$getSavesDir();

        final Path worldsDirectory = saveName.equals(levelName) ? savesDirectory : savesDirectory.resolve(levelName);

        if (!isSinglePlayer) {
            // Symlink needs special handling
            try {
                if (Files.isSymbolicLink(worldsDirectory)) {
                    final Path actualPathLink = Files.readSymbolicLink(worldsDirectory);
                    if (Files.notExists(actualPathLink)) {
                        Files.createDirectories(actualPathLink);
                    } else if (!Files.isDirectory(actualPathLink)) {
                        throw new IOException("Worlds directory '" + worldsDirectory + "' symlink to '" + actualPathLink + "' is not a directory!");
                    }
                } else {
                    Files.createDirectories(worldsDirectory);
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            if (!this.server.getAllowNether()) {
                SpongeCommon.getLogger().warn("The option 'allow-nether' has been set to 'false' in the server.properties. "
                        + "Multi-World support has been disabled and no worlds besides the default world will be loaded.");
            }
        }

        ((MinecraftServerAccessor_Vanilla) this.server).accessor$setUserMessage(new TranslationTextComponent("menu.loadingLevel"));

        for (Map.Entry<ResourceKey, WorldRegistration> entry : this.pendingWorlds.entrySet()) {
            final ResourceKey key = entry.getKey();
            final WorldRegistration worldRegistration = entry.getValue();

            final DimensionType dimensionType = worldRegistration.getDimensionType();
            final SpongeDimensionType logicType = ((DimensionTypeBridge) dimensionType).bridge$getSpongeDimensionType();

            final boolean isDefaultWorld = VanillaWorldManager.VANILLA_OVERWORLD.equals(key);

            if (!isDefaultWorld && !isSinglePlayer && !this.server.getAllowNether()) {
                continue;
            }

            final Path worldDirectory = isDefaultWorld ? worldsDirectory.resolve(saveName) : worldsDirectory.resolve(saveName).resolve(this.getDirectoryName(key));

            MinecraftServerAccessor_Vanilla.accessor$getLogger().info("Loading World '{}' ({}/{})", key, logicType.getKey().getFormatted(), dimensionType.getId());

            final InheritableConfigHandle<WorldConfig> configAdapter = SpongeConfigs.createWorld(logicType, key);
            if (!isDefaultWorld) {
                if (!configAdapter.get().getWorld().isWorldEnabled()) {
                    MinecraftServerAccessor_Vanilla.accessor$getLogger().warn("World '{}' ({}/{}) has been disabled in the configuration. "
                            + "World will not be loaded...", key, logicType.getKey().getFormatted(), dimensionType.getId());
                    continue;
                }
            }

            final SaveHandler saveHandler = new SaveHandler(worldDirectory.getParent().toFile(), isDefaultWorld ? saveName : this.getDirectoryName(key), this.server, dataFixer);

            if (isDefaultWorld) {
                ((MinecraftServerAccessor_Vanilla) this.server).accessor$setResourcePackFromWorld(saveName, saveHandler);
            }

            WorldInfo worldInfo = saveHandler.loadWorldInfo();
            if (defaultSettings == null) {
                defaultSettings = worldRegistration.getDefaultSettings();
            }

            if (worldInfo == null) {
                // Demo code does not run on SinglePlayer
                if (isSinglePlayer) {
                    worldInfo = new WorldInfo(defaultSettings, levelName);
                } else {
                    if (defaultSettings == null) {
                        defaultSettings = this.createDefaultSettings(defaultSettings, isDefaultWorld, seed, type, generatorOptions);
                        if (isDefaultWorld) {
                            defaultSettings.setGeneratorOptions(generatorOptions);
                            if (((MinecraftServerAccessor_Vanilla) this.server).accessor$getEnableBonusChest()) {
                                defaultSettings.enableBonusChest();
                            }
                        }
                    }

                    worldInfo = new WorldInfo(defaultSettings, isDefaultWorld ? saveName : this.getDirectoryName(key));
                }
                ((WorldInfoBridge) worldInfo).bridge$setConfigAdapter(configAdapter);

                ((ResourceKeyBridge) worldInfo).bridge$setKey(worldRegistration.getKey());
                ((WorldInfoBridge) worldInfo).bridge$setDimensionId(dimensionType);
                ((WorldInfoBridge) worldInfo).bridge$setUniqueId(UUID.randomUUID());

                SpongeCommon.postEvent(SpongeEventFactory.createConstructWorldPropertiesEvent(
                        PhaseTracker.getCauseStackManager().getCurrentCause(),
                        (WorldArchetype) (Object) defaultSettings, (WorldProperties) worldInfo));
            } else {
                ((WorldInfoBridge) worldInfo).bridge$setConfigAdapter(configAdapter);
                worldInfo.setWorldName(isDefaultWorld ? saveName : this.getDirectoryName(key));

                // This may be an existing world created before Sponge was installed, handle accordingly
                if (((ResourceKeyBridge) worldInfo).bridge$getKey() == null) {
                    ((ResourceKeyBridge) worldInfo).bridge$setKey(worldRegistration.getKey());
                    ((WorldInfoBridge) worldInfo).bridge$setDimensionId(dimensionType);
                    ((WorldInfoBridge) worldInfo).bridge$setUniqueId(UUID.randomUUID());
                }

                defaultSettings = new WorldSettings(worldInfo);
            }

            if (((WorldInfoBridge) worldInfo).bridge$getLogicType() != null) {
                ((DimensionTypeBridge) dimensionType).bridge$setSpongeDimensionType(((WorldInfoBridge) worldInfo).bridge$getLogicType());
            } else {
                ((WorldInfoBridge) worldInfo).bridge$setLogicType(((DimensionTypeBridge) dimensionType).bridge$getSpongeDimensionType(), false);
            }

            if (isDefaultWorld) {
                ((MinecraftServerAccessor_Vanilla) this.server).accessor$loadDataPacks(worldDirectory.toFile(), worldInfo);
            }

            this.loadedWorldInfos.put(key, worldInfo);
            this.infoByType.put(dimensionType, worldInfo);
            this.allInfos.put(key, worldInfo);

            if (!isDefaultWorld && !((WorldProperties) worldInfo).doesLoadOnStartup()) {
                SpongeCommon.getLogger().warn("World '{}' ({}/{}) has been set to not load on startup in the "
                        + "configuration. Skipping...", key, logicType.getKey().getFormatted(), dimensionType.getId());
                continue;
            }

            final IChunkStatusListener chunkStatusListener = ((MinecraftServerAccessor_Vanilla) this.server).accessor$getChunkStatusListenerFactory().create(11);

            final ServerWorld serverWorld = new ServerWorld(this.server, this.server.getBackgroundExecutor(), saveHandler, worldInfo,
                    dimensionType, this.server.getProfiler(), chunkStatusListener);

            this.worlds.put(key, serverWorld);
            this.worldsByType.put(dimensionType, serverWorld);

            this.performPostLoadWorldLogic(serverWorld, defaultSettings, worldDirectory, chunkStatusListener);
        }

        this.pendingWorlds.clear();

        if (!isSinglePlayer) {
            this.server.setDifficultyForAllWorlds(this.server.getDifficulty(), true);
        } else {
            this.worldsByType.forEach((k, v) -> {
                if (v.getWorldInfo().getDifficulty() == null) {
                    v.getWorldInfo().setDifficulty(defaultDifficulty);
                }
            });
        }

        // TODO May not be the best spot for this...
        ((SpongeServer) SpongeCommon.getServer()).getPlayerDataManager().load();
    }

    private void loadSpawnChunks(final ServerWorld serverWorld, final IChunkStatusListener chunkStatusListener) {
        ((MinecraftServerAccessor_Vanilla) this.server).accessor$setUserMessage(new TranslationTextComponent("menu.generatingTerrain"));
        final org.spongepowered.api.world.server.ServerWorld apiWorld = (org.spongepowered.api.world.server.ServerWorld) serverWorld;
        MinecraftServerAccessor_Vanilla.accessor$getLogger().info("Preparing start region for world '{}' ({}/{})", apiWorld.getKey(),
                apiWorld.getProperties().getDimensionType().getKey(), serverWorld.getDimension().getType().getId());
        final BlockPos spawnPoint = serverWorld.getSpawnPoint();
        final ChunkPos spawnChunkPos = new ChunkPos(spawnPoint);
        chunkStatusListener.start(spawnChunkPos);
        final ServerChunkProvider chunkProvider = serverWorld.getChunkProvider();
        chunkProvider.getLightManager().func_215598_a(500);
        ((MinecraftServerAccessor_Vanilla) this.server).accessor$setServerTime(Util.milliTime());
        chunkProvider.registerTicket(TicketType.START, spawnChunkPos, 11, Unit.INSTANCE);

        while (chunkProvider.func_217229_b() != 441) {
            ((MinecraftServerAccessor_Vanilla) this.server).accessor$setServerTime(Util.milliTime() + 10L);
            ((MinecraftServerAccessor_Vanilla) this.server).accessor$runScheduledTasks();
        }

        ((MinecraftServerAccessor_Vanilla) this.server).accessor$setServerTime(Util.milliTime() + 10L);
        ((MinecraftServerAccessor_Vanilla) this.server).accessor$runScheduledTasks();

        ForcedChunksSaveData forcedChunksSaveData = serverWorld.getSavedData().get(ForcedChunksSaveData::new, "chunks");
        if (forcedChunksSaveData != null) {
            LongIterator longIterator = forcedChunksSaveData.getChunks().iterator();

            while (longIterator.hasNext()) {
                final long i = longIterator.nextLong();
                final ChunkPos chunkpos = new ChunkPos(i);
                serverWorld.getChunkProvider().forceChunk(chunkpos, true);
            }
        }

        ((MinecraftServerAccessor_Vanilla) this.server).accessor$setServerTime(Util.milliTime() + 10L);
        ((MinecraftServerAccessor_Vanilla) this.server).accessor$runScheduledTasks();
        chunkStatusListener.stop();
        chunkProvider.getLightManager().func_215598_a(5);
    }

    private DimensionType createDimensionType(final ResourceKey key, final SpongeDimensionType logicType, final String worldDirectory,
            final int dimensionId) {
        final DimensionType registeredType = DimensionTypeAccessor.accessor$construct(dimensionId, "", worldDirectory, logicType.getDimensionFactory(),
                logicType.hasSkylight());
        DimensionTypeAccessor.accessor$register(key.getFormatted(), registeredType);

        ((DimensionTypeBridge) registeredType).bridge$setSpongeDimensionType(logicType);
        return registeredType;
    }

    private void loadExistingWorldRegistrations() throws IOException {
        if (Files.notExists(this.savesDirectory)) {
            return;
        }

        for (Path path : Files.walk(this.savesDirectory, 1).filter(path -> !this.savesDirectory.equals(path) && Files.isDirectory(path) && !this.isVanillaSubLevel(path.getFileName().toString()) && Files.exists(path.resolve(Constants.Sponge.World.LEVEL_SPONGE_DAT))).collect(Collectors.toList())) {
            final String worldDirectory = path.getFileName().toString();
            final CompoundNBT worldCompound;
            try {
                worldCompound = CompressedStreamTools.readCompressed(new FileInputStream(path.resolve(Constants.Sponge.World.LEVEL_SPONGE_DAT).toFile()));
            } catch (IOException ex) {
                SpongeCommon.getLogger().error("Attempt to load world '{}' sponge level data failed!", worldDirectory, ex);
                continue;
            }
            final CompoundNBT spongeDataCompound = worldCompound.getCompound(Constants.Sponge.SPONGE_DATA);
            final String rawKey = spongeDataCompound.getString(Constants.Sponge.World.KEY);
            final ResourceKey key;
            if (rawKey.isEmpty()) {
                key = ResourceKey.sponge(worldDirectory.toLowerCase());
                SpongeCommon.getLogger().warn("World '{}' was created on an older Sponge version (before Minecraft 1.15) which had no concept of "
                        + "which plugin created it. To compensate, this world will be created with the key '{}'. Please specify this key to any plugins"
                        + " that need to know of this world", worldDirectory, key);
            } else {
                key = ResourceKey.resolve(rawKey);
            }
            if (this.pendingWorlds.containsKey(key)) {
                SpongeCommon.getLogger().error("Duplicate World '{}' is being loaded from '{}'. Skipping...", key, worldDirectory);
                continue;
            }

            final int dimensionId = spongeDataCompound.getInt(Constants.Sponge.World.DIMENSION_ID);
            DimensionType registeredType = DimensionType.getById(dimensionId + 1);
            if (registeredType != null) {
                SpongeCommon.getLogger().error("Duplicate id '{}' is being loaded by '{}' but was "
                        + "previously loaded by '{}'. Skipping...", dimensionId, worldDirectory, DimensionType.getKey(registeredType).getPath());
                continue;
            }

            final String rawLogicType = spongeDataCompound.getString(Constants.Sponge.World.DIMENSION_TYPE);

            final SpongeDimensionType logicType = (SpongeDimensionType) SpongeCommon.getRegistry().getCatalogRegistry().get(org.spongepowered
                    .api.world.dimension.DimensionType.class, ResourceKey.resolve(rawLogicType)).orElse(null);

            if (logicType == null) {
                SpongeCommon.getLogger().error("World '{}' has an unknown dimension type '{}'. Skipping...",
                        worldDirectory, rawLogicType);
                continue;
            }

            registeredType = this.createDimensionType(key, logicType, worldDirectory, dimensionId + 1);
            this.pendingWorlds.put(key, new WorldRegistration(key, registeredType, null));
        }
    }

    private boolean isVanillaSubLevel(final String directoryName) {
        return "DIM-1".equals(directoryName) || "DIM1" .equals(directoryName);
    }

    private WorldSettings createDefaultSettings(@Nullable WorldSettings providedSettings, boolean isDefaultWorld, long seed, WorldType worldType,
            @Nullable JsonElement generatorOptions) {
        // Pure fresh Vanilla worlds situation (plugins registering worlds to load before now *must* give us an archetype)
        if (providedSettings == null) {
            if (this.server.isDemo()) {
                return MinecraftServer.DEMO_WORLD_SETTINGS;
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

        if (serverWorld.getWorldInfo().getGameType() == GameType.NOT_SET) {
            serverWorld.getWorldInfo().setGameType(this.server.getGameType());
        }

        // Initialize scoreboard data. This will hook to the ServerScoreboard, needs to be made multi-world aware
        ((MinecraftServerAccessor_Vanilla) this.server).accessor$func_213204_a(serverWorld.getSavedData());
        serverWorld.getWorldBorder().copyFrom(serverWorld.getWorldInfo());
        if (!serverWorld.getWorldInfo().isInitialized()) {
            try {
                serverWorld.createSpawnPosition(defaultSettings);
                if (serverWorld.getWorldInfo().getGenerator() == WorldType.DEBUG_ALL_BLOCK_STATES) {
                    ((MinecraftServerAccessor_Vanilla) this.server).accessor$applyDebugWorldInfo(serverWorld.getWorldInfo());
                }
            } catch (Throwable throwable) {
                final CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Exception initializing world '" + worldDirectory + "'");
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
        if (serverWorld.dimension.getType() == DimensionType.OVERWORLD) {
            ((SpongeUserManager) ((Server) this.server).getUserManager()).init();
        }

        if (serverWorld.getWorldInfo().getCustomBossEvents() != null) {
            ((ServerWorldBridge) serverWorld).bridge$getBossBarManager().read(serverWorld.getWorldInfo().getCustomBossEvents());
        }

        SpongeCommon.postEvent(SpongeEventFactory.createLoadWorldEvent(
                PhaseTracker.getCauseStackManager().getCurrentCause(),
                (org.spongepowered.api.world.server.ServerWorld) serverWorld));

        if (serverWorld.dimension.getType() == DimensionType.OVERWORLD || ((WorldInfoBridge) serverWorld.getWorldInfo()).bridge$doesGenerateSpawnOnLoad()) {
            this.loadSpawnChunks(serverWorld, listener);
        }
    }
}
