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

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableDirectionalData;
import org.spongepowered.api.data.manipulator.mutable.block.DirectionalData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeDirectionalData;
import org.spongepowered.common.data.processor.common.AbstractSingleDataSingleTargetProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.mixin.core.entity.EntityHangingAccessor;
import org.spongepowered.common.util.Constants;

import java.util.Optional;
import net.minecraft.entity.item.HangingEntity;

public class HangingDataProcessor extends AbstractSingleDataSingleTargetProcessor<HangingEntity, Direction, Value<Direction>, DirectionalData, ImmutableDirectionalData> {

    public HangingDataProcessor() {
        super(Keys.DIRECTION, HangingEntity.class);
    }

    @Override
    protected DirectionalData createManipulator() {
        return new SpongeDirectionalData();
    }

    @Override
    protected boolean set(HangingEntity dataHolder, Direction value) {
        ((EntityHangingAccessor) dataHolder).accessor$updateFacingWithBoundingBox(Constants.DirectionFunctions.getFor(value));
        return true;
    }

    @Override
    protected Optional<Direction> getVal(HangingEntity dataHolder) {
        return Optional.of(dataHolder.field_174860_b == null ? Direction.NONE : Constants.DirectionFunctions.getFor(dataHolder.field_174860_b));
    }

    @Override
    protected ImmutableValue<Direction> constructImmutableValue(Direction value) {
        return ImmutableSpongeValue.cachedOf(this.key, Direction.NONE, value);
    }

    @Override
    protected Value<Direction> constructValue(Direction actualValue) {
        return new SpongeValue<>(this.key, Direction.NONE, actualValue);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

}
