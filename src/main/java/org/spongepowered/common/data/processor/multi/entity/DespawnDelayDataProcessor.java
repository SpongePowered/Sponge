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
package org.spongepowered.common.data.processor.multi.entity;

import static org.spongepowered.common.data.util.DataUtil.getData;

import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableDespawnDelayData;
import org.spongepowered.api.data.manipulator.mutable.entity.DespawnDelayData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeDespawnDelayData;
import org.spongepowered.common.data.processor.common.AbstractEntityDataProcessor;
import org.spongepowered.common.bridge.entity.EntityItemBridge;

import java.util.Map;
import java.util.Optional;
import net.minecraft.entity.item.ItemEntity;

public final class DespawnDelayDataProcessor extends AbstractEntityDataProcessor<ItemEntity, DespawnDelayData, ImmutableDespawnDelayData> {

    public DespawnDelayDataProcessor() {
        super(ItemEntity.class);
    }

    @Override
    protected boolean doesDataExist(ItemEntity container) {
        return true;
    }

    @Override
    protected boolean set(ItemEntity container, Map<Key<?>, Object> keyValues) {
        ((EntityItemBridge) container).bridge$setDespawnDelay(
                (Integer) keyValues.get(Keys.DESPAWN_DELAY),
                (Boolean) keyValues.get(Keys.INFINITE_DESPAWN_DELAY)
        );
        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(ItemEntity container) {
        return ImmutableMap.<Key<?>, Object> builder()
                .put(Keys.DESPAWN_DELAY, ((EntityItemBridge) container).bridge$getDespawnDelay())
                .put(Keys.INFINITE_DESPAWN_DELAY, ((EntityItemBridge) container).bridge$infiniteDespawnDelay())
                .build();
    }

    @Override
    protected DespawnDelayData createManipulator() {
        return new SpongeDespawnDelayData();
    }

    @Override
    public Optional<DespawnDelayData> fill(DataContainer container, DespawnDelayData data) {
        data.set(Keys.DESPAWN_DELAY, getData(container, Keys.DESPAWN_DELAY));
        return Optional.of(data);
    }

    @Override
    public DataTransactionResult remove(DataHolder container) {
        return DataTransactionResult.failNoData();
    }

}
