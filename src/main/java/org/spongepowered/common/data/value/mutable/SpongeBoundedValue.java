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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeBoundedValue;

import java.util.Comparator;
import java.util.function.Function;

public class SpongeBoundedValue<E> extends SpongeValue<E> implements MutableBoundedValue<E> {

    private final Comparator<E> comparator;
    private final E minimum;
    private final E maximum;

    public SpongeBoundedValue(Key<? extends BaseValue<E>> key, E defaultValue, Comparator<E> comparator, E minimum, E maximum) {
        this(key, defaultValue, comparator, minimum, maximum, defaultValue);
    }

    public SpongeBoundedValue(Key<? extends BaseValue<E>> key, E defaultValue, Comparator<E> comparator, E minimum, E maximum, E actualValue) {
        super(key, defaultValue, actualValue);
        this.comparator = checkNotNull(comparator);
        this.minimum = checkNotNull(minimum);
        this.maximum = checkNotNull(maximum);
        checkState(comparator.compare(maximum, minimum) >= 0);
    }


    @Override
    public E getMinValue() {
        return this.minimum;
    }

    @Override
    public E getMaxValue() {
        return this.maximum;
    }

    @Override
    public Comparator<E> getComparator() {
        return this.comparator;
    }

    @Override
    public MutableBoundedValue<E> set(E value) {
        if (this.comparator.compare(value, this.minimum) >= 0 && this.comparator.compare(value, this.maximum) <= 0) {
            this.actualValue = checkNotNull(value);
        }
        return this;
    }

    @Override
    public MutableBoundedValue<E> transform(Function<E, E> function) {
        return set(checkNotNull(checkNotNull(function).apply(get())));
    }

    @Override
    public ImmutableBoundedValue<E> asImmutable() {
        return new ImmutableSpongeBoundedValue<>(getKey(), this.getDefault(), this.actualValue, this.comparator, this.minimum, this.maximum);
    }

    @Override
    public MutableBoundedValue<E> copy() {
        return new SpongeBoundedValue<>(this.getKey(), this.getDefault(), this.comparator, this.minimum, this.maximum, this.actualValue);
    }
}
