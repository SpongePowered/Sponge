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
package org.spongepowered.common.data.processor.data.entity;

import net.minecraft.entity.item.EntityItem;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutablePickupDelayData;
import org.spongepowered.api.data.manipulator.mutable.entity.PickupDelayData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongePickupDelayData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.interfaces.entity.item.IMixinEntityItem;

import java.util.Optional;

public final class PickupDelayDataProcessor extends AbstractEntitySingleDataProcessor<EntityItem, Integer, MutableBoundedValue<Integer>,
        PickupDelayData, ImmutablePickupDelayData> {

    public PickupDelayDataProcessor() {
        super(EntityItem.class, Keys.PICKUP_DELAY);
    }

    @Override
    protected MutableBoundedValue<Integer> constructValue(Integer actualValue) {
        return SpongeValueFactory.boundedBuilder(this.key)
                .actualValue(actualValue)
                .minimum(DataConstants.Entity.Item.MIN_PICKUP_DELAY)
                .maximum(DataConstants.Entity.Item.MAX_PICKUP_DELAY)
                .defaultValue(DataConstants.Entity.Item.DEFAULT_PICKUP_DELAY)
                .build();
    }

    @Override
    protected boolean set(EntityItem entity, Integer value) {
        entity.setPickupDelay(value);
        return true;
    }

    @Override
    protected Optional<Integer> getVal(EntityItem entity) {
        return Optional.of(((IMixinEntityItem) entity).getPickupDelay());
    }

    @Override
    protected ImmutableValue<Integer> constructImmutableValue(Integer value) {
        return new ImmutableSpongeValue<>(this.key, DataConstants.Entity.Item.DEFAULT_PICKUP_DELAY, value);
    }

    @Override
    protected PickupDelayData createManipulator() {
        return new SpongePickupDelayData();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

}
