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
package org.spongepowered.common.data.persistence;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.data.persistence.DataStore;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.data.SpongeDataManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class SpongeDataStoreBuilder implements DataStore.Builder {

    private Map<Key<?>, Tuple<BiConsumer<DataView, ?>, Function<DataView, Optional<?>>>> serializers = new IdentityHashMap<>();
    private List<TypeToken<? extends DataHolder>> dataHolderTypes = new ArrayList<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T> DataStore.Builder key(Key<? extends Value<T>> key, DataQuery dataQuery) {
        final TypeToken<?> elementToken = key.getElementToken();
        final Class<?> rawType = elementToken.getRawType();
        final Function<DataView, Optional<T>> deserializer;
        if (elementToken.isSubtypeOf(DataView.class)) {
            deserializer = view -> (Optional<T>) view.getView(dataQuery);
        } else if (elementToken.isSubtypeOf(DataSerializable.class)) {
            deserializer = view -> (Optional<T>) view.getSerializable(dataQuery, (Class<? extends DataSerializable>) rawType);
        } else if (elementToken.isSubtypeOf(CatalogType.class)) {
            deserializer = view -> (Optional<T>) view.getCatalogType(dataQuery, ((Class<? extends CatalogType>) rawType));
        } else if (SpongeDataManager.getInstance().getTranslator(rawType).isPresent()) {
            deserializer = view -> (Optional<T>) view.getObject(dataQuery, rawType);
        } else if (elementToken.isSubtypeOf(Collection.class)) {
            throw new UnsupportedOperationException("Collection deserialization is not supported. Provide the deserializer for it.");
        } else if (elementToken.isSubtypeOf(Map.class)) {
            throw new UnsupportedOperationException("Map deserialization is not supported. Provide the deserializer for it.");
        } else if (elementToken.isArray()) {
            throw new UnsupportedOperationException("Array deserialization is not supported. Provide the deserializer for it.");
        } else {
            deserializer = view -> (Optional<T>) view.get(dataQuery);
        }

        return this.key(key, (view, value) -> view.set(dataQuery, value), deserializer);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public <T> DataStore.Builder key(Key<? extends Value<T>> key, BiConsumer<DataView, T> serializer, Function<DataView, Optional<T>> deserializer) {
        this.serializers.put(key, (Tuple) Tuple.of(serializer, deserializer));
        return this;
    }

    @Override
    public DataStore.Builder reset() {
        this.serializers.clear();
        this.dataHolderTypes.clear();
        return this;
    }

    @Override
    public DataStore.Builder holder(TypeToken<? extends DataHolder>... typeTokens) {
        this.dataHolderTypes.addAll(Arrays.asList(typeTokens));
        return this;
    }

    @Override
    public DataStore build() {
        return new SpongeDataStore(Collections.unmodifiableMap(this.serializers), this.dataHolderTypes);
    }
}
