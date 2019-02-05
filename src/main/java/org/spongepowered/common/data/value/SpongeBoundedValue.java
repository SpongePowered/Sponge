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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.api.data.value.Value;

import java.util.Comparator;
import java.util.Objects;

public abstract class SpongeBoundedValue<E> extends SpongeValue<E> implements BoundedValue<E> {

    protected final E min;
    protected final E max;
    protected final Comparator<E> comparator;

    protected SpongeBoundedValue(Key<? extends Value<E>> key, E value, E min, E max, Comparator<E> comparator) {
        super(key, value);
        this.min = checkNotNull(min, "min");
        this.max = checkNotNull(max, "max");
        this.comparator = checkNotNull(comparator, "comparator");
    }

    @Override
    public E getMinValue() {
        return this.min;
    }

    @Override
    public E getMaxValue() {
        return this.max;
    }

    @Override
    public Comparator<E> getComparator() {
        return this.comparator;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.key, this.value, this.min, this.max, this.comparator);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final SpongeBoundedValue other = (SpongeBoundedValue) obj;
        return Objects.equals(this.key, other.key)
                && Objects.equals(this.value, other.value)
                && Objects.equals(this.min, other.min)
                && Objects.equals(this.max, other.max)
                && Objects.equals(this.comparator, other.comparator);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("key", this.key)
                .add("value", this.value)
                .add("min", this.min)
                .add("max", this.max)
                .add("comparator", this.comparator)
                .toString();
    }
}
