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


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.ArrayUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.common.util.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Stream;


/**
 * Default implementation of a {@link DataView} being used in memory.
 */
public class MemoryDataView extends SpongeDataView {

    protected final Map<String, Object> map = Maps.newLinkedHashMap();

    MemoryDataView(final DataView.SafetyMode safety) {
        super(safety);
    }

    protected MemoryDataView(final DataView parent, final String key, final DataView.SafetyMode safety) {
        super(parent, key, safety);
    }

    @Override
    public Stream<String> streamRootKeys() {
        return this.map.keySet().stream();
    }

    @Override
    public Map<DataQuery, Object> values(final boolean deep) {
        final ImmutableMap.Builder<DataQuery, Object> builder = ImmutableMap.builder();
        for (final DataQuery query : this.keys(deep)) {
            final Object value = this.get(query).get();
            if (value instanceof DataView) {
                builder.put(query, ((DataView) value).values(deep));
            } else {
                builder.put(query, this.get(query).get());
            }
        }
        return builder.build();
    }

    @Override
    public Stream<Map.Entry<String, Object>> streamRootValues() {
        return this.map.entrySet().stream().map(e -> Map.entry(e.getKey(), this.transformValue(e.getValue())));
    }

    @Override
    public boolean contains(final DataQuery path) {
        Objects.requireNonNull(path, "path");
        final List<String> queryParts = path.parts();

        final String key = queryParts.get(0);
        if (queryParts.size() == 1) {
            return this.map.containsKey(key);
        }
        final Optional<DataView> subViewOptional = this.getUnsafeView(key);
        return subViewOptional.isPresent() && subViewOptional.get().contains(path.popFirst());
    }

    @Override
    public boolean contains(final DataQuery path, final DataQuery... paths) {
        Objects.requireNonNull(path, "DataQuery cannot be null!");
        Objects.requireNonNull(paths, "DataQuery varargs cannot be null!");
        if (paths.length == 0) {
            return this.contains(path);
        }
        final List<DataQuery> queries = new ArrayList<>();
        queries.add(path);
        for (final DataQuery query : paths) {
            queries.add(Objects.requireNonNull(query, "No null queries!"));
        }
        for (final DataQuery query : queries) {
            if (!this.contains(query)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Optional<Object> get(final DataQuery path) {
        Objects.requireNonNull(path, "path");
        final List<String> queryParts = path.parts();

        final int sz = queryParts.size();

        if (sz == 0) {
            return Optional.of(this);
        }

        final String key = queryParts.get(0);
        if (sz == 1) {
            final Object object = this.map.get(key);
            if (object == null) {
                return Optional.empty();
            }
            return Optional.of(this.transformValue(object));
        }
        final Optional<DataView> subViewOptional = this.getUnsafeView(key);
        if (!subViewOptional.isPresent()) {
            return Optional.empty();
        }
        final DataView subView = subViewOptional.get();
        return subView.get(path.popFirst());

    }

    private Object transformValue(final Object object) {
        if (this.safetyMode() == org.spongepowered.api.data.persistence.DataView.SafetyMode.ALL_DATA_CLONED) {
            if (object.getClass().isArray()) {
                return switch (object) {
                    case byte[] bytes -> ArrayUtils.clone(bytes);
                    case short[] shorts -> ArrayUtils.clone(shorts);
                    case int[] ints -> ArrayUtils.clone(ints);
                    case long[] longs -> ArrayUtils.clone(longs);
                    case float[] floats -> ArrayUtils.clone(floats);
                    case double[] doubles -> ArrayUtils.clone(doubles);
                    case boolean[] booleans -> ArrayUtils.clone(booleans);
                    default -> ArrayUtils.clone((Object[]) object);
                };
            }
        }

        return object;
    }

    @Override
    public DataView set(final DataQuery path, final Object value) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(value, "value");
        Preconditions.checkState(!path.parts().isEmpty(), "The path is empty");
        Preconditions.checkArgument(value != this, "Cannot set a DataView to itself.");

        if (path.parts().size() == 1) {
            return this.set0(path.parts().getFirst(), value);
        }

        return this.setChild(path, value);
    }

    @Override
    public DataView set(final DataQuery path, final String value) {
        Objects.requireNonNull(value, "value");
        return this.setScalar(path, value);
    }

    @Override
    public DataView set(final DataQuery path, final boolean value) {
        return this.setScalar(path, value);
    }

    @Override
    public DataView set(final DataQuery path, final byte value) {
        return this.setScalar(path, value);
    }

    @Override
    public DataView set(final DataQuery path, final short value) {
        return this.setScalar(path, value);
    }

    @Override
    public DataView set(final DataQuery path, final int value) {
        return this.setScalar(path, value);
    }

    @Override
    public DataView set(final DataQuery path, final long value) {
        return this.setScalar(path, value);
    }

    @Override
    public DataView set(final DataQuery path, final float value) {
        return this.setScalar(path, value);
    }

    @Override
    public DataView set(final DataQuery path, final double value) {
        return this.setScalar(path, value);
    }

    private DataView setScalar(final DataQuery path, final Object value) {
        Objects.requireNonNull(path, "path");
        Preconditions.checkState(!path.parts().isEmpty(), "The path is empty");

        if (path.parts().size() == 1) {
            this.map.put(path.parts().getFirst(), value);
            return this;
        }

        return this.setChild(path, value);
    }

    private DataView setChild(final DataQuery path, final Object value) {
        final DataQuery subQuery = DataQuery.of(path.parts().getFirst());
        final DataView subView = this.getUnsafeView(subQuery).orElseGet(() -> this.createView(subQuery));
        subView.set(path.popFirst(), value);
        return this;
    }

    @Override
    public DataView set(final String key, final Object value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        Preconditions.checkState(!key.isEmpty(), "The key is empty");
        Preconditions.checkArgument(value != this, "Cannot set a DataView to itself.");

        return this.set0(key, value);
    }

    @Override
    public DataView set(final String key, final String value) {
        Objects.requireNonNull(value, "value");
        return this.setScalar(key, value);
    }

    @Override
    public DataView set(final String key, final boolean value) {
        return this.setScalar(key, value);
    }

    @Override
    public DataView set(final String key, final byte value) {
        return this.setScalar(key, value);
    }

    @Override
    public DataView set(final String key, final short value) {
        return this.setScalar(key, value);
    }

    @Override
    public DataView set(final String key, final int value) {
        return this.setScalar(key, value);
    }

    @Override
    public DataView set(final String key, final long value) {
        return this.setScalar(key, value);
    }

    @Override
    public DataView set(final String key, final float value) {
        return this.setScalar(key, value);
    }

    @Override
    public DataView set(final String key, final double value) {
        return this.setScalar(key, value);
    }

    private DataView setScalar(final String key, final Object value) {
        Objects.requireNonNull(key, "key");
        Preconditions.checkState(!key.isEmpty(), "The key is empty");

        this.map.put(key, value);
        return this;
    }

    private DataView set0(final String key, final Object value) {
        if (value.getClass() == String.class || value.getClass() == Integer.class) {
            this.map.put(key, value);
            return this;
        }

        DataSerializer.serialize(this.safetyMode(), value, () -> this.createView(key), v ->  this.map.put(key, v));

        return this;
    }

    @Override
    public DataView remove(final DataQuery path) {
        Objects.requireNonNull(path, "path");
        final List<String> parts = path.parts();
        if (parts.size() > 1) {
            final String subKey = parts.get(0);
            final DataQuery subQuery = DataQuery.of(subKey);
            final Optional<DataView> subViewOptional = this.getUnsafeView(subQuery);
            if (!subViewOptional.isPresent()) {
                return this;
            }
            final DataView subView = subViewOptional.get();
            subView.remove(path.popFirst());
        } else {
            this.map.remove(parts.get(0));
        }
        return this;
    }

    @Override
    public DataView createView(final DataQuery path) {
        Objects.requireNonNull(path, "path");
        final List<String> queryParts = path.parts();

        final int sz = queryParts.size();

        Preconditions.checkArgument(sz != 0, "The size of the query must be at least 1");

        final String key = queryParts.get(0);

        if (sz == 1) {
            return this.createView(key);
        }
        final DataQuery subQuery = path.popFirst();
        DataView subView = (DataView) this.map.get(key);
        if (subView == null) {
            subView = new MemoryDataView(this, key, this.safetyMode());
            this.map.put(key, subView);
        }
        return subView.createView(subQuery);
    }

    @Override
    public DataView createView(final String key) {
        final DataView result = new MemoryDataView(this, key, this.safetyMode());
        this.map.put(key, result);
        return result;
    }

    @Override
    public Optional<DataView> getView(final DataQuery path) {
        return this.get(path).filter(obj -> obj instanceof DataView).map(obj -> (DataView) obj);
    }

    @Override
    public Optional<DataView> getView(final String key) {
        final Object object = this.map.get(key);
        if (object instanceof final DataView dataView) {
            return Optional.of(dataView);
        }
        return Optional.empty();
    }

    private Optional<DataView> getUnsafeView(final DataQuery path) {
        return this.get(path).filter(obj -> obj instanceof DataView).map(obj -> (DataView) obj);
    }

    private Optional<DataView> getUnsafeView(final String path) {
        final Object object = this.map.get(path);
        if (!(object instanceof DataView)) {
            return Optional.empty();
        }
        return Optional.of((DataView) object);
    }

    @Override
    public DataContainer copy() {
        final DataContainer container = new MemoryDataContainer(this.safetyMode());
        this.keys(false)
            .forEach(query ->
                this.get(query).ifPresent(obj ->
                    container.set(query, obj)
                )
            );
        return container;
    }

    @Override
    public DataContainer copy(final org.spongepowered.api.data.persistence.DataView.SafetyMode safety) {
        final DataContainer container = new MemoryDataContainer(safety);
        this.keys(false)
            .forEach(query ->
                this.get(query).ifPresent(obj ->
                    container.set(query, obj)
                )
            );
        return container;
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.map, this.currentPath());
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return super.equals(obj);
        }
        final MemoryDataView other = (MemoryDataView) obj;

        return Objects.equals(this.map.entrySet(), other.map.entrySet())
                && Objects.equals(this.currentPath(), other.currentPath());
    }

    @Override
    public String toString() {
        final StringJoiner helper = new StringJoiner(", ", MemoryDataView.class.getSimpleName() + "[", "]");
        if (!this.currentPath().toString().isEmpty()) {
            helper.add("path=" + this.currentPath());
        }
        helper.add("safety=" + this.safetyMode().name());
        return helper.add("map=" + this.map).toString();
    }
}
