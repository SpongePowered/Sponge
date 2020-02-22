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
package org.spongepowered.common.data.provider;

import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.data.DataProvider;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.data.key.SpongeKey;

import java.util.Collection;
import java.util.Map;

public final class DataProviderLookup {

    private final Map<Key<?>, DataProvider<?,?>> providerMap;

    DataProviderLookup(Map<Key<?>, DataProvider<?, ?>> providerMap) {
        this.providerMap = ImmutableMap.copyOf(providerMap);
    }

    /**
     * Gets all the non-empty delegate {@link DataProvider}s.
     *
     * @return The delegate data providers
     */
    public Collection<DataProvider<?,?>> getAllProviders() {
        return this.providerMap.values();
    }

    /**
     * Gets the delegate {@link DataProvider} for the given {@link Key}.
     *
     * @param key The key
     * @param <V> The value type
     * @param <E> The element type
     * @return The delegate provider
     */
    public <V extends Value<E>, E> DataProvider<V, E> getProvider(Key<V> key) {
        //noinspection unchecked
        return (DataProvider<V, E>) this.providerMap.getOrDefault(key, ((SpongeKey<V, E>) key).getEmptyDataProvider());
    }
}
