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

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.MapValue.Immutable;
import org.spongepowered.api.data.value.MapValue.Mutable;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.data.value.mutable.SpongeMapValue;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class ImmutableSpongeMapValue<K, V> extends ImmutableSpongeValue<Map<K, V>> implements Immutable<K, V> {

    public ImmutableSpongeMapValue(Key<? extends Value<Map<K, V>>> key) {
        this(key, ImmutableMap.<K, V>of());
    }

    public ImmutableSpongeMapValue(Key<? extends Value<Map<K, V>>> key, Map<K, V> actualValue) {
        super(key, ImmutableMap.<K, V>of(), ImmutableMap.copyOf(actualValue));
    }

    @Override
    public Immutable<K, V> with(Map<K, V> value) {
        return new ImmutableSpongeMapValue<>(this.getKey(), checkNotNull(value));
    }

    @Override
    public Immutable<K, V> transform(Function<Map<K, V>, Map<K, V>> function) {
        return new ImmutableSpongeMapValue<>(this.getKey(), checkNotNull(checkNotNull(function).apply(this.actualValue)));
    }

    @Override
    public Mutable<K, V> asMutable() {
        final Map<K, V> map = Maps.newHashMap();
        map.putAll(this.actualValue);
        return new SpongeMapValue<>(this.getKey(), map);
    }

    @Override
    public int size() {
        return this.actualValue.size();
    }

    @Override
    public Immutable<K, V> with(K key, V value) {
        return new ImmutableSpongeMapValue<>(this.getKey(), ImmutableMap.<K, V>builder().putAll(this.actualValue).put(checkNotNull(key),
                                                                                                                 checkNotNull(value)).build());
    }

    @Override
    public Immutable<K, V> withAll(Map<K, V> map) {
        return new ImmutableSpongeMapValue<>(this.getKey(), ImmutableMap.<K, V>builder().putAll(this.actualValue).putAll(map).build());
    }

    @Override
    public Immutable<K, V> without(K key) {
        final ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
        this.actualValue.entrySet().stream()
            .filter(entry -> !entry.getKey().equals(key))
            .forEach(entry -> builder.put(entry.getKey(), entry.getValue()));
        return new ImmutableSpongeMapValue<>(this.getKey(), builder.build());
    }

    @Override
    public Immutable<K, V> withoutAll(Iterable<K> keys) {
        final ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
        this.actualValue.entrySet().stream()
            .filter(entry -> !Iterables.contains(keys, entry.getKey()))
            .forEach(entry -> builder.put(entry.getKey(), entry.getValue()));
        return new ImmutableSpongeMapValue<>(this.getKey(), builder.build());
    }

    @Override
    public Immutable<K, V> withoutAll(Predicate<Map.Entry<K, V>> predicate) {
        final ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
        this.actualValue.entrySet().stream()
            .filter(entry -> checkNotNull(predicate).test(entry))
            .forEach(entry -> builder.put(entry.getKey(), entry.getValue()));
        return new ImmutableSpongeMapValue<>(this.getKey(), builder.build());
    }

    @Override
    public boolean containsKey(K key) {
        return this.actualValue.containsKey(checkNotNull(key));
    }

    @Override
    public boolean containsValue(V value) {
        return this.actualValue.containsValue(checkNotNull(value));
    }

    @Override
    public ImmutableSet<K> keySet() {
        return (ImmutableSet<K>) this.actualValue.keySet();
    }

    @Override
    public ImmutableSet<Map.Entry<K, V>> entrySet() {
        return (ImmutableSet<Map.Entry<K, V>>) this.actualValue.entrySet();
    }

    @Override
    public ImmutableCollection<V> values() {
        return (ImmutableCollection<V>) this.actualValue.values();
    }
}
