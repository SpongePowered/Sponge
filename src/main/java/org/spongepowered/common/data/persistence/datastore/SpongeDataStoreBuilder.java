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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataContentUpdater;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.data.persistence.DataStore;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.data.DataUtil;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.configurate.util.Types;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;


public final class SpongeDataStoreBuilder implements DataStore.Builder, DataStore.Builder.UpdaterStep, DataStore.Builder.HolderStep, DataStore.Builder.SerializersStep, DataStore.Builder.EndStep {

    private final Map<Key<?>, Tuple<BiConsumer<DataView, ?>, Function<DataView, Optional<?>>>> serializers = new IdentityHashMap<>();
    private final List<Type> dataHolderTypes = new ArrayList<>();
    private @Nullable ResourceKey key;
    private int version = 1;
    private DataContentUpdater[] updaters = new DataContentUpdater[0];

    @Override
    public <T, V extends Value<T>> SpongeDataStoreBuilder key(final Key<V> key, final DataQuery dataQuery) {
        final BiFunction<DataView, DataQuery, Optional<T>> deserializer = this.getDeserializer(key.elementType());
        return this.key(key, (view, value) -> view.set(dataQuery, value), v -> deserializer.apply(v, dataQuery));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> BiFunction<DataView, DataQuery, Optional<T>> getDeserializer(final Type elementType) {
        final Class<?> rawType = GenericTypeReflector.erase(elementType);
        if (DataView.class.isAssignableFrom(rawType)) {
            return (view, dataQuery) -> (Optional<T>) view.getView(dataQuery);
        }
        if (DataSerializable.class.isAssignableFrom(rawType)) {
            return (view, dataQuery)  -> (Optional<T>) view.getSerializable(dataQuery, (Class<? extends DataSerializable>) rawType);
        }
        final Optional<RegistryType<Object>> registryTypeForValue = SpongeDataManager.INSTANCE.findRegistryTypeFor(rawType);
        if (registryTypeForValue.isPresent()) {
            return (view, dataQuery)  -> (Optional<T>) registryTypeForValue.flatMap(regType -> view.getRegistryValue(dataQuery, regType));
        }
        if (ResourceKey.class.isAssignableFrom(rawType)) {
            return (view, dataQuery) -> (Optional<T>) view.getString(dataQuery).map(ResourceKey::resolve);
        }
        if (Sponge.game().dataManager().translator(rawType).isPresent()) {
            return (view, dataQuery)  -> (Optional<T>) view.getObject(dataQuery, rawType);
        }
        if (Set.class.isAssignableFrom(rawType)) {
            final Type listType = ((ParameterizedType) elementType).getActualTypeArguments()[0];
            return (view, dataQuery)  -> (Optional<T>) SpongeDataStoreBuilder.deserializeList((Class<?>) listType, view, dataQuery).map(list -> new HashSet(list));
        }
        if (List.class.isAssignableFrom(rawType)) {
            final Type listType = ((ParameterizedType) elementType).getActualTypeArguments()[0];
            return (view, dataQuery)  -> (Optional<T>) SpongeDataStoreBuilder.deserializeList((Class<?>) listType, view, dataQuery);
        }
        if (Collection.class.isAssignableFrom(rawType)) {
            throw new UnsupportedOperationException("Collection deserialization is not supported. Provide the deserializer for it.");
        }
        if (Types.isArray(elementType)) {
            final Class arrayType = GenericTypeReflector.erase(GenericTypeReflector.getArrayComponentType(elementType));
            return (view, dataQuery)  -> (Optional<T>) SpongeDataStoreBuilder.deserializeList((Class<?>) arrayType, view, dataQuery).map(list -> this
                    .listToArray(arrayType, list));
        }
        if (Map.class.isAssignableFrom(rawType)) {
            final Type[] parameterTypes = ((ParameterizedType) elementType).getActualTypeArguments();
            final Type keyType = parameterTypes[0];
            final Type valueType = parameterTypes[1];
            if (!(keyType instanceof Class)) {
                throw new UnsupportedOperationException("Unsupported map-key type " + keyType);
            }
            final Function<DataQuery, Optional<?>> keyDeserializer;
            final Optional<RegistryType<Object>> registryTypeForKey = SpongeDataManager.INSTANCE.findRegistryTypeFor((Class) keyType);
            if (registryTypeForKey.isPresent()) {
                keyDeserializer = key -> registryTypeForKey.flatMap(regType -> Sponge.game().registries().findRegistry(regType))
                                                           .flatMap(r -> r.findValue(ResourceKey.resolve(key.toString())));
            } else if (((Class<?>) keyType).isEnum()) {
                keyDeserializer = key -> Optional.ofNullable(Enum.valueOf(((Class<? extends Enum>) keyType), key.toString()));
            } else if (keyType == String.class) {
                keyDeserializer = key -> Optional.of(key.toString());
            } else if (keyType == UUID.class) {
                keyDeserializer = key -> Optional.of(UUID.fromString(key.toString()));
            } else if (keyType == ResourceKey.class) {
                keyDeserializer = key -> Optional.of(ResourceKey.resolve(key.toString()));
            } else {
                throw new UnsupportedOperationException("Unsupported map-key type " + keyType);
            }
            final BiFunction<DataView, DataQuery, Optional<Object>> valueDeserializer = this.getDeserializer(valueType);
            return (view, dataQuery) -> (Optional<T>) view.getView(dataQuery).map(mapView -> {
                    final Map<Object, Object> resultMap = new HashMap<>();
                    for (final DataQuery key : mapView.keys(false)) {
                        final Object mapKey = keyDeserializer.apply(key)
                                        .orElseThrow(() -> new UnsupportedOperationException("Key not found " + key + " as " + keyType));
                        final Optional<?> mapValue = valueDeserializer.apply(mapView, key);
                        resultMap.put(mapKey, mapValue.get());
                    }
                    return resultMap;
                });
        }
        return (view, dataQuery) -> (Optional<T>) view.get(dataQuery);
    }

    @SuppressWarnings("unchecked")
    private static <T> Optional<List<T>> deserializeList(final Class<T> listType, final DataView view, final DataQuery dataQuery) {
        if (DataView.class.isAssignableFrom(listType)) {
            return (Optional) view.getViewList(dataQuery);
        }
        if (DataSerializable.class.isAssignableFrom(listType)) {
            return (Optional) view.getSerializableList(dataQuery, (Class<? extends DataSerializable>) listType);
        }
        final Optional<List<Object>> fromRegistry = SpongeDataManager.INSTANCE.findRegistryTypeFor(listType)
                .flatMap(regType -> view.getRegistryValueList(dataQuery, regType));
        if (fromRegistry.isPresent()) {
            return (Optional) fromRegistry;
        }
        if (Sponge.game().dataManager().translator(listType).isPresent()) {
            return view.getObjectList(dataQuery, listType);
        }
        return (Optional) view.getList(dataQuery);
    }

    private <AT> AT[] listToArray(final Class<AT> componentType, final List<AT> list) {
        return list.toArray((AT[])Array.newInstance(componentType, list.size()));
    }

    public boolean isEmpty() {
        return this.serializers.isEmpty();
    }

    public List<Type> getDataHolderTypes() {
        return this.dataHolderTypes;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public <T, V extends Value<T>> SpongeDataStoreBuilder key(final Key<V> key, final BiConsumer<DataView, T> serializer, final Function<DataView, Optional<T>> deserializer) {
        if (this.key != null) {
            final DataQuery query = DataQuery.of(this.key.namespace(), this.key.value());
            final SpongeDataSerializer<T> customSerializer = new SpongeDataSerializer<>(serializer, this.version, query);
            final SpongeDataDeserializer<T> customDeserializer = new SpongeDataDeserializer<>(deserializer, this.version, query);
            this.serializers.put(key, (Tuple) Tuple.of(customSerializer, customDeserializer));
        } else {
            this.serializers.put(key, (Tuple) Tuple.of(serializer, deserializer));
        }

        return this;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public EndStep keys(final Key<?> key, final Key<?>... moreKeys) {
        this.key((Key) key, key.key().value());
        for (final Key<?> moreKey : moreKeys) {
            this.key((Key) moreKey, moreKey.key().value());
        }
        return this;
    }

    @Override
    public DataStore.Builder reset() {
        this.serializers.clear();
        this.dataHolderTypes.clear();
        this.key = null;
        this.version = 1;
        this.updaters = new DataContentUpdater[0];
        return this;
    }

    @Override
    public SpongeDataStoreBuilder holder(final TypeToken<? extends DataHolder>... typeTokens) {
        for (final TypeToken<? extends DataHolder> token : typeTokens) {
            this.dataHolderTypes.add(token.getType());
        }
        return this;
    }

    @Override
    public SpongeDataStoreBuilder holder(final Class<? extends DataHolder>... classes) {
        for (final Class<? extends DataHolder> clazz : classes) {
            this.dataHolderTypes.add(Types.requireCompleteParameters(clazz));
        }
        return this;
    }

    @Override
    public SpongeDataStoreBuilder pluginData(final ResourceKey key) {
        this.key = key;
        return this;
    }

    @Override
    public SpongeDataStoreBuilder pluginData(final ResourceKey key, final int version) {
        this.pluginData(key);
        this.version = version;
        return this;
    }

    @Override
    public HolderStep updater(final DataContentUpdater... updaters) {
        this.updaters = updaters;
        return this;
    }

    @Override
    public SpongeDataStoreBuilder vanillaData() {
        this.key = null;
        return this;
    }

    @Override
    public DataStore build() {
        return new SpongeDataStore(this.key, ImmutableMap.copyOf(this.serializers), ImmutableList.copyOf(this.dataHolderTypes), this.version, this.updaters);
    }

    public DataStore buildVanillaDataStore() {
        return new VanillaDataStore(Collections.unmodifiableMap(this.serializers), this.dataHolderTypes);
    }

    private static class SpongeDataSerializer<T> implements BiConsumer<DataView, T> {

        private final BiConsumer<DataView, T> serializer;
        private final DataQuery key;
        private final int version;

        public SpongeDataSerializer(final BiConsumer<DataView, T> serializer, final int version, final DataQuery key) {
            this.serializer = serializer;
            this.key = key;
            this.version = version;
        }

        @Override
        public void accept(final DataView view, final T v) {
            final DataView data = DataUtil.getSpongeData(view, this.key, this.version).orElse(DataContainer.createNew());
            this.serializer.accept(data, v);
            if (data.isEmpty()) {
                return;
            }
            DataUtil.setSpongeData(view, this.key, data, this.version);
        }
    }

    private static class SpongeDataDeserializer<T> implements Function<DataView, Optional<T>> {

        private final Function<DataView, Optional<T>> deserializer;
        private final DataQuery key;
        private int version;

        public SpongeDataDeserializer(final Function<DataView, Optional<T>> deserializer, final int version, final DataQuery key) {
            this.deserializer = deserializer;
            this.key = key;
            this.version = version;
        }

        @Override
        public Optional<T> apply(final DataView view) {
            return DataUtil.getSpongeData(view, this.key, this.version).flatMap(this.deserializer);
        }
    }
}
