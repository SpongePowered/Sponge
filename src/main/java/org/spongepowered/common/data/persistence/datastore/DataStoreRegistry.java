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
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataStore;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class DataStoreRegistry {

    private final DataStore NO_OP_DATASTORE = new SpongeDataStore(Collections.emptyMap(), Collections.emptyList());
    private final Multimap<Key<?>, DataStore> dataStoreByValueKey = HashMultimap.create();
    private final Multimap<ResourceKey, DataStore> dataStoreByDataStoreKey = HashMultimap.create();
    private final List<DataStore> allDataStores = new ArrayList<>();

    private final Map<LookupKey, DataStore> dataStoreCache = new ConcurrentHashMap<>();
    private final Multimap<Type, DataStore> dataStoreByTokenCache = HashMultimap.create();

    public void register(final DataStore dataStore, Iterable<Key<?>> keys) {
        keys.forEach(k -> this.dataStoreByValueKey.put(k, dataStore));
        if (dataStore instanceof SpongeCustomDataStore) {
            final ResourceKey customDataKey = ((SpongeCustomDataStore) dataStore).getCustomDataKey();
            this.dataStoreByDataStoreKey.put(customDataKey, dataStore);
        }
        this.allDataStores.add(dataStore);
        this.dataStoreCache.clear();
        this.dataStoreByTokenCache.clear();
    }

    public Collection<DataStore> getDataStores(Key<?> dataKey) {
        return this.dataStoreByValueKey.get(dataKey);
    }

    public DataStore getDataStore(final Key<?> dataKey, final TypeToken<? extends DataHolder> holderType) {
        return this.getDataStore(dataKey, holderType.getType());
    }

    public DataStore getDataStore(final Key<?> dataKey, final Type holderType) {
        return this.dataStoreCache.computeIfAbsent(new LookupKey(holderType, dataKey), this::loadDataStore);
    }

    public Optional<DataStore> getDataStore(final ResourceKey key, final Type holderType) {
        // TODO do we need caching for this too?
        final List<DataStore> dataStores = this.filterDataStoreCandidates(this.dataStoreByDataStoreKey.get(key), holderType);
        if (dataStores.size() > 1) {
            throw new IllegalStateException("Multiple data-stores registered for the same key (" + key + ") and data-holder " + holderType.toString());
        }
        return dataStores.stream().findAny();
    }

    private DataStore loadDataStore(final LookupKey lookupKey) {
        final List<DataStore> dataStores = filterDataStoreCandidates(this.dataStoreByValueKey.get(lookupKey.key), lookupKey.holderType);
        if (dataStores.size() > 1) {
            throw new IllegalStateException("Multiple data-stores registered for the same data-key (" + lookupKey.key.getKey() + ") and data-holder " + lookupKey.holderType.toString());
        }
        if (dataStores.isEmpty()) {
            dataStores.add(this.NO_OP_DATASTORE);
        }
        return dataStores.get(0);
    }

    private List<DataStore> filterDataStoreCandidates(Collection<DataStore> candidates, Type holderType) {
        return candidates.stream()
                .filter(ds -> ds.getSupportedTypes().stream().anyMatch(token -> GenericTypeReflector.isSuperType(token, holderType)))
                .collect(Collectors.toList());
    }

    public Collection<DataStore> getDataStoresForType(Class<? extends DataHolder> holderType) {
        if (!this.dataStoreByTokenCache.containsKey(holderType)) {
            for (DataStore dataStore : this.allDataStores) {
                if (dataStore.getSupportedTypes().stream().anyMatch(token -> GenericTypeReflector.isSuperType(token, holderType))) {
                    this.dataStoreByTokenCache.put(holderType, dataStore);
                }
            }
        }
        return this.dataStoreByTokenCache.get(holderType);
    }

    private static class LookupKey {

        private final Type holderType;
        private final Key<?> key;

        public LookupKey(final Type holderType, final Key<?> key) {
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
