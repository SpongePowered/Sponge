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
package org.spongepowered.common.data.component;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.spongepowered.api.data.component.MappedComponent;

import java.util.Map;
import java.util.Set;
@SuppressWarnings("unchecked")
public abstract class AbstractMappedComponent<K, V, M extends MappedComponent<K, V, M>> extends SpongeAbstractComponent<M> implements MappedComponent<K, V, M> {

    protected Map<K, V> keyValueMap = Maps.newHashMap();

    protected AbstractMappedComponent(Class<M> manipulatorClass) {
        super(manipulatorClass);
    }

    @Override
    public Set<K> getKeys() {
        return ImmutableSet.copyOf(this.keyValueMap.keySet());
    }

    @Override
    public Map<K, V> asMap() {
        return ImmutableMap.copyOf(this.keyValueMap);
    }

    @Override
    public Optional<V> get(K key) {
        return Optional.<V>fromNullable(this.keyValueMap.get(checkNotNull(key)));
    }

    @Override
    public M setUnsafe(K key, V value) {
        this.keyValueMap.put(checkNotNull(key), checkNotNull(value));
        return (M) this;
    }

    @Override
    public M setUnsafe(Map<K, V> mapped) {
        this.keyValueMap = Maps.newHashMap();
        for (Map.Entry<K, V> entry : checkNotNull(mapped).entrySet()) {
            setUnsafe(entry.getKey(), entry.getValue());
        }
        return (M) this;
    }

    @Override
    public M remove(K key) {
        this.keyValueMap.remove(checkNotNull(key));
        return (M) this;
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hashCode(this.keyValueMap);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        final AbstractMappedComponent other = (AbstractMappedComponent) obj;
        return Objects.equal(this.keyValueMap, other.keyValueMap);
    }

    @Override
    public M reset() {
        this.keyValueMap = Maps.newHashMap();
        return (M) this;
    }
}
