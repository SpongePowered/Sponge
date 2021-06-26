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
package org.spongepowered.common.map;

import com.google.common.collect.BiMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.maps.MapIndex;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.map.MapInfo;
import org.spongepowered.api.map.MapStorage;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.map.MapIdTrackerBridge;
import org.spongepowered.common.bridge.world.level.storage.PrimaryLevelDataBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.Constants;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Although minecraft no longer has a "Map Storage", it makes sense
 * as a concept to provide the plugin creators with a single place
 * to handle maps, instead of putting the methods at overworld,
 * or at server level.
 */
public final class SpongeMapStorage implements MapStorage {

    private final Map<UUID, MapInfo> loadedMapUUIDs = new HashMap<>();
    private @Nullable BiMap<Integer, UUID> mapIdUUIDIndex = null;

    @Override
    public Collection<MapInfo> allMapInfos() {
        final Set<MapInfo> mapInfos = new HashSet<>();
        final ServerLevel defaultWorld = (ServerLevel) Sponge.server().worldManager().defaultWorld();

        final int highestId = ((MapIdTrackerBridge) defaultWorld.getDataStorage()
                .computeIfAbsent(MapIndex::new, Constants.Map.MAP_INDEX_DATA_NAME)).bridge$getHighestMapId().orElse(-1);
        for (int i = 0; i <= highestId; i++) {
            final @Nullable MapInfo mapInfo = (MapInfo) defaultWorld.getMapData(Constants.Map.MAP_PREFIX + i);
            if (mapInfo == null) {
                SpongeCommon.logger().warn("Missing map with id: " + i);
                continue;
            }
            this.addMapInfo(mapInfo);
            mapInfos.add(mapInfo);
        }
        return mapInfos;
    }

    @Override
    public Optional<MapInfo> mapInfo(final UUID uuid) {
        this.ensureHasMapUUIDIndex();
        final MapInfo mapInfo = this.loadedMapUUIDs.get(uuid);
        if (mapInfo != null) {
            return Optional.of(mapInfo);
        }
        final Integer mapId = this.mapIdUUIDIndex.inverse().get(uuid);
        if (mapId == null) {
            return Optional.empty();
        }
        final ServerLevel defaultWorld = (ServerLevel) Sponge.server().worldManager().defaultWorld();
        final MapInfo loadedMapInfo = (MapInfo) defaultWorld.getMapData(Constants.Map.MAP_PREFIX + mapId);
        return Optional.ofNullable(loadedMapInfo);
    }

    @Override
    public Optional<MapInfo> createNewMapInfo() {
        return SpongeCommonEventFactory.fireCreateMapEvent(PhaseTracker.getCauseStackManager().currentCause());
    }

    /**
     * Request the UUID for the given map id.
     * If there is no known UUID, one is created and saved.
     *
     * @param id Map id
     * @return UUID of the map.
     */
    public UUID requestUUID(final int id) {
        this.ensureHasMapUUIDIndex();
        UUID uuid = this.mapIdUUIDIndex.get(id);
        if (uuid == null) {
            uuid = UUID.randomUUID();
            this.mapIdUUIDIndex.put(id, uuid);
        }
        return uuid;
    }

    public void addMapInfo(final MapInfo mapInfo) {
        this.loadedMapUUIDs.put(mapInfo.uniqueId(), mapInfo);
    }

    private void ensureHasMapUUIDIndex() {
        if (this.mapIdUUIDIndex == null) {
            this.mapIdUUIDIndex = ((PrimaryLevelDataBridge) Sponge.server().worldManager().defaultWorld().properties()).bridge$getMapUUIDIndex();
        }
    }
}
