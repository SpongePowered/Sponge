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
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.api.data.value.ListValue;
import org.spongepowered.api.data.value.MapValue;
import org.spongepowered.api.data.value.OptionalValue;
import org.spongepowered.api.data.value.SetValue;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

public class SpongeValueFactory implements ValueFactory {

    private static ValueFactory instance = new SpongeValueFactory();

    public static ValueFactory getInstance() {
        return instance;
    }

    private SpongeValueFactory() {}

    @Override
    public <E> Value.Mutable<E> createValue(Key<? extends Value<E>> key, E element) {
        return new SpongeMutableValue<>(key, element);
    }

    @Override
    public <E> ListValue.Mutable<E> createListValue(Key<? extends Value<List<E>>> key, List<E> elements) {
        return new SpongeMutableListValue<>(key, elements);
    }

    @Override
    public <E> SetValue.Mutable<E> createSetValue(Key<? extends Value<Set<E>>> key, Set<E> elements) {
        return new SpongeMutableSetValue<>(key, elements);
    }

    @Override
    public <K, V> MapValue.Mutable<K, V> createMapValue(Key<? extends Value<Map<K, V>>> key, Map<K, V> map) {
        return new SpongeMutableMapValue<>(key, map);
    }

    @Override
    public <E> BoundedValueBuilder<E> createBoundedValueBuilder(Key<? extends BoundedValue<E>> key) {
        return new SpongeBoundedValueBuilder<>(key);
    }

    @Override
    public <E> OptionalValue.Mutable<E> createOptionalValue(Key<? extends OptionalValue<E>> key, @Nullable E element) {
        return new SpongeMutableOptionalValue<>(key, Optional.ofNullable(element));
    }

    @SuppressWarnings("unchecked")
    public static <E> BoundedValueBuilder<E> boundedBuilder(Key<? extends Value<E>> key) {
        return new SpongeBoundedValueBuilder<>((Key<? extends BoundedValue<E>>) key);
    }

    public static final class SpongeBoundedValueBuilder<E> implements BoundedValueBuilder<E> {

        private final Key<? extends BoundedValue<E>> key;
        private Comparator<E> comparator;
        private E minimum;
        private E maximum;
        private E value;

        public SpongeBoundedValueBuilder(Key<? extends BoundedValue<E>> key) {
            this.key = checkNotNull(key);
        }

        @Override
        public BoundedValueBuilder<E> comparator(Comparator<E> comparator) {
            this.comparator = checkNotNull(comparator);
            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public BoundedValueBuilder<E> minimum(E minimum) {
            this.minimum = checkNotNull(minimum);
            if (this.comparator == null && minimum instanceof Comparable) {
                this.comparator = (o1, o2) -> ((Comparable<E>) o1).compareTo(o2);
            }
            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public BoundedValueBuilder<E> maximum(E maximum) {
            this.maximum = checkNotNull(maximum);
            if (this.comparator == null && maximum instanceof Comparable) {
                this.comparator = (o1, o2) -> ((Comparable<E>) o1).compareTo(o2);
            }
            return this;
        }

        @Override
        public BoundedValueBuilder<E> value(E value) {
            this.value = checkNotNull(value);
            return this;
        }

        @Override
        public SpongeMutableBoundedValue<E> build() {
            checkState(this.comparator != null);
            checkState(this.minimum != null);
            checkState(this.maximum != null);
            checkState(this.value != null);
            return new SpongeMutableBoundedValue<>(this.key, this.value, this.minimum, this.maximum, this.comparator);
        }
    }
}
