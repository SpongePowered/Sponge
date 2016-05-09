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

import com.google.common.base.Throwables;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.MapMaker;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.storage.AnvilSaveHandler;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.apache.commons.io.FileUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.interfaces.IMixinMinecraftServer;
import org.spongepowered.common.interfaces.world.IMixinWorldInfo;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.interfaces.world.IMixinWorldSettings;
import org.spongepowered.common.scheduler.SpongeScheduler;
import org.spongepowered.common.util.SpongeHooks;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

public final class WorldManager {

    public static final DirectoryStream.Filter<Path> LEVEL_AND_SPONGE =
            entry -> Files.isDirectory(entry) && Files.exists(entry.resolve("level.dat")) && Files.exists(entry.resolve("level_sponge.dat"));

    private static final TIntObjectHashMap<DimensionType> dimensionTypeByTypeId = new TIntObjectHashMap<>(3);
    private static final TIntObjectHashMap<DimensionType> dimensionTypeByDimensionId = new TIntObjectHashMap<>(3);
    private static final TIntObjectHashMap<Path> dimensionPathByDimensionId = new TIntObjectHashMap<>(3);
    private static final TIntObjectHashMap<WorldServer> worldByDimensionId = new TIntObjectHashMap<>(3);
    private static final Map<String, WorldProperties> worldPropertiesByFolderName = new HashMap<>(3);
    private static final Map<UUID, WorldProperties> worldPropertiesByWorldUuid =  new HashMap<>(3);
    private static final BiMap<String, UUID> worldUuidByFolderName =  HashBiMap.create(3);
    private static final BitSet dimensionBits = new BitSet(Long.SIZE << 4);
    private static final Map<World, World> weakWorldByWorld = new MapMaker().weakKeys().weakValues().concurrencyLevel(1).makeMap();
    private static final Queue<WorldServer> unloadQueue = new ArrayDeque<>();
    private static final Comparator<WorldServer>
            WORLD_SERVER_COMPARATOR =
            (world1, world2) -> ((IMixinWorldServer) world1).getDimensionId() - ((IMixinWorldServer) world2).getDimensionId();

    static {
        registerDimensionType(0, DimensionType.OVERWORLD);
        registerDimensionType(-1, DimensionType.NETHER);
        registerDimensionType(1, DimensionType.THE_END);
    }

    public static RegisterDimensionTypeResult registerDimensionType(DimensionType type) {
        checkNotNull(type);
        final Optional<Integer> optNextDimensionTypeId = getNextFreeDimensionTypeId();
        if (!optNextDimensionTypeId.isPresent()) {
            return RegisterDimensionTypeResult.DIMENSION_TYPE_LIMIT_EXCEEDED;
        }

        return registerDimensionType(optNextDimensionTypeId.get(), type);
    }

    public static RegisterDimensionTypeResult registerDimensionType(int dimensionTypeId, DimensionType type) {
        checkNotNull(type);
        if (dimensionTypeByTypeId.containsKey(dimensionTypeId)) {
            return RegisterDimensionTypeResult.DIMENSION_TYPE_ALREADY_REGISTERED;
        }

        dimensionTypeByTypeId.put(dimensionTypeId, type);
        return RegisterDimensionTypeResult.DIMENSION_TYPE_REGISTERED;
    }

    private static Optional<Integer> getNextFreeDimensionTypeId() {
        Integer highestDimensionTypeId = null;

        for (Integer dimensionTypeId : dimensionTypeByTypeId.keys()) {
            if (highestDimensionTypeId == null || highestDimensionTypeId < dimensionTypeId) {
                highestDimensionTypeId = dimensionTypeId;
            }
        }

        if (highestDimensionTypeId != null && highestDimensionTypeId < 127) {
            return Optional.of(++highestDimensionTypeId);
        }
        return Optional.empty();
    }

    private static Integer getNextFreeDimensionId() {
        return dimensionBits.nextClearBit(0);
    }

    public static RegisterDimensionResult registerDimension(int dimensionId, DimensionType type, Path dimensionDataRoot) {
        checkNotNull(type);
        checkNotNull(dimensionDataRoot);
        if (!dimensionTypeByTypeId.containsValue(type)) {
            return RegisterDimensionResult.DIMENSION_TYPE_IS_NOT_REGISTERED;
        }

        if (dimensionTypeByDimensionId.containsKey(dimensionId)) {
            return RegisterDimensionResult.DIMENSION_ALREADY_REGISTERED;
        }
        dimensionTypeByDimensionId.put(dimensionId, type);
        dimensionPathByDimensionId.put(dimensionId, dimensionDataRoot);
        if (dimensionId >= 0) {
            dimensionBits.set(dimensionId);
        }
        return RegisterDimensionResult.DIMENSION_REGISTERED;
    }

    public static Optional<DimensionType> getDimensionType(int dimensionId) {
        return Optional.ofNullable(dimensionTypeByDimensionId.get(dimensionId));
    }

    public static Optional<DimensionType> getDimensionType(Class<? extends WorldProvider> providerClass) {
        checkNotNull(providerClass);
        for (Object rawDimensionType : dimensionTypeByTypeId.values()) {
            final DimensionType dimensionType = (DimensionType) rawDimensionType;
            if (((org.spongepowered.api.world.DimensionType) (Object) dimensionType).getDimensionClass().equals(providerClass)) {
                return Optional.of(dimensionType);
            }
        }

        return Optional.empty();
    }

    public static Collection<DimensionType> getDimensionTypes() {
        return dimensionTypeByTypeId.valueCollection();
    }

    public static Optional<Path> getWorldFolder(int dimensionId) {
        return Optional.ofNullable(dimensionPathByDimensionId.get(dimensionId));
    }

    public static boolean isDimensionRegistered(int dimensionId) {
        return dimensionTypeByDimensionId.containsKey(dimensionId);
    }

    public static Map<Integer, DimensionType> sortedDimensionMap() {
        TIntObjectMap<DimensionType> copy = new TIntObjectHashMap<>(dimensionTypeByDimensionId);

        HashMap<Integer, DimensionType> newMap = new LinkedHashMap<>();

        newMap.put(0, copy.remove(0));
        newMap.put(-1, copy.remove(-1));
        newMap.put(1, copy.remove(1));

        int[] ids = copy.keys();
        Arrays.sort(ids);

        for (int id: ids) {
            newMap.put(id, copy.get(id));
        }

        return newMap;
    }

    public static TIntObjectIterator<WorldServer> worldsIterator() {
        return worldByDimensionId.iterator();
    }

    public static Collection<WorldServer> getWorlds() {
        return worldByDimensionId.valueCollection();
    }

    public static Optional<WorldServer> getWorldByDimensionId(int dimensionId) {
        return Optional.ofNullable(worldByDimensionId.get(dimensionId));
    }

    public static Optional<WorldServer> getWorld(String worldName) {
        for (WorldServer worldServer : getWorlds()) {
            final org.spongepowered.api.world.World apiWorld = (org.spongepowered.api.world.World) worldServer;
            if (apiWorld.getName().equals(worldName)) {
                return Optional.of(worldServer);
            }
        }
        return Optional.empty();
    }

    public static void registerWorldProperties(WorldProperties properties) {
        checkNotNull(properties);
        worldPropertiesByFolderName.put(properties.getWorldName(), properties);
        worldPropertiesByWorldUuid.put(properties.getUniqueId(), properties);
        worldUuidByFolderName.put(properties.getWorldName(), properties.getUniqueId());
    }

    public static void unregisterWorldProperties(WorldProperties properties) {
        checkNotNull(properties);
        worldPropertiesByFolderName.remove(properties.getWorldName());
        worldPropertiesByWorldUuid.remove(properties.getUniqueId());
        worldUuidByFolderName.remove(properties.getWorldName());
    }

    public static Optional<WorldProperties> getWorldProperties(String folderName) {
        checkNotNull(folderName);
        return Optional.ofNullable(worldPropertiesByFolderName.get(folderName));
    }

    public static Collection<WorldProperties> getAllWorldProperties() {
        return Collections.unmodifiableCollection(worldPropertiesByFolderName.values());
    }

    public static Optional<WorldProperties> getWorldProperties(UUID uuid) {
        checkNotNull(uuid);
        return Optional.ofNullable(worldPropertiesByWorldUuid.get(uuid));
    }

    public static Optional<UUID> getUuidForFolder(String folderName) {
        checkNotNull(folderName);
        return Optional.ofNullable(worldUuidByFolderName.get(folderName));
    }

    public static Optional<String> getFolderForUuid(UUID uuid) {
        checkNotNull(uuid);
        return Optional.ofNullable(worldUuidByFolderName.inverse().get(uuid));
    }

    // TODO Some result mechanism
    public static WorldProperties createWorldProperties(String folderName, WorldCreationSettings settings) {
        checkNotNull(settings);
        final Optional<WorldServer> optWorldServer = getWorld(folderName);
        if (optWorldServer.isPresent()) {
            return ((org.spongepowered.api.world.World) optWorldServer.get()).getProperties();
        }

        final Optional<WorldProperties> optWorldProperties = WorldManager.getWorldProperties(folderName);

        if (optWorldProperties.isPresent()) {
            return optWorldProperties.get();
        }

        final ISaveHandler saveHandler = new AnvilSaveHandler(WorldManager.getCurrentSavesDirectory().get().toFile(), folderName, true,
                ((MinecraftServer) Sponge.getServer()).getDataFixer());
        WorldInfo worldInfo = saveHandler.loadWorldInfo();

        if (worldInfo == null) {
            worldInfo = new WorldInfo((WorldSettings) (Object) settings, folderName);
        } else {
            ((IMixinWorldInfo) worldInfo).createWorldConfig();
            ((WorldProperties) worldInfo).setGeneratorModifiers(settings.getGeneratorModifiers());
        }

        setUuidOnProperties(getCurrentSavesDirectory().get(), (WorldProperties) worldInfo);
        ((IMixinWorldInfo) worldInfo).setDimensionId(Integer.MIN_VALUE);
        ((IMixinWorldInfo) worldInfo).setDimensionType(settings.getDimensionType());
        ((WorldProperties) worldInfo).setGeneratorType(settings.getGeneratorType());
        ((IMixinWorldInfo) worldInfo).getWorldConfig().save();
        registerWorldProperties((WorldProperties) worldInfo);

        SpongeImpl.postEvent(SpongeEventFactory.createConstructWorldPropertiesEvent(Cause.of(NamedCause.source(Sponge.getServer())), settings,
                (WorldProperties) worldInfo));

        saveHandler.saveWorldInfoWithPlayer(worldInfo, ((MinecraftServer) Sponge.getServer()).getPlayerList().getHostPlayerData());

        return (WorldProperties) worldInfo;

    }

    public static boolean saveWorldProperties(WorldProperties properties) {
        checkNotNull(properties);
        final Optional<WorldServer> optWorldServer = getWorldByDimensionId(((IMixinWorldInfo) properties).getDimensionId());
        // If the World represented in the properties is still loaded, save the properties and have the World reload its info
        if (optWorldServer.isPresent()) {
            final WorldServer worldServer = optWorldServer.get();
            worldServer.getSaveHandler().saveWorldInfo((WorldInfo) properties);
            worldServer.getSaveHandler().loadWorldInfo();
        } else {
            new AnvilSaveHandler(WorldManager.getCurrentSavesDirectory().get().toFile(), properties.getWorldName(), true, ((MinecraftServer)
                    Sponge.getServer()).getDataFixer()).saveWorldInfo((WorldInfo) properties);
        }
        // No return values or exceptions so can only assume true.
        return true;
    }

    public static void unloadQueuedWorlds() {
        while (unloadQueue.peek() != null) {
            final WorldServer worldServer = unloadQueue.poll();

            unloadWorld(worldServer);
        }

        unloadQueue.clear();
    }

    // TODO Result
    public static boolean unloadWorld(WorldServer worldServer) {
        checkNotNull(worldServer);
        final MinecraftServer server = SpongeImpl.getServer();

        if (worldByDimensionId.containsValue(worldServer)) {
            if (!worldServer.playerEntities.isEmpty()) {
                return false;
            }

            try {
                saveWorld(worldServer);
            } catch (MinecraftException e) {
                e.printStackTrace();
            }
            finally {
                final IMixinWorldServer mixinWorldServer = (IMixinWorldServer) worldServer;
                final int dimensionId = mixinWorldServer.getDimensionId();
                mixinWorldServer.getActiveConfig().save();
                worldByDimensionId.remove(dimensionId);
                ((IMixinMinecraftServer) server).getWorldTickTimes().remove(dimensionId);
                SpongeImpl.getLogger().info("Unloading dimension {} ({})", dimensionId, worldServer.getWorldInfo().getWorldName());
            }

            SpongeImpl.postEvent(SpongeEventFactory.createUnloadWorldEvent(Cause.of(NamedCause.source(server)), (org.spongepowered.api.world.World)
                    worldServer));

            return true;
        }
        return false;
    }

    private static void saveWorld(WorldServer worldServer) throws MinecraftException {
        final MinecraftServer server = (MinecraftServer) Sponge.getServer();
        final org.spongepowered.api.world.World apiWorld = (org.spongepowered.api.world.World) worldServer;

        Sponge.getEventManager().post(SpongeEventFactory.createSaveWorldEventPre(Cause.of(NamedCause.source(server)), apiWorld));

        worldServer.saveAllChunks(true, null);
        worldServer.flush();

        Sponge.getEventManager().post(SpongeEventFactory.createSaveWorldEventPost(Cause.of(NamedCause.source(server)), apiWorld));
    }

    public static Collection<WorldProperties> getUnloadedWorlds() throws IOException {
        final Optional<Path> optCurrentSavesDir = getCurrentSavesDirectory();
        checkState(optCurrentSavesDir.isPresent(), "Attempt made to get unloaded worlds too early!");

        final List<WorldProperties> worlds = new ArrayList<>();
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(optCurrentSavesDir.get(), LEVEL_AND_SPONGE)) {
            for (Path worldFolder : stream) {
                final String worldFolderName = worldFolder.getFileName().toString();
                final WorldInfo worldInfo = new AnvilSaveHandler(WorldManager.getCurrentSavesDirectory().get().toFile(), worldFolderName, true,
                        ((MinecraftServer) Sponge.getServer()).getDataFixer()).loadWorldInfo();
                if (worldInfo != null) {
                    worlds.add((WorldProperties) worldInfo);
                }
            }
        }
        return worlds;
    }

    public static Optional<WorldServer> loadWorld(UUID uuid) {
        checkNotNull(uuid);
        // TODO UUID check
        return Optional.empty();
    }

    public static Optional<WorldServer> loadWorld(String worldName) {
        checkNotNull(worldName);
        return loadWorld(worldName, null, null);
    }

    public static Optional<WorldServer> loadWorld(WorldProperties properties) {
        checkNotNull(properties);
        return loadWorld(properties.getWorldName(), null, properties);
    }

    private static Optional<WorldServer> loadWorld(String worldName, @Nullable ISaveHandler saveHandler, @Nullable WorldProperties properties) {
        checkNotNull(worldName);
        final Optional<Path> optCurrentSavesDir = getCurrentSavesDirectory();
        checkState(optCurrentSavesDir.isPresent(), "Attempt made to load world too early!");
        final MinecraftServer server = (MinecraftServer) Sponge.getServer();
        final Optional<WorldServer> optExistingWorldServer = getWorld(worldName);
        if (optExistingWorldServer.isPresent()) {
            return optExistingWorldServer;
        }

        if (!server.getAllowNether()) {
            SpongeImpl.getLogger().error("Unable to load world [{}]. Multi-world is disabled via [allow-nether] in [server.properties].", worldName);
            return Optional.empty();
        }

        final Path worldFolder = optCurrentSavesDir.get().resolve(worldName);
        if (!Files.isDirectory(worldFolder)) {
            SpongeImpl.getLogger().error("Unable to load world [{}]. We cannot find its folder under [{}].", optCurrentSavesDir.get(), worldFolder);
            return Optional.empty();
        }

        if (saveHandler == null) {
            saveHandler = new AnvilSaveHandler(WorldManager.getCurrentSavesDirectory().get().toFile(), worldName, true, ((MinecraftServer)
                    Sponge.getServer()).getDataFixer());
        }

        // We weren't given a properties, see if one is cached
        if (properties == null) {
            properties = worldPropertiesByFolderName.get(worldName);

            // One wasn't cached, lets load one
            if (properties == null) {
                properties = (WorldProperties) saveHandler.loadWorldInfo();

                // We tried :'(
                if (properties == null) {
                    SpongeImpl.getLogger().error("Unable to load world [{}]. No world properties was found!", worldName);
                    return Optional.empty();
                }

                if (((IMixinWorldInfo) properties).getDimensionId() == Integer.MIN_VALUE) {
                    ((IMixinWorldInfo) properties).setDimensionId(getNextFreeDimensionId());
                }

                registerWorldProperties(properties);
            }
        } else {
            if (((IMixinWorldInfo) properties).getDimensionId() == Integer.MIN_VALUE) {
                ((IMixinWorldInfo) properties).setDimensionId(getNextFreeDimensionId());
            }
        }

        setUuidOnProperties(getCurrentSavesDirectory().get(), properties);

        final WorldInfo worldInfo = (WorldInfo) properties;
        ((IMixinWorldInfo) worldInfo).createWorldConfig();

        // check if enabled
        if (!((WorldProperties) worldInfo).isEnabled()) {
            SpongeImpl.getLogger().error("Unable to load world [{}]. It is disabled.", worldName);
            return Optional.empty();
        }

        final int dimensionId = ((IMixinWorldInfo) properties).getDimensionId();
        registerDimension(dimensionId, (DimensionType) (Object) properties.getDimensionType(), worldFolder);

        SpongeImpl.getLogger().info("Loading world [{}] ({})", properties.getWorldName(), getDimensionType
                (dimensionId).get().getName());

        final WorldServer worldServer = createWorldFromProperties(dimensionId, saveHandler, (WorldInfo) properties, null,
                true);

        worldByDimensionId.put(dimensionId, worldServer);
        weakWorldByWorld.put(worldServer, worldServer);

        ((IMixinMinecraftServer) Sponge.getServer()).getWorldTickTimes().put(dimensionId, new long[100]);

        server.worldServers = reorderWorldsVanillaFirst();
        return Optional.of(worldServer);
    }

    public static void loadAllWorlds(String worldName, long defaultSeed, WorldType defaultWorldType, String generatorOptions) {
        final MinecraftServer server = (MinecraftServer) Sponge.getServer();

        // We cannot call getCurrentSavesDirectory here as that would generate a savehandler and trigger a session lock.
        // We'll go ahead and make the directories for the save name here so that the migrator won't fail
        final Path currentSavesDir = server.anvilFile.toPath().resolve(server.getFolderName());
        try {
            Files.createDirectories(currentSavesDir);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        registerDimension(0, DimensionType.OVERWORLD, currentSavesDir);
        registerDimension(-1, DimensionType.NETHER, currentSavesDir.resolve("DIM-1"));
        registerDimension(1, DimensionType.THE_END, currentSavesDir.resolve("DIM1"));

        WorldMigrator.migrateWorldsTo(currentSavesDir);

        registerExistingSpongeDimensions(currentSavesDir);

        for (Map.Entry<Integer, DimensionType> entry: sortedDimensionMap().entrySet()) {

            final int dimensionId = entry.getKey();
            final DimensionType dimensionType = entry.getValue();

            // Skip all worlds besides dimension 0 if multi-world is disabled
            if (dimensionId != 0 && !((MinecraftServer) Sponge.getServer()).getAllowNether()) {
                continue;
            }

            // Skip already loaded worlds by plugins
            if (getWorldByDimensionId(dimensionId).isPresent()) {
                continue;
            }

            // Step 1 - Grab the world's data folder

            final Optional<Path> optWorldFolder = getWorldFolder(dimensionId);
            if (!optWorldFolder.isPresent()) {
                SpongeImpl.getLogger().error("An attempt was made to load a World with dimension id [{}] that has no registered world folder!",
                        dimensionId);
                continue;
            }

            final Path worldFolder = optWorldFolder.get();
            final String worldFolderName = worldFolder.getFileName().toString();
            // Step 2 - See if we are allowed to load it

            if (dimensionId != 0) {
                final SpongeConfig<?> activeConfig = SpongeHooks.getActiveConfig(((org.spongepowered.api.world.DimensionType) (Object) dimensionType)
                        .getId(), worldFolderName);
                if (!activeConfig.getConfig().getWorld().isWorldEnabled()) {
                    SpongeImpl.getLogger().warn("World [{}] (DIM{}) is disabled. World will not be loaded...", worldFolder, dimensionId);
                    continue;
                }
            }

            // Step 3 - Get our world information from disk

            final ISaveHandler saveHandler;
            if (dimensionId == 0) {
                saveHandler = ((MinecraftServer) Sponge.getServer()).getActiveAnvilConverter().getSaveLoader(((MinecraftServer)
                        Sponge.getServer()).getFolderName(), true);
            } else {
                saveHandler = new AnvilSaveHandler(WorldManager.getCurrentSavesDirectory().get().toFile(), worldFolderName, true, ((MinecraftServer)
                        Sponge.getServer()).getDataFixer());
            }

            WorldInfo worldInfo;

            // If this is dimension 0 and we're on the client, we need to do special handling to get the world's info. This is due to the ability to
            // copy a world.
            // TODO Check this in SpongeForge...doesn't seem right
            if (dimensionId == 0 && Sponge.getPlatform().getType().isClient()) {
                worldInfo = (WorldInfo) getWorldProperties(worldFolderName).orElse(null);
                if (worldInfo == null) {
                    worldInfo = (WorldInfo) getWorldProperties(worldName).orElse(null);
                }
            } else {
                worldInfo = saveHandler.loadWorldInfo();
            }

            WorldSettings worldSettings = null;

            // Step 4 - At this point, we have either have the world's info or we have none. We'll construct it if needed and build our
            //          WorldSettings
            if (worldInfo == null) {
                worldSettings = new WorldSettings(defaultSeed, server.getGameType(), server.canStructuresSpawn(), server.isHardcore(),
                        defaultWorldType);
                worldInfo = createWorldInfoFromSettings(currentSavesDir, (org.spongepowered.api.world.DimensionType) (Object) dimensionType,
                        dimensionId, worldFolderName, worldSettings, generatorOptions);
            } else {
                // While we DO have the WorldInfo already from disk, still set the UUID in-case this is an old world.
                // Also, we need to step up one level in folder structure so that DIM 0 will have a chance to read old UUID
                setUuidOnProperties(dimensionId == 0 ? currentSavesDir.getParent() : currentSavesDir, (WorldProperties) worldInfo);
            }

            // Step 5 - Load server resource pack from dimension 0
            if (dimensionId == 0) {
                server.setResourcePackFromWorld(worldFolderName, saveHandler);
            }

            // TODO Revise this silly configuration system
            ((IMixinWorldInfo) worldInfo).createWorldConfig();

            // Step 6 - Cache the WorldProperties we've made so we don't load from disk later.
            registerWorldProperties((WorldProperties) worldInfo);

            if (dimensionId != 0 && !((WorldProperties) worldInfo).loadOnStartup()) {
                SpongeImpl.getLogger().warn("World [{}] (DIM{}) is set to not load on startup. To load it later, enable [load-on-startup] in config "
                        + "or use a plugin", worldFolder, dimensionId);
                continue;
            }

            // Step 7 - Finally, we can create the world and tell it to load
            final WorldServer worldServer = createWorldFromProperties(dimensionId, saveHandler, worldInfo, worldSettings, false);
            worldByDimensionId.put(dimensionId, worldServer);
            weakWorldByWorld.put(worldServer, worldServer);

            ((IMixinMinecraftServer) Sponge.getServer()).getWorldTickTimes().put(dimensionId, new long[100]);

            SpongeImpl.getLogger().info("Loading world [{}] ({})", ((org.spongepowered.api.world.World) worldServer).getName(), getDimensionType
                    (dimensionId).get().getName());
        }

        ((MinecraftServer) Sponge.getServer()).worldServers = reorderWorldsVanillaFirst();
    }

    public static WorldInfo createWorldInfoFromSettings(Path currentSaveRoot, org.spongepowered.api.world.DimensionType dimensionType, int
            dimensionId, String worldFolderName, WorldSettings worldSettings, String generatorOptions) {
        final MinecraftServer server = (MinecraftServer) Sponge.getServer();

        worldSettings.setGeneratorOptions(generatorOptions);

        ((IMixinWorldSettings) (Object) worldSettings).setDimensionType(dimensionType);

        final WorldInfo worldInfo = new WorldInfo(worldSettings, worldFolderName);
        setUuidOnProperties(dimensionId == 0 ? currentSaveRoot.getParent() : currentSaveRoot, (WorldProperties) worldInfo);
        ((IMixinWorldInfo) worldInfo).setDimensionId(dimensionId);
        SpongeImpl.postEvent(SpongeEventFactory.createConstructWorldPropertiesEvent(Cause.of(NamedCause.source(server)),
                (WorldCreationSettings)(Object) worldSettings, (WorldProperties) worldInfo));

        return worldInfo;

    }

    public static WorldServer createWorldFromProperties(int dimensionId, ISaveHandler saveHandler, WorldInfo worldInfo, @Nullable WorldSettings
            worldSettings, boolean prepareSpawn) {
        final MinecraftServer server = (MinecraftServer) Sponge.getServer();
        final WorldServer worldServer = (WorldServer) new WorldServer(server, saveHandler, worldInfo, dimensionId, server.theProfiler)
                .init();

        // WorldSettings is only non-null here if this is a newly generated WorldInfo and therefore we need to initialize to calculate spawn.
        if (worldSettings != null) {
            worldServer.initialize(worldSettings);
        }

        worldServer.addEventListener(new net.minecraft.world.WorldManager(server, worldServer));

        // This code changes from Mojang's to account for per-world API-set GameModes.
        if (!server.isSinglePlayer() && worldServer.getWorldInfo().getGameType().equals(WorldSettings.GameType.NOT_SET)) {
            worldServer.getWorldInfo().setGameType(server.getGameType());
        }

        SpongeImpl.postEvent(SpongeImplHooks.createLoadWorldEvent((org.spongepowered.api.world.World) worldServer));

        if (prepareSpawn) {
            ((IMixinMinecraftServer) server).prepareSpawnArea(worldServer);
        }
        return worldServer;
    }

    private static WorldServer[] reorderWorldsVanillaFirst() {
        final List<WorldServer> sorted = new ArrayList<>(worldByDimensionId.valueCollection());

        // Ensure that we'll load in Vanilla order then plugins
        WorldServer worldServer = worldByDimensionId.get(0);
        sorted.remove(worldServer);
        sorted.add(0, worldServer);

        int count = 1;
        worldServer = worldByDimensionId.get(-1);
        if (worldServer != null) {
            sorted.remove(worldServer);
            sorted.add(1, worldServer);
            count++;
        }
        worldServer = worldByDimensionId.get(1);
        if (worldServer != null) {
            sorted.remove(worldServer);
            sorted.add(2, worldServer);
            count++;
        }

        sorted.subList(count, sorted.size()).sort(WORLD_SERVER_COMPARATOR);

        //System.err.println("World order: ");
        //sorted.stream().forEach(w -> System.err.println("World id: " + w.getWorldInfo().getWorldName() + " " + ((IMixinWorld) w).getDimensionId()));

        return sorted.toArray(new WorldServer[sorted.size()]);
    }

    /**
     * Parses a {@link UUID} from disk from other known plugin platforms and sets it on the
     * {@link WorldProperties}. Currently only Bukkit is supported.
     */
    private static UUID setUuidOnProperties(Path savesRoot, WorldProperties properties) {
        checkNotNull(properties);

        UUID uuid;
        if (properties.getUniqueId() == null || properties.getUniqueId().equals
                (UUID.fromString("00000000-0000-0000-0000-000000000000"))) {
            // Check if Bukkit's uid.dat file is here and use it
            final Path uidPath = savesRoot.resolve(properties.getWorldName()).resolve("uid.dat");
            if (!Files.exists(uidPath)) {
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

        ((IMixinWorldInfo) properties).setUUID(uuid);
        return uuid;
    }

    /**
     * Handles registering existing Sponge dimensions that are not the root dimension (known as overworld).
     */
    private static void registerExistingSpongeDimensions(Path rootPath) {
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(rootPath, LEVEL_AND_SPONGE)) {
            for (Path worldPath : stream) {
                final Path spongeLevelPath = worldPath.resolve("level_sponge.dat");
                final String worldFolderName = worldPath.getFileName().toString();

                NBTTagCompound compound;
                try {
                    compound = CompressedStreamTools.readCompressed(Files.newInputStream(spongeLevelPath));
                } catch (IOException e) {
                    SpongeImpl.getLogger().error("Failed loading Sponge data for World [{}]}. Report to Sponge ASAP.", worldFolderName, e);
                    continue;
                }

                final NBTTagCompound spongeDataCompound = compound.getCompoundTag(NbtDataUtil.SPONGE_DATA);

                if (!compound.hasKey(NbtDataUtil.SPONGE_DATA)) {
                    SpongeImpl.getLogger()
                            .error("World [{}] has Sponge related data in the form of [level-sponge.dat] but the structure is not proper."
                                            + " Generally, the data is within a [{}] tag but it is not for this world. Report to Sponge ASAP.",
                                    worldFolderName, NbtDataUtil.SPONGE_DATA);
                    continue;
                }

                if (!spongeDataCompound.hasKey(NbtDataUtil.DIMENSION_ID)) {
                    SpongeImpl.getLogger().error("World [{}] has no dimension id. Report this to Sponge ASAP.", worldFolderName);
                    continue;
                }

                final int dimensionId = spongeDataCompound.getInteger(NbtDataUtil.DIMENSION_ID);

                // We do not handle Vanilla dimensions, skip them
                if (dimensionId == 0 || dimensionId == -1 || dimensionId == 1) {
                    continue;
                }

                String dimensionTypeId = "overworld";

                if (spongeDataCompound.hasKey(NbtDataUtil.DIMENSION_TYPE)) {
                    dimensionTypeId = spongeDataCompound.getString(NbtDataUtil.DIMENSION_TYPE);
                } else {
                    SpongeImpl.getLogger().warn("World [{}] (DIM{}) has no specified dimension type. Defaulting to [{}}]...", worldFolderName,
                            dimensionId, DimensionTypes.OVERWORLD.getName());
                }

                final Optional<org.spongepowered.api.world.DimensionType> optDimensionType
                        = Sponge.getRegistry().getType(org.spongepowered.api.world.DimensionType.class, dimensionTypeId);
                if (!optDimensionType.isPresent()) {
                    SpongeImpl.getLogger().warn("World [{}] (DIM{}) has specified dimension type that is not registered. Defaulting to [{}]...",
                            worldFolderName, DimensionTypes.OVERWORLD.getName());
                }
                final DimensionType dimensionType = (DimensionType) (Object) optDimensionType.get();
                spongeDataCompound.setString(NbtDataUtil.DIMENSION_TYPE, dimensionTypeId);

                if (!spongeDataCompound.hasUniqueId(NbtDataUtil.UUID)) {
                    SpongeImpl.getLogger().error("World [{}] (DIM{}) has no valid unique identifier. This is a critical error and should be reported"
                            + " to Sponge ASAP.", worldFolderName, dimensionId);
                    continue;
                }

                if (isDimensionRegistered(dimensionId)) {
                    SpongeImpl.getLogger().error("World [{}] (DIM{}) has already been registered (likely by a mod). Going to print existing "
                            + "registration", worldFolderName, dimensionId);
                    continue;
                }

                registerDimension(dimensionId, dimensionType, rootPath.resolve(worldFolderName));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static CompletableFuture<Optional<WorldProperties>> copyWorld(WorldProperties worldProperties, String copyName) {
        checkArgument(!worldPropertiesByFolderName.containsKey(worldProperties.getWorldName()), "World properties not registered!");
        checkArgument(worldPropertiesByFolderName.containsKey(copyName), "Destination world name already is registered!");
        final WorldInfo info = (WorldInfo) worldProperties;

        final WorldServer worldServer = worldByDimensionId.get(((IMixinWorldInfo) info).getDimensionId());
        if (worldServer != null) {
            try {
                saveWorld(worldServer);
            } catch (MinecraftException e) {
                Throwables.propagate(e);
            }

            ((IMixinMinecraftServer) Sponge.getServer()).setSaveEnabled(false);
        }

        final CompletableFuture<Optional<WorldProperties>> future = SpongeScheduler.getInstance().submitAsyncTask(new CopyWorldTask(info, copyName));
        if (worldServer != null) { // World was loaded
            future.thenRun(() -> ((IMixinMinecraftServer) Sponge.getServer()).setSaveEnabled(true));
        }
        return future;
    }

    public static Optional<WorldProperties> renameWorld(WorldProperties worldProperties, String newName) {
        checkNotNull(worldProperties);
        checkNotNull(newName);
        checkState(worldByDimensionId.containsKey(((IMixinWorldInfo) worldProperties).getDimensionId()), "World is still loaded!");

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

        unregisterWorldProperties(worldProperties);

        final WorldInfo info = new WorldInfo((WorldInfo) worldProperties);
        info.setWorldName(newName);
        ((IMixinWorldInfo) info).createWorldConfig();
        new AnvilSaveHandler(WorldManager.getCurrentSavesDirectory().get().toFile(), newName, true, ((MinecraftServer)
                Sponge.getServer()).getDataFixer()).saveWorldInfo(info);
        registerWorldProperties((WorldProperties) info);
        return Optional.of((WorldProperties) info);
    }

    public static CompletableFuture<Boolean> deleteWorld(WorldProperties worldProperties) {
        checkNotNull(worldProperties);
        checkArgument(worldPropertiesByWorldUuid.containsKey(worldProperties.getUniqueId()), "World properties not registered!");
        checkState(!worldByDimensionId.containsKey(((IMixinWorldInfo) worldProperties).getDimensionId()), "World not unloaded!");
        return SpongeScheduler.getInstance().submitAsyncTask(new DeleteWorldTask(worldProperties));
    }

    private static class CopyWorldTask implements Callable<Optional<WorldProperties>> {

        private final WorldInfo oldInfo;
        private final String newName;

        public CopyWorldTask(WorldInfo info, String newName) {
            this.oldInfo = info;
            this.newName = newName;
        }

        @Override
        public Optional<WorldProperties> call() throws Exception {
            Path oldWorldFolder = getCurrentSavesDirectory().get().resolve(this.oldInfo.getWorldName());
            final Path newWorldFolder = getCurrentSavesDirectory().get().resolve(this.newName);

            if (Files.exists(newWorldFolder)) {
                return Optional.empty();
            }

            FileFilter filter = null;
            if (((IMixinWorldInfo) this.oldInfo).getDimensionId() == 0) {
                oldWorldFolder = getCurrentSavesDirectory().get();
                filter = (file) -> !file.isDirectory() || !new File(file, "level.dat").exists();
            }

            FileUtils.copyDirectory(oldWorldFolder.toFile(), newWorldFolder.toFile(), filter);

            final WorldInfo info = new WorldInfo(this.oldInfo);
            info.setWorldName(this.newName);
            ((IMixinWorldInfo) info).setDimensionId(getNextFreeDimensionId());
            ((IMixinWorldInfo) info).setUUID(UUID.randomUUID());
            ((IMixinWorldInfo) info).createWorldConfig();
            registerWorldProperties((WorldProperties) info);
            new AnvilSaveHandler(WorldManager.getCurrentSavesDirectory().get().toFile(), newName, true, ((MinecraftServer)
                    Sponge.getServer()).getDataFixer()).saveWorldInfo(info);
            return Optional.of((WorldProperties) info);
        }
    }

    private static class DeleteWorldTask implements Callable<Boolean> {

        private final WorldProperties props;

        public DeleteWorldTask(WorldProperties props) {
            this.props = props;
        }

        @Override
        public Boolean call() throws Exception {
            final Path worldFolder = getCurrentSavesDirectory().get().resolve(props.getWorldName());
            if (!Files.exists(worldFolder)) {
                unregisterWorldProperties(this.props);
                return true;
            }

            try {
                FileUtils.deleteDirectory(worldFolder.toFile());
                unregisterWorldProperties(this.props);
                return true;
            } catch (IOException e) {
                return false;
            }
        }

    }

    public static DimensionType getClientDimensionType(DimensionType serverDimensionType) {
        switch (serverDimensionType) {
            case OVERWORLD:
            case NETHER:
            case THE_END:
                return serverDimensionType;
            default:
                return DimensionType.OVERWORLD;
        }
    }

    public static void sendDimensionRegistration(EntityPlayerMP playerMP, DimensionType dimensionType) {
        // Do nothing in Common
    }

    public static void loadDimensionDataMap(@Nullable NBTTagCompound compound) {
        dimensionBits.clear();
        if (compound == null) {
            for (int dimensionId : dimensionTypeByDimensionId.keys()) {
                if (dimensionId >= 0) {
                    dimensionBits.set(dimensionId);
                }
            }
        } else {
            final int[] intArray = compound.getIntArray("DimensionArray");
            for (int i = 0; i < intArray.length; i++) {
                for (int j = 0; j < Integer.SIZE; j++) {
                    dimensionBits.set(i * Integer.SIZE + j, (intArray[i] & (1 << j)) != 0);
                }
            }
        }
    }

    public static NBTTagCompound saveDimensionDataMap() {
        int[] data = new int[(dimensionBits.length() + Integer.SIZE - 1 )/ Integer.SIZE];
        NBTTagCompound dimMap = new NBTTagCompound();
        for (int i = 0; i < data.length; i++) {
            int val = 0;
            for (int j = 0; j < Integer.SIZE; j++) {
                val |= dimensionBits.get(i * Integer.SIZE + j) ? (1 << j) : 0;
            }
            data[i] = val;
        }
        dimMap.setIntArray("DimensionArray", data);
        return dimMap;
    }

    public enum RegisterDimensionTypeResult {
        DIMENSION_TYPE_ALREADY_REGISTERED,
        DIMENSION_TYPE_LIMIT_EXCEEDED,
        DIMENSION_TYPE_REGISTERED
    }

    public enum RegisterDimensionResult {
        DIMENSION_TYPE_IS_NOT_REGISTERED,
        DIMENSION_ALREADY_REGISTERED,
        DIMENSION_REGISTERED
    }

    public enum QueueWorldToUnloadResult {
        WORLD_IS_NOT_REGISTERED,
        WORLD_STILL_HAS_PLAYERS,
        WORLD_KEEPS_SPAWN_LOADED,
        WORLD_IS_QUEUED
    }

    public static Optional<Path> getCurrentSavesDirectory() {
        final Optional<WorldServer> optWorldServer = getWorldByDimensionId(0);

        if (optWorldServer.isPresent()) {
            return Optional.of(optWorldServer.get().getSaveHandler().getWorldDirectory().toPath());
        }

        return Optional.empty();
    }
}
