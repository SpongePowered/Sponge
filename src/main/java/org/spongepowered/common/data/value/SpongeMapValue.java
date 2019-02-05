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

import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.MapValue;
import org.spongepowered.api.data.value.Value;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public abstract class SpongeMapValue<K, V> extends SpongeValue<Map<K, V>> implements MapValue<K, V> {

    protected SpongeMapValue(Key<? extends Value<Map<K, V>>> key, Map<K, V> value) {
        super(key, value);
    }

    @Override
    public int size() {
        return this.value.size();
    }

    @Override
    public boolean containsKey(K key) {
        return this.value.containsKey(key);
    }

    @Override
    public boolean containsValue(V value) {
        return this.value.containsValue(value);
    }

    @Override
    public Set<K> keySet() {
        return get().keySet();
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return get().entrySet();
    }

    @Override
    public Collection<V> values() {
        return get().values();
    }
}
