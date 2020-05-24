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

import com.google.common.reflect.TypeToken;
import com.google.inject.internal.cglib.proxy.$Factory;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataProvider;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.UnregisteredKeyException;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataStore;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.util.Constants;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class SpongeDataRegistration implements DataRegistration {

    final CatalogKey key;
    final List<Key<?>> keys;
    final Map<TypeToken, DataStore> dataStoreMap;
    final Map<Key, DataProvider> dataProviderMap;
    final PluginContainer plugin;

    SpongeDataRegistration(CatalogKey key, PluginContainer plugin, SpongeDataRegistrationBuilder builder) {
        this.key = key;
        this.keys = builder.keys;
        this.dataStoreMap = builder.dataStoreMap;
        this.dataProviderMap = builder.dataProviderMap;
        this.plugin = plugin;
    }

    @Override
    public <V extends Value<E>, E> Optional<DataProvider<V, E>> getProviderFor(Key<V> key) throws UnregisteredKeyException {
        return Optional.ofNullable(this.dataProviderMap.get(key));
    }

    @Override
    public Optional<DataStore> getDataStore(TypeToken<? extends DataHolder> token) {
        DataStore dataStore = this.dataStoreMap.get(token);
        if (dataStore != null) {
            return Optional.of(dataStore);
        }
        for (Map.Entry<TypeToken, DataStore> entry : this.dataStoreMap.entrySet()) {
            if (entry.getKey().isSupertypeOf(token)) {
                this.dataStoreMap.put(token, entry.getValue());
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }

    @Override
    public Iterable<Key<?>> getKeys() {
        return this.keys;
    }

    @Override
    public CatalogKey getKey() {
        return this.key;
    }

    @Override
    public PluginContainer getPluginContainer() {
        return this.plugin;
    }
}
