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
package org.spongepowered.server.world;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

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
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.world.WorldSettingsBridge;
import org.spongepowered.common.bridge.world.dimension.DimensionTypeBridge;
import org.spongepowered.common.bridge.world.storage.WorldInfoBridge;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.type.GeneralConfigBase;
import org.spongepowered.common.accessor.util.registry.SimpleRegistryAccessor;
import org.spongepowered.common.accessor.world.dimension.DimensionTypeAccessor;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.world.dimension.SpongeDimensionType;
import org.spongepowered.common.world.server.SpongeWorldManager;
import org.spongepowered.common.world.server.WorldRegistration;
import org.spongepowered.server.accessor.world.storage.SaveFormatAccessor_Vanilla;
import org.spongepowered.server.accessor.server.MinecraftServerAccessor_Vanilla;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class VanillaWorldManager implements SpongeWorldManager {

    private final MinecraftServer server;
    private final MinecraftServerAccessor_Vanilla serverAccessor;
    private final Path savesDirectory;
    private final Map<DimensionType, ServerWorld> worldsByType;
    private final Map<DimensionType, WorldInfo> dataByType;
    private final Map<UUID, ServerWorld> worldById;
    private final Map<String, ServerWorld> worldByName;
    private final Map<String, WorldRegistration> pendingWorlds;

    public VanillaWorldManager(MinecraftServer server) {
        this.server = server;
        this.serverAccessor = (MinecraftServerAccessor_Vanilla) this.server;
        this.savesDirectory = this.serverAccessor.accessor$getAnvilFile().toPath().resolve(this.server.getFolderName());
        this.worldsByType = this.serverAccessor.accessor$getWorlds();
        this.dataByType = new IdentityHashMap<>();
        this.worldById = new Object2ObjectOpenHashMap<>();
        this.worldByName = new Object2ObjectOpenHashMap<>();
        this.pendingWorlds = new Object2ObjectOpenHashMap<>();

        this.submitRegistration0(this.server.getFolderName(), null);
        this.submitRegistration0("DIM-1", null);
        this.submitRegistration0("DIM1", null);

        // Not sure if I'm sold on doing this-this early...
        try {
            this.loadExistingWorldRegistrations();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public MinecraftServer getServer() {
        return this.server;
    }

    @Override
    public boolean isDimensionTypeRegistered(DimensionType dimensionType) {
        return Registry.DIMENSION_TYPE.getKey(dimensionType) != null;
    }

    @Override
    public WorldInfo getInfo(DimensionType dimensionType) {
        return this.dataByType.get(dimensionType);
    }

    @Override
    public Path getSavesDirectory() {
        return this.savesDirectory;
    }

    @Override
    public Optional<org.spongepowered.api.world.server.ServerWorld> getWorld(UUID uniqueId) {
        return Optional.ofNullable((org.spongepowered.api.world.server.ServerWorld) this.worldById.get(checkNotNull(uniqueId)));
    }

    @Override
    public Optional<org.spongepowered.api.world.server.ServerWorld> getWorld(String directoryName) {
        return Optional.ofNullable((org.spongepowered.api.world.server.ServerWorld) this.worldByName.get(checkNotNull(directoryName)));
    }

    @Override
    public Collection<org.spongepowered.api.world.server.ServerWorld> getWorlds() {
        return Collections.unmodifiableCollection((Collection< org.spongepowered.api.world.server.ServerWorld>) (Object) this.worldsByType.values());
    }

    @Override
    public String getDefaultPropertiesName() {
        return this.server.getFolderName();
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
    public boolean submitRegistration(String directoryName, WorldArchetype archetype) {
        checkNotNull(directoryName);
        checkNotNull(archetype);

        if (this.pendingWorlds.containsKey(directoryName)) {
            return false;
        }

        this.submitRegistration0(directoryName, (WorldSettings) (Object) archetype);
        return true;
    }

    private void submitRegistration0(String directoryName, WorldSettings settings) {
        checkNotNull(directoryName);

        DimensionType registeredType;

        if (settings == null) {
            // Only null for the initial Vanilla registrations but I could create generic settings for them. I'll ponder it
            registeredType = directoryName.equals("DIM-1") ? DimensionType.THE_NETHER : directoryName.equals("DIM1") ?
                DimensionType.THE_END : DimensionType.OVERWORLD;
        } else {
            registeredType = this.createDimensionType(directoryName, ((WorldSettingsBridge) (Object) settings).bridge$getLogicType(),
                ((SimpleRegistryAccessor) Registry.DIMENSION_TYPE).accessor$getNextFreeId());
        }

        this.pendingWorlds.put(directoryName, new WorldRegistration(directoryName, registeredType, settings));
    }

    @Override
    public Optional<WorldProperties> createProperties(String directoryName, WorldArchetype archetype) throws IOException {
        return Optional.empty();
    }

    @Override
    public Optional<org.spongepowered.api.world.server.ServerWorld> loadWorld(String directoryName) {
        return Optional.empty();
    }

    @Override
    public Optional<org.spongepowered.api.world.server.ServerWorld> loadWorld(WorldProperties properties) {
        return Optional.empty();
    }

    @Override
    public boolean unloadWorld(org.spongepowered.api.world.server.ServerWorld world) {
        checkNotNull(world);
        return false;
    }

    @Override
    public Optional<WorldProperties> getProperties(String directoryName) {
        checkNotNull(directoryName);
        return Optional.empty();
    }

    @Override
    public Optional<WorldProperties> getProperties(UUID uniqueId) {
        checkNotNull(uniqueId);
        return Optional.empty();
    }

    @Override
    public Collection<WorldProperties> getUnloadedProperties() {
        return null;
    }

    @Override
    public Collection<WorldProperties> getAllProperties() {
        return null;
    }

    @Override
    public boolean saveProperties(WorldProperties properties) {
        checkNotNull(properties);
        return false;
    }

    @Override
    public CompletableFuture<Optional<WorldProperties>> copyWorld(String directoryName, String copyName) {
        checkNotNull(directoryName);
        checkNotNull(copyName);
        return null;
    }

    @Override
    public Optional<WorldProperties> renameWorld(String oldDirectoryName, String newDirectoryName) {
        checkNotNull(oldDirectoryName);
        checkNotNull(newDirectoryName);
        return Optional.empty();
    }

    @Override
    public CompletableFuture<Boolean> deleteWorld(String directoryName) {
        checkNotNull(directoryName);
        return null;
    }

    @Override
    public void adjustWorldForDifficulty(ServerWorld world, Difficulty newDifficulty, boolean isCustom) {

    }

    @Override
    public void loadAllWorlds(MinecraftServer server, String saveName, String levelName, long seed, WorldType type, JsonElement generatorOptions) {
        this.serverAccessor.accessor$convertMapIfNeeded(saveName);

        final DataFixer dataFixer = ((SaveFormatAccessor_Vanilla) this.server.getActiveAnvilConverter()).accessor$getDataFixer();
        final Path savesDir = ((SaveFormatAccessor_Vanilla) this.server.getActiveAnvilConverter()).accessor$getSavesDir();

        final Path worldsDirectory = savesDir.resolve(server.getFolderName());

        // Symlink needs special handling
        try {
            if (Files.isSymbolicLink(worldsDirectory)) {
                final Path actualPathLink = Files.readSymbolicLink(worldsDirectory);
                if (Files.notExists(actualPathLink)) {
                    Files.createDirectories(actualPathLink);
                } else if (!Files.isDirectory(actualPathLink)) {
                    throw new IOException("Saves directory '" + worldsDirectory + "' symlink to '" + actualPathLink + "' is not a directory!");
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

        for (Map.Entry<String, WorldRegistration> entry : this.pendingWorlds.entrySet()) {
            final String directoryName = entry.getKey();
            final WorldRegistration worldRegistration = entry.getValue();
            final boolean isDefaultWorld = directoryName.equals(saveName);

            if (!isDefaultWorld && !this.server.getAllowNether()) {
                continue;
            }

            this.serverAccessor.accessor$setUserMessage(new TranslationTextComponent("menu.loadingLevel"));

            final Path worldDirectory = isDefaultWorld ? worldsDirectory : worldsDirectory.resolve(directoryName);

            final SpongeDimensionType logicType = ((DimensionTypeBridge) (Object) worldRegistration.getDimensionType()).bridge$getSpongeDimensionType();

            MinecraftServerAccessor_Vanilla.accessor$getLogger().warn("Loading World '{}' ({}/{})", directoryName, logicType.getKey().getFormatted(), worldRegistration.getDimensionType().getId());

            if (!isDefaultWorld) {
                final SpongeConfig<? extends GeneralConfigBase> configAdapter = SpongeHooks.getOrLoadConfigAdapter(logicType.getConfigPath(), directoryName);
                if (!configAdapter.getConfig().getWorld().isWorldEnabled()) {
                    MinecraftServerAccessor_Vanilla.accessor$getLogger().warn("World '{}' ({}/{}) has been disabled in the configuration. "
                        + "World will not be loaded...", directoryName, logicType.getKey().getFormatted(), worldRegistration.getDimensionType().getId());
                    continue;
                }
            }

            final SaveHandler saveHandler = new SaveHandler(worldDirectory.toFile(), directoryName, this.server, dataFixer);

            if (isDefaultWorld) {
                this.serverAccessor.accessor$setResourcePackFromWorld(directoryName, saveHandler);
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
                worldInfo = new WorldInfo(defaultSettings, directoryName);
                ((WorldInfoBridge) worldInfo).bridge$setUniqueId(UUID.randomUUID());
                ((WorldInfoBridge) worldInfo).bridge$setDimensionType(worldRegistration.getDimensionType());

                if (isDefaultWorld) {
                    ((WorldInfoBridge) worldInfo).bridge$setGenerateSpawnOnLoad(true);
                }

                SpongeImpl.postEvent(SpongeEventFactory.createConstructWorldPropertiesEvent(SpongeImpl.getCauseStackManager().getCurrentCause(),
                    (WorldArchetype) (Object) defaultSettings, (WorldProperties) worldInfo));
            } else {
                worldInfo.setWorldName(directoryName);
                defaultSettings = new WorldSettings(worldInfo);
            }

            final WorldInfoBridge infoBridge = (WorldInfoBridge) worldInfo;

            if (isDefaultWorld) {
                this.serverAccessor.accessor$loadDataPacks(worldDirectory.toFile(), worldInfo);
            } else {
                final ServerWorld existingWorld = this.worldById.get(((WorldProperties) worldInfo).getUniqueId());
                if (existingWorld != null) {
                    MinecraftServerAccessor_Vanilla.accessor$getLogger().warn("World '{}' ({}/{}) has a unique identifier that has already been loaded "
                            + "by '{}'. Skipping...", directoryName, logicType.getKey().getFormatted(),
                        worldRegistration.getDimensionType().getId(), existingWorld.getWorldInfo().getWorldName());
                    continue;
                }
            }

            this.dataByType.put(worldRegistration.getDimensionType(), worldInfo);

            if (!isDefaultWorld && !((WorldProperties) worldInfo).doesLoadOnStartup()) {
                MinecraftServerAccessor_Vanilla.accessor$getLogger().warn("World '{}' ({}/{}) has been set to not load on startup in the "
                    + "configuration. Skipping...", directoryName, logicType.getKey().getFormatted(), worldRegistration.getDimensionType().getId());
                continue;
            }

            infoBridge.bridge$createWorldConfig();

            final IChunkStatusListener chunkStatusListener = this.serverAccessor.accessor$getChunkStatusListenerFactory().create(11);

            final ServerWorld serverWorld = new ServerWorld(this.server, this.server.getBackgroundExecutor(), saveHandler, worldInfo, worldRegistration.getDimensionType(), this.server.getProfiler(), chunkStatusListener);

            if (worldInfo.getGameType() == GameType.NOT_SET) {
                worldInfo.setGameType(this.server.getGameType());
            }

            this.worldsByType.put(worldRegistration.getDimensionType(), serverWorld);
            this.worldByName.put(directoryName, serverWorld);
            this.worldById.put(((WorldProperties) worldInfo).getUniqueId(), serverWorld);

            // Initialize scoreboard data. This will hook to the ServerScoreboard, needs to be made multi-world aware
            this.serverAccessor.accessor$func_213204_a(serverWorld.getSavedData());
            serverWorld.getWorldBorder().copyFrom(worldInfo);
            if (!worldInfo.isInitialized()) {
                try {
                    serverWorld.createSpawnPosition(defaultSettings);
                    if (worldInfo.getGenerator() == WorldType.DEBUG_ALL_BLOCK_STATES) {
                        this.serverAccessor.accessor$applyDebugWorldInfo(worldInfo);
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
                // Need to see about making custom boss events be per-world
                if (worldInfo.getCustomBossEvents() != null) {
                    this.server.getCustomBossEvents().read(worldInfo.getCustomBossEvents());
                }
            }

            this.server.setDifficultyForAllWorlds(this.server.getDifficulty(), true);

            SpongeImpl.postEvent(SpongeEventFactory.createLoadWorldEvent(SpongeImpl.getCauseStackManager().getCurrentCause(), (World) serverWorld));

            if (infoBridge.bridge$doesGenerateSpawnOnLoad()) {
                this.loadSpawnChunks(serverWorld, chunkStatusListener);
            }
        }
    }

    private void loadSpawnChunks(ServerWorld serverWorld, IChunkStatusListener chunkStatusListener) {
        this.serverAccessor.accessor$setUserMessage(new TranslationTextComponent("menu.generatingTerrain"));
        MinecraftServerAccessor_Vanilla.accessor$getLogger().info("Preparing start region for world '{}' ({}/{})", serverWorld
            .getWorldInfo().getWorldName(), ((DimensionTypeBridge) serverWorld.getDimension().getType()).bridge$getSpongeDimensionType()
            .getKey().getFormatted(), serverWorld.getDimension().getType().getId());
        final BlockPos spawnPoint = serverWorld.getSpawnPoint();
        final ChunkPos spawnChunkPos = new ChunkPos(spawnPoint);
        chunkStatusListener.start(spawnChunkPos);
        final ServerChunkProvider chunkProvider = serverWorld.getChunkProvider();
        chunkProvider.getLightManager().func_215598_a(500);
        this.serverAccessor.accessor$setServerTime(Util.milliTime());
        chunkProvider.registerTicket(TicketType.START, spawnChunkPos, 11, Unit.INSTANCE);

        while (chunkProvider.func_217229_b() != 441) {
            this.serverAccessor.accessor$setServerTime(Util.milliTime() + 10L);
            this.serverAccessor.accessor$runScheduledTasks();
        }

        this.serverAccessor.accessor$setServerTime(Util.milliTime() + 10L);
        this.serverAccessor.accessor$runScheduledTasks();

        ForcedChunksSaveData forcedChunksSaveData = serverWorld.getSavedData().get(ForcedChunksSaveData::new, "chunks");
        if (forcedChunksSaveData != null) {
            LongIterator longIterator = forcedChunksSaveData.getChunks().iterator();

            while (longIterator.hasNext()) {
                final long i = longIterator.nextLong();
                final ChunkPos chunkpos = new ChunkPos(i);
                serverWorld.getChunkProvider().forceChunk(chunkpos, true);
            }
        }

        this.serverAccessor.accessor$setServerTime(Util.milliTime() + 10L);
        this.serverAccessor.accessor$runScheduledTasks();
        chunkStatusListener.stop();
        chunkProvider.getLightManager().func_215598_a(5);
    }

    private DimensionType createDimensionType(String directoryName, SpongeDimensionType logicType, int dimensionId) {
        final DimensionType registeredType = DimensionTypeAccessor.accessor$construct(dimensionId, "", directoryName, logicType.getDimensionFactory(), logicType.hasSkylight());
        DimensionTypeAccessor.accessor$register(directoryName, registeredType);

        ((DimensionTypeBridge) registeredType).bridge$setSpongeDimensionType(logicType);
        return registeredType;
    }

    private void loadExistingWorldRegistrations() throws IOException {
        for (Path path : Files.walk(this.savesDirectory, 1).filter(path -> Files.isDirectory(path) && !this.isVanillaLevelDimension(path.getFileName().toString()) && Files.exists(path.resolve(Constants.Sponge.World.LEVEL_SPONGE_DAT))).collect(Collectors.toList())) {
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
            final int dimensionId = spongeDataCompound.getInt(Constants.Sponge.World.DIMENSION_ID);
            DimensionType registeredType = DimensionType.getById(dimensionId + 1);
            if (registeredType != null) {
                MinecraftServerAccessor_Vanilla.accessor$getLogger().error("Duplicate id '{}' is being loaded by '{}' but was "
                    + "previously loaded by '{}'. Skipping...", dimensionId, worldDirectory, DimensionType.getKey(registeredType).getPath());
                continue;
            }

            final String rawLogicType = spongeDataCompound.getString(Constants.Sponge.World.DIMENSION_TYPE);

            final SpongeDimensionType logicType = (SpongeDimensionType) SpongeImpl.getRegistry().getCatalogRegistry().get(org.spongepowered
                .api.world.dimension.DimensionType.class, CatalogKey.resolve(rawLogicType)).orElse(null);

            if (logicType == null) {
                MinecraftServerAccessor_Vanilla.accessor$getLogger().error("World '{}' has an unknown DimensionType '{}'. Skipping...",
                    worldDirectory, rawLogicType);
                continue;
            }

            if (!spongeDataCompound.hasUniqueId(Constants.Sponge.World.UNIQUE_ID)) {
                MinecraftServerAccessor_Vanilla.accessor$getLogger().error("World '{}' has no unique identifier. Skipping...", worldDirectory);
                continue;
            }

            registeredType = this.createDimensionType(worldDirectory, logicType, dimensionId);
            this.pendingWorlds.put(worldDirectory, new WorldRegistration(worldDirectory, registeredType, null));
        }
    }

    private boolean isVanillaLevelDimension(String directoryName) {
        return directoryName.equalsIgnoreCase("DIM-1") || directoryName.equalsIgnoreCase("DIM1");
    }
}
