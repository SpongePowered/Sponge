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

import com.google.common.collect.Lists;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.api.data.value.ValueFactory;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.data.value.mutable.MapValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.api.data.value.mutable.SetValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.value.mutable.SpongeBoundedValue;
import org.spongepowered.common.data.value.mutable.SpongeListValue;
import org.spongepowered.common.data.value.mutable.SpongeMapValue;
import org.spongepowered.common.data.value.mutable.SpongeOptionalValue;
import org.spongepowered.common.data.value.mutable.SpongeSetValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

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

    @Override
    public <E> Value<E> createValue(Key<Value<E>> key, E element) {
        return new SpongeValue<>(checkNotNull(key, "key"), checkNotNull(element, "element"));
    }

    @Override
    public <E> Value<E> createValue(Key<Value<E>> key, E element, E defaultValue) {
        return new SpongeValue<>(checkNotNull(key, "key"), checkNotNull(defaultValue, "defaultValue"), checkNotNull(element, "element"));
    }

    @Override
    public <E> ListValue<E> createListValue(Key<ListValue<E>> key, List<E> elements) {
        return new SpongeListValue<>(checkNotNull(key, "key"), Lists.<E>newArrayList(), Lists.newArrayList(elements));
    }

    @Override
    public <E> ListValue<E> createListValue(Key<ListValue<E>> key, List<E> elements, List<E> defaults) {
        return new SpongeListValue<>(checkNotNull(key, "key"), checkNotNull(defaults, "defaults"), checkNotNull(elements));
    }

    @Override
    public <E> SetValue<E> createSetValue(Key<SetValue<E>> key, Set<E> elements) {
        return new SpongeSetValue<>(checkNotNull(key, "key"), checkNotNull(elements, "elements"));
    }

    @Override
    public <E> SetValue<E> createSetValue(Key<SetValue<E>> key, Set<E> elements, Set<E> defaults) {
        return new SpongeSetValue<>(checkNotNull(key, "key"), checkNotNull(defaults, "defaults"), checkNotNull(elements, "elements"));
    }

    @Override
    public <K, V> MapValue<K, V> createMapValue(Key<MapValue<K, V>> key, Map<K, V> map) {
        return new SpongeMapValue<>(checkNotNull(key, "key"), checkNotNull(map, "map"));
    }

    @Override
    public <K, V> MapValue<K, V> createMapValue(Key<MapValue<K, V>> key, Map<K, V> map, Map<K, V> defaults) {
        return new SpongeMapValue<>(checkNotNull(key, "key"), checkNotNull(defaults, "defaults"), checkNotNull(map, "map"));
    }

    @Override
    public <E> BoundedValueBuilder<E> createBoundedValueBuilder(Key<MutableBoundedValue<E>> key) {
        return new SpongeBoundedValueBuilder<>(checkNotNull(key));
    }

    @Override
    public <E> OptionalValue<E> createOptionalValue(Key<OptionalValue<E>> key, @Nullable E element) {
        return new SpongeOptionalValue<>(checkNotNull(key, "key"), Optional.<E>empty(), Optional.ofNullable(element));
    }

    @Override
    public <E> OptionalValue<E> createOptionalValue(Key<OptionalValue<E>> key, @Nullable E element, E defaultElement) {
        return new SpongeOptionalValue<>(checkNotNull(key, "key"), Optional.of(defaultElement), Optional.ofNullable(element));
    }

    public static <E> BoundedValueBuilder<E> boundedBuilder(Key<? extends BoundedValue<E>> key) {
        return new SpongeBoundedValueBuilder<>(checkNotNull(key));
    }

    private SpongeValueFactory() { }

    public static final class SpongeBoundedValueBuilder<E> implements BoundedValueBuilder<E> {

        private final Key<? extends BoundedValue<E>> key;
        private Comparator<E> comparator;
        private E minimum;
        private E maximum;
        private E defaultValue;
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
        public BoundedValueBuilder<E> defaultValue(E defaultValue) {
            this.defaultValue = checkNotNull(defaultValue);
            return this;
        }

        @Override
        public BoundedValueBuilder<E> actualValue(E value) {
            this.value = checkNotNull(value);
            return this;
        }

        @Override
        public SpongeBoundedValue<E> build() {
            checkState(this.comparator != null);
            checkState(this.minimum != null);
            checkState(this.maximum != null);
            checkState(this.defaultValue != null);
            if (this.value == null) {
                return new SpongeBoundedValue<>(this.key, this.defaultValue, this.comparator, this.minimum, this.maximum);
            }
            return new SpongeBoundedValue<>(this.key, this.defaultValue, this.comparator, this.minimum, this.maximum, this.value);
        }
    }
}
