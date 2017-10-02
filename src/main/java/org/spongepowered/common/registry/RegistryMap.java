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
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * A special map for use in registry modules.
 *
 * @param <V> The value type
 */
public interface RegistryMap<V> {

    /**
     * Creates a new registry map with a default namespace of {@link Namespaces#MINECRAFT minecraft}.
     *
     * @param <V> The value type
     * @return A new registry map
     */
    static <V> RegistryMap<V> create() {
        return create(Namespaces.MINECRAFT);
    }

    /**
     * Creates a new registry map with a default namespace.
     *
     * @param defaultNamespace The default namespace
     * @param <V> The value type
     * @return A new registry map
     */
    static <V> RegistryMap<V> create(final String defaultNamespace) {
        return new RegistryMapImpl<>(defaultNamespace);
    }

    /**
     * Gets a value by its id.
     *
     * <p>If {@code id} is not namespaced it will have the default namespace prepended.</p>
     *
     * @param id The id
     * @return The value
     */
    @Nullable
    V get(final String id);

    /**
     * Gets a value by its id.
     *
     * <p>If {@code id} is not namespaced it will have the default namespace prepended.</p>
     *
     * @param id The id
     * @return The optional value
     */
    Optional<V> getOptional(final String id);

    /**
     * Inserts a mapping between the id and value.
     *
     * <p>If {@code id} is not namespaced it will have the default namespace prepended.</p>
     *
     * @param id The id
     * @param value The value
     * @return The previous value
     */
    @Nullable
    V put(final String id, final V value);

    default void putAll(final Map<String, ? extends V> that) {
        that.forEach(this::put);
    }

    /**
     * Creates a copy of this map prepared for catalog field registration.
     *
     * @return A map of ids to values
     */
    Map<String, V> forCatalogRegistration();

    /*
     * Forwarding methods
     */

    /**
     * @see Map#containsKey(Object)
     */
    boolean containsKey(final String key);

    // Use Object instead of V to avoid casting
    /**
     * @see Map#containsValue(Object)
     */
    boolean containsValue(final Object value);

    /**
     * @see Map#size()
     */
    int size();

    /**
     * @see Map#values()
     */
    Collection<V> values();

    /**
     * @see Map#entrySet()
     */
    Set<Map.Entry<String, V>> entrySet();
}
