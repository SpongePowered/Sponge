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

import com.google.common.collect.Lists;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.ValueBuilder;
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

public class SpongeValueBuilder implements ValueBuilder {

    @Override
    public <E> Value<E> createValue(Key<Value<E>> key, E element) {
        return new SpongeValue<E>(checkNotNull(key, "key"), checkNotNull(element, "element"));
    }

    @Override
    public <E> Value<E> createValue(Key<Value<E>> key, E element, E defaultValue) {
        return new SpongeValue<E>(checkNotNull(key, "key"), checkNotNull(defaultValue, "defaultValue"), checkNotNull(element, "element"));
    }

    @Override
    public <E> ListValue<E> createListValue(Key<ListValue<E>> key, List<E> elements) {
        return new SpongeListValue<E>(checkNotNull(key, "key"), Lists.<E>newArrayList(), Lists.newArrayList(elements));
    }

    @Override
    public <E> ListValue<E> createListValue(Key<ListValue<E>> key, List<E> elements, List<E> defaults) {
        return new SpongeListValue<E>(checkNotNull(key, "key"), checkNotNull(defaults, "defaults"), checkNotNull(elements));
    }

    @Override
    public <E> SetValue<E> createSetValue(Key<SetValue<E>> key, Set<E> elements) {
        return new SpongeSetValue<E>(checkNotNull(key, "key"), checkNotNull(elements, "elements"));
    }

    @Override
    public <E> SetValue<E> createSetValue(Key<SetValue<E>> key, Set<E> elements, Set<E> defaults) {
        return new SpongeSetValue<E>(checkNotNull(key, "key"), checkNotNull(defaults, "defaults"), checkNotNull(elements, "elements"));
    }

    @Override
    public <K, V> MapValue<K, V> createMapValue(Key<MapValue<K, V>> key, Map<K, V> map) {
        return new SpongeMapValue<K, V>(checkNotNull(key, "key"), checkNotNull(map, "map"));
    }

    @Override
    public <K, V> MapValue<K, V> createMapValue(Key<MapValue<K, V>> key, Map<K, V> map, Map<K, V> defaults) {
        return new SpongeMapValue<K, V>(checkNotNull(key, "key"), checkNotNull(defaults, "defaults"), checkNotNull(map, "map"));
    }

    @Override
    public <E> MutableBoundedValue<E> createBoundedValue(Key<MutableBoundedValue<E>> key, E value, Comparator<E> comparator, E minimum, E maximum) {
        return new SpongeBoundedValue<E>(checkNotNull(key, "key"), checkNotNull(value, "value"), checkNotNull(comparator, "comparator"),
                                         checkNotNull(minimum, "minimum"), checkNotNull(maximum, "maximum"));
    }

    @Override
    public <E> MutableBoundedValue<E> createBoundedValue(Key<MutableBoundedValue<E>> key, E value, Comparator<E> comparator, E minimum, E maximum,
                                                         E defaultElement) {
        return new SpongeBoundedValue<E>(checkNotNull(key, "key"), checkNotNull(defaultElement, "defaultElement"),
                                         checkNotNull(comparator, "comparator"), checkNotNull(minimum, "minimum"), checkNotNull(maximum, "maximum"),
                                         checkNotNull(value, "value"));
    }

    @Override
    public <E> OptionalValue<E> createOptionalValue(Key<OptionalValue<E>> key, @Nullable E element) {
        return new SpongeOptionalValue<E>(checkNotNull(key, "key"), Optional.<E>empty(), Optional.ofNullable(element));
    }

    @Override
    public <E> OptionalValue<E> createOptionalValue(Key<OptionalValue<E>> key, @Nullable E element, E defaultElement) {
        return new SpongeOptionalValue<E>(checkNotNull(key, "key"), Optional.of(defaultElement), Optional.ofNullable(element));
    }
}
