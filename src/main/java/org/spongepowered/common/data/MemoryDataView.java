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


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.ArrayUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.common.data.builder.Coerce;
import org.spongepowered.common.registry.provider.KeyProvider;
import org.spongepowered.common.util.Preconditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Default implementation of a {@link DataView} being used in memory.
 */
public class MemoryDataView implements DataView {

    protected final Map<String, Object> map = Maps.newLinkedHashMap();
    private final DataContainer container;
    private final DataView parent;
    private final DataQuery path;
    private final DataView.SafetyMode safety;

    MemoryDataView(final DataView.SafetyMode safety) {
        Preconditions.checkState(this instanceof DataContainer, "Cannot construct a root MemoryDataView without a container!");
        this.path = DataQuery.of();
        this.parent = this;
        this.container = (DataContainer) this;
        this.safety = Objects.requireNonNull(safety, "Safety mode");
    }

    private MemoryDataView(final DataView parent, final DataQuery path, final DataView.SafetyMode safety) {
        Preconditions.checkArgument(path.parts().size() >= 1, "Path must have at least one part");
        this.parent = parent;
        this.container = parent.container();
        this.path = parent.currentPath().then(path);
        this.safety = Objects.requireNonNull(safety, "Safety mode");
    }

    @Override
    public DataContainer container() {
        return this.container;
    }

    @Override
    public DataQuery currentPath() {
        return this.path;
    }

    @Override
    public String name() {
        final List<String> parts = this.path.parts();
        return parts.isEmpty() ? "" : parts.get(parts.size() - 1);
    }

    @Override
    public Optional<DataView> parent() {
        return Optional.ofNullable(this.parent);
    }

    @Override
    public Set<DataQuery> keys(final boolean deep) {
        final ImmutableSet.Builder<DataQuery> builder = ImmutableSet.builder();

        for (final Map.Entry<String, Object> entry : this.map.entrySet()) {
            builder.add(DataQuery.of(entry.getKey()));
        }
        if (deep) {
            for (final Map.Entry<String, Object> entry : this.map.entrySet()) {
                if (entry.getValue() instanceof DataView) {
                    for (final DataQuery query : ((DataView) entry.getValue()).keys(true)) {
                        builder.add(DataQuery.of(entry.getKey()).then(query));
                    }
                }
            }
        }
        return builder.build();
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
    public final boolean contains(final DataQuery path) {
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
            if (this.safety == org.spongepowered.api.data.persistence.DataView.SafetyMode.ALL_DATA_CLONED) {
                if (object.getClass().isArray()) {
                    if (object instanceof byte[]) {
                        return Optional.of(ArrayUtils.clone((byte[]) object));
                    } else if (object instanceof short[]) {
                        return Optional.of(ArrayUtils.clone((short[]) object));
                    } else if (object instanceof int[]) {
                        return Optional.of(ArrayUtils.clone((int[]) object));
                    } else if (object instanceof long[]) {
                        return Optional.of(ArrayUtils.clone((long[]) object));
                    } else if (object instanceof float[]) {
                        return Optional.of(ArrayUtils.clone((float[]) object));
                    } else if (object instanceof double[]) {
                        return Optional.of(ArrayUtils.clone((double[]) object));
                    } else if (object instanceof boolean[]) {
                        return Optional.of(ArrayUtils.clone((boolean[]) object));
                    } else {
                        return Optional.of(ArrayUtils.clone((Object[]) object));
                    }
                }
            }
            return Optional.of(object);
        }
        final Optional<DataView> subViewOptional = this.getUnsafeView(key);
        if (!subViewOptional.isPresent()) {
            return Optional.empty();
        }
        final DataView subView = subViewOptional.get();
        return subView.get(path.popFirst());

    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public DataView set(final DataQuery path, final Object value) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(value, "value");
        Preconditions.checkState(this.container != null);
        Preconditions.checkState(!path.parts().isEmpty(), "The path is empty");
        Preconditions.checkArgument(value != this, "Cannot set a DataView to itself.");

        final List<String> parts = path.parts();
        final String key = parts.get(0);
        if (parts.size() > 1) {
            final DataQuery subQuery = DataQuery.of(key);
            final Optional<DataView> subViewOptional = this.getUnsafeView(subQuery);
            final DataView subView;
            if (!subViewOptional.isPresent()) {
                this.createView(subQuery);
                subView = (DataView) this.map.get(key);
            } else {
                subView = subViewOptional.get();
            }
            subView.set(path.popFirst(), value);
            return this;
        }

        final Object serialized = DataSerializer.serialize(this.safetyMode(), value);

        Preconditions.checkArgument(this.isEmpty() || !this.equals(serialized), "Cannot insert self-referencing DataView!");
        if (serialized instanceof DataView) {
            // always have to copy a data view to avoid overwriting existing
            // views and to set the interior path correctly.
            final Collection<DataQuery> valueKeys = ((DataView) serialized).keys(true);
            if (!valueKeys.isEmpty()) {
                for (final DataQuery oldKey : valueKeys) {
                    this.set(path.then(oldKey), ((DataView) serialized).get(oldKey).get());
                }
            } else {
                this.createView(path);
            }
        } else {
            this.map.put(key, serialized);
        }

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
        final DataQuery keyQuery = DataQuery.of(key);

        if (sz == 1) {
            final DataView result = new MemoryDataView(this, keyQuery, this.safety);
            this.map.put(key, result);
            return result;
        }
        final DataQuery subQuery = path.popFirst();
        DataView subView = (DataView) this.map.get(key);
        if (subView == null) {
            subView = new MemoryDataView(this.parent, keyQuery, this.safety);
            this.map.put(key, subView);
        }
        return subView.createView(subQuery);
    }

    @Override
    public DataView createView(final DataQuery path, final Map<?, ?> map) {
        Objects.requireNonNull(path, "path");
        final DataView section = this.createView(path);

        for (final Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getValue() instanceof Map) {
                section.createView(DataQuery.of('.', entry.getKey().toString()), (Map<?, ?>) entry.getValue());
            } else {
                section.set(DataQuery.of('.', entry.getKey().toString()), entry.getValue());
            }
        }
        return section;
    }

    @Override
    public Optional<DataView> getView(final DataQuery path) {
        return this.get(path).filter(obj -> obj instanceof DataView).map(obj -> (DataView) obj);
    }

    @Override
    public Optional<? extends Map<?, ?>> getMap(final DataQuery path) {
        final Optional<Object> val = this.get(path);
        if (val.isPresent()) {
            if (val.get() instanceof DataView) {
                final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
                for (final Map.Entry<DataQuery, Object> entry : ((DataView) val.get()).values(false).entrySet()) {
                    builder.put(entry.getKey().asString('.'), this.ensureMappingOf(entry.getValue()));
                }
                return Optional.of(builder.build());
            } else if (val.get() instanceof Map) {
                return Optional.of((Map<?, ?>) this.ensureMappingOf(val.get()));
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("rawtypes")
    private Object ensureMappingOf(final Object object) {
        if (object instanceof DataView) {
            final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
            for (final Map.Entry<DataQuery, Object> entry : ((DataView) object).values(false).entrySet()) {
                builder.put(entry.getKey().asString('.'), this.ensureMappingOf(entry.getValue()));
            }
            return builder.build();
        } else if (object instanceof Map) {
            final ImmutableMap.Builder<Object, Object> builder = ImmutableMap.builder();
            for (final Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
                builder.put(entry.getKey().toString(), this.ensureMappingOf(entry.getValue()));
            }
            return builder.build();
        } else if (object instanceof Collection) {
            final ImmutableList.Builder<Object> builder = ImmutableList.builder();
            for (final Object entry : (Collection) object) {
                builder.add(this.ensureMappingOf(entry));
            }
            return builder.build();
        } else {
            return object;
        }
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
    public Optional<Boolean> getBoolean(final DataQuery path) {
        return this.get(path).flatMap(Coerce::asBoolean);
    }

    @Override
    public Optional<Byte> getByte(final DataQuery path) {
        return this.get(path).flatMap(Coerce::asByte);
    }

    @Override
    public Optional<Short> getShort(final DataQuery path) {
        return this.get(path).flatMap(Coerce::asShort);
    }

    @Override
    public Optional<Integer> getInt(final DataQuery path) {
        return this.get(path).flatMap(Coerce::asInteger);
    }

    @Override
    public Optional<Long> getLong(final DataQuery path) {
        return this.get(path).flatMap(Coerce::asLong);
    }

    @Override
    public Optional<Float> getFloat(final DataQuery path) {
        return this.get(path).flatMap(Coerce::asFloat);
    }

    @Override
    public Optional<Double> getDouble(final DataQuery path) {
        return this.get(path).flatMap(Coerce::asDouble);
    }

    @Override
    public Optional<String> getString(final DataQuery path) {
        return this.get(path).flatMap(Coerce::asString);
    }

    @Override
    public Optional<List<?>> getList(final DataQuery path) {
        final Optional<Object> val = this.get(path);
        if (val.isPresent()) {
            if (val.get() instanceof List<?>) {
                return Optional.<List<?>>of(Lists.newArrayList((List<?>) val.get()));
            }
            if (val.get() instanceof Object[]) {
                return Optional.<List<?>>of(Lists.newArrayList((Object[]) val.get()));
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<String>> getStringList(final DataQuery path) {
        return this.getUnsafeList(path).map(list ->
            list.stream()
                .map(Coerce::asString)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList())
        );
    }

    private Optional<List<?>> getUnsafeList(final DataQuery path) {
        return this.get(path)
            .filter(obj -> obj instanceof List<?> || obj instanceof Object[])
            .map(obj -> {
                    if (obj instanceof List<?>) {
                        return (List<?>) obj;
                    }
                    return Arrays.asList((Object[]) obj);
                }
            );
    }

    @Override
    public Optional<List<Character>> getCharacterList(final DataQuery path) {
        return this.getUnsafeList(path).map(list ->
            list.stream()
                .map(Coerce::asChar)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList())
        );
    }

    @Override
    public Optional<List<Boolean>> getBooleanList(final DataQuery path) {
        return this.getUnsafeList(path).map(list ->
            list.stream()
                .map(Coerce::asBoolean)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList())
        );
    }

    @Override
    public Optional<List<Byte>> getByteList(final DataQuery path) {
        return this.getUnsafeList(path).map(list ->
            list.stream()
                .map(Coerce::asByte)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList())
        );
    }

    @Override
    public Optional<List<Short>> getShortList(final DataQuery path) {
        return this.getUnsafeList(path).map(list ->
            list.stream()
                .map(Coerce::asShort)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList())
        );
    }

    @Override
    public Optional<List<Integer>> getIntegerList(final DataQuery path) {
        return this.getUnsafeList(path).map(list ->
            list.stream()
                .map(Coerce::asInteger)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList())
        );
    }

    @Override
    public Optional<List<Long>> getLongList(final DataQuery path) {
        return this.getUnsafeList(path).map(list ->
            list.stream()
                .map(Coerce::asLong)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList())
        );
    }

    @Override
    public Optional<List<Float>> getFloatList(final DataQuery path) {
        return this.getUnsafeList(path).map(list ->
            list.stream()
                .map(Coerce::asFloat)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList())
        );
    }

    @Override
    public Optional<List<Double>> getDoubleList(final DataQuery path) {
        return this.getUnsafeList(path).map(list ->
            list.stream()
                .map(Coerce::asDouble)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList())
        );
    }

    @Override
    public Optional<List<Map<?, ?>>> getMapList(final DataQuery path) {
        return this.getUnsafeList(path).<List<Map<?, ?>>>map(list ->
            list.stream()
                .filter(obj -> obj instanceof Map<?, ?>)
                .map(obj -> (Map<?, ?>) obj)
                .collect(Collectors.toList())
        );
    }

    @Override
    public Optional<List<DataView>> getViewList(final DataQuery path) {
        return this.getUnsafeList(path).map(list ->
            list.stream()
                .filter(obj -> obj instanceof DataView)
                .map(obj -> (DataView) obj)
                .collect(Collectors.toList())
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataSerializable> Optional<T> getSerializable(final DataQuery path, final Class<T> clazz) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(clazz, "clazz");

        return this.getUnsafeView(path).flatMap(view -> Sponge.dataManager().builder(clazz).flatMap(builder -> builder.build(view)));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataSerializable> Optional<List<T>> getSerializableList(final DataQuery path, final Class<T> clazz) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(clazz, "clazz");
        return Stream.<Supplier<Optional<List<T>>>>of(
            () -> this.getViewList(path).flatMap(list ->
                Sponge.dataManager().builder(clazz).map(builder ->
                    list.stream()
                        .map(builder::build)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList())
                )
            )
        )
            .map(Supplier::get)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }

    @Override
    public <T> Optional<T> getRegistryValue(final DataQuery path, final RegistryType<T> registryType, final RegistryHolder holder) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(registryType, "registry type");
        return this.getString(path).flatMap(string -> holder.findRegistry(registryType).flatMap(r -> r.findValue(ResourceKey.resolve(string))));
    }

    @Override
    public <T> Optional<List<T>> getRegistryValueList(final DataQuery path, final RegistryType<T> registryType, final RegistryHolder holder) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(registryType, "registry type");
        return this.getStringList(path).map(list ->
            list.stream()
                .<Optional<T>>map(string -> holder.findRegistry(registryType).flatMap(r -> r.findValue(ResourceKey.resolve(string))))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList())
        );
    }

    @Override
    public <E, V extends Value<E>> Optional<Key<V>> getDataKey(final DataQuery path) {
        Objects.requireNonNull(path, "path");
        return this.getResourceKey(path).flatMap(r -> KeyProvider.INSTANCE.get(r));
    }

    @Override
    public Optional<List<Key<? extends Value<?>>>> getDataKeyList(final DataQuery path) {
        Objects.requireNonNull(path, "path");
        final Optional<List<ResourceKey>> resourceKeys = this.getResourceKeyList(path);
        if (!resourceKeys.isPresent()) {
            return Optional.empty();
        }

        final List<Key<? extends Value<?>>> keys = new ArrayList<>();
        for (final ResourceKey resourceKey : resourceKeys.get()) {
            KeyProvider.INSTANCE.get(resourceKey).ifPresent(keys::add);
        }
        if (keys.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(keys);
    }

    @Override
    public <T> Optional<T> getObject(final DataQuery path, final Class<T> objectClass) {
        return this.getView(path).flatMap(view ->
            Sponge.dataManager().translator(objectClass)
                .flatMap(serializer -> Optional.of(serializer.translate(view)))
        );
    }

    @Override
    public <T> Optional<List<T>> getObjectList(final DataQuery path, final Class<T> objectClass) {
        return this.getViewList(path).flatMap(viewList ->
            Sponge.dataManager().translator(objectClass).map(serializer ->
                viewList.stream()
                    .map(serializer::translate)
                    .collect(Collectors.toList())
            )
        );
    }

    @Override
    public DataContainer copy() {
        final DataContainer container = new MemoryDataContainer(this.safety);
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
    public org.spongepowered.api.data.persistence.DataView.SafetyMode safetyMode() {
        return this.safety;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.map, this.path);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        final MemoryDataView other = (MemoryDataView) obj;

        return Objects.equals(this.map.entrySet(), other.map.entrySet())
                && Objects.equals(this.path, other.path);
    }

    @Override
    public String toString() {
        final StringJoiner helper = new StringJoiner(", ", MemoryDataView.class.getSimpleName() + "[", "]");
        if (!this.path.toString().isEmpty()) {
            helper.add("path=" + this.path);
        }
        helper.add("safety=" + this.safety.name());
        return helper.add("map=" + this.map).toString();
    }
}
