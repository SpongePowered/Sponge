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

public class SpongeMutableMapValue<K, V> extends SpongeMapValue<K, V> implements MapValue.Mutable<K, V> {

    public SpongeMutableMapValue(Key<? extends Value<Map<K, V>>> key, Map<K, V> value) {
        super(key, value);
    }

    @Override
    public MapValue.Mutable<K, V> put(K key, V value) {
        this.value.put(key, value);
        return this;
    }

    @Override
    public MapValue.Mutable<K, V> putAll(Map<K, V> map) {
        this.value.putAll(map);
        return this;
    }

    @Override
    public MapValue.Mutable<K, V> remove(K key) {
        this.value.remove(key);
        return this;
    }

    @Override
    public MapValue.Mutable<K, V> removeAll(Iterable<K> keys) {
        keys.forEach(this::remove);
        return this;
    }

    @Override
    public MapValue.Mutable<K, V> removeAll(Predicate<Map.Entry<K, V>> predicate) {
        this.value.entrySet().removeIf(predicate);
        return this;
    }

    @Override
    public MapValue.Mutable<K, V> set(Map<K, V> value) {
        this.value = checkNotNull(value, "value");
        return this;
    }

    @Override
    public MapValue.Mutable<K, V> transform(Function<Map<K, V>, Map<K, V>> function) {
        return set(checkNotNull(function, "function").apply(get()));
    }

    @Override
    public MapValue.Mutable<K, V> copy() {
        return new SpongeMutableMapValue<>(this.key, CopyHelper.copyMap(this.value));
    }

    @Override
    public MapValue.Immutable<K, V> asImmutable() {
        return new SpongeImmutableMapValue<>(this.key, CopyHelper.copyMap(this.value));
    }
}
