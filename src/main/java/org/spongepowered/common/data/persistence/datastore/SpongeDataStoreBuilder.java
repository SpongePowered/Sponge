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

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.data.persistence.DataStore;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.util.Constants;

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

import javax.annotation.Nullable;

public class SpongeDataStoreBuilder implements DataStore.Builder, DataStore.Builder.HolderStep, DataStore.Builder.SerializersStep,
        DataStore.Builder.EndStep {

    private final Map<Key<?>, Tuple<BiConsumer<DataView, ?>, Function<DataView, Optional<?>>>> serializers = new IdentityHashMap<>();
    private final List<TypeToken<? extends DataHolder>> dataHolderTypes = new ArrayList<>();
    @Nullable private ResourceKey key;

    @Override
    @SuppressWarnings("unchecked")
    public <T> SpongeDataStoreBuilder key(Key<? extends Value<T>> key, DataQuery dataQuery) {
        final TypeToken<?> elementToken = key.getElementToken();
        final BiFunction<DataView, DataQuery, Optional<T>> deserializer = this.getDeserializer(elementToken);
        return this.key(key, (view, value) -> view.set(dataQuery, value), v -> deserializer.apply(v, dataQuery));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
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
            deserializer = (view, dataQuery)  -> (Optional<T>) SpongeDataStoreBuilder.deserializeList((Class<?>) listType, view, dataQuery).map(list -> new HashSet(list));
        } else if (elementToken.isSubtypeOf(List.class)) {
            final Type listType = ((ParameterizedType) elementToken.getType()).getActualTypeArguments()[0];
            deserializer = (view, dataQuery)  -> (Optional<T>) SpongeDataStoreBuilder.deserializeList((Class<?>) listType, view, dataQuery);
        } else if (elementToken.isSubtypeOf(Collection.class)) {
            throw new UnsupportedOperationException("Collection deserialization is not supported. Provide the deserializer for it.");
        } else if (elementToken.isArray()) {
            final Class arrayType = elementToken.getComponentType().getRawType();
            deserializer = (view, dataQuery)  -> (Optional<T>) SpongeDataStoreBuilder.deserializeList((Class<?>) arrayType, view, dataQuery).map(list -> listToArray(arrayType, list));
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
            } else if (keyType == String.class) {
                keyDeserializer = key -> Optional.of(key.toString());
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

    @SuppressWarnings("unchecked")
    private static <T> Optional<List<T>> deserializeList(Class<T> listType, DataView view, DataQuery dataQuery) {
        if (DataView.class.isAssignableFrom(listType)) {
            return (Optional) view.getViewList(dataQuery);
        }
        if (DataSerializable.class.isAssignableFrom(listType)) {
            return (Optional) view.getSerializableList(dataQuery, (Class<? extends DataSerializable>) listType);
        }
        if (CatalogType.class.isAssignableFrom(listType)) {
            return (Optional) view.getCatalogTypeList(dataQuery, (Class<? extends CatalogType>) listType);
        }
        if (SpongeDataManager.getInstance().getTranslator(listType).isPresent()) {
            return view.getObjectList(dataQuery, listType);
        }
        return (Optional) view.getList(dataQuery);
    }

    private <AT> AT[] listToArray(Class<AT> componentType, List<AT> list) {
        return list.toArray((AT[])Array.newInstance(componentType, list.size()));
    }

    public boolean isEmpty() {
        return this.serializers.isEmpty();
    }

    public List<TypeToken<? extends DataHolder>> getDataHolderTypes() {
        return this.dataHolderTypes;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public <T> SpongeDataStoreBuilder key(Key<? extends Value<T>> key, BiConsumer<DataView, T> serializer, Function<DataView, Optional<T>> deserializer) {
        if (this.key != null) {
            this.serializers.put(key, (Tuple) Tuple.of(new CustomDataSerializer<>(serializer, this.key.toString()), new CustomDataDeserializer<>(deserializer, this.key.toString())));
        } else {
            this.serializers.put(key, (Tuple) Tuple.of(serializer, deserializer));
        }

        return this;
    }

    @Override
    public DataStore.Builder reset() {
        this.serializers.clear();
        this.dataHolderTypes.clear();
        this.key = null;
        return this;
    }

    @Override
    public SpongeDataStoreBuilder holder(TypeToken<? extends DataHolder>... typeTokens) {
        this.dataHolderTypes.addAll(Arrays.asList(typeTokens));
        return this;
    }

    @Override
    public SpongeDataStoreBuilder holder(Class<? extends DataHolder>... types) {
        for (Class<? extends DataHolder> type : types) {
            this.dataHolderTypes.add(TypeToken.of(type));
        }
        return this;
    }

    @Override
    public SpongeDataStoreBuilder pluginData(ResourceKey key) {
        this.key = key;
        return this;
    }

    @Override
    public SpongeDataStoreBuilder vanillaData() {
        this.key = null;
        return this;
    }

    @Override
    public DataStore build() {
        return new SpongeDataStore(Collections.unmodifiableMap(this.serializers), this.dataHolderTypes);
    }

    public DataStore buildVanillaDataStore() {
        return new SpongeDataStore(Collections.unmodifiableMap(this.serializers), this.dataHolderTypes);
    }

    private static class CustomDataSerializer<T> implements BiConsumer<DataView, T> {

        private final BiConsumer<DataView, T> serializer;
        private final String key;

        public CustomDataSerializer(BiConsumer<DataView, T> serializer, String key) {
            this.serializer = serializer;
            this.key = key;
        }

        @Override
        public void accept(DataView view, T v) {

            final DataContainer internalData = DataContainer.createNew();
            serializer.accept(internalData, v);

            if (internalData.isEmpty()) {
                return;
            }

            final DataView forgeData = view.getView(Constants.Forge.ROOT).orElseGet(() -> view.createView(Constants.Forge.ROOT));
            final DataView spongeData = forgeData.getView(Constants.Sponge.SPONGE_ROOT).orElseGet(() -> forgeData.createView(Constants.Sponge.SPONGE_ROOT));

            List<DataView> viewList = spongeData.getViewList(Constants.Sponge.CUSTOM_MANIPULATOR_LIST).orElse(null);
            if (viewList == null) {
                viewList = new ArrayList<>();
                spongeData.set(Constants.Sponge.CUSTOM_MANIPULATOR_LIST, viewList);
            }
            final Optional<DataView> existingContainer =
                    viewList.stream().filter(potentialContainer -> potentialContainer.getString(Constants.Sponge.DATA_ID)
                            .map(id -> id.equals(this.key)).orElse(false))
                    
                    .findFirst();
            final DataView manipulatorContainer;
            if (existingContainer.isPresent()) {
                manipulatorContainer = existingContainer.get();
            } else {
                manipulatorContainer = DataContainer.createNew();
                viewList.add(manipulatorContainer);
            }

            manipulatorContainer.set(Queries.CONTENT_VERSION, Constants.Sponge.CURRENT_CUSTOM_DATA)
                    .set(Constants.Sponge.DATA_ID, this.key)
                    .set(Constants.Sponge.INTERNAL_DATA, internalData);

            spongeData.set(Constants.Sponge.CUSTOM_MANIPULATOR_LIST, viewList);
        }
    }

    private static class CustomDataDeserializer<T> implements Function<DataView, Optional<T>> {

        private final Function<DataView, Optional<T>> deserializer;
        private final String key;

        public CustomDataDeserializer(Function<DataView, Optional<T>> deserializer, String key) {
            this.deserializer = deserializer;
            this.key = key;
        }

        @Override
        public Optional<T> apply(DataView view) {
            return view.getView(Constants.Forge.ROOT)
                    .flatMap(v -> v.getView(Constants.Sponge.SPONGE_ROOT))
                    .flatMap(v -> v.getViewList(Constants.Sponge.CUSTOM_MANIPULATOR_LIST))
                    .flatMap(manipulators -> manipulators.stream().filter(v -> v.getString(Constants.Sponge.DATA_ID)
                            .map(id -> id.equals(this.key.toString())).orElse(false))
                            .findFirst()
                            .map(v -> v.getView(Constants.Sponge.INTERNAL_DATA).orElse(DataContainer.createNew()))
                            .flatMap(deserializer));
        }
    }
}
