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
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableExplosionRadiusData;
import org.spongepowered.api.data.manipulator.mutable.entity.ExplosionRadiusData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.api.entity.explosive.Explosive;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeExplosionRadiusData;
import org.spongepowered.common.data.processor.common.AbstractSingleDataSingleTargetProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeOptionalValue;
import org.spongepowered.common.data.value.mutable.SpongeOptionalValue;
import org.spongepowered.common.bridge.explosives.ExplosiveBridge;

import java.util.Optional;

public class ExplosionRadiusDataProcessor extends AbstractSingleDataSingleTargetProcessor<Explosive, Optional<Integer>, OptionalValue<Integer>,
        ExplosionRadiusData, ImmutableExplosionRadiusData> {

    public ExplosionRadiusDataProcessor() {
        super(Keys.EXPLOSION_RADIUS, Explosive.class);
    }

    @Override
    protected boolean set(Explosive explosive, Optional<Integer> value) {
        checkArgument(!value.isPresent() || value.get() >= 0, "value must be empty or no less than zero");
        ((ExplosiveBridge) explosive).bridge$setExplosionRadius(value.orElse(null));
        return true;
    }

    @Override
    protected Optional<Optional<Integer>> getVal(Explosive explosive) {
        return Optional.of(((ExplosiveBridge) explosive).bridge$getExplosionRadius());
    }

    @Override
    protected ImmutableValue<Optional<Integer>> constructImmutableValue(Optional<Integer> value) {
        return new ImmutableSpongeOptionalValue<>(Keys.EXPLOSION_RADIUS, value);
    }

    @Override
    protected OptionalValue<Integer> constructValue(Optional<Integer> actualValue) {
        return new SpongeOptionalValue<>(Keys.EXPLOSION_RADIUS, actualValue);
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
