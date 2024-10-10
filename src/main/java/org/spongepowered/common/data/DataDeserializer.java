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

import io.leangen.geantyref.GenericTypeReflector;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.configurate.util.Types;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DataDeserializer {

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> BiFunction<DataView, DataQuery, Optional<T>> deserializer(final Type elementType) {
        final Class<?> rawType = GenericTypeReflector.erase(elementType);
        if (DataView.class.isAssignableFrom(rawType)) {
            return (view, dataQuery) -> (Optional<T>) view.getView(dataQuery);
        }
        if (DataSerializable.class.isAssignableFrom(rawType)) {
            return (view, dataQuery) -> (Optional<T>) view.getSerializable(dataQuery, (Class<? extends DataSerializable>) rawType);
        }
        final Optional<RegistryType<Object>> registryTypeForValue = SpongeDataManager.INSTANCE.findRegistryTypeFor(rawType);
        if (registryTypeForValue.isPresent()) {
            return (view, dataQuery) -> (Optional<T>) registryTypeForValue.flatMap(regType -> view.getRegistryValue(dataQuery, regType));
        }
        if (SpongeDataManager.INSTANCE.translator(rawType).isPresent()) {
            return (view, dataQuery) -> (Optional<T>) view.getObject(dataQuery, rawType);
        }
        if (ResourceKey.class.isAssignableFrom(rawType)) {
            return (view, dataQuery) -> (Optional<T>) view.getString(dataQuery).map(ResourceKey::resolve);
        }
        if (Set.class.isAssignableFrom(rawType)) {
            final Type listType = ((ParameterizedType) elementType).getActualTypeArguments()[0];
            return (view, dataQuery) -> (Optional<T>) DataDeserializer.deserializeList((Class<?>) listType, view, dataQuery).map(list -> new HashSet(list));
        }
        if (List.class.isAssignableFrom(rawType)) {
            final Type listType = ((ParameterizedType) elementType).getActualTypeArguments()[0];
            return (view, dataQuery) -> (Optional<T>) DataDeserializer.deserializeList((Class<?>) listType, view, dataQuery);
        }
        if (Collection.class.isAssignableFrom(rawType)) {
            throw new UnsupportedOperationException("Collection deserialization is not supported. Provide the deserializer for it.");
        }
        if (Types.isArray(elementType)) {
            final Class arrayType = GenericTypeReflector.erase(GenericTypeReflector.getArrayComponentType(elementType));
            return (view, dataQuery) -> (Optional<T>) DataDeserializer.deserializeList((Class<?>) arrayType, view, dataQuery).map(list -> DataDeserializer.listToArray(arrayType, list));
        }
        if (Map.class.isAssignableFrom(rawType)) {
            final Type[] parameterTypes = ((ParameterizedType) elementType).getActualTypeArguments();
            final Type keyType = parameterTypes[0];
            final Type valueType = parameterTypes[1];
            if (!(keyType instanceof Class)) {
                throw new UnsupportedOperationException("Unsupported map-key type " + keyType);
            }
            final Function<DataQuery, ?> keyDeserializer = DataDeserializer.mapKeyDeserializer(keyType);
            final BiFunction<DataView, DataQuery, Optional<Object>> valueDeserializer = DataDeserializer.deserializer(valueType);
            return (view, dataQuery) -> (Optional<T>) DataDeserializer.deserializeMap(view, dataQuery, keyDeserializer, valueDeserializer);
        }
        // Number Type Deserializers
        if (rawType == Long.class) {
            return (view, dataQuery) -> (Optional<T>) view.getLong(dataQuery);
        }
        if (rawType == Integer.class) {
            return (view, dataQuery) -> (Optional<T>) view.getInt(dataQuery);
        }
        if (rawType == Short.class) {
            return (view, dataQuery) -> (Optional<T>) view.getShort(dataQuery);
        }
        if (rawType == Byte.class) {
            return (view, dataQuery) -> (Optional<T>) view.getByte(dataQuery);
        }
        if (rawType == Double.class) {
            return (view, dataQuery) -> (Optional<T>) view.getDouble(dataQuery);
        }
        if (rawType == Float.class) {
            return (view, dataQuery) -> (Optional<T>) view.getFloat(dataQuery);
        }
        if (rawType == Boolean.class) {
            return (view, dataQuery) -> (Optional<T>) view.getBoolean(dataQuery);
        }
        return (view, dataQuery) -> (Optional<T>) view.get(dataQuery);
    }

    private static Function<DataQuery, ?> mapKeyDeserializer(Type keyType) {
        final Optional<RegistryType<Object>> registryTypeForKey = SpongeDataManager.INSTANCE.findRegistryTypeFor((Class) keyType);
        if (registryTypeForKey.isPresent()) {
            final Registry<Object> registry = Sponge.game().findRegistry(registryTypeForKey.get()).get();
            return key -> registry.value(ResourceKey.resolve(key.toString()));
        }
        if (((Class<?>) keyType).isEnum()) {
            return key -> Enum.valueOf(((Class<? extends Enum>) keyType), key.toString());
        }
        if (keyType == String.class) {
            return DataQuery::toString;
        }
        if (keyType == UUID.class) {
            return key -> UUID.fromString(key.toString());
        }
        if (keyType == ResourceKey.class) {
            return key -> ResourceKey.resolve(key.toString());
        }
        if (keyType == Integer.class) {
            return key -> Integer.valueOf(key.toString());
        }
        // TODO other number types?
        throw new UnsupportedOperationException("Unsupported map-key type " + keyType);
    }

    private static Optional<?> deserializeMap(final DataView view, final DataQuery dataQuery,
            final Function<DataQuery, ?> keyDeserializer,
            final BiFunction<DataView, DataQuery, Optional<Object>> valueDeserializer) {
        return view.getView(dataQuery).map(mapView -> {
            final Map<Object, Object> resultMap = new HashMap<>();
            for (final DataQuery key : mapView.keys(false)) {
                final Object mapKey = keyDeserializer.apply(key);
                final Optional<?> mapValue = valueDeserializer.apply(mapView, key);
                resultMap.put(mapKey, mapValue.get());
            }
            return resultMap;
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> Optional<List<T>> deserializeList(final Class<T> listType, final DataView view, final DataQuery dataQuery) {
        if (DataView.class.isAssignableFrom(listType)) {
            return (Optional) view.getViewList(dataQuery);
        }
        if (DataSerializable.class.isAssignableFrom(listType)) {
            return (Optional) view.getSerializableList(dataQuery, (Class<? extends DataSerializable>) listType);
        }
        final Optional<RegistryType<Object>> registryTypeFor = SpongeDataManager.INSTANCE.findRegistryTypeFor(listType);
        if (registryTypeFor.isPresent()) {
            return (Optional) view.getRegistryValueList(dataQuery, registryTypeFor.get());
        }
        if (SpongeDataManager.INSTANCE.translator(listType).isPresent()) {
            return view.getObjectList(dataQuery, listType);
        }
        return (Optional) view.getList(dataQuery);
    }

    private static <AT> AT[] listToArray(final Class<AT> componentType, final List<AT> list) {
        return list.toArray((AT[]) Array.newInstance(componentType, list.size()));
    }
}
