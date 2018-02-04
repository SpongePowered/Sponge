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

import com.google.common.base.MoreObjects;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.InternalCopies;
import org.spongepowered.common.data.value.SpongeValueFactory;

import java.util.Comparator;
import java.util.function.Function;

import javax.annotation.Nullable;

public class ImmutableSpongeBoundedValue<E> extends ImmutableSpongeValue<E> implements ImmutableBoundedValue<E> {

    public static <T> ImmutableBoundedValue<T> cachedOf(Key<? extends BaseValue<T>> key, T defaultValue, T actualValue,
            Comparator<T> comparator, T minimum, T maximum) {
        return ImmutableDataCachingUtil.getValue(ImmutableSpongeBoundedValue.class, key, defaultValue, actualValue, comparator, minimum, maximum);
    }

    /*
     * A constructor method to avoid unnecessary copies. INTERNAL USE ONLY!
     */
    private static <E> ImmutableSpongeBoundedValue<E> constructUnsafe(
            Key<? extends BaseValue<E>> key, E defaultValue, E actualValue, Comparator<E> comparator, E minimum, E maximum) {
        return new ImmutableSpongeBoundedValue<>(key, defaultValue, actualValue, comparator, minimum, maximum, null);
    }

    private final Comparator<E> comparator;
    private final E minimum;
    private final E maximum;

    public ImmutableSpongeBoundedValue(Key<? extends BaseValue<E>> key, E actualValue, Comparator<E> comparator, E minimum, E maximum) {
        this(key, InternalCopies.immutableCopy(actualValue), comparator,
                InternalCopies.immutableCopy(minimum),
                InternalCopies.immutableCopy(maximum), null);
    }

    // DO NOT MODIFY THE SIGNATURE OR REMOVE THE CONSTRUCTOR
    public ImmutableSpongeBoundedValue(Key<? extends BaseValue<E>> key, E defaultValue, E actualValue,
            Comparator<E> comparator, E minimum, E maximum) {
        this(key, InternalCopies.immutableCopy(defaultValue),
                InternalCopies.immutableCopy(actualValue), comparator,
                InternalCopies.immutableCopy(minimum),
                InternalCopies.immutableCopy(maximum), null);
    }

    protected ImmutableSpongeBoundedValue(Key<? extends BaseValue<E>> key, E actualValue,
            Comparator<E> comparator, E minimum, E maximum, @Nullable Void nothing) {
        this(key, actualValue, actualValue, comparator, minimum, maximum, nothing);
    }

    // A constructor to avoid unnecessary copies
    protected ImmutableSpongeBoundedValue(Key<? extends BaseValue<E>> key, E defaultValue, E actualValue,
            Comparator<E> comparator, E minimum, E maximum, @Nullable Void nothing) {
        super(key, defaultValue, actualValue, nothing);
        this.comparator = checkNotNull(comparator);
        this.minimum = checkNotNull(minimum);
        this.maximum = checkNotNull(maximum);
        checkState(comparator.compare(maximum, minimum) >= 0);
    }

    @Override
    protected ImmutableSpongeBoundedValue<E> withValueUnsafe(E value) {
        return constructUnsafe(getKey(), this.defaultValue, value, this.comparator, this.minimum, this.maximum);
    }

    @Override
    public ImmutableBoundedValue<E> with(E value) {
        if (this.comparator.compare(value, this.minimum) >= 0 &&
                this.comparator.compare(value, this.maximum) <= 0) {
            return withValueUnsafe(InternalCopies.immutableCopy(value));
        }
        return withValueUnsafe(this.defaultValue);
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
        return InternalCopies.immutableCopy(this.minimum); // Safely copy the object, if needed
    }

    @Override
    public E getMaxValue() {
        return InternalCopies.immutableCopy(this.maximum); // Safely copy the object, if needed
    }

    @Override
    public Comparator<E> getComparator() {
        return this.comparator;
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("minimum", this.maximum)
                .add("maximum", this.maximum);
    }
}
