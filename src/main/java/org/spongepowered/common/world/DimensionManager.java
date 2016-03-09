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

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldManager;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.interfaces.IMixinMinecraftServer;
import org.spongepowered.common.interfaces.world.IMixinWorldInfo;
import org.spongepowered.common.interfaces.world.IMixinWorldSettings;
import org.spongepowered.common.util.SpongeHooks;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

import javax.annotation.Nullable;

public class DimensionManager {

    private static final Map<Integer, DimensionType> DIMENSION_TYPE_BY_TYPE_ID = new HashMap<>();
    private static final Map<Integer, DimensionType> DIMENSION_TYPE_BY_DIMENSION_ID = new HashMap<>();
    private static final Map<Integer, Path> DIMENSION_PATH_BY_DIMENSION_ID = new HashMap<>();
    private static final Map<Integer, WorldServer> DIMENSION_ID_BY_WORLD = new HashMap<>();
    private static final Map<String, WorldProperties> WORLD_PROPERTIES_BY_FOLDER_NAME = new HashMap<>();
    private static final Map<UUID, WorldProperties> WORLD_PROPERTIES_BY_WORLD_UUID = new HashMap<>();
    private static final BitSet DIMENSION_MAP = new BitSet(Long.SIZE << 4);
    private static final Map<World, World> WEAK_WORLD_BY_WORLD = new WeakHashMap<>(3);
    private static final Queue<Integer> UNLOAD_QUEUE = new ArrayDeque<>();

    static {
        registerDimensionType(0, DimensionType.OVERWORLD);
        registerDimensionType(-1, DimensionType.NETHER);
        registerDimensionType(1, DimensionType.THE_END);

        registerDimension(0, DimensionType.OVERWORLD);
        registerDimension(-1, DimensionType.NETHER);
        registerDimension(1, DimensionType.THE_END);
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
        if (DIMENSION_TYPE_BY_TYPE_ID.containsKey(dimensionTypeId)) {
            return RegisterDimensionTypeResult.DIMENSION_TYPE_ALREADY_REGISTERED;
        }

        DIMENSION_TYPE_BY_TYPE_ID.put(dimensionTypeId, type);
        return RegisterDimensionTypeResult.DIMENSION_TYPE_REGISTERED;
    }

    private static Optional<Integer> getNextFreeDimensionTypeId() {
        Integer highestDimensionTypeId = null;

        for (Integer dimensionTypeId : DIMENSION_TYPE_BY_TYPE_ID.keySet()) {
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
        return DIMENSION_MAP.nextClearBit(0);
    }

    public static RegisterDimensionResult registerDimension(int dimensionId, DimensionType type) {
        checkNotNull(type);
        Path dimensionDataRoot;
        if (dimensionId == 0) {
            dimensionDataRoot = SpongeImpl.getGame().getSavesDirectory();
        } else {
            dimensionDataRoot = SpongeImpl.getGame().getSavesDirectory().resolve("DIM" + dimensionId);
        }
        return registerDimension(dimensionId, type, dimensionDataRoot);
    }

    public static RegisterDimensionResult registerDimension(int dimensionId, DimensionType type, Path dimensionDataRoot) {
        checkNotNull(type);
        checkNotNull(dimensionDataRoot);
        if (!DIMENSION_TYPE_BY_TYPE_ID.containsValue(type)) {
            return RegisterDimensionResult.DIMENSION_TYPE_IS_NOT_REGISTERED;
        }

        if (DIMENSION_TYPE_BY_DIMENSION_ID.containsKey(dimensionId)) {
            return RegisterDimensionResult.DIMENSION_ALREADY_REGISTERED;
        }
        DIMENSION_TYPE_BY_DIMENSION_ID.put(dimensionId, type);
        DIMENSION_PATH_BY_DIMENSION_ID.put(dimensionId, dimensionDataRoot);
        if (dimensionId >= 0) {
            DIMENSION_MAP.set(dimensionId);
        }
        return RegisterDimensionResult.DIMENSION_REGISTERED;
    }

    public static Optional<DimensionType> getDimensionType(int dimensionId) {
        return Optional.ofNullable(DIMENSION_TYPE_BY_DIMENSION_ID.get(dimensionId));
    }

    public static Optional<DimensionType> getDimensionType(Class<? extends WorldProvider> providerClass) {
        checkNotNull(providerClass);
        for (DimensionType dimensionType : DIMENSION_TYPE_BY_TYPE_ID.values()) {
            if (((org.spongepowered.api.world.DimensionType) (Object) dimensionType).getDimensionClass().equals(providerClass)) {
                return Optional.of(dimensionType);
            }
        }

        return Optional.empty();
    }

    public static Optional<Path> getWorldFolder(int dimensionId) {
        return Optional.ofNullable(DIMENSION_PATH_BY_DIMENSION_ID.get(dimensionId));
    }

    public static Optional<WorldProvider> createProviderForType(DimensionType type) {
        checkNotNull(type);
        if (!DIMENSION_TYPE_BY_TYPE_ID.containsValue(type)) {
            return Optional.empty();
        }

        Optional<WorldProvider> optWorldProvider;
        try {
            optWorldProvider = Optional.of(type.createDimension());
        } catch (Exception ex) {
            optWorldProvider = Optional.empty();
        }

        return optWorldProvider;
    }

    public static boolean isDimensionRegistered(int dimensionId) {
        return DIMENSION_TYPE_BY_DIMENSION_ID.containsKey(dimensionId);
    }

    public static Set<Map.Entry<Integer, DimensionType>> getRegisteredDimensions() {
        return DIMENSION_TYPE_BY_DIMENSION_ID.entrySet();
    }

    public static Set<Map.Entry<Integer, WorldServer>> getLoadedWorlds() {
        return DIMENSION_ID_BY_WORLD.entrySet();
    }

    public static Optional<WorldServer> getWorldByDimensionId(int dimensionId) {
        return Optional.ofNullable(DIMENSION_ID_BY_WORLD.get(dimensionId));
    }

    public static QueueWorldToUnloadResult queueWorldToUnload(int dimensionId) {
        final World world = DIMENSION_ID_BY_WORLD.get(dimensionId);
        if (world == null) {
            return QueueWorldToUnloadResult.WORLD_IS_NOT_REGISTERED;
        }

        if (!world.playerEntities.isEmpty()) {
            return QueueWorldToUnloadResult.WORLD_STILL_HAS_PLAYERS;
        }

        if (((org.spongepowered.api.world.World) world).doesKeepSpawnLoaded()) {
            return QueueWorldToUnloadResult.WORLD_KEEPS_SPAWN_LOADED;
        }

        UNLOAD_QUEUE.add(dimensionId);
        return QueueWorldToUnloadResult.WORLD_IS_QUEUED;
    }

    public static void registerWorldProperties(WorldProperties properties) {
        checkNotNull(properties);
        WORLD_PROPERTIES_BY_FOLDER_NAME.put(properties.getWorldName(), properties);
        WORLD_PROPERTIES_BY_WORLD_UUID.put(properties.getUniqueId(), properties);
    }

    public static void unregisterWorldProperties(WorldProperties properties) {
        checkNotNull(properties);
        WORLD_PROPERTIES_BY_FOLDER_NAME.remove(properties.getWorldName());
        WORLD_PROPERTIES_BY_WORLD_UUID.remove(properties.getUniqueId());
    }

    public static Optional<WorldProperties> getWorldProperties(String folderName) {
        checkNotNull(folderName);
        return Optional.ofNullable(WORLD_PROPERTIES_BY_FOLDER_NAME.get(folderName));
    }

    public static Collection<WorldProperties> getAllWorldProperties() {
        return Collections.unmodifiableCollection(WORLD_PROPERTIES_BY_FOLDER_NAME.values());
    }

    public static Optional<WorldProperties> getWorldProperties(UUID uuid) {
        checkNotNull(uuid);
        return Optional.ofNullable(WORLD_PROPERTIES_BY_WORLD_UUID.get(uuid));
    }

    public static void loadAllWorlds(String saveName, long defaultSeed, WorldType defaultWorldType, String generatorOptions) {
        final MinecraftServer server = (MinecraftServer) Sponge.getServer();

        for (Map.Entry<Integer, DimensionType> entry : getRegisteredDimensions()) {
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
                SpongeImpl.getLogger().error("An attempt was made to load a World with id [{}] that has no registered world folder!", dimensionId);
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

            WorldInfo worldInfo;
            ISaveHandler saveHandler = ((IMixinMinecraftServer) Sponge.getServer()).getHandler(worldFolderName);

            // If this is dimension 0 and we're on the client, we need to do special handling to get the world's info. This is due to the ability to
            // copy a world.
            if (dimensionId == 0 && Sponge.getPlatform().getType().isClient()) {
                worldInfo = (WorldInfo) getWorldProperties(worldFolderName).orElse(null);
                if (worldInfo == null) {
                    worldInfo = (WorldInfo) getWorldProperties(saveName).orElse(null);
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

                ((IMixinWorldSettings) (Object) worldSettings).setDimensionId(dimensionId);
                ((IMixinWorldSettings) (Object) worldSettings).setDimensionType((org.spongepowered.api.world.DimensionType) (Object) dimensionType);

                // Bad MCP mapping name. Actually is generatorOptions
                worldSettings.setWorldName(generatorOptions);

                worldInfo = new WorldInfo(worldSettings, worldFolderName);
                setUuidOnProperties((WorldProperties) worldInfo);

                SpongeImpl.postEvent(SpongeEventFactory.createConstructWorldPropertiesEvent(Cause.of(NamedCause.source(server)),
                        (WorldCreationSettings)(Object) worldSettings, (WorldProperties) worldInfo));
            } else {
                // While we DO have the WorldInfo already from disk, still set the UUID in-case this is an old world.
                setUuidOnProperties((WorldProperties) worldInfo);
            }

            // Step 5 - Load server resource pack from dimension 0
            if (dimensionId == 0 && saveHandler != null) {
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
            final WorldServer worldServer = (WorldServer) new WorldServer(server, saveHandler, worldInfo, dimensionId, server.theProfiler)
                    .init();

            // WorldSettings is only non-null here if this is a newly generated WorldInfo and therefore we need to initialize to calculate spawn.
            if (worldSettings != null) {
                worldServer.initialize(worldSettings);
            }

            worldServer.addEventListener(new WorldManager(server, worldServer));

            // This code changes from Mojang's to account for per-world API-set GameModes.
            if (!server.isSinglePlayer() && worldServer.getWorldInfo().getGameType().equals(WorldSettings.GameType.NOT_SET)) {
                worldServer.getWorldInfo().setGameType(server.getGameType());
            }

            SpongeImpl.postEvent(SpongeImplHooks.createLoadWorldEvent((org.spongepowered.api.world.World) worldServer));

            DIMENSION_ID_BY_WORLD.put(dimensionId, worldServer);
            WEAK_WORLD_BY_WORLD.put(worldServer, worldServer);

            ((IMixinMinecraftServer) Sponge.getServer()).getWorldTickTimes().put(dimensionId, new long[100]);

            SpongeImpl.getLogger().info("Loading world {} ({})", ((org.spongepowered.api.world.World) worldServer).getName(), getDimensionType
                    (dimensionId).get().getName());
        }

        ((MinecraftServer) Sponge.getServer()).worldServers = reorderWorldsVanillaFirst();
    }

    public static Optional<WorldServer> createWorld(int dimensionId ) {
        final Optional<WorldServer> optRootWorld = getWorldByDimensionId(0);
        if (dimensionId != 0 && !optRootWorld.isPresent()) {
            return Optional.empty();
        }

        final Optional<DimensionType> optDimensionType = getDimensionType(dimensionId);
        if (!optDimensionType.isPresent()) {
            return Optional.empty();
        }

        final Optional<WorldProvider> optWorldProvider = createProviderForType(optDimensionType.get());
        if (!optWorldProvider.isPresent()) {
            return Optional.empty();
        }

        final MinecraftServer server = (MinecraftServer) Sponge.getServer();

        // TODO SaveHandler
        // TODO WorldInfo
        final WorldServer worldServer = new WorldServer((MinecraftServer) Sponge.getServer(), null, null, optDimensionType.get().getId(),
                server.theProfiler);

        return Optional.of(worldServer);
    }

    public static void setWorld(int dimensionId, @Nullable WorldServer worldServer) {
        if (worldServer == null) {
            final WorldServer removed = DIMENSION_ID_BY_WORLD.remove(dimensionId);
            if (removed != null) {
                ((IMixinMinecraftServer) Sponge.getServer()).getWorldTickTimes().remove(dimensionId);
                SpongeImpl.getLogger().info("Unloading world {} ({})", ((org.spongepowered.api.world.World) removed).getName(), getDimensionType
                        (dimensionId).get().getName());
            }
        } else {
            DIMENSION_ID_BY_WORLD.put(dimensionId, worldServer);
            WEAK_WORLD_BY_WORLD.put(worldServer, worldServer);
            ((IMixinMinecraftServer) Sponge.getServer()).getWorldTickTimes().put(dimensionId, new long[100]);
            SpongeImpl.getLogger().info("Loading world {} ({})", ((org.spongepowered.api.world.World) worldServer).getName(), getDimensionType
                    (dimensionId).get().getName());
        }

        ((MinecraftServer) Sponge.getServer()).worldServers = reorderWorldsVanillaFirst();
    }

    private static WorldServer[] reorderWorldsVanillaFirst() {
        final List<WorldServer> tmp = new ArrayList<>();

        if (DIMENSION_ID_BY_WORLD.get(0) != null) {
            tmp.add(DIMENSION_ID_BY_WORLD.get(0));
        }
        if (DIMENSION_ID_BY_WORLD.get(-1) != null) {
            tmp.add(DIMENSION_ID_BY_WORLD.get(-1));
        }
        if (DIMENSION_ID_BY_WORLD.get(1) != null) {
            tmp.add(DIMENSION_ID_BY_WORLD.get(1));
        }

        for (Map.Entry<Integer, WorldServer> entry : DIMENSION_ID_BY_WORLD.entrySet()) {
            int dim = entry.getKey();
            if (dim >= -1 && dim <= 1) {
                continue;
            }
            tmp.add(entry.getValue());
        }

        return tmp.toArray(new WorldServer[tmp.size()]);
    }

    /**
     * Parses a {@link UUID} from disk from other known plugin platforms and sets it on the
     * {@link WorldProperties}. Currently only Bukkit is supported.
     */
    private static UUID setUuidOnProperties(WorldProperties properties) {
        checkNotNull(properties);

        UUID uuid;
        if (properties.getUniqueId() == null || properties.getUniqueId().equals
                (UUID.fromString("00000000-0000-0000-0000-000000000000"))) {
            // Check if Bukkit's uid.dat file is here and use it
            final Path uidPath = SpongeImpl.getGame().getSavesDirectory().resolve(properties.getWorldName()).resolve("uid.dat");
            if (!Files.exists(uidPath)) {
                uuid = UUID.randomUUID();
            } else {
                DataInputStream dis;

                try {
                    dis = new DataInputStream(Files.newInputStream(uidPath));
                    uuid = new UUID(dis.readLong(), dis.readLong());
                } catch (IOException e) {
                    SpongeImpl.getLogger().error("World folder [{}] has an existing Bukkit unique identifier for it but we encountered issues "
                                    + "parsing the file. We will have to use a new unique id. Please report this to Sponge ASAP.",
                            properties.getWorldName(), e);
                    uuid = UUID.randomUUID();
                }
            }
        } else {
            uuid = properties.getUniqueId();
        }

        ((IMixinWorldInfo) properties).setUUID(uuid);
        return uuid;
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
}
