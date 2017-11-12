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

import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.value.SpongeValueFactory;

import java.util.Comparator;
import java.util.function.Function;

public class ImmutableSpongeBoundedValue<E> extends ImmutableSpongeValue<E> implements ImmutableBoundedValue<E> {

    public static <T> ImmutableBoundedValue<T> cachedOf(Key<? extends BaseValue<T>> key, T defaultValue, T actualValue, Comparator<T>
            comparator, T minimum, T maximum) {
        return ImmutableDataCachingUtil.getValue(ImmutableSpongeBoundedValue.class, key, defaultValue, actualValue, comparator, minimum, maximum);
    }

    private final Comparator<E> comparator;
    private final E minimum;
    private final E maximum;

    public ImmutableSpongeBoundedValue(Key<? extends BaseValue<E>> key, E defaultValue, Comparator<E> comparator, E minimum, E maximum) {
        super(key, defaultValue);
        this.comparator = checkNotNull(comparator);
        this.minimum = checkNotNull(minimum);
        this.maximum = checkNotNull(maximum);
        checkState(comparator.compare(maximum, minimum) >= 0);
    }

    public ImmutableSpongeBoundedValue(Key<? extends BaseValue<E>> key, E defaultValue, E actualValue, Comparator<E> comparator, E minimum, E maximum) {
        super(key, defaultValue, actualValue);
        this.comparator = checkNotNull(comparator);
        this.minimum = checkNotNull(minimum);
        this.maximum = checkNotNull(maximum);
        checkState(comparator.compare(maximum, minimum) >= 0);
    }

    @Override
    public ImmutableBoundedValue<E> with(E value) {
        if (this.comparator.compare(value, this.minimum) >= 0 && this.comparator.compare(value, this.maximum) <= 0) {
            return new ImmutableSpongeBoundedValue<>(getKey(), getDefault(), value,  getComparator(), getMinValue(), getMaxValue());
        }
        return new ImmutableSpongeBoundedValue<>(getKey(), getDefault(), getComparator(), getMinValue(), getMaxValue());
    }

    @Override
    public ImmutableBoundedValue<E> transform(Function<E, E> function) {
        return with(checkNotNull(checkNotNull(function).apply(get())));
    }

    @SuppressWarnings("unchecked")
    @Override
    public MutableBoundedValue<E> asMutable() {
        return SpongeValueFactory.boundedBuilder((Key<? extends BoundedValue<E>>) getKey())
            .defaultValue(getDefault())
            .minimum(getMinValue())
            .maximum(getMaxValue())
            .actualValue(get())
            .comparator(getComparator())
            .build();
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
