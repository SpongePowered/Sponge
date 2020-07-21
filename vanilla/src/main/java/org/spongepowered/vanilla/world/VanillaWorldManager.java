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

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.server.MinecraftServer;
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
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.ResourceKeyBridge;
import org.spongepowered.common.bridge.world.WorldSettingsBridge;
import org.spongepowered.common.bridge.world.dimension.DimensionTypeBridge;
import org.spongepowered.common.bridge.world.storage.WorldInfoBridge;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.type.GeneralConfigBase;
import org.spongepowered.common.accessor.util.registry.SimpleRegistryAccessor;
import org.spongepowered.common.accessor.world.dimension.DimensionTypeAccessor;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.user.SpongeUserManager;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.world.dimension.SpongeDimensionType;
import org.spongepowered.common.world.server.SpongeWorldManager;
import org.spongepowered.common.world.server.WorldRegistration;
import org.spongepowered.vanilla.accessor.world.storage.SaveFormatAccessor_Vanilla;
import org.spongepowered.vanilla.accessor.server.MinecraftServerAccessor_Vanilla;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public final class VanillaWorldManager implements SpongeWorldManager {

    private static final ResourceKey VANILLA_OVERWORLD = ResourceKey.minecraft("overworld");
    private static final ResourceKey VANILLA_THE_NETHER = ResourceKey.minecraft("the_nether");
    private static final ResourceKey VANILLA_THE_END = ResourceKey.minecraft("the_end");

    private final MinecraftServer server;
    private final Path savesDirectory;
    private final Map<ResourceKey, ServerWorld> worlds;
    private final Map<DimensionType, ServerWorld> worldsByType;
    private final Map<ResourceKey, WorldInfo> infos;
    private final Map<DimensionType, WorldInfo> infoByType;
    private final Map<ResourceKey, WorldRegistration> pendingWorlds;
    private final Collection<WorldInfo> allInfos;

    public VanillaWorldManager(final MinecraftServer server) {
        this.server = server;
        this.savesDirectory = ((MinecraftServerAccessor_Vanilla) server).accessor$getAnvilFile().toPath().resolve(server.getFolderName());
        this.worlds = new Object2ObjectOpenHashMap<>();
        this.worldsByType = ((MinecraftServerAccessor_Vanilla) server).accessor$getWorlds();
        this.infos = new Object2ObjectOpenHashMap<>();
        this.infoByType = new IdentityHashMap<>();
        this.pendingWorlds = new Object2ObjectOpenHashMap<>();
        this.allInfos = new ArrayList<>();

        this.registerPendingWorld(VanillaWorldManager.VANILLA_OVERWORLD, null);
        this.registerPendingWorld(VanillaWorldManager.VANILLA_THE_NETHER, null);
        this.registerPendingWorld(VanillaWorldManager.VANILLA_THE_END, null);
    }

    @Override
    public Path getSavesDirectory() {
        return this.savesDirectory;
    }

    @Override
    public boolean isDimensionTypeRegistered(final DimensionType dimensionType) {
        return Registry.DIMENSION_TYPE.getKey(dimensionType) != null;
    }

    @Override
    public WorldInfo getInfo(final DimensionType dimensionType) {
        return this.infoByType.get(dimensionType);
    }

    @Override
    public Optional<org.spongepowered.api.world.server.ServerWorld> getWorld(final ResourceKey key) {
        Preconditions.checkNotNull(key);

        return (Optional< org.spongepowered.api.world.server.ServerWorld>) (Object) Optional.ofNullable(this.worlds.get(key));
    }

    @Override
    public Collection<org.spongepowered.api.world.server.ServerWorld> getWorlds() {
        return Collections.unmodifiableCollection((Collection< org.spongepowered.api.world.server.ServerWorld>) (Object) this.worldsByType.values());
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
        return null;
    }

    @Override
    public CompletableFuture<Optional<org.spongepowered.api.world.server.ServerWorld>> loadWorld(ResourceKey key) {
        return null;
    }

    @Override
    public CompletableFuture<Optional<org.spongepowered.api.world.server.ServerWorld>> loadWorld(WorldProperties properties) throws IOException {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> unloadWorld(org.spongepowered.api.world.server.ServerWorld world) {
        return null;
    }

    @Override
    public Optional<WorldProperties> getProperties(ResourceKey key) {
        Preconditions.checkNotNull(key);

        return (Optional<WorldProperties>) (Object) Optional.ofNullable(this.infos.get(key));
    }

    @Override
    public Collection<WorldProperties> getUnloadedProperties() {
        return null;
    }

    @Override
    public Collection<WorldProperties> getAllProperties() {
        return (Collection<WorldProperties>) (Object) Collections.unmodifiableCollection(this.allInfos);
    }

    @Override
    public CompletableFuture<Boolean> saveProperties(WorldProperties properties) {
        return null;
    }

    @Override
    public CompletableFuture<Optional<WorldProperties>> copyWorld(ResourceKey key, String copyValue) {
        return null;
    }

    @Override
    public CompletableFuture<Optional<WorldProperties>> renameWorld(ResourceKey key, String newValue) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> deleteWorld(ResourceKey key) {
        return null;
    }

    @Override
    public boolean registerPendingWorld(ResourceKey key, @Nullable WorldArchetype archetype) {
        Preconditions.checkNotNull(key);

        if (this.pendingWorlds.containsKey(key)) {
            return false;
        }

        DimensionType registeredType;

        if (archetype == null) {
            // Only null for the initial Vanilla registrations but I could create generic settings for them. I'll ponder it
            registeredType = VANILLA_THE_NETHER.equals(key) ? DimensionType.THE_NETHER : VANILLA_THE_END.equals(key) ? DimensionType.THE_END : DimensionType.OVERWORLD;
        } else {
            registeredType = this.createDimensionType(key, ((WorldSettingsBridge) (Object) archetype).bridge$getLogicType(),
                ((SimpleRegistryAccessor) Registry.DIMENSION_TYPE).accessor$getNextFreeId());
        }

        this.pendingWorlds.put(key, new WorldRegistration(key, registeredType, (WorldSettings) (Object) archetype));
        return true;
    }

    @Override
    public @org.checkerframework.checker.nullness.qual.Nullable ServerWorld getWorld(DimensionType dimensionType) {
        Preconditions.checkNotNull(dimensionType);

        return this.worldsByType.get(dimensionType);
    }

    @Override
    public @org.checkerframework.checker.nullness.qual.Nullable ServerWorld getDefaultWorld() {
        return this.worldsByType.get(VanillaWorldManager.VANILLA_OVERWORLD);
    }

    @Override
    public void adjustWorldForDifficulty(ServerWorld world, Difficulty newDifficulty, boolean isCustom) {

    }

    @Override
    public void loadAllWorlds(final String saveName, final String levelName, final long seed, final WorldType type, final JsonElement generatorOptions) {
        ((MinecraftServerAccessor_Vanilla) this.server).accessor$convertMapIfNeeded(saveName);

        try {
            this.loadExistingWorldRegistrations();
        } catch (IOException e) {
            SpongeCommon.getLogger().error("Exception caught registering existing Sponge worlds!", e);
        }

        final DataFixer dataFixer = ((SaveFormatAccessor_Vanilla) this.server.getActiveAnvilConverter()).accessor$getDataFixer();
        final Path savesDirectory = ((SaveFormatAccessor_Vanilla) this.server.getActiveAnvilConverter()).accessor$getSavesDir();

        final Path worldsDirectory = saveName.equals(levelName) ? savesDirectory : savesDirectory.resolve(levelName);

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
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

        if (!this.server.getAllowNether()) {
            MinecraftServerAccessor_Vanilla.accessor$getLogger().warn("The option 'allow-nether' has been set to 'false' in the server.properties. "
                + "Multi-World support has been disabled and no worlds besides the default world will be loaded.");
        }

        for (Map.Entry<ResourceKey, WorldRegistration> entry : this.pendingWorlds.entrySet()) {
            final ResourceKey key = entry.getKey();
            final WorldRegistration worldRegistration = entry.getValue();
            final boolean isDefaultWorld = VanillaWorldManager.VANILLA_OVERWORLD.equals(key);

            if (!isDefaultWorld && !this.server.getAllowNether()) {
                continue;
            }

            ((MinecraftServerAccessor_Vanilla) this.server).accessor$setUserMessage(new TranslationTextComponent("menu.loadingLevel"));

            final Path worldDirectory = isDefaultWorld ? worldsDirectory : worldsDirectory.resolve(key.getValue());

            final SpongeDimensionType logicType = ((DimensionTypeBridge) (Object) worldRegistration.getDimensionType()).bridge$getSpongeDimensionType();

            MinecraftServerAccessor_Vanilla.accessor$getLogger().warn("Loading World '{}' ({}/{})", key, logicType.getKey().getFormatted(), worldRegistration.getDimensionType().getId());

            if (!isDefaultWorld) {
                final SpongeConfig<? extends GeneralConfigBase> configAdapter = SpongeHooks.getOrLoadConfigAdapter(logicType.getConfigPath(), key);
                if (!configAdapter.getConfig().getWorld().isWorldEnabled()) {
                    MinecraftServerAccessor_Vanilla.accessor$getLogger().warn("World '{}' ({}/{}) has been disabled in the configuration. "
                        + "World will not be loaded...", key, logicType.getKey().getFormatted(), worldRegistration.getDimensionType().getId());
                    continue;
                }
            }

            final SaveHandler saveHandler = new SaveHandler(worldDirectory.getParent().toFile(), key.getValue(), this.server, dataFixer);

            if (isDefaultWorld) {
                ((MinecraftServerAccessor_Vanilla) this.server).accessor$setResourcePackFromWorld(key.getValue(), saveHandler);
            }

            WorldInfo worldInfo = saveHandler.loadWorldInfo();
            WorldSettings defaultSettings = worldRegistration.getDefaultSettings();

            if (worldInfo == null) {
                // Pure fresh Vanilla worlds situation (plugins registering worlds to load before now *must* give us an archetype)
                if (defaultSettings == null) {
                    if (this.server.isDemo()) {
                        defaultSettings = MinecraftServer.DEMO_WORLD_SETTINGS;
                    } else {
                        defaultSettings = new WorldSettings(seed, this.server.getGameType(), this.server.canStructuresSpawn(), this.server.isHardcore(), type);
                        if (isDefaultWorld) {
                            defaultSettings.setGeneratorOptions(generatorOptions);
                            if (((MinecraftServerAccessor_Vanilla) this.server).accessor$getEnableBonusChest()) {
                                defaultSettings.enableBonusChest();
                            }
                        }
                    }
                }
                worldInfo = new WorldInfo(defaultSettings, key.getValue());
                ((ResourceKeyBridge) worldInfo).bridge$setKey(worldRegistration.getKey());
                ((WorldInfoBridge) worldInfo).bridge$setDimensionType(worldRegistration.getDimensionType());
                ((WorldInfoBridge) worldInfo).bridge$setUniqueId(UUID.randomUUID());

                if (isDefaultWorld) {
                    ((WorldInfoBridge) worldInfo).bridge$setGenerateSpawnOnLoad(true);
                }

                SpongeCommon.postEvent(SpongeEventFactory.createConstructWorldPropertiesEvent(
                    PhaseTracker.getCauseStackManager().getCurrentCause(),
                    (WorldArchetype) (Object) defaultSettings, (WorldProperties) worldInfo));
            } else {
                worldInfo.setWorldName(key.getValue());
                defaultSettings = new WorldSettings(worldInfo);
            }

            final WorldInfoBridge infoBridge = (WorldInfoBridge) worldInfo;

            if (isDefaultWorld) {
                ((MinecraftServerAccessor_Vanilla) this.server).accessor$loadDataPacks(worldDirectory.toFile(), worldInfo);
            }

            this.infoByType.put(worldRegistration.getDimensionType(), worldInfo);

            if (!isDefaultWorld && !((WorldProperties) worldInfo).doesLoadOnStartup()) {
                MinecraftServerAccessor_Vanilla.accessor$getLogger().warn("World '{}' ({}/{}) has been set to not load on startup in the "
                    + "configuration. Skipping...", key.getValue(), logicType.getKey().getFormatted(), worldRegistration.getDimensionType().getId());
                continue;
            }

            infoBridge.bridge$createWorldConfig();

            final IChunkStatusListener chunkStatusListener = ((MinecraftServerAccessor_Vanilla) this.server).accessor$getChunkStatusListenerFactory().create(11);

            final ServerWorld serverWorld = new ServerWorld(this.server, this.server.getBackgroundExecutor(), saveHandler, worldInfo, worldRegistration.getDimensionType(), this.server.getProfiler(), chunkStatusListener);

            if (worldInfo.getGameType() == GameType.NOT_SET) {
                worldInfo.setGameType(this.server.getGameType());
            }

            this.worldsByType.put(worldRegistration.getDimensionType(), serverWorld);
            this.worlds.put(key, serverWorld);

            // Initialize scoreboard data. This will hook to the ServerScoreboard, needs to be made multi-world aware
            ((MinecraftServerAccessor_Vanilla) this.server).accessor$func_213204_a(serverWorld.getSavedData());
            serverWorld.getWorldBorder().copyFrom(worldInfo);
            if (!worldInfo.isInitialized()) {
                try {
                    serverWorld.createSpawnPosition(defaultSettings);
                    if (worldInfo.getGenerator() == WorldType.DEBUG_ALL_BLOCK_STATES) {
                        ((MinecraftServerAccessor_Vanilla) this.server).accessor$applyDebugWorldInfo(worldInfo);
                    }
                } catch (Throwable throwable) {
                    final CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Exception initializing world '" + worldDirectory + "'");
                    try {
                        serverWorld.fillCrashReport(crashReport);
                    } catch (Throwable ignore) {
                    }

                    throw new ReportedException(crashReport);
                } finally {
                    worldInfo.setInitialized(true);
                }
            }

            // Initialize PlayerData in PlayerList, add WorldBorder listener. We change the method in PlayerList to handle per-world border
            this.server.getPlayerList().func_212504_a(serverWorld);
            if (isDefaultWorld) {
                ((SpongeUserManager) ((Server) this.server).getUserManager()).init();

                // Need to see about making custom boss events be per-world
                if (worldInfo.getCustomBossEvents() != null) {
                    this.server.getCustomBossEvents().read(worldInfo.getCustomBossEvents());
                }
            }

            this.server.setDifficultyForAllWorlds(this.server.getDifficulty(), true);

            SpongeCommon.postEvent(SpongeEventFactory.createLoadWorldEvent(
                PhaseTracker.getCauseStackManager().getCurrentCause(),
                (org.spongepowered.api.world.server.ServerWorld) serverWorld));

            if (infoBridge.bridge$doesGenerateSpawnOnLoad()) {
                this.loadSpawnChunks(serverWorld, chunkStatusListener);
            }
        }
    }

    private void loadSpawnChunks(final ServerWorld serverWorld, final IChunkStatusListener chunkStatusListener) {
        ((MinecraftServerAccessor_Vanilla) this.server).accessor$setUserMessage(new TranslationTextComponent("menu.generatingTerrain"));
        MinecraftServerAccessor_Vanilla.accessor$getLogger().info("Preparing start region for world '{}' ({}/{})", serverWorld
            .getWorldInfo().getWorldName(), ((DimensionTypeBridge) serverWorld.getDimension().getType()).bridge$getSpongeDimensionType()
            .getKey().getFormatted(), serverWorld.getDimension().getType().getId());
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

    private DimensionType createDimensionType(final ResourceKey key, final SpongeDimensionType logicType, final int dimensionId) {
        final DimensionType registeredType = DimensionTypeAccessor.accessor$construct(dimensionId, "", key.getValue(), logicType.getDimensionFactory(), logicType.hasSkylight());
        DimensionTypeAccessor.accessor$register(key.getValue(), registeredType);

        ((DimensionTypeBridge) registeredType).bridge$setSpongeDimensionType(logicType);
        return registeredType;
    }

    private void loadExistingWorldRegistrations() throws IOException {
        for (Path path : Files.walk(this.savesDirectory, 1).filter(path -> Files.isDirectory(path) && !this.isVanillaAlternateDimension(path.getFileName().toString()) && Files.exists(path.resolve(Constants.Sponge.World.LEVEL_SPONGE_DAT))).collect(Collectors.toList())) {
            final String worldDirectory = path.getFileName().toString();
            final CompoundNBT worldCompound;
            try {
                worldCompound = CompressedStreamTools.readCompressed(new FileInputStream(path.resolve(Constants.Sponge.World.LEVEL_SPONGE_DAT).toFile()));
            } catch (IOException ex) {
                MinecraftServerAccessor_Vanilla.accessor$getLogger().error("Attempt to load world '{}' sponge level data failed!", worldDirectory);
                ex.printStackTrace();
                continue;
            }
            final CompoundNBT spongeDataCompound = worldCompound.getCompound(Constants.Sponge.SPONGE_DATA);
            final String rawKey = spongeDataCompound.getString(Constants.Sponge.World.KEY);
            final ResourceKey key;
            if (rawKey.isEmpty()) {
                key = ResourceKey.sponge(worldDirectory.toLowerCase());
                SpongeCommon.getLogger().debug("World '{}' was created on an older Sponge version (before Minecraft 1.15) which had no concept of "
                    + "which plugin created it. To compensate, this world will be created with the key '{}'. Please specify this key to any plugins"
                    + " that need to know of this world", worldDirectory, key);
            } else {
                key = ResourceKey.resolve(rawKey);
            }
            final int dimensionId = spongeDataCompound.getInt(Constants.Sponge.World.DIMENSION_ID);
            DimensionType registeredType = DimensionType.getById(dimensionId + 1);
            if (registeredType != null) {
                MinecraftServerAccessor_Vanilla.accessor$getLogger().error("Duplicate id '{}' is being loaded by '{}' but was "
                    + "previously loaded by '{}'. Skipping...", dimensionId, worldDirectory, DimensionType.getKey(registeredType).getPath());
                continue;
            }

            final String rawLogicType = spongeDataCompound.getString(Constants.Sponge.World.DIMENSION_TYPE);

            final SpongeDimensionType logicType = (SpongeDimensionType) SpongeCommon.getRegistry().getCatalogRegistry().get(org.spongepowered
                .api.world.dimension.DimensionType.class, ResourceKey.resolve(rawLogicType)).orElse(null);

            if (logicType == null) {
                MinecraftServerAccessor_Vanilla.accessor$getLogger().error("World '{}' has an unknown dimension type '{}'. Skipping...",
                    worldDirectory, rawLogicType);
                continue;
            }

            registeredType = this.createDimensionType(key, logicType, dimensionId);
            this.pendingWorlds.put(key, new WorldRegistration(key, registeredType, null));
        }
    }

    private boolean isVanillaAlternateDimension(final String directoryName) {
        return "DIM-1".equals(directoryName) || "DIM1" .equals(directoryName);
    }
}
