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

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataProvider;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DataProviderRegistry {

    private static class LookupKey {

        final Class<?> holderType;
        final Key<?> key;

        private LookupKey(Class<?> holderType, Key<?> key) {
            this.holderType = holderType;
            this.key = key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final LookupKey lookupKey = (LookupKey) o;
            return this.holderType.equals(lookupKey.holderType) &&
                    this.key.equals(lookupKey.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.holderType, this.key);
        }
    }

    private final Multimap<Key<?>, DataProvider<?, ?>> dataProviders = HashMultimap.create();
    private final LoadingCache<LookupKey, DataProvider<?,?>> dataProviderCache = Caffeine.newBuilder().build(this::loadProviders);

    private DataProvider<?,?> loadProviders(LookupKey key) {
        final List<DataProvider<?,?>> providers = this.dataProviders.get(key.key).stream()
                .filter(provider -> {
                    // Filter out data providers of which we know that they will never be relevant.
                    if (provider instanceof AbstractDataProvider.KnownHolderType) {
                        final Class<?> holderType = ((AbstractDataProvider.KnownHolderType) provider).getHolderType();
                        return holderType.isAssignableFrom(key.holderType);
                    }
                    return true;
                })
                .collect(Collectors.toList());
        if (providers.isEmpty()) {
            //noinspection unchecked,rawtypes
            return new EmptyDataProvider(key.key);
        }
        if (providers.size() == 1) {
            return providers.get(0);
        }
        //noinspection unchecked,rawtypes
        return new DelegateDataProvider(key.key, providers);
    }

    /**
     * Gets a delegate data provider for the given {@link Key}.
     *
     * @param key The key
     * @param <V> The value type
     * @param <E> The element type of the value
     * @return The delegate data provider
     */
    public <V extends Value<E>, E> DataProvider<V, E> getProvider(Key<V> key) {
        return this.getProvider(key, DataHolder.class);
    }

    /**
     * Gets a delegate data provider for the given {@link Key} and data holder type.
     *
     * @param key The key
     * @param dataHolderType The data holder type
     * @param <V> The value type
     * @param <E> The element type of the value
     * @return The delegate data provider
     */
    public <V extends Value<E>, E> DataProvider<V, E> getProvider(Key<V> key, Class<?> dataHolderType) {
        //noinspection ConstantConditions,unchecked
        return (DataProvider<V, E>) this.dataProviderCache.get(new LookupKey(dataHolderType, key));
    }

    /**
     * Registers a new {@link DataProvider}.
     *
     * @param provider The data provider
     */
    public void register(DataProvider<?,?> provider) {
        this.dataProviders.put(provider.getKey(), provider);
    }
}
