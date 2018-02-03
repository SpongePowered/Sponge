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
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableMapValue;
import org.spongepowered.api.data.value.mutable.MapValue;
import org.spongepowered.common.data.value.mutable.SpongeMapValue;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;

public class ImmutableSpongeMapValue<K, V> extends ImmutableSpongeValue<Map<K, V>> implements ImmutableMapValue<K, V> {

    /*
     * A constructor method to avoid unnecessary copies. INTERNAL USE ONLY!
     */
    private static <K, V> ImmutableSpongeMapValue<K, V> constructUnsafe(
            Key<? extends BaseValue<Map<K, V>>> key, Map<K, V> defaultValue, Map<K, V> actualValue) {
        return new ImmutableSpongeMapValue<>(key, defaultValue, actualValue, null);
    }

    public ImmutableSpongeMapValue(
            Key<? extends BaseValue<Map<K, V>>> key) {
        this(key, ImmutableMap.of());
    }

    public ImmutableSpongeMapValue(
            Key<? extends BaseValue<Map<K, V>>> key, Map<K, V> actualValue) {
        super(key, ImmutableMap.of(), ImmutableMap.copyOf(actualValue));
    }

    // DO NOT MODIFY THE SIGNATURE
    public ImmutableSpongeMapValue(
            Key<? extends BaseValue<Map<K, V>>> key, Map<K, V> defaultValue, Map<K, V> actualValue) {
        super(key, ImmutableMap.copyOf(defaultValue), ImmutableMap.copyOf(actualValue));
    }

    /*
     * A constructor to avoid unnecessary copies. INTERNAL USE ONLY!
     */
    protected ImmutableSpongeMapValue(
            Key<? extends BaseValue<Map<K, V>>> key, Map<K, V> defaultValue, Map<K, V> actualValue, @Nullable Void nothing) {
        super(key, defaultValue, actualValue, nothing);
    }

    @Override
    public ImmutableMapValue<K, V> with(Map<K, V> value) {
        return constructUnsafe(getKey(), this.defaultValue, ImmutableMap.copyOf(checkNotNull(value)));
    }

    @Override
    public ImmutableMapValue<K, V> transform(Function<Map<K, V>, Map<K, V>> function) {
        return constructUnsafe(getKey(), this.defaultValue,
                ImmutableMap.copyOf(checkNotNull(checkNotNull(function).apply(this.actualValue))));
    }

    @Override
    public MapValue<K, V> asMutable() {
        return new SpongeMapValue<>(getKey(), this.defaultValue, this.actualValue);
    }

    @Override
    public int size() {
        return this.actualValue.size();
    }

    @Override
    public ImmutableMapValue<K, V> with(K key, V value) {
        return constructUnsafe(getKey(), this.defaultValue,
                ImmutableMap.<K, V>builder().putAll(this.actualValue).put(checkNotNull(key), checkNotNull(value)).build());
    }

    @Override
    public ImmutableMapValue<K, V> withAll(Map<K, V> map) {
        return constructUnsafe(getKey(), this.defaultValue,
                ImmutableMap.<K, V>builder().putAll(this.actualValue).putAll(map).build());
    }

    @Override
    public ImmutableMapValue<K, V> without(K key) {
        final ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
        this.actualValue.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(key))
                .forEach(entry -> builder.put(entry.getKey(), entry.getValue()));
        return constructUnsafe(getKey(), this.defaultValue, builder.build());
    }

    @Override
    public ImmutableMapValue<K, V> withoutAll(Iterable<K> keys) {
        final ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
        this.actualValue.entrySet().stream()
                .filter(entry -> !Iterables.contains(keys, entry.getKey()))
                .forEach(entry -> builder.put(entry.getKey(), entry.getValue()));
        return constructUnsafe(getKey(), this.defaultValue, builder.build());
    }

    @Override
    public ImmutableMapValue<K, V> withoutAll(Predicate<Map.Entry<K, V>> predicate) {
        checkNotNull(predicate, "predicate");
        final ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
        this.actualValue.entrySet().stream()
                .filter(predicate)
                .forEach(entry -> builder.put(entry.getKey(), entry.getValue()));
        return constructUnsafe(getKey(), this.defaultValue, builder.build());
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

    @Override
    public Map<K, V> getNullable() {
        return this.actualValue; // Faster?
    }
}
