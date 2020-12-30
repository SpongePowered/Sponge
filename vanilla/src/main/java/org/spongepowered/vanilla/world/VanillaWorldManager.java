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
import net.minecraft.util.registry.WorldSettingsImport;
import net.minecraft.world.Difficulty;
import net.minecraft.world.ForcedChunksSaveData;
import net.minecraft.world.World;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;
import net.minecraft.world.storage.CommandStorage;
import net.minecraft.world.storage.SaveFormat;
import net.minecraft.world.storage.ServerWorldInfo;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.world.server.WorldTemplate;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.server.MinecraftServerAccessor;
import org.spongepowered.common.accessor.world.storage.SaveFormat_LevelSaveAccessor;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.bridge.world.storage.IServerWorldInfoBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.user.SpongeUserManager;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.FutureUtil;
import org.spongepowered.common.world.server.SpongeWorldManager;
import org.spongepowered.vanilla.accessor.server.MinecraftServerAccessor_Vanilla;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class VanillaWorldManager implements SpongeWorldManager {

    private final MinecraftServer server;
    private final Path defaultWorldDirectory;
    private final Path customWorldsDirectory;
    private final Map<RegistryKey<World>, ServerWorld> worlds;
    private final Map<RegistryKey<World>, ServerWorldInfo> loadedWorldInfos;
    private final Map<ResourceKey, ServerWorldInfo> allInfos;

    private static final TicketType<ResourceLocation> SPAWN_CHUNKS = TicketType.create("spawn_chunks", (i, o) -> i.compareTo(o));

    public VanillaWorldManager(final MinecraftServer server) {
        this.server = server;
        this.defaultWorldDirectory = ((SaveFormat_LevelSaveAccessor) ((MinecraftServerAccessor) this.server).accessor$storageSource()).accessor$levelPath();
        this.customWorldsDirectory = this.defaultWorldDirectory.resolve("dimensions");
        this.worlds = ((MinecraftServerAccessor) this.server).accessor$levels();
        this.loadedWorldInfos = new Object2ObjectOpenHashMap<>();
        this.allInfos = new LinkedHashMap<>();
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
        return Optional.ofNullable((org.spongepowered.api.world.server.ServerWorld) this.worlds.get(SpongeWorldManager.createRegistryKey(Objects.requireNonNull(key, "key"))));
    }

    @Override
    public Collection<org.spongepowered.api.world.server.ServerWorld> getWorlds() {
        return Collections.unmodifiableCollection((Collection<org.spongepowered.api.world.server.ServerWorld>) (Object) this.worlds.values());
    }

    @Override
    public Collection<ResourceKey> getWorldKeys() {
        return null;
    }

    @Override
    public Optional<ResourceKey> getWorldKey(final UUID uniqueId) {
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
        final ServerWorld serverWorld = this.worlds.get(registryKey);
        if (serverWorld != null) {
            return CompletableFuture.completedFuture((org.spongepowered.api.world.server.ServerWorld) serverWorld);
        }

        return null;
    }

    @Override
    public CompletableFuture<org.spongepowered.api.world.server.ServerWorld> loadWorld(final ResourceKey key) {
        final RegistryKey<World> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(key, "key"));
        ServerWorld world = this.worlds.get(registryKey);
        if (world != null) {
            return CompletableFuture.completedFuture((org.spongepowered.api.world.server.ServerWorld) world);
        }

        return null;
    }

    @Override
    public CompletableFuture<Boolean> unloadWorld(final ResourceKey key) {
        final RegistryKey<World> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(key, "key"));

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
        final RegistryKey<World> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(world, "world").getKey());

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
    public CompletableFuture<Boolean> saveTemplate(final WorldTemplate template) {
        Objects.requireNonNull(template, "template");
        return null;
    }

    @Override
    public Optional<WorldTemplate> getTemplate(final ResourceKey key) {
        Objects.requireNonNull(key, "key");
        return Optional.empty();
    }

    @Override
    public CompletableFuture<Boolean> copyWorld(final ResourceKey key, final ResourceKey copyKey) {
        final RegistryKey<World> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(key, "key"));
        final RegistryKey<World> copyRegistryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(copyKey, "copyKey"));

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

        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> moveWorld(final ResourceKey key, final ResourceKey movedKey) {
        final RegistryKey<World> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(key, "key"));
        final RegistryKey<World> copyRegistryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(movedKey, "movedKey"));

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

        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> deleteWorld(final ResourceKey key) {
        final RegistryKey<World> registryKey = SpongeWorldManager.createRegistryKey(Objects.requireNonNull(key, "key"));

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

        SpongeCommon.postEvent(SpongeEventFactory.createUnloadWorldEvent(PhaseTracker.getCauseStackManager().getCurrentCause(), (org.spongepowered.api.world.server.ServerWorld) world));
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
