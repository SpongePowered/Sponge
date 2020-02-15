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

import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.MapValue;
import org.spongepowered.common.data.copy.CopyHelper;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public final class MutableSpongeMapValue<K, V> extends AbstractMutableSpongeValue<Map<K, V>> implements MapValue.Mutable<K, V> {

    public MutableSpongeMapValue(Key<? extends MapValue<K, V>> key, Map<K, V> element) {
        super(key, element);
    }

    @Override
    public Key<? extends MapValue<K, V>> getKey() {
        //noinspection unchecked
        return (Key<? extends MapValue<K, V>>) super.getKey();
    }

    @Override
    public int size() {
        return this.get().size();
    }

    @Override
    public boolean containsKey(K key) {
        return this.get().containsKey(key);
    }

    @Override
    public boolean containsValue(V value) {
        return this.get().containsValue(value);
    }

    @Override
    public Set<K> keySet() {
        return this.get().keySet();
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return this.get().entrySet();
    }

    @Override
    public Collection<V> values() {
        return this.get().values();
    }

    @Override
    public MapValue.Mutable<K, V> set(Map<K, V> value) {
        super.set(value);
        return this;
    }

    @Override
    public MapValue.Mutable<K, V> put(K key, V value) {
        this.modifyMap(map -> map.put(key, value));
        return this;
    }

    @Override
    public MapValue.Mutable<K, V> putAll(Map<K, V> other) {
        this.modifyMap(map -> map.putAll(other));
        return this;
    }

    @Override
    public MapValue.Mutable<K, V> remove(K key) {
        this.modifyMap(map -> map.remove(key));
        return this;
    }

    @Override
    public MapValue.Mutable<K, V> removeAll(Iterable<K> keys) {
        this.modifyMap(map -> keys.forEach(map::remove));
        return this;
    }

    @Override
    public MapValue.Mutable<K, V> removeAll(Predicate<Map.Entry<K, V>> predicate) {
        this.modifyMap(map -> map.entrySet().removeIf(predicate));
        return this;
    }

    private void modifyMap(Consumer<Map<K, V>> consumer) {
        final Map<K, V> map = this.get();
        if (map instanceof ImmutableMap) {
            final Map<K, V> mutable = new LinkedHashMap<>(map);
            consumer.accept(mutable);
            this.set(ImmutableMap.copyOf(mutable));
        } else {
            consumer.accept(map);
        }
    }

    @Override
    public MapValue.Mutable<K, V> transform(Function<Map<K, V>, Map<K, V>> function) {
        return this.set(function.apply(this.get()));
    }

    @Override
    public MapValue.Mutable<K, V> copy() {
        return new MutableSpongeMapValue<>(this.getKey(), CopyHelper.copy(this.get()));
    }

    @Override
    public MapValue.Immutable<K, V> asImmutable() {
        return new ImmutableSpongeMapValue<>(this.getKey(), CopyHelper.copy(this.get()));
    }
}
