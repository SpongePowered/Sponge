package org.spongepowered.common.world;

import com.google.common.collect.MapMaker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.IMixinMinecraftServer;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import javax.annotation.Nullable;

public class DimensionManager {

    private static final Map<Integer, DimensionType> DIMENSION_TYPE_BY_TYPE_ID = new HashMap<>();
    private static final Map<Integer, DimensionType> DIMENSION_TYPE_BY_DIMENSION_ID = new HashMap<>();
    private static final Map<Integer, WorldServer> DIMENSION_ID_BY_WORLD = new HashMap<>();
    private static final BitSet DIMENSION_MAP = new BitSet(Long.SIZE << 4);
    private static final Map<World, World> WEAK_WORLD_BY_WORLD = new MapMaker().weakKeys().weakValues().makeMap();
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
        final Optional<Integer> optNextDimensionTypeId = getNextFreeDimensionTypeId();
        if (!optNextDimensionTypeId.isPresent()) {
            return RegisterDimensionTypeResult.DIMENSION_TYPE_LIMIT_EXCEEDED;
        }

        return registerDimensionType(optNextDimensionTypeId.get(), type);
    }

    public static RegisterDimensionTypeResult registerDimensionType(int dimensionTypeId, DimensionType type) {
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
        if (!DIMENSION_TYPE_BY_TYPE_ID.containsValue(type)) {
            return RegisterDimensionResult.DIMENSION_TYPE_IS_NOT_REGISTERED;
        }

        if (DIMENSION_TYPE_BY_DIMENSION_ID.containsKey(dimensionId)) {
            return RegisterDimensionResult.DIMENSION_ALREADY_REGISTERED;
        }
        DIMENSION_TYPE_BY_DIMENSION_ID.put(dimensionId, type);
        if (dimensionId >= 0) {
            DIMENSION_MAP.set(dimensionId);
        }
        return RegisterDimensionResult.DIMENSION_REGISTERED;
    }

    public static Optional<DimensionType> getDimensionType(int dimensionId) {
        return Optional.ofNullable(DIMENSION_TYPE_BY_DIMENSION_ID.get(dimensionId));
    }

    public static Optional<WorldProvider> createProviderForType(DimensionType type) {
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

    public static void loadAllWorlds(Path rootFolder) {
        for (Map.Entry<Integer, DimensionType> entry : getRegisteredDimensions()) {

        }
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
