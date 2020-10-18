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
package org.spongepowered.common.data;

import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataProvider;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.UnregisteredKeyException;
import org.spongepowered.api.data.persistence.DataStore;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.plugin.PluginContainer;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class SpongeDataRegistration implements DataRegistration {

    final ResourceKey key;
    final List<Key<?>> keys;
    final Map<TypeToken, DataStore> dataStoreMap;
    final Multimap<Key, DataProvider> dataProviderMap;
    final PluginContainer plugin;

    SpongeDataRegistration(ResourceKey key, PluginContainer plugin, SpongeDataRegistrationBuilder builder) {
        this.key = key;
        this.keys = builder.keys;
        this.dataStoreMap = builder.dataStoreMap;
        this.dataProviderMap = builder.dataProviderMap;
        this.plugin = plugin;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V extends Value<E>, E> Collection<DataProvider<V, E>> getProvidersFor(Key<V> key) throws UnregisteredKeyException {
        return (Collection) this.dataProviderMap.get(key);
    }

    @Override
    public Optional<DataStore> getDataStore(TypeToken<? extends DataHolder> token) {
        DataStore dataStore = this.dataStoreMap.get(token);
        if (dataStore != null) {
            return Optional.of(dataStore);
        }
        for (Map.Entry<TypeToken, DataStore> entry : this.dataStoreMap.entrySet()) {
            if (entry.getKey().isSupertypeOf(token)) {
                dataStore = entry.getValue();
                this.dataStoreMap.put(token, dataStore);
                return Optional.of(dataStore);
            }
        }
        return Optional.empty();
    }

    @Override
    public Iterable<Key<?>> getKeys() {
        return this.keys;
    }

    @Override
    public ResourceKey getKey() {
        return this.key;
    }

    @Override
    public PluginContainer getPluginContainer() {
        return this.plugin;
    }

    public Collection<DataStore> getDataStores() {
        return dataStoreMap.values();
    }

    public Collection<DataProvider> getDataProviders() {
        return this.dataProviderMap.values();
    }
}
