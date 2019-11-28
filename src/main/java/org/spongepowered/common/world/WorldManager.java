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
package org.spongepowered.common.world;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.MapMaker;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.world.DimensionType;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameType;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.ServerWorldEventHandler;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.storage.AnvilSaveHandler;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.GameState;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.util.file.CopyFileVisitor;
import org.spongepowered.api.util.file.DeleteFileVisitor;
import org.spongepowered.api.util.file.ForwardingFileVisitor;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.SerializationBehaviors;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.entity.player.EntityPlayerMPBridge;
import org.spongepowered.common.bridge.server.MinecraftServerBridge;
import org.spongepowered.common.bridge.server.integrated.IntegratedServerBridge;
import org.spongepowered.common.bridge.world.DimensionTypeBridge;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.bridge.world.WorldServerBridge_AsyncLighting;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.WorldInfoBridge;
import org.spongepowered.common.bridge.world.WorldSettingsBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkProviderServerBridge;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.type.GeneralConfigBase;
import org.spongepowered.common.config.type.GlobalConfig;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.general.GeneralPhase;
import org.spongepowered.common.mixin.core.server.MinecraftServerAccessor;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.SpongeHooks;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

@SuppressWarnings("ConstantConditions")
public final class WorldManager {

    private static final DirectoryStream.Filter<Path> LEVEL_AND_SPONGE =
            entry -> Files.isDirectory(entry) && Files.exists(entry.resolve("level.dat")) && Files.exists(entry.resolve("level_sponge.dat"));

    private static final Int2ObjectMap<DimensionType> dimensionTypeByTypeId = new Int2ObjectOpenHashMap<>(3);
    private static final Int2ObjectMap<DimensionType> dimensionTypeByDimensionId = new Int2ObjectOpenHashMap<>(3);
    private static final Int2ObjectMap<Path> dimensionPathByDimensionId = new Int2ObjectOpenHashMap<>(3);
    private static final Int2ObjectOpenHashMap<WorldServer> worldByDimensionId = new Int2ObjectOpenHashMap<>(3);
    private static final Map<String, WorldProperties> worldPropertiesByFolderName = new HashMap<>(3);
    private static final Map<UUID, WorldProperties> worldPropertiesByWorldUuid =  new HashMap<>(3);
    private static final Map<Integer, String> worldFolderByDimensionId = new HashMap<>();
    private static final BiMap<String, UUID> worldUuidByFolderName =  HashBiMap.create(3);
    private static final IntSet usedDimensionIds = new IntOpenHashSet();
    private static final Map<WorldServer, WorldServer> weakWorldByWorld = new MapMaker().weakKeys().weakValues().concurrencyLevel(1).makeMap();
    private static final Queue<WorldServer> unloadQueue = new ArrayDeque<>();
    private static final Comparator<WorldServer>
            WORLD_SERVER_COMPARATOR =
            (world1, world2) -> {
                final int world1DimId = ((WorldServerBridge) world1).bridge$getDimensionId();

                if (world2 == null) {
                    return world1DimId;
                }

                final int world2DimId = ((WorldServerBridge) world2).bridge$getDimensionId();
                return world1DimId - world2DimId;
            };

    private static boolean isVanillaRegistered = false;
    private static int lastUsedDimensionId = 0;

    public static void registerVanillaTypesAndDimensions() {
        if (!isVanillaRegistered) {
            WorldManager.registerDimensionType(0, DimensionType.OVERWORLD);
            WorldManager.registerDimensionType(-1, DimensionType.NETHER);
            WorldManager.registerDimensionType(1, DimensionType.THE_END);

            WorldManager.registerDimension(0, DimensionType.OVERWORLD);
            WorldManager.registerDimension(-1, DimensionType.NETHER);
            WorldManager.registerDimension(1, DimensionType.THE_END);
        }

        isVanillaRegistered = true;
    }

    public static void registerDimensionType(final DimensionType type) {
        checkNotNull(type);
        final Optional<Integer> optNextDimensionTypeId = getNextFreeDimensionTypeId();
        optNextDimensionTypeId.ifPresent(integer -> registerDimensionType(integer, type));

    }

    public static void registerDimensionType(final int dimensionTypeId, final DimensionType type) {
        checkNotNull(type);
        if (dimensionTypeByTypeId.containsKey(dimensionTypeId)) {
            return;
        }

        dimensionTypeByTypeId.put(dimensionTypeId, type);
    }

    private static Optional<Integer> getNextFreeDimensionTypeId() {
        Integer highestDimensionTypeId = null;

        for (final Integer dimensionTypeId : dimensionTypeByTypeId.keySet()) {
            if (highestDimensionTypeId == null || highestDimensionTypeId < dimensionTypeId) {
                highestDimensionTypeId = dimensionTypeId;
            }
        }

        if (highestDimensionTypeId != null && highestDimensionTypeId < 127) {
            return Optional.of(++highestDimensionTypeId);
        }
        return Optional.empty();
    }

    public static Integer getNextFreeDimensionId() {
        int next = lastUsedDimensionId;
        while (usedDimensionIds.contains(next) || !checkAvailable(next)) {
            next++;
        }
        return lastUsedDimensionId = next;
    }

    private static boolean checkAvailable(final int dimensionId) {
        if (worldByDimensionId.containsKey(dimensionId)) {
            usedDimensionIds.add(dimensionId);
            return false;
        }
        return true;
    }

    public static void registerDimension(final int dimensionId, final DimensionType type) {
        checkNotNull(type);
        if (!dimensionTypeByTypeId.containsValue(type)) {
            return;
        }

        if (dimensionTypeByDimensionId.containsKey(dimensionId)) {
            return;
        }

        dimensionTypeByDimensionId.put(dimensionId, type);
        if (dimensionId >= 0) {
            usedDimensionIds.add(dimensionId);
        }
    }

    public static void unregisterDimension(final int dimensionId) {
        if (!dimensionTypeByDimensionId.containsKey(dimensionId))
        {
            throw new IllegalArgumentException("Failed to unregister dimension [" + dimensionId + "] as it is not registered!");
        }
        dimensionTypeByDimensionId.remove(dimensionId);
    }

    private static void registerVanillaDimensionPaths(final Path savePath) {
        WorldManager.registerDimensionPath(0, savePath);
        WorldManager.registerDimensionPath(-1, savePath.resolve("DIM-1"));
        WorldManager.registerDimensionPath(1, savePath.resolve("DIM1"));
    }

    public static void registerDimensionPath(final int dimensionId, final Path dimensionDataRoot) {
        checkNotNull(dimensionDataRoot);
        dimensionPathByDimensionId.put(dimensionId, dimensionDataRoot);
    }

    public static Path getDimensionPath(final int dimensionId) {
        return dimensionPathByDimensionId.get(dimensionId);
    }

    public static Optional<DimensionType> getDimensionType(final int dimensionId) {
        return Optional.ofNullable(dimensionTypeByDimensionId.get(dimensionId));
    }

    public static Optional<DimensionType> getDimensionTypeByTypeId(final int dimensionTypeId) {
        return Optional.ofNullable(dimensionTypeByTypeId.get(dimensionTypeId));
    }

    public static Optional<DimensionType> getDimensionType(final Class<? extends WorldProvider> providerClass) {
        checkNotNull(providerClass);
        for (final Object rawDimensionType : dimensionTypeByTypeId.values()) {
            final DimensionType dimensionType = (DimensionType) rawDimensionType;
            if (((org.spongepowered.api.world.DimensionType) (Object) dimensionType).getDimensionClass().equals(providerClass)) {
                return Optional.of(dimensionType);
            }
        }

        return Optional.empty();
    }

    public static Collection<DimensionType> getDimensionTypes() {
        return dimensionTypeByTypeId.values();
    }

    public static int[] getRegisteredDimensionIdsFor(final DimensionType type) {
        return dimensionTypeByDimensionId.int2ObjectEntrySet().stream()
                .filter(entry -> entry.getValue() == type)
                .mapToInt(Int2ObjectMap.Entry::getIntKey)
                .toArray();
    }

    public static int[] getRegisteredDimensionIds() {
        return dimensionTypeByDimensionId.keySet().toIntArray();
    }

    @Nullable
    private static Path getWorldFolder(final DimensionType dimensionType, final int dimensionId) {
        return dimensionPathByDimensionId.get(dimensionId);
    }

    public static boolean isDimensionRegistered(final int dimensionId) {
        return dimensionTypeByDimensionId.containsKey(dimensionId);
    }

    private static Map<Integer, DimensionType> sortedDimensionMap() {
        final Int2ObjectMap<DimensionType> copy = new Int2ObjectOpenHashMap<>(dimensionTypeByDimensionId);

        final HashMap<Integer, DimensionType> newMap = new LinkedHashMap<>();

        newMap.put(0, copy.remove(0));

        DimensionType removed = copy.remove(-1);
        if (removed != null) {
            newMap.put(-1, removed);
        }

        removed = copy.remove(1);
        if (removed != null) {
            newMap.put(1, removed);
        }

        final int[] ids = copy.keySet().toIntArray();
        Arrays.sort(ids);

        for (final int id : ids) {
            newMap.put(id, copy.get(id));
        }

        return newMap;
    }

    public static ObjectIterator<Int2ObjectMap.Entry<WorldServer>> worldsIterator() {
        return worldByDimensionId.int2ObjectEntrySet().fastIterator();
    }

    public static Collection<WorldServer> getWorlds() {
        return worldByDimensionId.values();
    }

    public static Optional<WorldServer> getWorldByDimensionId(final int dimensionId) {
        return Optional.ofNullable(worldByDimensionId.get(dimensionId));
    }

    public static Optional<String> getWorldFolderByDimensionId(final int dimensionId) {
        return Optional.ofNullable(worldFolderByDimensionId.get(dimensionId));
    }

    public static int[] getLoadedWorldDimensionIds() {
        return worldByDimensionId.keySet().toIntArray();
    }

    public static Optional<WorldServer> getWorld(final String worldName) {
        for (final WorldServer worldServer : getWorlds()) {
            final org.spongepowered.api.world.World apiWorld = (org.spongepowered.api.world.World) worldServer;
            if (apiWorld.getName().equals(worldName)) {
                return Optional.of(worldServer);
            }
        }
        return Optional.empty();
    }

    private static void registerWorldProperties(final WorldProperties properties) {
        checkNotNull(properties);
        worldPropertiesByFolderName.put(properties.getWorldName(), properties);
        worldPropertiesByWorldUuid.put(properties.getUniqueId(), properties);
        worldUuidByFolderName.put(properties.getWorldName(), properties.getUniqueId());
        final Integer dimensionId = ((WorldInfoBridge) properties).bridge$getDimensionId();
        worldFolderByDimensionId.put(dimensionId, properties.getWorldName());
        usedDimensionIds.add(dimensionId);
    }

    public static void unregisterWorldProperties(final WorldProperties properties, final boolean freeDimensionId) {
        checkNotNull(properties);
        worldPropertiesByFolderName.remove(properties.getWorldName());
        worldPropertiesByWorldUuid.remove(properties.getUniqueId());
        worldUuidByFolderName.remove(properties.getWorldName());
        final Integer dimensionId = ((WorldInfoBridge) properties).bridge$getDimensionId();
        worldFolderByDimensionId.remove(dimensionId);
        if (dimensionId != null && freeDimensionId) {
            usedDimensionIds.remove(dimensionId.intValue());
        }
    }

    // used by SpongeForge client
    public static void unregisterAllWorldSettings() {
        worldPropertiesByFolderName.clear();
        worldPropertiesByWorldUuid.clear();
        worldUuidByFolderName.clear();
        worldByDimensionId.clear();
        worldFolderByDimensionId.clear();
        dimensionTypeByDimensionId.clear();
        dimensionPathByDimensionId.clear();
        usedDimensionIds.clear();
        weakWorldByWorld.clear();

        isVanillaRegistered = false;
        // This is needed to ensure that DimensionType is usable by GuiListWorldSelection, which is only ever used when the server isn't running
        registerVanillaTypesAndDimensions();
    }

    public static Optional<WorldProperties> getWorldProperties(final String folderName) {
        checkNotNull(folderName);
        return Optional.ofNullable(worldPropertiesByFolderName.get(folderName));
    }

    public static Collection<WorldProperties> getAllWorldProperties() {
        return Collections.unmodifiableCollection(worldPropertiesByFolderName.values());
    }

    public static Optional<WorldProperties> getWorldProperties(final UUID uuid) {
        checkNotNull(uuid);
        return Optional.ofNullable(worldPropertiesByWorldUuid.get(uuid));
    }

    public static Optional<UUID> getUuidForFolder(final String folderName) {
        checkNotNull(folderName);
        return Optional.ofNullable(worldUuidByFolderName.get(folderName));
    }

    public static Optional<String> getFolderForUuid(final UUID uuid) {
        checkNotNull(uuid);
        return Optional.ofNullable(worldUuidByFolderName.inverse().get(uuid));
    }

    public static WorldProperties createWorldProperties(final String folderName, final WorldArchetype archetype) {
        return createWorldProperties(folderName, archetype, null);
    }

    @SuppressWarnings("ConstantConditions")
    public static WorldProperties createWorldProperties(final String folderName, final WorldArchetype archetype, @Nullable final Integer dimensionId) {
        checkNotNull(folderName);
        checkNotNull(archetype);
        final Optional<WorldServer> optWorldServer = getWorld(folderName);
        if (optWorldServer.isPresent()) {
            return ((org.spongepowered.api.world.World) optWorldServer.get()).getProperties();
        }

        final Optional<WorldProperties> optWorldProperties = WorldManager.getWorldProperties(folderName);

        if (optWorldProperties.isPresent()) {
            return optWorldProperties.get();
        }

        final ISaveHandler saveHandler = new AnvilSaveHandler(WorldManager.getCurrentSavesDirectory().get().toFile(), folderName, true, ((MinecraftServerAccessor) SpongeImpl.getServer()).accessor$getDataFixer());
        WorldInfo worldInfo = saveHandler.func_75757_d();

        if (worldInfo == null) {
            worldInfo = new WorldInfo((WorldSettings) (Object) archetype, folderName);
            // Don't want to randomize the seed if there is an existing save file!
            if (archetype.isSeedRandomized()) {
                ((WorldProperties) worldInfo).setSeed(SpongeImpl.random.nextLong());
            }
        } else {
            // DimensionType must be set before world config is created to get proper path
            ((WorldInfoBridge) worldInfo).bridge$setDimensionType(archetype.getDimensionType());
            ((WorldInfoBridge) worldInfo).bridge$createWorldConfig();
            ((WorldProperties) worldInfo).setGeneratorModifiers(archetype.getGeneratorModifiers());
        }

        setUuidOnProperties(getCurrentSavesDirectory().get(), (WorldProperties) worldInfo);
        if (dimensionId != null) {
            ((WorldInfoBridge) worldInfo).bridge$setDimensionId(dimensionId);
        } else if (((WorldInfoBridge) worldInfo).bridge$getDimensionId() == null
                //|| ((WorldInfoBridge) worldInfo).bridge$bridge$getDimensionId() == Integer.MIN_VALUE // TODO: Evaulate all uses of Integer.MIN_VALUE for dimension ids
                || getWorldByDimensionId(((WorldInfoBridge) worldInfo).bridge$getDimensionId()).isPresent()) {
            // DimensionID is null or 0 or the dimensionID is already assigned to a loaded world
            ((WorldInfoBridge) worldInfo).bridge$setDimensionId(WorldManager.getNextFreeDimensionId());
        }
        ((WorldProperties) worldInfo).setGeneratorType(archetype.getGeneratorType());
        ((WorldInfoBridge) worldInfo).bridge$getConfigAdapter().save();
        registerWorldProperties((WorldProperties) worldInfo);

        SpongeImpl.postEvent(SpongeEventFactory.createConstructWorldPropertiesEvent(Sponge.getCauseStackManager().getCurrentCause(), archetype,
                (WorldProperties) worldInfo));

        saveHandler.func_75755_a(worldInfo, SpongeImpl.getServer().func_184103_al().func_72378_q());

        return (WorldProperties) worldInfo;

    }

    public static boolean saveWorldProperties(final WorldProperties properties) {
        checkNotNull(properties);
        final Optional<WorldServer> optWorldServer = getWorldByDimensionId(((WorldInfoBridge) properties).bridge$getDimensionId());
        // If the World represented in the properties is still loaded, save the properties and have the World reload its info
        if (optWorldServer.isPresent()) {
            final WorldServer worldServer = optWorldServer.get();
            worldServer.func_72860_G().func_75761_a((WorldInfo) properties);
            worldServer.func_72860_G().func_75757_d();
        } else {
            new AnvilSaveHandler(WorldManager.getCurrentSavesDirectory().get().toFile(), properties.getWorldName(), true, ((MinecraftServerAccessor) SpongeImpl.getServer()).accessor$getDataFixer()).func_75761_a((WorldInfo) properties);
        }
        ((WorldInfoBridge) properties).bridge$getConfigAdapter().save();
        // No return values or exceptions so can only assume true.
        return true;
    }

    public static void unloadQueuedWorlds() {
        WorldServer server;

        while ((server = unloadQueue.poll()) != null) {
            unloadWorld(server, true, false);
        }

        unloadQueue.clear();
    }

    public static void queueWorldToUnload(final WorldServer worldServer) {
        checkNotNull(worldServer);

        unloadQueue.add(worldServer);
    }

    public static boolean unloadWorld(final WorldServer worldServer, final boolean checkConfig, final boolean isShuttingDown) {
        checkNotNull(worldServer);

        final MinecraftServer server = SpongeImpl.getServer();

        // Likely leaked, don't want to drop leaked world data
        if (!worldByDimensionId.containsValue(worldServer)) {
            return false;
        }

        // Vanilla sometimes doesn't remove player entities from world first
        if (!isShuttingDown) {
            if (!worldServer.field_73010_i.isEmpty()) {
                return false;
            }

            // We only check config if base game wants to unload world. If mods/plugins say unload, we unload
            if (checkConfig) {
                if (((WorldProperties) worldServer.func_72912_H()).doesKeepSpawnLoaded()) {
                    return false;
                }
            }
        }

        final SpongeConfig<GlobalConfig> globalConfigAdapter = SpongeImpl.getGlobalConfigAdapter();

        try (final PhaseContext<?> ignored = GeneralPhase.State.WORLD_UNLOAD.createPhaseContext().source(worldServer)) {
            ignored.buildAndSwitch();
            final UnloadWorldEvent event = SpongeEventFactory.createUnloadWorldEvent(Sponge.getCauseStackManager().getCurrentCause(),
                (org.spongepowered.api.world.World) worldServer);
            final boolean isCancelled = SpongeImpl.postEvent(event);

            if (!isShuttingDown && isCancelled) {
                return false;
            }

            final WorldServerBridge mixinWorldServer = (WorldServerBridge) worldServer;
            final int dimensionId = mixinWorldServer.bridge$getDimensionId();

            try {
                // Don't save if server is stopping to avoid duplicate saving.
                if (!isShuttingDown) {
                    saveWorld(worldServer, true);
                }

                ((WorldInfoBridge) worldServer.func_72912_H()).bridge$getConfigAdapter().save();
            } catch (MinecraftException e) {
                e.printStackTrace();
            } finally {
                SpongeImpl.getLogger().info("Unloading world [{}] ({}/{})", worldServer.func_72912_H().func_76065_j(),
                    ((org.spongepowered.api.world.World) worldServer).getDimension().getType().getId(), dimensionId);

                // Stop the lighting executor only when the world is going to unload - there's no point in running any more lighting tasks.
                if (globalConfigAdapter.getConfig().getModules().useOptimizations() && globalConfigAdapter.getConfig().getOptimizations().useAsyncLighting()) {
                    ((WorldServerBridge_AsyncLighting) worldServer).asyncLightingBridge$getLightingExecutor().shutdownNow();
                }

                worldByDimensionId.remove(dimensionId);
                weakWorldByWorld.remove(worldServer);
                ((MinecraftServerBridge) server).bridge$removeWorldTickTimes(dimensionId);
                reorderWorldsVanillaFirst();
            }
        }
        return true;
    }

    public static void saveWorld(final WorldServer worldServer, final boolean flush) throws MinecraftException {
        if (((WorldProperties) worldServer.func_72912_H()).getSerializationBehavior() == SerializationBehaviors.NONE) {
            return;
        } else {
            worldServer.func_73044_a(true, null);
        }
        if (flush) {
            worldServer.func_73041_k();
        }
    }

    public static Optional<WorldServer> loadWorld(final UUID uuid) {
        checkNotNull(uuid);
        // If someone tries to load loaded world, return it
        final Optional<org.spongepowered.api.world.World> optWorld = Sponge.getServer().getWorld(uuid);
        if (optWorld.isPresent()) {
            return Optional.of((WorldServer) optWorld.get());
        }
        // Check if we even know of this UUID's folder
        final String worldFolder = worldUuidByFolderName.inverse().get(uuid);
        // We don't know of this UUID at all.
        if (worldFolder == null) {
            return Optional.empty();
        }
        return loadWorld(worldFolder, null);
    }

    public static Optional<WorldServer> loadWorld(final String worldName) {
        checkNotNull(worldName);
        return loadWorld(worldName, null);
    }

    public static Optional<WorldServer> loadWorld(final WorldProperties properties) {
        checkNotNull(properties);
        return loadWorld(properties.getWorldName(), properties);
    }

    private static Optional<WorldServer> loadWorld(final String worldName, @Nullable WorldProperties properties) {
        checkNotNull(worldName);
        final Path currentSavesDir = WorldManager.getCurrentSavesDirectory().orElseThrow(() -> new IllegalStateException("Attempt "
                + "made to load world too early!"));
        final MinecraftServer server = SpongeImpl.getServer();
        final Optional<WorldServer> optExistingWorldServer = getWorld(worldName);
        if (optExistingWorldServer.isPresent()) {
            return optExistingWorldServer;
        }

        if (!server.func_71255_r()) {
            SpongeImpl.getLogger().error("Unable to load world [{}]. Multi-world is disabled via [allow-nether] in [server.properties].", worldName);
            return Optional.empty();
        }

        final Path worldFolder = currentSavesDir.resolve(worldName);
        if (!Files.isDirectory(worldFolder)) {
            SpongeImpl.getLogger().error("Unable to load world [{}]. We cannot find its folder under [{}].", worldFolder, currentSavesDir);
            return Optional.empty();
        }

        final ISaveHandler saveHandler = new AnvilSaveHandler(currentSavesDir.toFile(), worldName, true, ((MinecraftServerAccessor) SpongeImpl.getServer()).accessor$getDataFixer());

        // We weren't given a properties, see if one is cached
        if (properties == null) {
            properties = (WorldProperties) saveHandler.func_75757_d();

            // We tried :'(
            if (properties == null) {
                SpongeImpl.getLogger().error("Unable to load world [{}]. No world properties was found!", worldName);
                return Optional.empty();
            }
        }

        Integer dimensionId = ((WorldInfoBridge) properties).bridge$getDimensionId();

        if (dimensionId == null || dimensionId == Integer.MIN_VALUE) {
            dimensionId = getNextFreeDimensionId();
            ((WorldInfoBridge) properties).bridge$setDimensionId(dimensionId);
        }

        setUuidOnProperties(getCurrentSavesDirectory().get(), properties);
        registerWorldProperties(properties);

        final WorldInfo worldInfo = (WorldInfo) properties;
        ((WorldInfoBridge) worldInfo).bridge$createWorldConfig();

        // check if enabled
        if (!((WorldProperties) worldInfo).isEnabled()) {
            SpongeImpl.getLogger().error("Unable to load world [{}] ({}/{}). It is disabled.", properties.getWorldName(), properties.getDimensionType().getId(), dimensionId);
            return Optional.empty();
        }

        registerDimension(dimensionId, (DimensionType) (Object) properties.getDimensionType());
        registerDimensionPath(dimensionId, worldFolder);
        SpongeImpl.getLogger().info("Loading world [{}] ({}/{})", properties.getWorldName(), properties.getDimensionType().getId(), dimensionId);

        final WorldServer worldServer = createWorldFromProperties(dimensionId, saveHandler, (WorldInfo) properties, new WorldSettings((WorldInfo)
                        properties));

        // Set the worlds on the Minecraft server
        reorderWorldsVanillaFirst();

        return Optional.of(worldServer);
    }

    public static void loadAllWorlds(final long defaultSeed, final WorldType defaultWorldType, final String generatorOptions) {
        final MinecraftServer server = SpongeImpl.getServer();

        // We cannot call getCurrentSavesDirectory here as that would generate a savehandler and trigger a session lock.
        // We'll go ahead and make the directories for the save name here so that the migrator won't fail
        final Path currentSavesDir = ((MinecraftServerAccessor) server).accessor$getAnvilFile().toPath().resolve(server.func_71270_I());
        try {
            // Symlink needs special handling
            if (Files.isSymbolicLink(currentSavesDir)) {
                final Path actualPathLink = Files.readSymbolicLink(currentSavesDir);
                if (Files.notExists(actualPathLink)) {
                    Files.createDirectories(actualPathLink);
                } else if (!Files.isDirectory(actualPathLink)) {
                    throw new IOException("Saves directory [" + currentSavesDir + "] symlink to [" + actualPathLink + "] is not a directory!");
                }
            } else {
                Files.createDirectories(currentSavesDir);
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

        WorldManager.registerVanillaDimensionPaths(currentSavesDir);

        WorldMigrator.migrateWorldsTo(currentSavesDir);

        registerExistingSpongeDimensions(currentSavesDir);

        for (final Map.Entry<Integer, DimensionType> entry: sortedDimensionMap().entrySet()) {

            final int dimensionId = entry.getKey();
            final DimensionType dimensionType = entry.getValue();
            final org.spongepowered.api.world.DimensionType apiDimensionType = (org.spongepowered.api.world.DimensionType) (Object) dimensionType;
            // Skip all worlds besides dimension 0 if multi-world is disabled
            if (dimensionId != 0 && !server.func_71255_r()) {
                continue;
            }

            // Skip already loaded worlds by plugins
            if (getWorldByDimensionId(dimensionId).isPresent()) {
                continue;
            }

            // Step 1 - Grab the world's data folder
            final Path worldFolder = getWorldFolder(dimensionType, dimensionId);
            if (worldFolder == null) {
                SpongeImpl.getLogger().error("An attempt was made to load a world in dimension [{}] ({}) that has no registered world folder!",
                        apiDimensionType.getId(), dimensionId);
                continue;
            }

            final String worldFolderName = worldFolder.getFileName().toString();

            // Step 2 - See if we are allowed to load it
            if (dimensionId != 0) {
                final SpongeConfig<? extends GeneralConfigBase> spongeConfig = SpongeHooks.getConfigAdapter(((DimensionTypeBridge)(Object) dimensionType).bridge$getConfigPath(), worldFolderName);
                if (!spongeConfig.getConfig().getWorld().isWorldEnabled()) {
                    SpongeImpl.getLogger().warn("World [{}] ({}/{}) is disabled. World will not be loaded...", worldFolder,
                        apiDimensionType.getId(), dimensionId);
                    continue;
                }
            }

            // Step 3 - Get our world information from disk
            final ISaveHandler saveHandler;
            if (dimensionId == 0) {
                saveHandler = server.func_71254_M().func_75804_a(server.func_71270_I(), true);
            } else {
                saveHandler = new AnvilSaveHandler(WorldManager.getCurrentSavesDirectory().get().toFile(), worldFolderName, true, ((MinecraftServerAccessor) SpongeImpl.getServer()).accessor$getDataFixer());
            }

            WorldInfo worldInfo = saveHandler.func_75757_d();

            final WorldSettings worldSettings;

            // If this is integrated server, we need to use the WorldSettings from the client's Single Player menu to construct the worlds
            if (server instanceof IntegratedServerBridge) {
                worldSettings = ((IntegratedServerBridge) server).bridge$getSettings();

                // If this is overworld and a new save, the WorldInfo has already been made but we want to still fire the construct event.
                if (dimensionId == 0 && ((IntegratedServerBridge) server).bridge$isNewSave()) {
                    SpongeImpl.postEvent(SpongeEventFactory.createConstructWorldPropertiesEvent(Sponge.getCauseStackManager().getCurrentCause(), (WorldArchetype)
                            (Object) worldSettings, (WorldProperties) worldInfo));
                }
            } else {
                // WorldSettings will be null here on dedicated server so we need to build one
                worldSettings = new WorldSettings(defaultSeed, server.func_71265_f(), server.func_71225_e(), server.func_71199_h(),
                        defaultWorldType);
            }

            if (worldInfo == null) {
                // Step 4 - At this point, we have either have the WorldInfo or we have none. If we have none, we'll use the settings built above to
                // create the WorldInfo
                worldInfo = createWorldInfoFromSettings(currentSavesDir, apiDimensionType,
                        dimensionId, worldFolderName, worldSettings, generatorOptions);
            } else {
                // create config
                ((WorldInfoBridge) worldInfo).bridge$setDimensionType(apiDimensionType);
                ((WorldInfoBridge) worldInfo).bridge$createWorldConfig();
                ((WorldProperties) worldInfo).setGenerateSpawnOnLoad(((DimensionTypeBridge) (Object) dimensionType).bridge$shouldGenerateSpawnOnLoad());
                if (((WorldInfoBridge) worldInfo).bridge$getDimensionId() == null) {
                    ((WorldInfoBridge) worldInfo).bridge$setDimensionId(dimensionId);
                }
            }

            // Safety check to ensure we'll get a unique id no matter what
            UUID uniqueId = ((WorldProperties) worldInfo).getUniqueId();
            if (uniqueId == null) {
                setUuidOnProperties(dimensionId == 0 ? currentSavesDir.getParent() : currentSavesDir, (WorldProperties) worldInfo);
                uniqueId = ((WorldProperties) worldInfo).getUniqueId();
            }

            // Check if this world's unique id has already been registered
            final String previousWorldForUUID = worldUuidByFolderName.inverse().get(uniqueId);
            if (previousWorldForUUID != null) {
                SpongeImpl.getLogger().error("UUID [{}] has already been registered by world [{}] but is attempting to be registered by world [{}]."
                    + " This means worlds have been copied outside of Sponge. Skipping world load...", uniqueId, previousWorldForUUID, worldInfo.func_76065_j());
                continue;
            }

            // Keep the LevelName in the LevelInfo up to date with the directory name
            if (!worldInfo.func_76065_j().equals(worldFolderName)) {
                worldInfo.func_76062_a(worldFolderName);
            }

            // Step 5 - Load server resource pack from dimension 0
            if (dimensionId == 0) {
                ((MinecraftServerAccessor) server).accessor$setResourcePackFromWorld(worldFolderName, saveHandler);
            }

            // Step 6 - Cache the WorldProperties we've made so we don't load from disk later.
            registerWorldProperties((WorldProperties) worldInfo);

            if (dimensionId != 0 && !((WorldProperties) worldInfo).loadOnStartup()) {
                SpongeImpl.getLogger().warn("World [{}] ({}/{}) is set to not load on startup. To load it later, enable "
                    + "[load-on-startup] in config or use a plugin.", worldInfo.func_76065_j(), apiDimensionType.getId(), dimensionId);
                continue;
            }

            // Step 7 - Finally, we can create the world and tell it to load
            final WorldServer worldServer = createWorldFromProperties(dimensionId, saveHandler, worldInfo, worldSettings);
            ;
            SpongeImpl.getLogger().info("Loading world [{}] ({}/{})", ((org.spongepowered.api.world.World) worldServer).getName(),
                apiDimensionType.getId(), dimensionId);
        }

        // Set the worlds on the Minecraft server
        reorderWorldsVanillaFirst();
    }

    private static WorldInfo createWorldInfoFromSettings(final Path currentSaveRoot, final org.spongepowered.api.world.DimensionType dimensionType, final int
      dimensionId, final String worldFolderName, final WorldSettings worldSettings, final String generatorOptions) {

        worldSettings.func_82750_a(generatorOptions);

        ((WorldSettingsBridge) (Object) worldSettings).bridge$setDimensionType(dimensionType);
        ((WorldSettingsBridge)(Object) worldSettings).bridge$setGenerateSpawnOnLoad(((DimensionTypeBridge) dimensionType).bridge$shouldGenerateSpawnOnLoad());

        final WorldInfo worldInfo = new WorldInfo(worldSettings, worldFolderName);
        setUuidOnProperties(dimensionId == 0 ? currentSaveRoot.getParent() : currentSaveRoot, (WorldProperties) worldInfo);
        ((WorldInfoBridge) worldInfo).bridge$setDimensionId(dimensionId);
        SpongeImpl.postEvent(SpongeEventFactory.createConstructWorldPropertiesEvent(Sponge.getCauseStackManager().getCurrentCause(),
                (WorldArchetype) (Object) worldSettings, (WorldProperties) worldInfo));

        return worldInfo;

    }

    @SuppressWarnings("ConstantConditions")
    private static WorldServer createWorldFromProperties(
        final int dimensionId, final ISaveHandler saveHandler, final WorldInfo worldInfo, @Nullable final WorldSettings
        worldSettings) {
        final MinecraftServer server = SpongeImpl.getServer();
        final WorldServer worldServer = new WorldServer(server, saveHandler, worldInfo, dimensionId, server.field_71304_b);

        worldByDimensionId.put(dimensionId, worldServer);
        weakWorldByWorld.put(worldServer, worldServer);

        WorldManager.reorderWorldsVanillaFirst();

        ((MinecraftServerBridge) server).bridge$putWorldTickTimes(dimensionId, new long[100]);

        worldServer.func_175643_b();

        worldServer.func_72954_a(new ServerWorldEventHandler(server, worldServer));

        // This code changes from Mojang's to account for per-world API-set GameModes.
        if (!server.func_71264_H() && worldServer.func_72912_H().func_76077_q() == GameType.NOT_SET) {
            worldServer.func_72912_H().func_76060_a(server.func_71265_f());
        }

        ((ChunkProviderServerBridge) worldServer.func_72863_F()).bridge$setForceChunkRequests(true);
        try {
            SpongeImpl.postEvent(SpongeEventFactory.createLoadWorldEvent(Sponge.getCauseStackManager().getCurrentCause(),
                (org.spongepowered.api.world.World) worldServer));

            // WorldSettings is only non-null here if this is a newly generated WorldInfo and therefore we need to initialize to calculate spawn.
            if (worldSettings != null) {
                worldServer.func_72963_a(worldSettings);
            }

            if (((DimensionTypeBridge) ((org.spongepowered.api.world.World) worldServer).getDimension().getType()).bridge$shouldLoadSpawn()) {
                ((MinecraftServerBridge) server).bridge$prepareSpawnArea(worldServer);
            }

            // While we try to prevnt mods from changing a worlds' WorldInfo, we aren't always
            // successful. We re-do the fake world check to catch any changes made to WorldInfo
            // that would make it invalid
            ((WorldBridge) worldServer).bridge$clearFakeCheck();

            return worldServer;
        } finally {
            ((ChunkProviderServerBridge) worldServer.func_72863_F()).bridge$setForceChunkRequests(false);
        }
    }

    /**
     * Internal use only - Namely for SpongeForge.
     * @param dimensionId The world instance dimension id
     * @param worldServer The world server
     */
    public static void forceAddWorld(final int dimensionId, final WorldServer worldServer) {
        worldByDimensionId.put(dimensionId, worldServer);
        weakWorldByWorld.put(worldServer, worldServer);

        ((MinecraftServerBridge) SpongeImpl.getServer()).bridge$putWorldTickTimes(dimensionId, new long[100]);
    }

    public static void reorderWorldsVanillaFirst() {
        final List<WorldServer> sorted = new LinkedList<>();

        final List<Integer> vanillaWorldIds = new ArrayList<>();
        WorldServer worldServer = worldByDimensionId.get(0);

        if (worldServer != null) {
            vanillaWorldIds.add(0);
            sorted.add(worldServer);
        }

        worldServer = worldByDimensionId.get(-1);

        if (worldServer != null) {
            vanillaWorldIds.add(-1);
            sorted.add(worldServer);
        }

        worldServer = worldByDimensionId.get(1);

        if (worldServer != null) {
            vanillaWorldIds.add(1);
            sorted.add(worldServer);
        }

        final List<WorldServer> worlds = new ArrayList<>(worldByDimensionId.values());
        final Iterator<WorldServer> iterator = worlds.iterator();
        while(iterator.hasNext()) {
            final WorldServerBridge mixinWorld = (WorldServerBridge) iterator.next();
            final int dimensionId = mixinWorld.bridge$getDimensionId();
            if (vanillaWorldIds.contains(dimensionId)) {
                iterator.remove();
            }
        }

        worlds.sort(WORLD_SERVER_COMPARATOR);
        sorted.addAll(worlds);
        SpongeImpl.getServer().field_71305_c = sorted.toArray(new WorldServer[0]);
    }

    /**
     * Parses a {@link UUID} from disk from other known plugin platforms and sets it on the
     * {@link WorldProperties}. Currently only Bukkit is supported.
     */
    private static void setUuidOnProperties(final Path savesRoot, final WorldProperties properties) {
        checkNotNull(properties);

        UUID uuid;
        if (properties.getUniqueId() == null || properties.getUniqueId().equals
                (UUID.fromString("00000000-0000-0000-0000-000000000000"))) {
            // Check if Bukkit's uid.dat file is here and use it
            final Path uidPath = savesRoot.resolve(properties.getWorldName()).resolve("uid.dat");
            if (Files.notExists(uidPath)) {
                uuid = UUID.randomUUID();
            } else {
                try(final DataInputStream dis = new DataInputStream(Files.newInputStream(uidPath))) {
                    uuid = new UUID(dis.readLong(), dis.readLong());
                } catch (IOException e) {
                    SpongeImpl.getLogger().error("World folder [{}] has an existing Bukkit unique identifier for it but we encountered issues parsing "
                            + "the file. We will have to use a new unique id. Please report this to Sponge ASAP.", properties.getWorldName(), e);
                    uuid = UUID.randomUUID();
                }
            }
        } else {
            uuid = properties.getUniqueId();
        }

        ((WorldInfoBridge) properties).bridge$setUniqueId(uuid);
    }

    /**
     * Handles registering existing Sponge dimensions that are not the root dimension (known as overworld).
     */
    private static void registerExistingSpongeDimensions(final Path rootPath) {
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(rootPath, LEVEL_AND_SPONGE)) {
            for (final Path worldPath : stream) {
                final Path spongeLevelPath = worldPath.resolve("level_sponge.dat");
                final String worldFolderName = worldPath.getFileName().toString();

                final NBTTagCompound compound;
                try {
                    compound = CompressedStreamTools.func_74796_a(Files.newInputStream(spongeLevelPath));
                } catch (IOException e) {
                    SpongeImpl.getLogger().error("Failed loading Sponge data for World [{}]}. Report to Sponge ASAP.", worldFolderName, e);
                    continue;
                }

                NBTTagCompound spongeDataCompound = compound.func_74775_l(Constants.Sponge.SPONGE_DATA);

                if (!compound.func_74764_b(Constants.Sponge.SPONGE_DATA)) {
                    SpongeImpl.getLogger()
                            .error("World [{}] has Sponge related data in the form of [level-sponge.dat] but the structure is not proper."
                                            + " Generally, the data is within a [{}] tag but it is not for this world. Report to Sponge ASAP.",
                                    worldFolderName, Constants.Sponge.SPONGE_DATA);
                    continue;
                }

                if (!spongeDataCompound.func_74764_b(Constants.Sponge.World.DIMENSION_ID)) {
                    SpongeImpl.getLogger().error("World [{}] has no dimension id. Report this to Sponge ASAP.", worldFolderName);
                    continue;
                }

                spongeDataCompound = DataUtil.spongeDataFixer.func_188257_a(FixTypes.LEVEL, spongeDataCompound);

                final int dimensionId = spongeDataCompound.func_74762_e(Constants.Sponge.World.DIMENSION_ID);
                // We do not handle Vanilla dimensions, skip them
                if (dimensionId == 0 || dimensionId == -1 || dimensionId == 1) {
                    continue;
                }

                if (dimensionId == Integer.MIN_VALUE) {
                    continue;
                }

                if (dimensionTypeByDimensionId.containsKey(dimensionId)) {
                    SpongeImpl.getLogger().warn("World [{}] ({}) is attempting to be registered as an " +
                                    "existing dimension but it's dimension id has already been registered for folder " +
                                    "[{}]. This means the world has been copied outside of Sponge. This is not a " +
                                    "supported configuration.", worldFolderName, dimensionId, worldFolderByDimensionId
                            .get(dimensionId));
                    continue;
                }

                if (!spongeDataCompound.func_186855_b(Constants.UUID)) {
                    SpongeImpl.getLogger().error("World [{}] ({}) has no valid unique identifier. Report this to Sponge ASAP.", worldFolderName, dimensionId);
                    continue;
                }

                String dimensionTypeId = "overworld";

                if (spongeDataCompound.func_74764_b(Constants.Sponge.World.DIMENSION_TYPE)) {
                    dimensionTypeId = spongeDataCompound.func_74779_i(Constants.Sponge.World.DIMENSION_TYPE);
                } else {
                    SpongeImpl.getLogger().warn("World [{}] ({}) has no specified dimension type. Defaulting to [{}}]...", worldFolderName,
                            dimensionId, DimensionTypes.OVERWORLD.getName());
                }

                dimensionTypeId = fixDimensionTypeId(dimensionTypeId);
                final org.spongepowered.api.world.DimensionType dimensionType
                        = Sponge.getRegistry().getType(org.spongepowered.api.world.DimensionType.class, dimensionTypeId).orElse(null);
                if (dimensionType == null) {
                    SpongeImpl.getLogger().warn("World [{}] ({}) has specified dimension type that is not registered. Skipping...", worldFolderName, dimensionId);
                    continue;
                }

                spongeDataCompound.func_74778_a(Constants.Sponge.World.DIMENSION_TYPE, dimensionTypeId);

                worldFolderByDimensionId.put(dimensionId, worldFolderName);
                registerDimensionPath(dimensionId, rootPath.resolve(worldFolderName));
                registerDimension(dimensionId, (DimensionType)(Object) dimensionType);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Checks if the saved dimension type contains a modid and if not, attempts to locate one
    public static String fixDimensionTypeId(final String name) {
        // Since we now store the modid, we need to support older save files that only include id without modid.
        if (!name.contains(":")) {
            for (final org.spongepowered.api.world.DimensionType type : Sponge.getRegistry().getAllOf(org.spongepowered.api.world.DimensionType.class)) {
                final String typeId = (type.getId().substring(type.getId().lastIndexOf(":") + 1));
                if (typeId.equals(name)) {
                    return type.getId();
                    // Note: We don't update the NBT here but instead fix it on next
                    //       world save in case there are 2 types using same name.
                }
            }
        }

        return name;
    }

    public static CompletableFuture<Optional<WorldProperties>> copyWorld(final WorldProperties worldProperties, final String copyName) {
        checkArgument(worldPropertiesByFolderName.containsKey(worldProperties.getWorldName()), "World properties not registered!");
        checkArgument(!worldPropertiesByFolderName.containsKey(copyName), "Destination world name already is registered!");
        final WorldInfo info = (WorldInfo) worldProperties;

        final WorldServer worldServer = worldByDimensionId.get(((WorldInfoBridge) info).bridge$getDimensionId().intValue());
        if (worldServer != null) {
            try {
                saveWorld(worldServer, true);
            } catch (MinecraftException e) {
                throw new RuntimeException(e);
            }

            ((MinecraftServerBridge) SpongeImpl.getServer()).bridge$setSaveEnabled(false);
        }

        final CompletableFuture<Optional<WorldProperties>> future = SpongeImpl.getScheduler().submitAsyncTask(new CopyWorldTask(info, copyName));
        if (worldServer != null) { // World was loaded
            future.thenRun(() -> ((MinecraftServerBridge) SpongeImpl.getServer()).bridge$setSaveEnabled(true));
        }
        return future;
    }

    public static Optional<WorldProperties> renameWorld(final WorldProperties worldProperties, final String newName) {
        checkNotNull(worldProperties);
        checkNotNull(newName);
        checkState(!worldByDimensionId.containsKey(((WorldInfoBridge) worldProperties).bridge$getDimensionId()), "World is still loaded!");

        final Path oldWorldFolder = getCurrentSavesDirectory().get().resolve(worldProperties.getWorldName());
        final Path newWorldFolder = oldWorldFolder.resolveSibling(newName);
        if (Files.exists(newWorldFolder)) {
            return Optional.empty();
        }

        try {
            Files.move(oldWorldFolder, newWorldFolder);
        } catch (IOException e) {
            return Optional.empty();
        }

        unregisterWorldProperties(worldProperties, false);

        final WorldInfo info = new WorldInfo((WorldInfo) worldProperties);
        info.func_76062_a(newName);

        // As we are moving a world, we want to move the dimension ID and UUID with the world to ensure
        // plugins and Sponge do not break.
        ((WorldInfoBridge) info).bridge$setUniqueId(worldProperties.getUniqueId());
        if (((WorldInfoBridge) worldProperties).bridge$getDimensionId() != null) {
            ((WorldInfoBridge) info).bridge$setDimensionId(((WorldInfoBridge) worldProperties).bridge$getDimensionId());
        }

        ((WorldInfoBridge) info).bridge$createWorldConfig();
        new AnvilSaveHandler(WorldManager.getCurrentSavesDirectory().get().toFile(), newName, true, ((MinecraftServerAccessor) SpongeImpl.getServer()).accessor$getDataFixer())
                .func_75761_a(info);
        registerWorldProperties((WorldProperties) info);
        return Optional.of((WorldProperties) info);
    }

    public static CompletableFuture<Boolean> deleteWorld(final WorldProperties worldProperties) {
        checkNotNull(worldProperties);
        checkArgument(worldPropertiesByWorldUuid.containsKey(worldProperties.getUniqueId()), "World properties not registered!");
        checkState(!worldByDimensionId.containsKey(((WorldInfoBridge) worldProperties).bridge$getDimensionId()), "World not unloaded!");
        return SpongeImpl.getScheduler().submitAsyncTask(new DeleteWorldTask(worldProperties));
    }

    /**
     * Called when the server wants to update the difficulty on all worlds.
     *
     * If the world has a difficulty set via external means (command, plugin, mod) then we honor that difficulty always.
     */
    public static void updateServerDifficulty() {
        final EnumDifficulty serverDifficulty = SpongeImpl.getServer().func_147135_j();

        for (final WorldServer worldServer : getWorlds()) {
            final boolean alreadySet = ((WorldInfoBridge) worldServer.func_72912_H()).bridge$hasCustomDifficulty();
            adjustWorldForDifficulty(worldServer, alreadySet ? worldServer.func_72912_H().func_176130_y() : serverDifficulty, false);
        }
    }

    public static void adjustWorldForDifficulty(final WorldServer worldServer, EnumDifficulty difficulty, final boolean isCustom) {
        final MinecraftServer server = SpongeImpl.getServer();

        if (worldServer.func_72912_H().func_76093_s()) {
            difficulty = EnumDifficulty.HARD;
            worldServer.func_72891_a(true, true);
        } else if (SpongeImpl.getServer().func_71264_H()) {
            worldServer.func_72891_a(worldServer.func_175659_aa() != EnumDifficulty.PEACEFUL, true);
        } else {
            worldServer.func_72891_a(server.func_71193_K(), server.func_71268_U());
        }

        if (isCustom) {
            worldServer.func_72912_H().func_176144_a(difficulty);
        } else if (!((WorldInfoBridge) worldServer.func_72912_H()).bridge$hasCustomDifficulty()) {
            ((WorldInfoBridge) worldServer.func_72912_H()).bridge$forceSetDifficulty(difficulty);
        }
    }

    private static class CopyWorldTask implements Callable<Optional<WorldProperties>> {

        private final WorldInfo oldInfo;
        private final String newName;

        CopyWorldTask(final WorldInfo info, final String newName) {
            this.oldInfo = info;
            this.newName = newName;
        }

        @Override
        public Optional<WorldProperties> call() throws Exception {
            Path oldWorldFolder = getCurrentSavesDirectory().get().resolve(this.oldInfo.func_76065_j());
            final Path newWorldFolder = getCurrentSavesDirectory().get().resolve(this.newName);

            if (Files.exists(newWorldFolder)) {
                return Optional.empty();
            }

            FileVisitor<Path> visitor = new CopyFileVisitor(newWorldFolder);
            if (((WorldInfoBridge) this.oldInfo).bridge$getDimensionId() == 0) {
                oldWorldFolder = getCurrentSavesDirectory().get();
                visitor = new ForwardingFileVisitor<Path>(visitor) {

                    private boolean root = true;

                    @Override
                    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                        if (!this.root && Files.exists(dir.resolve("level.dat"))) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }

                        this.root = false;
                        return super.preVisitDirectory(dir, attrs);
                    }
                };
            }

            // Copy the world folder
            Files.walkFileTree(oldWorldFolder, visitor);

            final WorldInfo info = new WorldInfo(this.oldInfo);
            info.func_76062_a(this.newName);

            ((WorldInfoBridge) info).bridge$setDimensionId(Integer.MIN_VALUE);
            ((WorldInfoBridge) info).bridge$setUniqueId(UUID.fromString("00000000-0000-0000-0000-000000000000"));

            new AnvilSaveHandler(WorldManager.getCurrentSavesDirectory().get().toFile(), this.newName, true, ((MinecraftServerAccessor) SpongeImpl.getServer()).accessor$getDataFixer())
                    .func_75761_a(info);

            return Optional.of((WorldProperties) info);
        }
    }

    private static class DeleteWorldTask implements Callable<Boolean> {

        private final WorldProperties props;

        DeleteWorldTask(final WorldProperties props) {
            this.props = props;
        }

        @Override
        public Boolean call() {
            final Path worldFolder = getCurrentSavesDirectory().get().resolve(this.props.getWorldName());
            if (!Files.exists(worldFolder)) {
                unregisterWorldProperties(this.props, true);
                return true;
            }

            try {
                Files.walkFileTree(worldFolder, DeleteFileVisitor.INSTANCE);
                unregisterWorldProperties(this.props, true);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

    }

    public static void sendDimensionRegistration(final EntityPlayerMP playerMP, final WorldProvider provider) {
        // Do nothing in Common
    }

    public static void loadDimensionDataMap(@Nullable final NBTTagCompound compound) {
        usedDimensionIds.clear();
        lastUsedDimensionId = 0;

        if (compound == null) {
            dimensionTypeByDimensionId.keySet().stream().filter(dimensionId -> dimensionId >= 0).forEach(usedDimensionIds::add);
        } else {
            for (final int id : compound.func_74759_k(Constants.Forge.USED_DIMENSION_IDS)) {
                usedDimensionIds.add(id);
            }

            // legacy data (load but don't save)
            final int[] intArray = compound.func_74759_k(Constants.Legacy.LEGACY_DIMENSION_ARRAY);
            for (int i = 0; i < intArray.length; i++) {
                final int data = intArray[i];
                if (data == 0) continue;
                for (int j = 0; j < Integer.SIZE; j++) {
                    if ((data & (1 << j)) != 0) usedDimensionIds.add(i * Integer.SIZE + j);
                }
            }
        }
    }

    public static NBTTagCompound saveDimensionDataMap() {
        final NBTTagCompound dimMap = new NBTTagCompound();
        dimMap.func_74783_a(Constants.Forge.USED_DIMENSION_IDS, usedDimensionIds.toIntArray());
        return dimMap;
    }

    public static Optional<Path> getCurrentSavesDirectory() {
        final Optional<WorldServer> optWorldServer = getWorldByDimensionId(0);

        if (optWorldServer.isPresent()) {
            return Optional.of(optWorldServer.get().func_72860_G().func_75765_b().toPath());
        } else if (SpongeImpl.getGame().getState().ordinal() >= GameState.SERVER_ABOUT_TO_START.ordinal()) {
            final SaveHandler saveHandler = (SaveHandler) SpongeImpl.getServer().func_71254_M().func_75804_a(SpongeImpl.getServer().func_71270_I(), false);
            return Optional.of(saveHandler.func_75765_b().toPath());
        }

        return Optional.empty();
    }

    public static Map<WorldServer, WorldServer> getWeakWorldMap() {
        return weakWorldByWorld;
    }

    public static int getClientDimensionId(final EntityPlayerMP player, final World world) {
        final DimensionType type = world.field_73011_w.func_186058_p();
        if (type == DimensionType.OVERWORLD) {
            return 0;
        } else if (type == DimensionType.NETHER) {
            return -1;
        } else if (type == DimensionType.THE_END) {
            return 1;
        } else {
            return ((WorldServerBridge) world).bridge$getDimensionId();
        }
    }

    public static boolean isKnownWorld(final WorldServer world) {
        return weakWorldByWorld.containsKey(world);
    }
}
