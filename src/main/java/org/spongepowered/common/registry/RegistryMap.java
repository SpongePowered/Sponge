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

import org.apache.commons.lang3.text.WordUtils;
import org.spongepowered.api.CatalogKey;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A special map for use in registry modules.
 *
 * @param <V> The value type
 */
public final class RegistryMap<V> extends HashMap<CatalogKey, V> {

    /**
     * Gets a value by its key.
     *
     * @param key The key
     * @return The optional value
     */
    public Optional<V> getOptional(final CatalogKey key) {
        return Optional.ofNullable(this.get(key));
    }

    /**
     * Creates a copy of this map prepared for catalog field registration.
     *
     * @return A map of keys to values
     */
    public Map<String, V> forCatalogRegistration() {
        final Map<String, V> map = new HashMap<>(this.size());
        for (final Map.Entry<CatalogKey, V> entry : this.entrySet()) {
            map.put(entry.getKey().getValue(), entry.getValue());
        }
        return map;
    }

    @Override
    public Collection<V> values() {
        return Collections.unmodifiableCollection(super.values());
    }

    public static String name(final CatalogKey key) {
        return WordUtils.capitalize(key.getValue(), '_');
    }
}
