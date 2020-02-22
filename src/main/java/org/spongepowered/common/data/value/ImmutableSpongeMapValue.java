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
import com.google.common.collect.Streams;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.MapValue;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.data.key.SpongeKey;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public final class ImmutableSpongeMapValue<K, V> extends AbstractImmutableSpongeValue<Map<K, V>> implements MapValue.Immutable<K, V> {

    public ImmutableSpongeMapValue(Key<? extends Value<Map<K, V>>> key,
            Map<K, V> element) {
        super(key, element);
    }

    @Override
    public SpongeKey<? extends MapValue<K, V>, Map<K, V>> getKey() {
        //noinspection unchecked
        return (SpongeKey<? extends MapValue<K, V>, Map<K, V>>) super.getKey();
    }

    @Override
    public int size() {
        return this.element.size();
    }

    @Override
    public boolean containsKey(K key) {
        return this.element.containsKey(key);
    }

    @Override
    public boolean containsValue(V value) {
        return this.element.containsValue(value);
    }

    @Override
    public Set<K> keySet() {
        return this.element.keySet();
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return this.element.entrySet();
    }

    @Override
    public Collection<V> values() {
        return this.element.values();
    }

    @Override
    public MapValue.Immutable<K, V> transform(Function<Map<K, V>, Map<K, V>> function) {
        return this.with(function.apply(this.get()));
    }

    @Override
    public MapValue.Immutable<K, V> with(Map<K, V> value) {
        return this.getKey().getValueConstructor().getImmutable(value).asImmutable();
    }

    @Override
    public MapValue.Immutable<K, V> with(K key, V value) {
        return this.modifyMap(map -> map.put(key, value));
    }

    private MapValue.Immutable<K, V> modifyMap(Consumer<Map<K, V>> consumer) {
        final Map<K, V> map;
        if (this.element instanceof LinkedHashMap) {
            map = new LinkedHashMap<>(this.element);
            consumer.accept(map);
        } else if (this.element instanceof ImmutableMap) {
            final LinkedHashMap<K, V> temp = new LinkedHashMap<>(this.element);
            consumer.accept(temp);
            map = ImmutableMap.copyOf(temp);
        } else {
            map = new HashMap<>(this.element);
            consumer.accept(map);
        }
        return this.getKey().getValueConstructor().getRawImmutable(map).asImmutable();
    }

    @Override
    public MapValue.Immutable<K, V> withAll(Map<K, V> other) {
        return this.modifyMap(map -> map.putAll(other));
    }

    @Override
    public MapValue.Immutable<K, V> without(K key) {
        if (!this.element.containsKey(key)) {
            return this;
        }
        return this.modifyMap(map -> map.remove(key));
    }

    @Override
    public MapValue.Immutable<K, V> withoutAll(Iterable<K> keys) {
        if (Streams.stream(keys).noneMatch(key -> this.element.containsKey(key))) {
            return this;
        }
        return this.modifyMap(map -> keys.forEach(map::remove));
    }

    @Override
    public MapValue.Immutable<K, V> withoutAll(Predicate<Map.Entry<K, V>> predicate) {
        return this.modifyMap(map -> map.entrySet().removeIf(predicate));
    }

    @Override
    public MapValue.Mutable<K, V> asMutable() {
        return new MutableSpongeMapValue<>(this.getKey(), this.get());
    }
}
