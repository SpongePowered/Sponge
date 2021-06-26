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
package org.spongepowered.common.util;

import static com.google.common.base.Preconditions.checkNotNull;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.data.value.MergeFunction;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import net.minecraft.core.NonNullList;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
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
import java.util.function.Supplier;

public final class DataUtil {

    public static DataView checkDataExists(final DataView dataView, final DataQuery query) throws InvalidDataException {
        if (!checkNotNull(dataView).contains(checkNotNull(query))) {
            throw new InvalidDataException("Missing data for query: " + query.asString('.'));
        }
        return dataView;
    }

    public static ServerLocation getLocation(final DataView view, final boolean castToInt) {
        final ResourceKey world = view.getResourceKey(Queries.WORLD_KEY).orElseThrow(DataUtil.dataNotFound());
        final Vector3d pos = DataUtil.getPosition3d(view, null);
        if (castToInt) {
            return ServerLocation.of(SpongeCommon.game().server().worldManager().world(world).orElseThrow(DataUtil.dataNotFound()), pos.toInt());
        }
        return ServerLocation.of(SpongeCommon.game().server().worldManager().world(world).orElseThrow(DataUtil.dataNotFound()), pos);
    }

    public static Vector3i getPosition3i(final DataView view) {
        DataUtil.checkDataExists(view, Constants.Sponge.SNAPSHOT_WORLD_POSITION);
        final DataView internal = view.getView(Constants.Sponge.SNAPSHOT_WORLD_POSITION).orElseThrow(DataUtil.dataNotFound());
        final int x = internal.getInt(Queries.POSITION_X).orElseThrow(DataUtil.dataNotFound());
        final int y = internal.getInt(Queries.POSITION_Y).orElseThrow(DataUtil.dataNotFound());
        final int z = internal.getInt(Queries.POSITION_Z).orElseThrow(DataUtil.dataNotFound());
        return new Vector3i(x, y, z);
    }

    private static Supplier<InvalidDataException> dataNotFound() {
        return () -> new InvalidDataException("not found");
    }

    public static Vector3d getPosition3d(final DataView view, final @Nullable DataQuery query) {
        final DataView internal = query == null ? view : view.getView(query).orElseThrow(DataUtil.dataNotFound());
        final double x = internal.getDouble(Queries.POSITION_X).orElseThrow(DataUtil.dataNotFound());
        final double y = internal.getDouble(Queries.POSITION_Y).orElseThrow(DataUtil.dataNotFound());
        final double z = internal.getDouble(Queries.POSITION_Z).orElseThrow(DataUtil.dataNotFound());
        return new Vector3d(x, y, z);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <E> Collection<E> ensureMutable(Collection<E> collection) {
        if (collection instanceof List) {
            return DataUtil.ensureMutable((List<E>) collection);
        }
        if (collection instanceof Set) {
            return DataUtil.ensureMutable((Set<E>) collection);
        }
        if (collection instanceof WeightedTable) {
            return DataUtil.ensureMutable((WeightedTable) collection);
        }
        return new ArrayList<>(collection);
    }

    public static <T> WeightedTable<T> ensureMutable(WeightedTable<T> table) {
        final Class<?> type = table.getClass();
        if (type == WeightedTable.class) {
            return table;
        }
        final WeightedTable<T> copy = new WeightedTable<>(table.rolls());
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

    public static double mind(final Property<Double> property) {
        //noinspection OptionalGetWithoutIsPresent
        return property.getPossibleValues().stream().mapToDouble(i -> i).min().getAsDouble();
    }

    public static double maxd(final Property<Double> property) {
        //noinspection OptionalGetWithoutIsPresent
        return property.getPossibleValues().stream().mapToDouble(i -> i).max().getAsDouble();
    }

    public static int mini(final IntegerProperty property) {
        //noinspection OptionalGetWithoutIsPresent
        return property.getPossibleValues().stream().mapToInt(i -> i).min().getAsInt();
    }

    public static int maxi(final IntegerProperty property) {
        //noinspection OptionalGetWithoutIsPresent
        return property.getPossibleValues().stream().mapToInt(i -> i).max().getAsInt();
    }

    public static <E, V extends Value<E>> E merge(MergeFunction function, Key<V> key,
            @org.checkerframework.checker.nullness.qual.Nullable E original, @org.checkerframework.checker.nullness.qual.Nullable E replacement) {
        @org.checkerframework.checker.nullness.qual.Nullable final V originalValue = original == null ? null : Value.genericImmutableOf(key, original);
        @org.checkerframework.checker.nullness.qual.Nullable final V value = replacement == null ? null : Value.genericImmutableOf(key, replacement);
        return checkNotNull(function.merge(originalValue, value), "merged").get();
    }

    public static <E, V extends Value<E>> E merge(MergeFunction function, Key<V> key,
            Supplier<@org.checkerframework.checker.nullness.qual.Nullable E> original, Supplier<@org.checkerframework.checker.nullness.qual.Nullable E> replacement) {
        if (function == MergeFunction.ORIGINAL_PREFERRED) {
            return original.get();
        } else if (function == MergeFunction.REPLACEMENT_PREFERRED) {
            return replacement.get();
        }
        @org.checkerframework.checker.nullness.qual.Nullable final E originalElement = original.get();
        @org.checkerframework.checker.nullness.qual.Nullable final E replacementElement = replacement.get();
        @org.checkerframework.checker.nullness.qual.Nullable final V originalValue = originalElement == null ? null : Value.genericImmutableOf(key, originalElement);
        @org.checkerframework.checker.nullness.qual.Nullable final V replacementValue = replacementElement == null ? null : Value.genericImmutableOf(key, replacementElement);
        return checkNotNull(function.merge(originalValue, replacementValue), "merged").get();
    }

    private DataUtil() {
    }
}
