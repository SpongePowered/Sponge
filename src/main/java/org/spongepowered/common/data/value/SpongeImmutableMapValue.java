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

import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.MapValue;
import org.spongepowered.api.data.value.Value;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class SpongeImmutableMapValue<K, V> extends SpongeMapValue<K, V> implements MapValue.Immutable<K, V> {

    public SpongeImmutableMapValue(Key<? extends Value<Map<K, V>>> key, Map<K, V> value) {
        super(key, value);
    }

    private MapValue.Immutable<K, V> withValue(Map<K, V> value) {
        return new SpongeImmutableMapValue<>(this.key, value);
    }

    @Override
    public Map<K, V> get() {
        return CopyHelper.copyMap(super.get());
    }

    @Override
    public MapValue.Immutable<K, V> with(K key, V value) {
        final Map<K, V> map = get();
        map.put(key, value);
        return withValue(map);
    }

    @Override
    public MapValue.Immutable<K, V> withAll(Map<K, V> map) {
        final Map<K, V> value = get();
        value.putAll(map);
        return withValue(value);
    }

    @Override
    public MapValue.Immutable<K, V> without(K key) {
        if (this.value.containsKey(key)) {
            return this;
        }
        final Map<K, V> map = get();
        map.remove(key);
        return withValue(map);
    }

    @Override
    public MapValue.Immutable<K, V> withoutAll(Iterable<K> keys) {
        final Map<K, V> map = get();
        keys.forEach(map::remove);
        return withValue(map);
    }

    @Override
    public MapValue.Immutable<K, V> withoutAll(Predicate<Map.Entry<K, V>> predicate) {
        final Map<K, V> map = get();
        map.entrySet().removeIf(predicate);
        return withValue(map);
    }

    @Override
    public MapValue.Immutable<K, V> with(Map<K, V> value) {
        return withValue(CopyHelper.copy(value));
    }

    @Override
    public MapValue.Immutable<K, V> transform(Function<Map<K, V>, Map<K, V>> function) {
        return with(checkNotNull(function, "function").apply(get()));
    }

    @Override
    public MapValue.Mutable<K, V> asMutable() {
        return new SpongeMutableMapValue<>(this.key, get());
    }
}
