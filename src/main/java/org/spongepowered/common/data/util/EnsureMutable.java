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
package org.spongepowered.common.data.util;

import net.minecraft.util.NonNullList;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.common.util.NonNullArrayList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A class with utilities to ensure that certain objects are mutable.
 */
public final class EnsureMutable {

    public static <E> Collection<E> ensureMutable(Collection<E> collection) {
        if (collection instanceof List) {
            return ensureMutable((List<E>) collection);
        }
        if (collection instanceof Set) {
            return ensureMutable((Set<E>) collection);
        }
        if (collection instanceof WeightedTable) {
            //noinspection unchecked,rawtypes
            return ensureMutable((WeightedTable) collection);
        }
        return new ArrayList<>(collection);
    }

    public static <T> WeightedTable<T> ensureMutable(WeightedTable<T> table) {
        final Class<?> type = table.getClass();
        if (type == WeightedTable.class) {
            return table;
        }
        final WeightedTable<T> copy = new WeightedTable<>(table.getRolls());
        copy.addAll(table);
        return copy;
    }

    public static <E> List<E> ensureMutable(List<E> list) {
        final Class<?> type = list.getClass();
        if (type == ArrayList.class ||
                type == LinkedList.class ||
                type == CopyOnWriteArrayList.class ||
                type == Stack.class ||
                type == Vector.class ||
                type == NonNullArrayList.class ||
                type == NonNullList.class) {
            return list;
        }
        return new ArrayList<>(list);
    }

    public static <E> Set<E> ensureMutable(Set<E> set) {
        final Class<?> type = set.getClass();
        if (type == HashSet.class ||
                type == LinkedHashSet.class ||
                type == ConcurrentSkipListSet.class) {
            return set;
        }
        return new LinkedHashSet<>(set);
    }

    public static <K, V> Map<K, V> ensureMutable(Map<K, V> map) {
        final Class<?> type = map.getClass();
        if (type == HashMap.class ||
                type == LinkedHashMap.class ||
                type == TreeMap.class ||
                type == ConcurrentHashMap.class) {
            return map;
        }
        return new LinkedHashMap<>(map);
    }

    private EnsureMutable() {
    }
}
