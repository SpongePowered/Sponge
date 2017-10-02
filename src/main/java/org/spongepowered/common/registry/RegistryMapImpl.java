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
package org.spongepowered.common.registry;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

final class RegistryMapImpl<V> extends HashMap<String, V> implements RegistryMap<V> {

    private final String defaultNamespace;

    RegistryMapImpl(final String defaultNamespace) {
        this.defaultNamespace = defaultNamespace;
    }

    @Override
    public V get(final String id) {
        return super.get(Namespaces.namespacedId(this.defaultNamespace, id));
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public Optional<V> getOptional(final String id) {
        return Optional.ofNullable(this.get(id));
    }

    @Override
    public V put(final String key, final V value) {
        return super.put(Namespaces.toLowerCase(key), value);
    }

    @Override
    public boolean containsKey(final String key) {
        return super.containsKey(Namespaces.toLowerCase(key));
    }

    @Override
    public Collection<V> values() {
        return Collections.unmodifiableCollection(super.values());
    }

    @Override
    public Map<String, V> forCatalogRegistration() {
        final Map<String, V> map = new HashMap<>(this.size());
        for (Map.Entry<String, V> entry : this.entrySet()) {
            map.put(Namespaces.prepareForField(entry.getKey()), entry.getValue());
        }
        return map;
    }
}
