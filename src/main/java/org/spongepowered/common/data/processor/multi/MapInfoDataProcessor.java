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
package org.spongepowered.common.data.processor.multi;

import com.flowpowered.math.vector.Vector2i;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.world.storage.MapData;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableMapInfoData;
import org.spongepowered.api.data.manipulator.mutable.MapInfoData;
import org.spongepowered.api.data.value.mutable.SetValue;
import org.spongepowered.api.map.MapInfo;
import org.spongepowered.api.map.decoration.MapDecoration;
import org.spongepowered.api.world.World;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.bridge.world.storage.MapDataBridge;
import org.spongepowered.common.data.manipulator.mutable.SpongeMapInfoData;
import org.spongepowered.common.data.processor.common.AbstractMultiDataSingleTargetProcessor;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.map.canvas.SpongeMapByteCanvas;
import org.spongepowered.common.world.WorldManager;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class MapInfoDataProcessor extends AbstractMultiDataSingleTargetProcessor<MapInfo, MapInfoData, ImmutableMapInfoData> {


    public MapInfoDataProcessor() {
        super(MapInfo.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean set(MapInfo mapInfo, Map<Key<?>, Object> keyValues) {
        MapData mapData = (MapData)mapInfo;
        MapDataBridge spongeMapInfo = (MapDataBridge) mapInfo;

        Vector2i location = (Vector2i)keyValues.get(Keys.MAP_LOCATION);
        mapData.xCenter = location.getX();
        mapData.zCenter = location.getY();
        int dimensionId = ((WorldServerBridge)keyValues.get(Keys.MAP_WORLD)).bridge$getDimensionId();
        MapDataBridge mapDataBridge = (MapDataBridge)mapData;
        mapDataBridge.bridge$setDimensionId(dimensionId);
        mapData.trackingPosition = (boolean)keyValues.get(Keys.MAP_TRACKS_PLAYERS);
        mapData.unlimitedTracking = (boolean)keyValues.get(Keys.MAP_UNLIMITED_TRACKING);
        mapData.scale = ((Integer)keyValues.get(Keys.MAP_SCALE)).byteValue();
        spongeMapInfo.bridge$setLocked((boolean)keyValues.get(Keys.MAP_LOCKED));
        spongeMapInfo.bridge$setDecorations((Set<MapDecoration>) keyValues.get(Keys.MAP_DECORATIONS));
        mapData.markDirty();
        return true;
    }

    @Override
    public Map<Key<?>, ?> getValues(MapInfo mapInfo) {
        Map<Key<?>, Object> values = Maps.newIdentityHashMap();

        MapData mapData = (MapData)mapInfo;
        MapDataBridge spongeMapInfo = (MapDataBridge) mapInfo;

        values.put(Keys.MAP_LOCATION, new Vector2i(mapData.xCenter, mapData.zCenter));
        values.put(Keys.MAP_WORLD, (World)WorldManager.getWorldByDimensionId(((MapDataBridge) mapData).bridge$getDimensionId()).get());
        values.put(Keys.MAP_TRACKS_PLAYERS, mapData.trackingPosition);
        values.put(Keys.MAP_UNLIMITED_TRACKING, mapData.unlimitedTracking);
        values.put(Keys.MAP_SCALE, (int)mapData.scale);
        values.put(Keys.MAP_CANVAS, new SpongeMapByteCanvas(mapData.colors));
        values.put(Keys.MAP_LOCKED, spongeMapInfo.bridge$isLocked());
        values.put(Keys.MAP_DECORATIONS, spongeMapInfo.bridge$getDecorations());

        return values;
    }

    @Override
    public MapInfoData createManipulator() {
        return new SpongeMapInfoData();
    }

    @Override
    public Optional<MapInfoData> fill(DataContainer container, MapInfoData data) {
        if (!container.contains(
                Keys.MAP_LOCATION.getQuery(),
                Keys.MAP_WORLD.getQuery(),
                Keys.MAP_TRACKS_PLAYERS.getQuery(),
                Keys.MAP_UNLIMITED_TRACKING.getQuery(),
                Keys.MAP_SCALE.getQuery(),
                Keys.MAP_CANVAS.getQuery(),
                Keys.MAP_LOCKED.getQuery(),
                Keys.MAP_DECORATIONS.getQuery()
        )) {
            return Optional.empty();
        }

        data.set(Keys.MAP_LOCATION, DataUtil.getData(container, Keys.MAP_LOCATION));
        data.set(Keys.MAP_WORLD, DataUtil.getData(container, Keys.MAP_WORLD));
        data.set(Keys.MAP_TRACKS_PLAYERS, DataUtil.getData(container, Keys.MAP_TRACKS_PLAYERS));
        data.set(Keys.MAP_UNLIMITED_TRACKING, DataUtil.getData(container, Keys.MAP_UNLIMITED_TRACKING));
        data.set(Keys.MAP_SCALE, DataUtil.getData(container, Keys.MAP_SCALE));
        data.set(Keys.MAP_CANVAS, DataUtil.getData(container, Keys.MAP_CANVAS));
        data.set(Keys.MAP_LOCKED, DataUtil.getData(container, Keys.MAP_LOCKED));
        // We need to convert from List to Set here.
        data.set(Keys.MAP_DECORATIONS, new HashSet<>(DataUtil.getData(container, Keys.MAP_DECORATIONS)));

        return Optional.of(data);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public boolean doesDataExist(MapInfo mapInfo) {
        return true;
    }
}
