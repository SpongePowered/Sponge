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
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.data.persistence.DataStore;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.data.SpongeDataManager;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SpongeDataStoreBuilder implements DataStore.Builder {

    private Map<Key<?>, Tuple<BiConsumer<DataView, ?>, Function<DataView, Optional<?>>>> serializers = new IdentityHashMap<>();
    private List<TypeToken<? extends DataHolder>> dataHolderTypes = new ArrayList<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T> DataStore.Builder key(Key<? extends Value<T>> key, DataQuery dataQuery) {
        final TypeToken<?> elementToken = key.getElementToken();
        final BiFunction<DataView, DataQuery, Optional<T>> deserializer = this.getDeserializer(elementToken);
        return this.key(key, (view, value) -> view.set(dataQuery, value), v -> deserializer.apply(v, dataQuery));
    }

    public <T> BiFunction<DataView, DataQuery, Optional<T>> getDeserializer(TypeToken<?> elementToken) {
        Class<?> rawType = elementToken.getRawType();
        final BiFunction<DataView, DataQuery, Optional<T>> deserializer;
        if (elementToken.isSubtypeOf(DataView.class)) {
            deserializer = (view, dataQuery) -> (Optional<T>) view.getView(dataQuery);
        } else if (elementToken.isSubtypeOf(DataSerializable.class)) {
            deserializer = (view, dataQuery)  -> (Optional<T>) view.getSerializable(dataQuery, (Class<? extends DataSerializable>) rawType);
        } else if (elementToken.isSubtypeOf(CatalogType.class)) {
            deserializer = (view, dataQuery)  -> (Optional<T>) view.getCatalogType(dataQuery, ((Class<? extends CatalogType>) rawType));
        } else if (SpongeDataManager.getInstance().getTranslator(rawType).isPresent()) {
            deserializer = (view, dataQuery)  -> (Optional<T>) view.getObject(dataQuery, rawType);
        } else if (elementToken.isSubtypeOf(Set.class)) {
            final Type listType = ((ParameterizedType) elementToken.getType()).getActualTypeArguments()[0];
            deserializer = (view, dataQuery)  -> (Optional<T>) view.getObjectList(dataQuery, (Class<?>) listType).map(list -> new HashSet(list));
        } else if (elementToken.isSubtypeOf(List.class)) {
            final Type listType = ((ParameterizedType) elementToken.getType()).getActualTypeArguments()[0];
            deserializer = (view, dataQuery)  -> (Optional<T>) view.getObjectList(dataQuery, (Class<?>) listType);
        } else if (elementToken.isSubtypeOf(Collection.class)) {
            throw new UnsupportedOperationException("Collection deserialization is not supported. Provide the deserializer for it.");
        } else if (elementToken.isArray()) {
            final Class arrayType = elementToken.getComponentType().getRawType();
            deserializer = (view, dataQuery)  -> (Optional<T>) view.getObjectList(dataQuery, (Class<?>) arrayType).map(list -> listToArray(arrayType, list));
        } else if (elementToken.isSubtypeOf(Map.class)) {
            final Type[] parameterTypes = ((ParameterizedType) elementToken.getType()).getActualTypeArguments();
            final Type keyType = parameterTypes[0];
            final Type valueType = parameterTypes[1];
            if (!(keyType instanceof Class)) {
                throw new UnsupportedOperationException("Unsupported map-key type " + keyType);
            }
            Function<DataQuery, Optional<?>> keyDeserializer;
            if (((Class<?>) keyType).isAssignableFrom(CatalogType.class)) {
                keyDeserializer = key -> Sponge.getRegistry().getCatalogRegistry()
                        .get(((Class<? extends CatalogType>) keyType), ResourceKey.resolve(key.toString()));
            } else if (((Class<?>) keyType).isEnum()) {
                keyDeserializer = key -> Optional.ofNullable(Enum.valueOf(((Class<? extends Enum>) keyType), key.toString()));
            } else {
                throw new UnsupportedOperationException("Unsupported map-key type " + keyType);
            }
            final TypeToken<?> valueTypeToken = TypeToken.of(valueType);
            final BiFunction<DataView, DataQuery, Optional<Object>> valueDeserializer = this.getDeserializer(valueTypeToken);
            deserializer = (view, dataQuery) -> (Optional<T>) view.getView(dataQuery).map(mapView -> {
                final Map<Object, Object> resultMap = new HashMap<>();
                for (DataQuery key : mapView.getKeys(false)) {
                    Object mapKey = keyDeserializer.apply(key).orElseThrow(() -> new UnsupportedOperationException("Key not found " + key + " as " + keyType));
                    final Optional<?> mapValue = valueDeserializer.apply(mapView, key);
                    resultMap.put(mapKey, mapValue.get());
                }
                return resultMap;
            });
        } else {
            deserializer = (view, dataQuery) -> (Optional<T>) view.get(dataQuery);
        }
        return deserializer;
    }

    private <AT> AT[] listToArray(Class<AT> componentType, List<AT> list) {
        return list.toArray((AT[])Array.newInstance(componentType, list.size()));
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
