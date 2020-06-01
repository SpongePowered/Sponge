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
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableMapInfoData;
import org.spongepowered.api.data.manipulator.mutable.MapInfoData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.map.MapInfo;
import org.spongepowered.common.data.manipulator.mutable.SpongeMapInfoData;
import org.spongepowered.common.data.processor.common.AbstractSingleDataSingleTargetProcessor;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

public class MapInfoScaleValueProcessor extends AbstractSingleDataSingleTargetProcessor<MapInfo, Integer, MutableBoundedValue<Integer>, MapInfoData, ImmutableMapInfoData> {


    public MapInfoScaleValueProcessor() {
        super(Keys.MAP_SCALE, MapInfo.class);
    }

    @Override
    protected boolean set(MapInfo mapInfo, Integer value) {
        MapData mapData = (MapData)mapInfo;
        mapData.scale = value.byteValue();
        mapData.markDirty();
        return true;
    }

    @Override
    protected Optional<Integer> getVal(MapInfo mapInfo) {
        MapData mapData = (MapData)mapInfo;
        return Optional.of((int)mapData.scale);
    }

    @Override
    protected ImmutableValue<Integer> constructImmutableValue(Integer value) {
        return constructValue(value).asImmutable();
    }

    @Override
    protected MutableBoundedValue<Integer> constructValue(Integer actualValue) {
        return SpongeValueFactory.boundedBuilder(Keys.MAP_SCALE)
                .defaultValue(Constants.Map.DEFAULT_MAP_SCALE)
                .actualValue(actualValue)
                .minimum(Constants.Map.MIN_MAP_SCALE)
                .maximum(Constants.Map.MAX_MAP_SCALE)
                .build();
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
