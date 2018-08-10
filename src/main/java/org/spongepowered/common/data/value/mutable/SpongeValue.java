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
package org.spongepowered.common.data.value.mutable;

import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.value.AbstractBaseValue;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import java.util.function.Function;

public class SpongeValue<E> extends AbstractBaseValue<E> implements Value<E> {

    public SpongeValue(Key<? extends BaseValue<E>> key, E defaultValue) {
        this(key, defaultValue, defaultValue);
    }

    public SpongeValue(Key<? extends BaseValue<E>> key, E defaultValue, E actualValue) {
        super(key, defaultValue, actualValue);
    }

    @Override
    public Value<E> set(E value) {
        this.actualValue = value;
        return this;
    }

    @Override
    public Value<E> transform(Function<E, E> function) {
        this.actualValue = function.apply(this.actualValue);
        return this;
    }

    @Override
    public ImmutableValue<E> asImmutable() {
        return ImmutableSpongeValue.cachedOf(this.getKey(), this.getDefault(), this.actualValue);
    }

    @Override
    public Value<E> copy() {
        return new SpongeValue<>(this.getKey(), this.getDefault(), this.actualValue);
    }
}
