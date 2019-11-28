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
import org.spongepowered.common.mixin.core.entity.projectile.ShulkerBulletEntityAccessor;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

public class ShulkerBulletDirectionalDataProcessor extends AbstractSingleDataSingleTargetProcessor<ShulkerBulletEntityAccessor, Direction, Value<Direction>,
        DirectionalData, ImmutableDirectionalData> {

    public ShulkerBulletDirectionalDataProcessor() {
        super(Keys.DIRECTION, ShulkerBulletEntityAccessor.class);
    }

    @Override
    protected boolean set(ShulkerBulletEntityAccessor dataHolder, Direction value) {
        if (value == Direction.NONE) {
            dataHolder.accessor$setDirection(null);
        } else {
            dataHolder.accessor$setDirection(Constants.DirectionFunctions.getFor(value));
        }
        return true;
    }

    @Override
    protected Optional<Direction> getVal(ShulkerBulletEntityAccessor dataHolder) {
        final net.minecraft.util.Direction direction = dataHolder.accessor$getDirection();
        return Optional.of(direction != null ? Constants.DirectionFunctions.getFor(direction) : Direction.NONE);
    }

    @Override
    protected ImmutableValue<Direction> constructImmutableValue(Direction value) {
        return new ImmutableSpongeValue<>(Keys.DIRECTION, Direction.NONE, value);
    }

    @Override
    public boolean supports(ShulkerBulletEntityAccessor dataHolder) {
        return true;
    }

    @Override
    protected Value<Direction> constructValue(Direction actualValue) {
        return new SpongeValue<>(Keys.DIRECTION, Direction.NONE, actualValue);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected DirectionalData createManipulator() {
        return new SpongeDirectionalData();
    }
}
