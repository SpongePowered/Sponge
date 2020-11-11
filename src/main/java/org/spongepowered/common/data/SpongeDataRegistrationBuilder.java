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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataProvider;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.DuplicateDataStoreException;
import org.spongepowered.api.data.DuplicateProviderException;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataStore;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.util.SpongeCatalogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class SpongeDataRegistrationBuilder extends SpongeCatalogBuilder<DataRegistration, DataRegistration.Builder> implements DataRegistration.Builder{

    Multimap<Key, DataProvider> dataProviderMap = HashMultimap.create();
    Map<TypeToken, DataStore> dataStoreMap = new IdentityHashMap<>();
    List<Key<?>> keys = new ArrayList<>();

    @Override
    public DataRegistration.Builder store(DataStore store) throws DuplicateDataStoreException {
        for (TypeToken<? extends DataHolder> holderType : store.getSupportedTokens()) {
            this.dataStoreMap.put(holderType, store);
        }
        return this;
    }

    @Override
    public DataRegistration.Builder provider(DataProvider<?, ?> provider) throws DuplicateProviderException {
        this.dataProviderMap.put(provider.getKey(), provider);
        return this;
    }

    @Override
    public DataRegistration.Builder dataKey(Key<?> key) {
        this.keys.add(key);
        return this;
    }

    @Override
    public DataRegistration.Builder dataKey(Key<?> key, Key<?>... others) {
        this.keys.add(key);
        Collections.addAll(this.keys, others);
        return this;
    }

    @Override
    public DataRegistration.Builder dataKey(Iterable<Key<?>> keys) {
        keys.forEach(this.keys::add);
        return this;
    }

    @Override
    protected DataRegistration build(ResourceKey key) {
        return new SpongeDataRegistration(key, SpongeCommon.getActivePlugin(), this);
    }

    @Override
    public SpongeDataRegistrationBuilder reset() {
        super.reset();
        this.dataProviderMap = HashMultimap.create();
        this.dataStoreMap = new IdentityHashMap<>();
        this.keys = new ArrayList<>();
        return this;
    }
}
