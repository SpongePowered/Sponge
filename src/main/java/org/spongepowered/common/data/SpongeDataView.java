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
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class SpongeDataView implements DataView {

    private final DataView parent;
    private final DataContainer container;
    private final @Nullable String key;
    private final DataView.SafetyMode safety;
    private @MonotonicNonNull DataQuery path;

    protected SpongeDataView(final DataView.SafetyMode safety) {
        Preconditions.checkState(this instanceof DataContainer, "Cannot construct a root SpongeDataView without a container!");
        this.parent = this;
        this.container = (DataContainer) this;
        this.key = null;
        this.safety = Objects.requireNonNull(safety, "Safety mode");
    }

    protected SpongeDataView(final DataView parent, final String key, final DataView.SafetyMode safety) {
        Preconditions.checkArgument(!key.isEmpty(), "Key must have at least one part");
        this.parent = parent;
        this.container = parent.container();
        this.key = key;
        this.safety = Objects.requireNonNull(safety, "Safety mode");
    }

    @Override
    public final DataContainer container() {
        return this.container;
    }

    @Override
    public final DataQuery currentPath() {
        if (this.path == null) {
            if (this.key != null) {
                this.path = this.parent.currentPath().then(this.key);
            } else {
                this.path = DataQuery.of();
            }
        }
        return this.path;
    }

    @Override
    public final String name() {
        return this.key != null ? this.key : "";
    }

    @Override
    public Optional<DataView> parent() {
        return Optional.of(this.parent);
    }

    @Override
    public final org.spongepowered.api.data.persistence.DataView.SafetyMode safetyMode() {
        return this.safety;
    }

    @Override
    public Set<DataQuery> keys(final boolean deep) {
        final ImmutableSet.Builder<DataQuery> builder = ImmutableSet.builder();
        this.streamRootKeys().forEach(k -> builder.add(DataQuery.of(k)));

        if (deep) {
            this.keysDeep(builder, DataQuery.of(), this);
        }

        return builder.build();
    }

    private void keysDeep(final ImmutableSet.Builder<DataQuery> builder, final DataQuery path, final DataView view) {
        view.streamRootKeys().forEach(k -> {
            final DataQuery subPath = path.then(k);
            if (!path.parts().isEmpty()) {
                builder.add(subPath);
            }
            view.getView(k).ifPresent(v -> this.keysDeep(builder, subPath, v));
        });
    }

    @Override
    public Map<DataQuery, Object> values(final boolean deep) {
        final ImmutableMap.Builder<DataQuery, Object> builder = ImmutableMap.builder();
        this.values(builder, DataQuery.of(), this, deep);

        return builder.build();
    }

    private void values(final ImmutableMap.Builder<DataQuery, Object> builder, final DataQuery path, final DataView view, final boolean deep) {
        view.streamRootValues().forEach(e -> {
            final DataQuery subPath = path.then(e.getKey());
            if (e.getValue() instanceof final DataView subView) {
                builder.put(subPath, subView.values(deep));

                if (deep) {
                    this.values(builder, subPath, subView, true);
                }
            } else {
                builder.put(subPath, e.getValue());
            }
        });
    }

    private Optional<DataView> getUnsafeView(final DataQuery path) {
        return this.get(path).filter(obj -> obj instanceof DataView).map(obj -> (DataView) obj);
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
    public boolean equals(final Object obj) {
        if (!(obj instanceof final SpongeDataView other)) {
            return false;
        }
        return Objects.equals(this.values(true), other.values(true))
            && Objects.equals(this.currentPath(), other.currentPath());
    }
}
