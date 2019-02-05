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

import static com.google.common.base.Preconditions.checkArgument;

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableExplosionRadiusData;
import org.spongepowered.api.data.manipulator.mutable.ExplosionRadiusData;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.entity.explosive.Explosive;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeExplosionRadiusData;
import org.spongepowered.common.data.processor.common.AbstractSingleDataSingleTargetProcessor;
import org.spongepowered.common.data.value.SpongeImmutableOptionalValue;
import org.spongepowered.common.data.value.SpongeMutableOptionalValue;
import org.spongepowered.common.interfaces.entity.explosive.IMixinExplosive;

import java.util.Optional;

public class ExplosionRadiusDataProcessor extends AbstractSingleDataSingleTargetProcessor<Explosive, Optional<Integer>,
        ExplosionRadiusData, ImmutableExplosionRadiusData> {

    public ExplosionRadiusDataProcessor() {
        super(Keys.EXPLOSION_RADIUS, Explosive.class);
    }

    @Override
    protected boolean set(Explosive explosive, Optional<Integer> value) {
        checkArgument(!value.isPresent() || value.get() >= 0, "value must be empty or no less than zero");
        ((IMixinExplosive) explosive).setExplosionRadius(value);
        return true;
    }

    @Override
    protected Optional<Optional<Integer>> getVal(Explosive explosive) {
        return Optional.of(((IMixinExplosive) explosive).getExplosionRadius());
    }

    @Override
    protected Value.Immutable<Optional<Integer>> constructImmutableValue(Optional<Integer> value) {
        return new SpongeImmutableOptionalValue<>(Keys.EXPLOSION_RADIUS, value);
    }

    @Override
    protected Value.Mutable<Optional<Integer>> constructMutableValue(Optional<Integer> actualValue) {
        return new SpongeMutableOptionalValue<>(Keys.EXPLOSION_RADIUS, actualValue);
    }

    @Override
    protected ExplosionRadiusData createManipulator() {
        return new SpongeExplosionRadiusData();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

}
