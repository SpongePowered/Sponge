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

import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.MapIdTracker;
import org.spongepowered.api.map.MapInfo;
import org.spongepowered.api.map.MapStorage;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.map.MapIdTrackerBridge;
import org.spongepowered.common.bridge.world.storage.MapDataBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.Constants;

import javax.annotation.Nullable;
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

	private final Map<UUID, MapInfo> uuidCache = new HashMap<>();
	private final Map<UUID, Integer> uuidMapIndex = new HashMap<>();

	@Override
	public Collection<MapInfo> getAllMapInfos() {
		final Set<MapInfo> mapInfos = new HashSet<>();

		final ServerWorld defaultWorld = SpongeCommon.getServer().getWorld(DimensionType.OVERWORLD);
		final int highestId = ((MapIdTrackerBridge)defaultWorld.getSavedData().getOrCreate(MapIdTracker::new, "idcounts")).bridge$getHighestMapId()
				.orElse(-1);
		for (int i = 0; i <= highestId; i++) {
			final @Nullable MapInfo mapInfo = (MapInfo) defaultWorld.getMapData(Constants.Map.MAP_PREFIX + i);
			if (mapInfo == null) {
				SpongeCommon.getLogger().warn("Missing map with id: " + i);
				continue;
			}
			final UUID mapUUID = mapInfo.getUniqueId();
			this.addMapInfo(mapInfo);
			mapInfos.add(mapInfo);
		}
		return mapInfos;
	}

	@Override
	public Optional<MapInfo> getMapInfo(final UUID uuid) {
		// TODO: Have methods for getting cached and getting all (getting all could involve opening a *lot* of files)
		// 	maybe add a index file for uuids?
		final MapInfo cachedInfo = this.uuidCache.get(uuid);

		if (cachedInfo != null) {
			return Optional.of(cachedInfo);
		}

		final ServerWorld defaultWorld = SpongeCommon.getServer().getWorld(DimensionType.OVERWORLD);
		final int highestId = ((MapIdTrackerBridge)defaultWorld.getSavedData().getOrCreate(MapIdTracker::new, "idcounts")).bridge$getHighestMapId()
				.orElse(-1);
		for (int i = 0; i <= highestId; i++) {
			final @Nullable MapInfo mapInfo = (MapInfo) defaultWorld.getMapData(Constants.Map.MAP_PREFIX + i);
			if (mapInfo == null) {
				SpongeCommon.getLogger().warn("Missing map with id: " + i);
				continue;
			}
			final UUID mapUUID = mapInfo.getUniqueId();
			this.uuidCache.put(mapUUID, mapInfo);

			if (mapUUID.equals(uuid)) {
				return Optional.of(mapInfo);
			}
		}

		return Optional.empty();
	}

	@Override
	public Optional<MapInfo> createNewMapInfo() {
		return SpongeCommonEventFactory.fireCreateMapEvent(PhaseTracker.getCauseStackManager().getCurrentCause());
	}

	public void addMapInfo(final MapInfo mapInfo) {
		this.uuidCache.put(mapInfo.getUniqueId(), mapInfo);
		this.uuidMapIndex.put(mapInfo.getUniqueId(), ((MapDataBridge)mapInfo).bridge$getMapId());
	}

	public Map<UUID, Integer> getUuidMapIndex() {
		return this.uuidMapIndex;
	}
}
