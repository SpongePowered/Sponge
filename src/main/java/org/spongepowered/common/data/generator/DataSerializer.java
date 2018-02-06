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
package org.spongepowered.common.data.generator;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.DataBuilder;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.util.Coerce;
import org.spongepowered.api.util.weighted.WeightedTable;

import java.lang.reflect.Array;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

// TODO: Merge into DataView/DataContainer?
@SuppressWarnings("unchecked")
public final class DataSerializer {

    private static final TypeVariable<?> mapKeyVariable = Map.class.getTypeParameters()[0];
    private static final TypeVariable<?> mapValueVariable = Map.class.getTypeParameters()[1];
    private static final TypeVariable<?> collectionElementVariable = Set.class.getTypeParameters()[0];
    private static final TypeVariable<?> optionalElementVariable = Optional.class.getTypeParameters()[0];

    public static <E> Object serialize(E object, TypeToken<E> typeToken) {
        final Class<?> raw = typeToken.getRawType();
        if (raw.isPrimitive() ||
                raw == Integer.class ||
                raw == String.class ||
                raw == Byte.class ||
                raw == Short.class ||
                raw == Double.class ||
                raw == Long.class ||
                raw == Float.class ||
                raw == Character.class ||
                raw == Boolean.class) {
            return object;
        } else if (Map.class.isAssignableFrom(raw)) {
            final TypeToken keyType = typeToken.resolveType(mapKeyVariable);
            final TypeToken valueType = typeToken.resolveType(mapValueVariable);
            final Map<?,?> map = (Map<?, ?>) object;
            final Map newMap = new HashMap();
            for (Map.Entry<?,?> entry : map.entrySet()) {
                newMap.put(
                        serialize(entry.getKey(), keyType),
                        serialize(entry.getValue(), valueType));
            }
            return newMap;
        } else if (Collection.class.isAssignableFrom(raw)) {
            final Collection<?> collection = (Collection<?>) object;
            final TypeToken elementType = typeToken.resolveType(collectionElementVariable);
            final List newList = new ArrayList();
            for (Object entry : collection) {
                newList.add(serialize(entry, elementType));
            }
            return newList;
        } else if (Optional.class.isAssignableFrom(raw)) {
            final Optional optional = (Optional) object;
            if (!optional.isPresent()) {
                return "@empty@";
            }
            final TypeToken elementType = typeToken.resolveType(optionalElementVariable);
            return serialize(optional.get(), elementType);
        } else if (CatalogType.class.isAssignableFrom(raw)) {
            return ((CatalogType) object).getId();
        } else if (Enum.class.isAssignableFrom(raw)) {
            return ((Enum) object).name();
        } else if (raw.isArray()) {
            if (byte[].class.isAssignableFrom(raw) ||
                    short[].class.isAssignableFrom(raw) ||
                    int[].class.isAssignableFrom(raw) ||
                    float[].class.isAssignableFrom(raw) ||
                    long[].class.isAssignableFrom(raw) ||
                    double[].class.isAssignableFrom(raw) ||
                    boolean[].class.isAssignableFrom(raw) ||
                    char[].class.isAssignableFrom(raw)) {
                return object;
            }
            final TypeToken componentType = typeToken.getComponentType();
            checkNotNull(componentType);
            final Object[] array = (Object[]) object;
            final List list = new ArrayList<>();
            for (Object obj : array) {
                list.add(serialize(obj, componentType));
            }
            return list;
        } else if (DataSerializable.class.isAssignableFrom(raw)) {
            return ((DataSerializable) object).toContainer();
        } else {
            final Optional<DataTranslator> dataTranslator = Sponge.getDataManager().getTranslator((Class) raw);
            if (dataTranslator.isPresent()) {
                return dataTranslator.get().translate(object);
            }
        }
        throw new IllegalStateException("Cannot serialize " + object + " to " + typeToken);
    }

    public static <E> E deserialize(Object object, TypeToken<E> typeToken) {
        final Class<?> raw = typeToken.getRawType();
        if (int.class.equals(raw) || Integer.class.equals(raw)) {
            return (E) Coerce.asInteger(object)
                    .orElseThrow(() -> new IllegalStateException("Cannot convert " + object + " to int."));
        } else if (byte.class.equals(raw) || Byte.class.equals(raw)) {
            return (E) Coerce.asByte(object)
                    .orElseThrow(() -> new IllegalStateException("Cannot convert " + object + " to byte."));
        } else if (short.class.equals(raw) || Short.class.equals(raw)) {
            return (E) Coerce.asShort(object)
                    .orElseThrow(() -> new IllegalStateException("Cannot convert " + object + " to short."));
        } else if (double.class.equals(raw) || Double.class.equals(raw)) {
            return (E) Coerce.asDouble(object)
                    .orElseThrow(() -> new IllegalStateException("Cannot convert " + object + " to double."));
        } else if (float.class.equals(raw) || Float.class.equals(raw)) {
            return (E) Coerce.asFloat(object)
                    .orElseThrow(() -> new IllegalStateException("Cannot convert " + object + " to float."));
        } else if (boolean.class.equals(raw) || Boolean.class.equals(raw)) {
            return (E) Coerce.asBoolean(object)
                    .orElseThrow(() -> new IllegalStateException("Cannot convert " + object + " to boolean."));
        } else if (long.class.equals(raw) || Long.class.equals(raw)) {
            return (E) Coerce.asBoolean(object)
                    .orElseThrow(() -> new IllegalStateException("Cannot convert " + object + " to long."));
        } else if (char.class.equals(raw) || Character.class.equals(raw)) {
            return (E) Coerce.asChar(object)
                    .orElseThrow(() -> new IllegalStateException("Cannot convert " + object + " to char."));
        } else if (String.class.equals(raw)) {
            return (E) object.toString();
        } else if (Map.class.isAssignableFrom(raw)) {
            final TypeToken<?> keyType = typeToken.resolveType(mapKeyVariable);
            final TypeToken<?> valueType = typeToken.resolveType(mapValueVariable);
            final Map<?,?> map;
            if (object instanceof Map) {
                map = (Map<?, ?>) object;
            } else if (object instanceof DataView) {
                map = ((DataView) object).getValues(false);
            } else {
                throw new IllegalStateException("Cannot convert " + object + " to Map.");
            }
            final Map newMap = new HashMap();
            for (Map.Entry<?,?> entry : map.entrySet()) {
                newMap.put(
                        deserialize(entry.getKey(), keyType),
                        deserialize(entry.getValue(), valueType));
            }
            return (E) newMap;
        } else if (List.class.isAssignableFrom(raw) ||
                Set.class.isAssignableFrom(raw) ||
                WeightedTable.class.isAssignableFrom(raw)) {
            final Collection<?> collection;
            if (object instanceof Collection) {
                collection = (Collection<?>) object;
            } else {
                throw new IllegalStateException("Cannot convert " + object + " to " + raw.getSimpleName());
            }
            final TypeToken<?> elementType = typeToken.resolveType(collectionElementVariable);
            final Collection newCollection;
            if (List.class.isAssignableFrom(raw)) {
                newCollection = new ArrayList();
            } else if (Set.class.isAssignableFrom(raw)) {
                newCollection = new HashSet();
            } else {
                newCollection = new WeightedTable();
            }
            for (Object entry : collection) {
                newCollection.add(deserialize(entry, elementType));
            }
            return (E) newCollection;
        } else if (Optional.class.isAssignableFrom(raw)) {
            if (object.toString().equalsIgnoreCase("@empty@")) {
                return (E) Optional.empty();
            }
            final TypeToken<?> elementType = typeToken.resolveType(optionalElementVariable);
            return (E) Optional.of(deserialize(object, elementType));
        } else if (CatalogType.class.isAssignableFrom(raw)) {
            return (E) Sponge.getRegistry().getType((Class<CatalogType>) raw, object.toString())
                    .orElseThrow(() -> new IllegalStateException("Cannot find " + raw.getName() + " with id: " + object.toString()));
        } else if (Enum.class.isAssignableFrom(raw)) {
            final String name = object.toString();
            return (E) Arrays.stream(raw.getEnumConstants())
                    .filter(value -> ((Enum) value).name().equalsIgnoreCase(name) || value.toString().equalsIgnoreCase(name))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Failed to find a enum value with name: " + name));
        } else if (raw.isArray()) {
            if (byte[].class.isAssignableFrom(raw) ||
                    short[].class.isAssignableFrom(raw) ||
                    int[].class.isAssignableFrom(raw) ||
                    float[].class.isAssignableFrom(raw) ||
                    long[].class.isAssignableFrom(raw) ||
                    double[].class.isAssignableFrom(raw) ||
                    boolean[].class.isAssignableFrom(raw) ||
                    char[].class.isAssignableFrom(raw)) {
                return (E) object;
            }
            final TypeToken<?> componentType = typeToken.getComponentType();
            final List<?> list = (List<?>) object;
            final Object[] array = (Object[]) Array.newInstance(componentType.getRawType(), list.size());
            for (int i = 0; i < list.size(); i++) {
                array[i] = deserialize(list.get(i), componentType);
            }
            return (E) array;
        } else if (DataSerializable.class.isAssignableFrom(raw)) {
            final Optional<DataBuilder<DataSerializable>> builder =
                    Sponge.getDataManager().getBuilder((Class<DataSerializable>) raw);
            if (!builder.isPresent()) {
                throw new IllegalStateException("Builder missing for the DataSerializable: " + raw.getName());
            }
            final DataView dataView;
            if (object instanceof DataView) {
                dataView = (DataView) object;
            } else {
                throw new IllegalStateException("Cannot convert " + object + " to " + raw.getName());
            }
            return (E) builder.get().build(dataView)
                    .orElseThrow(() -> new IllegalStateException("Failed to parse the DataView into a " + raw.getName()));
        } else {
            final Optional<DataTranslator> dataTranslator = Sponge.getDataManager().getTranslator((Class) raw);
            if (dataTranslator.isPresent()) {
                final DataView dataView;
                if (object instanceof DataView) {
                    dataView = (DataView) object;
                } else {
                    throw new IllegalStateException("Cannot convert " + object + " to " + raw.getName());
                }
                return (E) dataTranslator.get().translate(dataView);
            }
        }
        throw new IllegalStateException("Cannot convert " + object + " to " + typeToken);
    }

    private DataSerializer() {
    }
}
