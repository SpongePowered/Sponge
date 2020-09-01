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
package org.spongepowered.common.mixin.api.mcp.world.storage;

import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import org.spongepowered.api.map.MapInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.manipulator.mutable.SpongeMapInfoData;
import org.spongepowered.common.map.MapUtil;
import org.spongepowered.common.util.Constants;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Mixin(MapStorage.class)
public abstract class MapStorageMixin_API implements org.spongepowered.api.world.map.MapStorage {

    @Final
    @Shadow
    private Map<String, Short> idCounts;

    @Shadow
    public abstract WorldSavedData getOrLoadData(Class<? extends WorldSavedData> clazz, String dataIdentifier);

    private Map<UUID, MapInfo> uuidCache = new HashMap<>();

    /**
     * Fully load the cache.
     * Bear in mind minecraft has its own cache so repeating this isn't too bad.
     * (getOrLoad only loads the file when needed)
     */
    private void api$fullyLoadCache() {
        Number highestId = idCounts.get(Constants.Map.ID_COUNTS_KEY); // Prefer intValue() as it means max map ids can be changed to int/long without breaking this
        if (highestId == null) {
            return;
        }
        Stream.iterate(0, i -> i + 1)
                .limit(highestId.intValue() + 1) // limit is < but we want <=
                .map(id -> Constants.Map.MAP_PREFIX + id)
                .map(s -> getOrLoadData(MapData.class, s))
                .filter(Objects::nonNull) // if we have missing map between 0 and highest map .getOrLoadData() returns null so filter out
                .map(worldSavedData -> (MapInfo)worldSavedData)
                .forEach(info -> uuidCache.put(info.getUniqueId(), info)); // Does not allow for map deletion!
    }

    @Override
    public Collection<MapInfo> getAllMapInfos() {
        this.api$fullyLoadCache();
        return this.uuidCache.values();
    }

    @Override
    public Optional<MapInfo> getMapInfo(UUID uuid) {
        Optional<MapInfo> mapInfo = Optional.ofNullable(this.uuidCache.get(uuid));
        if (!mapInfo.isPresent()) {
            this.api$fullyLoadCache();
            mapInfo = Optional.ofNullable(this.uuidCache.get(uuid));
        }
        return mapInfo;
    }

    @Override
    public Optional<MapInfo> createNewMapInfo() {
        return MapUtil.fireCreateMapEvent(new SpongeMapInfoData(), SpongeImpl.getCauseStackManager().getCurrentCause());
    }
}
