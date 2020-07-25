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
package org.spongepowered.common.util;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.util.Range;

public final class SpongeRange<T extends Number> implements Range<T> {

    public static final RangeFactory FACTORY_INSTANCE = new RangeFactory();

    @Nullable private final T min;
    @Nullable private final T max;

    public SpongeRange(@Nullable final T min, @Nullable final T max) {
        if (min == null && max == null) {
            throw new IllegalArgumentException("At least one of min or max must not be null.");
        }
        this.min = min;
        this.max = max;
    }

    @Override
    @Nullable
    public T getMin() {
        return this.min;
    }

    @Override
    @Nullable
    public T getMax() {
        return this.max;
    }

    public final static class RangeFactory implements Factory {

        @Override
        @NonNull
        public Range<@NonNull Integer> intRange(@Nullable final Integer min, @Nullable final Integer max) {
            if (min != null && max != null && max > min) {
                // nope
                throw new IllegalArgumentException("min must be smaller or equal to max if both are defined");
            }
            return new SpongeRange<>(min, max);
        }

        @Override
        @NonNull
        public Range<@NonNull Double> doubleRange(@Nullable final Double min, @Nullable final Double max) {
            if (min != null && max != null && max > min) {
                // nope
                throw new IllegalArgumentException("min must be smaller or equal to max if both are defined");
            }
            return new SpongeRange<>(min, max);
        }
    }
}
