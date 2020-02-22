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
package org.spongepowered.common.data.copy;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings({"unchecked", "rawtypes"})
public class CopyHelper {

    public static <T> T copy(T value) {
        if (value instanceof Copyable) {
            return (T) ((Copyable) value).copy();
        }
        if (value instanceof Map) {
            return (T) copyMap((Map<?,?>) value);
        }
        if (value instanceof List) {
            return (T) copyList((List<?>) value);
        }
        return value;
    }

    public static <L extends List<E>, E> L copyList(L list) {
        final boolean copyElements;
        if (list.isEmpty()) {
            copyElements = false;
        } else {
            final E first = list.get(0);
            copyElements = CopyHelper.copy(first) == first;
        }
        if (list instanceof ImmutableList) {
            if (copyElements) {
                return (L) list.stream().map(CopyHelper::copy).collect(ImmutableList.toImmutableList());
            }
            return list;
        }
        final L copy;
        final Class<?> type = list.getClass();
        if (type == LinkedList.class) {
            if (copyElements) {
                copy = (L) new LinkedList<E>();
                list.forEach(element -> copy.add(CopyHelper.copy(element)));
            } else {
                copy = (L) new LinkedList<>(list);
            }
        } else if (type == CopyOnWriteArrayList.class) {
            if (copyElements) {
                copy = (L) new CopyOnWriteArrayList<>(list.stream()
                        .map(CopyHelper::copy)
                        .collect(Collectors.toList()));
            } else {
                copy = (L) new CopyOnWriteArrayList(list);
            }
        } else {
            if (copyElements) {
                copy = (L) new ArrayList<E>(list.size());
                list.forEach(element -> copy.add(CopyHelper.copy(element)));
            } else {
                copy = (L) new ArrayList<>(list);
            }
        }
        return copy;
    }

    public static <M extends Map<K, V>, K, V> M copyMap(M map) {
        if (map instanceof Copyable) {
            return (M) ((Copyable) map).copy();
        }
        final boolean copyEntries;
        if (map.isEmpty()) {
            copyEntries = false;
        } else {
            final Map.Entry<K, V> firstEntry = map.entrySet().iterator().next();
            copyEntries = CopyHelper.copy(firstEntry.getKey()) == firstEntry.getKey() ||
                    CopyHelper.copy(firstEntry.getValue()) == firstEntry.getValue();
        }
        if (map instanceof ImmutableMap) {
            if (copyEntries) {
                final ImmutableMap.Builder<K, V> builder = ImmutableMap.builderWithExpectedSize(map.size());
                map.forEach((key, value) -> builder.put(CopyHelper.copy(key), CopyHelper.copy(value)));
                return (M) builder.build();
            }
            return map;
        }
        final M copy;
        final Class<?> type = map.getClass();
        if (type == HashMap.class) {
            if (copyEntries) {
                copy = (M) new HashMap<K, V>();
                map.forEach((key, value) -> copy.put(CopyHelper.copy(key), CopyHelper.copy(value)));
            } else {
                copy = (M) new HashMap<>(map);
            }
        } else {
            if (copyEntries) {
                copy = (M) new LinkedHashMap<K, V>();
                map.forEach((key, value) -> copy.put(CopyHelper.copy(key), CopyHelper.copy(value)));
            } else {
                copy = (M) new LinkedHashMap<>(map);
            }
        }
        return copy;
    }

    /**
     * Creates a {@link Supplier} which creates copies
     * of the provided value, if needed.
     *
     * @param value The value to create the supplier from
     * @param <T> The value type
     * @return The constructed supplier
     */
    public static <T> Supplier<T> createSupplier(T value) {
        final T copy = copy(value);
        if (copy == value) {
            return () -> value;
        } else {
            return () -> copy(copy);
        }
    }
}
