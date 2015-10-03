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
package org.spongepowered.common.data.value.immutable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Function;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.value.mutable.SpongeBoundedValue;

import java.util.Comparator;

public class ImmutableSpongeBoundedValue<E> extends ImmutableSpongeValue<E> implements ImmutableBoundedValue<E> {

    public static <T> ImmutableBoundedValue<T> cachedOf(Key<? extends BaseValue<T>> key, T defaultValue, T actualValue, Comparator<T>
            comparator, T minimum, T maximum) {
        return ImmutableDataCachingUtil.getValue(ImmutableSpongeBoundedValue.class, key, actualValue, defaultValue, comparator, minimum, maximum);
    }

    private final Comparator<E> comparator;
    private final E minimum;
    private final E maximum;

    public ImmutableSpongeBoundedValue(Key<? extends BaseValue<E>> key, E defaultValue, Comparator<E> comparator, E minimum, E maximum) {
        super(key, defaultValue);
        this.comparator = checkNotNull(comparator);
        this.minimum = checkNotNull(minimum);
        this.maximum = checkNotNull(maximum);
        checkState(comparator.compare(minimum, maximum) >= 0);
    }

    public ImmutableSpongeBoundedValue(Key<? extends BaseValue<E>> key, E actualValue, E defaultValue, Comparator<E> comparator, E minimum, E maximum) {
        super(key, defaultValue, actualValue);
        this.comparator = checkNotNull(comparator);
        this.minimum = checkNotNull(minimum);
        this.maximum = checkNotNull(maximum);
        checkState(comparator.compare(maximum, minimum) >= 0);
    }

    @Override
    public ImmutableBoundedValue<E> with(E value) {
        return (this.comparator.compare(checkNotNull(value), this.minimum) > 0 || this.comparator.compare(checkNotNull(value), this.maximum) < 0) ?
            new ImmutableSpongeBoundedValue<E>(getKey(), getDefault(), getComparator(), getMinValue(), getMaxValue()) :
            new ImmutableSpongeBoundedValue<E>(getKey(), value, getDefault(), getComparator(), getMinValue(), getMaxValue());
    }

    @Override
    public ImmutableBoundedValue<E> transform(Function<E, E> function) {
        return with(checkNotNull(checkNotNull(function).apply(get())));
    }

    @Override
    public MutableBoundedValue<E> asMutable() {
        return new SpongeBoundedValue<E>(getKey(), getDefault(), getComparator(), getMinValue(), getMaxValue(), get());
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
}
