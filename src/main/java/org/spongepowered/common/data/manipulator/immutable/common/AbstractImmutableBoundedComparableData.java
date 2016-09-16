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
package org.spongepowered.common.data.manipulator.immutable.common;

import static com.google.common.base.Preconditions.checkArgument;

import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeBoundedValue;
import org.spongepowered.common.util.ReflectionUtil;

import java.lang.reflect.Modifier;
import java.util.Comparator;

/**
 * An abstracted {@link ImmutableDataManipulator} that focuses solely on an
 * {@link ImmutableBoundedValue} as it's {@link Value} return type. The
 * advantage is that this type of {@link ImmutableDataManipulator} can easily
 * be cached in the {@link ImmutableDataCachingUtil}.
 *
 * @param <T> The type of comparable element
 * @param <I> The immutable data manipulator type
 * @param <M> The mutable data manipulator type
 */
public abstract class AbstractImmutableBoundedComparableData<T extends Comparable<T>, I extends ImmutableDataManipulator<I, M>,
    M extends DataManipulator<M, I>> extends AbstractImmutableSingleData<T, I, M> {

    private final Class<? extends M> mutableClass;
    protected final Comparator<T> comparator;
    protected final T lowerBound;
    protected final T upperBound;
    protected final T defaultValue;
    private final ImmutableBoundedValue<T> immutableBoundedValue;

    @SuppressWarnings("unchecked")
    protected AbstractImmutableBoundedComparableData(Class<I> immutableClass, T value,
                                                     Key<? extends BaseValue<T>> usedKey,
                                                     Comparator<T> comparator, Class<? extends M> mutableClass, T lowerBound, T upperBound, T defaultValue) {
        super(immutableClass, value, usedKey);
        this.comparator = comparator;
        checkArgument(!Modifier.isAbstract(mutableClass.getModifiers()), "The immutable class cannot be abstract!");
        checkArgument(!Modifier.isInterface(mutableClass.getModifiers()), "The immutable class cannot be an interface!");
        this.mutableClass = mutableClass;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.defaultValue = defaultValue;
        if (value instanceof Integer && ((Integer) upperBound) - ((Integer) lowerBound) <= ImmutableDataCachingUtil.CACHE_LIMIT_FOR_INDIVIDUAL_TYPE) {
            this.immutableBoundedValue = ImmutableSpongeBoundedValue.cachedOf(this.usedKey,
                                                                              this.defaultValue,
                                                                              this.value,
                                                                              this.comparator,
                                                                              this.lowerBound,
                                                                              this.upperBound);
        } else {
            this.immutableBoundedValue = SpongeValueFactory.boundedBuilder((Key<? extends BoundedValue<T>>) this.usedKey)
            .defaultValue(this.defaultValue)
            .actualValue(this.value)
            .minimum(this.lowerBound)
            .maximum(this.upperBound)
            .comparator(this.comparator)
            .build()
            .asImmutable();
        }
    }

    @Override
    protected final ImmutableBoundedValue<T> getValueGetter() {
        return this.immutableBoundedValue;
    }

    @Override
    public M asMutable() {
        return ReflectionUtil.createInstance(this.mutableClass, this.value, this.lowerBound, this.upperBound, this.defaultValue);
    }

}
