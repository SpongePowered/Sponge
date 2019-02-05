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
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.entity.explosive.FusedExplosive;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.SpongeImmutableValue;
import org.spongepowered.common.data.value.SpongeMutableValue;
import org.spongepowered.common.interfaces.entity.explosive.IMixinFusedExplosive;

import java.util.Optional;

public class FuseDurationValueProcessor extends AbstractSpongeValueProcessor<FusedExplosive, Integer> {

    public FuseDurationValueProcessor() {
        super(FusedExplosive.class, Keys.FUSE_DURATION);
    }

    @Override
    protected Value.Mutable<Integer> constructMutableValue(Integer actualValue) {
        return new SpongeMutableValue<>(Keys.FUSE_DURATION, actualValue);
    }

    @Override
    protected boolean set(FusedExplosive container, Integer value) {
        checkArgument(value >= 0, "fuse duration cannot be less than zero");
        ((IMixinFusedExplosive) container).setFuseDuration(value);
        return true;
    }

    @Override
    protected Optional<Integer> getVal(FusedExplosive container) {
        return Optional.of(((IMixinFusedExplosive) container).getFuseDuration());
    }

    @Override
    protected Value.Immutable<Integer> constructImmutableValue(Integer value) {
        return new SpongeImmutableValue<>(Keys.FUSE_DURATION, value);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

}
