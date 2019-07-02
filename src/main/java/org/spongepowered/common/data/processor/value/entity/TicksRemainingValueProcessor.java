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
package org.spongepowered.common.data.processor.value.entity;

import static com.google.common.base.Preconditions.checkArgument;

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.explosive.FusedExplosive;
import org.spongepowered.common.bridge.explosives.FusedExplosiveBridge;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;

public class TicksRemainingValueProcessor extends AbstractSpongeValueProcessor<FusedExplosive, Integer, Value<Integer>> {

    public TicksRemainingValueProcessor() {
        super(FusedExplosive.class, Keys.TICKS_REMAINING);
    }

    @Override
    protected Value<Integer> constructValue(final Integer actualValue) {
        return new SpongeValue<>(Keys.TICKS_REMAINING, actualValue);
    }

    @Override
    protected boolean set(final FusedExplosive container, final Integer value) {
        checkArgument(value >= 0, "ticks remaining cannot be less than zero");
        if (container.isPrimed()) {
            ((FusedExplosiveBridge) container).bridge$setFuseTicksRemaining(value);
            return true;
        }
        return false;
    }

    @Override
    protected Optional<Integer> getVal(final FusedExplosive container) {
        return Optional.of(((FusedExplosiveBridge) container).bridge$getFuseTicksRemaining());
    }

    @Override
    protected ImmutableValue<Integer> constructImmutableValue(final Integer value) {
        return new ImmutableSpongeValue<>(Keys.TICKS_REMAINING, value);
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

}
