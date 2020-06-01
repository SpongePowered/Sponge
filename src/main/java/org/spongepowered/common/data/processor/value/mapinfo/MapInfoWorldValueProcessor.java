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
package org.spongepowered.common.data.processor.value.mapinfo;

import net.minecraft.world.storage.MapData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableMapInfoData;
import org.spongepowered.api.data.manipulator.mutable.MapInfoData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.map.MapInfo;
import org.spongepowered.api.world.World;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.bridge.world.storage.MapDataBridge;
import org.spongepowered.common.data.manipulator.mutable.SpongeMapInfoData;
import org.spongepowered.common.data.processor.common.AbstractSingleDataSingleTargetProcessor;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.world.WorldManager;

import java.util.Optional;

public class MapInfoWorldValueProcessor extends AbstractSingleDataSingleTargetProcessor<MapInfo, World, Value<World>, MapInfoData, ImmutableMapInfoData> {

    public MapInfoWorldValueProcessor() {
        super(Keys.MAP_WORLD, MapInfo.class);
    }

    @Override
    protected Value<World> constructValue(World actualValue) {
        return new SpongeValue<>(Keys.MAP_WORLD,
                (World)WorldManager.getWorld(Sponge.getServer().getDefaultWorldName()).get(), actualValue);
    }

    @Override
    protected boolean set(MapInfo mapInfo, World value) {
        MapData mapData = (MapData)mapInfo;
        MapDataBridge mapDataBridge = (MapDataBridge)mapData;
        mapDataBridge.bridge$setDimensionId(((WorldServerBridge)value).bridge$getDimensionId());
        mapData.markDirty();
        return true;
    }

    @Override
    protected Optional<World> getVal(MapInfo mapInfo) {
        MapData mapData = (MapData)mapInfo;
        return WorldManager.getWorldByDimensionId(((MapDataBridge)mapData).bridge$getDimensionId())
                .map(worldServer -> (World)worldServer);
    }

    @Override
    protected ImmutableValue<World> constructImmutableValue(World value) {
        return constructValue(value).asImmutable();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected MapInfoData createManipulator() {
        return new SpongeMapInfoData();
    }
}
