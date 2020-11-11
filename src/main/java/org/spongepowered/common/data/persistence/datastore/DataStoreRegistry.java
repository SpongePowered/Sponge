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
package org.spongepowered.common.data.persistence.datastore;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataStore;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class DataStoreRegistry {

    private final DataStore NO_OP_DATASTORE = new SpongeDataStore(Collections.emptyMap(), Collections.emptyList());
    private final Multimap<Key<?>, DataStore> dataStores = HashMultimap.create();
    private final Map<LookupKey, DataStore> dataStoreCache = new ConcurrentHashMap<>();

    public void register(final DataStore dataStore, Iterable<Key<?>> keys) {
        keys.forEach(k -> this.dataStores.put(k, dataStore));
        this.dataStoreCache.clear();
    }

    public Collection<DataStore> getDataStores(Key<?> key) {
        return this.dataStores.get(key);
    }

    public DataStore getDataStore(Key<?> key, TypeToken<? extends DataHolder> typeToken) {
        return this.dataStoreCache.computeIfAbsent(new LookupKey(typeToken, key), this::loadDataStore);
    }

    private DataStore loadDataStore(LookupKey lookupKey) {
        final List<DataStore> dataStores = this.dataStores.get(lookupKey.key).stream()
                .filter(ds -> ds.getSupportedTokens().stream().anyMatch(token -> token.isSupertypeOf(lookupKey.holderType)))
                .collect(Collectors.toList());
        if (dataStores.size() > 1) {
            throw new IllegalStateException("Multiple data-stores registered for the same key (" + lookupKey.key.getKey() + ") and data-holder " + lookupKey.holderType.toString());
        }
        if (dataStores.isEmpty()) {
            dataStores.add(NO_OP_DATASTORE);
        }
        return dataStores.get(0);
    }

    private static class LookupKey {

        private final TypeToken<?> holderType;
        private final Key<?> key;

        public LookupKey(final TypeToken<?> holderType, final Key<?> key) {
            this.holderType = holderType;
            this.key = key;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
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

}
