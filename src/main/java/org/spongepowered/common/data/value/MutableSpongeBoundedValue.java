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
package org.spongepowered.common.data.value;

import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.common.data.copy.CopyHelper;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Supplier;

public final class MutableSpongeBoundedValue<E> extends AbstractMutableSpongeValue<E> implements BoundedValue.Mutable<E> {

    private final Supplier<E> minValue;
    private final Supplier<E> maxValue;
    private final Comparator<? super E> comparator;

    public MutableSpongeBoundedValue(Key<? extends BoundedValue<E>> key, E element,
            Supplier<E> minValue, Supplier<E> maxValue, Comparator<? super E> comparator) {
        super(key, element);
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.comparator = comparator;
    }

    @Override
    public Key<? extends BoundedValue<E>> getKey() {
        //noinspection unchecked
        return (Key<? extends BoundedValue<E>>) super.getKey();
    }

    @Override
    public BoundedValue.Mutable<E> set(E value) {
        super.set(value);
        return this;
    }

    @Override
    public BoundedValue.Mutable<E> transform(Function<E, E> function) {
        return this.set(function.apply(this.get()));
    }

    @Override
    public E getMinValue() {
        return this.minValue.get();
    }

    @Override
    public E getMaxValue() {
        return this.maxValue.get();
    }

    @Override
    public Comparator<? super E> getComparator() {
        return this.comparator;
    }

    @Override
    public BoundedValue.Mutable<E> copy() {
        return new MutableSpongeBoundedValue<>(this.getKey(),
                CopyHelper.copy(this.get()), this.minValue, this.maxValue, this.comparator);
    }

    @Override
    public BoundedValue.Immutable<E> asImmutable() {
        return new ImmutableSpongeBoundedValue<>(this.getKey(),
                CopyHelper.copy(this.get()), this.minValue, this.maxValue, this.comparator);
    }
}
