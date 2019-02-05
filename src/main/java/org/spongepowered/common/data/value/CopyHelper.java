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
package org.spongepowered.common.data.value;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.weighted.WeightedTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings("unchecked")
public final class CopyHelper {

    /**
     * Creates a copy for the given object of type {@link T}.
     *
     * @param object The object
     * @param <T> The object type
     * @return The copied object
     */
    public static <T> T copy(T object) {
        checkNotNull(object, "object");
        if (object instanceof Set) {
            return (T) copySet((Set) object);
        } else if (object instanceof List) {
            return (T) copyList((List) object);
        } else if (object instanceof Map) {
            return (T) copyMap((Map) object);
        } else if (object instanceof WeightedTable) {
            return (T) copyWeightedTable((WeightedTable) object);
        } else if (object instanceof ItemStack) {
            return (T) ((ItemStack) object).copy();
        } else if (object instanceof Optional) {
            return (T) ((Optional<Object>) object).map(CopyHelper::copy);
        }
        return object;
    }

    public static <T> Set<T> copySet(Set<T> set) {
        if (set instanceof LinkedHashSet) {
            return new LinkedHashSet<>(set);
        } else if (set instanceof TreeSet) {
            return new TreeSet<>(set);
        }
        return new HashSet<>(set);
    }

    public static <T> List<T> copyList(List<T> list) {
        if (list instanceof LinkedList) {
            return new LinkedList<>(list);
        } else if (list instanceof CopyOnWriteArrayList) {
            return new CopyOnWriteArrayList<>(list);
        }
        return new ArrayList<>(list);
    }

    public static <K, V> Map<K, V> copyMap(Map<K, V> map) {
        if (map instanceof LinkedHashMap) {
            return new LinkedHashMap<>(map);
        } else if (map instanceof ConcurrentHashMap) {
            return new ConcurrentHashMap<>(map);
        } else if (map instanceof WeakHashMap) {
            return new WeakHashMap<>(map);
        }
        return new HashMap<>(map);
    }

    public static <T> WeightedTable<T> copyWeightedTable(WeightedTable<T> table) {
        final WeightedTable<T> copy = new WeightedTable<>(table.getRolls());
        copy.addAll(table.getEntries());
        return copy;
    }

    private CopyHelper() {
    }
}
