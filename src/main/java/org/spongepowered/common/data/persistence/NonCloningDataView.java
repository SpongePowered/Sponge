/*
 * Copyright (c) 2015-2016 VoxelBox <http://engine.thevoxelbox.com>.
 * All Rights Reserved.
 */
package org.spongepowered.common.data.persistence;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.spongepowered.api.data.DataQuery.of;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataManager;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.MemoryDataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.persistence.DataBuilder;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.util.Coerce;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

public class NonCloningDataView implements DataView {

    protected final Map<String, Object> map = Maps.newLinkedHashMap();
    private final DataContainer container;
    private final DataView parent;
    private final DataQuery path;

    protected NonCloningDataView() {
        checkState(this instanceof DataContainer, "Cannot construct a root NonCloningDataView without a container!");
        this.path = of();
        this.parent = this;
        this.container = (DataContainer) this;
    }

    protected NonCloningDataView(DataView parent, DataQuery path) {
        checkArgument(path.getParts().size() >= 1, "Path must have at least one part");
        this.parent = parent;
        this.container = parent.getContainer();
        this.path = parent.getCurrentPath().then(path);
    }

    @Override
    public DataContainer getContainer() {
        return this.container;
    }

    @Override
    public DataQuery getCurrentPath() {
        return this.path;
    }

    @Override
    public String getName() {
        List<String> parts = this.path.getParts();
        return parts.isEmpty() ? "" : parts.get(parts.size() - 1);
    }

    @Override
    public Optional<DataView> getParent() {
        return Optional.ofNullable(this.parent);
    }

    @Override
    public Set<DataQuery> getKeys(boolean deep) {
        ImmutableSet.Builder<DataQuery> builder = ImmutableSet.builder();

        for (Map.Entry<String, Object> entry : this.map.entrySet()) {
            builder.add(of(entry.getKey()));
        }
        if (deep) {
            for (Map.Entry<String, Object> entry : this.map.entrySet()) {
                if (entry.getValue() instanceof DataView) {
                    for (DataQuery query : ((DataView) entry.getValue()).getKeys(true)) {
                        builder.add(of(entry.getKey()).then(query));
                    }
                }
            }
        }
        return builder.build();
    }

    @Override
    public Map<DataQuery, Object> getValues(boolean deep) {
        ImmutableMap.Builder<DataQuery, Object> builder = ImmutableMap.builder();
        for (DataQuery query : getKeys(deep)) {
            Object value = get(query).get();
            if (value instanceof DataView) {
                builder.put(query, ((DataView) value).getValues(deep));
            } else {
                builder.put(query, get(query).get());
            }
        }
        return builder.build();
    }

    @Override
    public final boolean contains(DataQuery path) {
        checkNotNull(path, "path");
        List<String> queryParts = path.getParts();

        String key = queryParts.get(0);
        if (queryParts.size() == 1) {
            return this.map.containsKey(key);
        }
        Optional<DataView> subViewOptional = this.getUnsafeView(key);
        if (!subViewOptional.isPresent()) {
            return false;
        }
        return subViewOptional.get().contains(path.popFirst());
    }

    @Override
    public boolean contains(DataQuery path, DataQuery... paths) {
        checkNotNull(path, "DataQuery cannot be null!");
        checkNotNull(paths, "DataQuery varargs cannot be null!");
        if(!contains(path)) {
            return false;
        }
        if (paths.length != 0) {
            for (DataQuery query : paths) {
                if (!contains(query)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Optional<Object> get(DataQuery path) {
        checkNotNull(path, "path");
        List<String> queryParts = path.getParts();

        int sz = queryParts.size();

        if (sz == 0) {
            return Optional.<Object>of(this);
        }

        String key = queryParts.get(0);
        if (sz == 1) {
            final Object object = this.map.get(key);
            return Optional.ofNullable(object);
        }
        Optional<DataView> subViewOptional = this.getUnsafeView(key);
        if (!subViewOptional.isPresent()) {
            return Optional.empty();
        }
        DataView subView = subViewOptional.get();
        return subView.get(path.popFirst());
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public DataView set(DataQuery path, Object value) {
        checkNotNull(path, "path");
        checkNotNull(value, "value");
        checkState(this.container != null);

        @Nullable DataManager manager;

        try {
            manager = Sponge.getDataManager();
        } catch (Exception e) {
            manager = null;
        }

        if (value instanceof DataView) {
            checkArgument(value != this, "Cannot set a DataView to itself.");
        }
        if (value instanceof DataSerializable) {
            DataContainer valueContainer = ((DataSerializable) value).toContainer();
            checkArgument(!(valueContainer).equals(this), "Cannot insert self-referencing DataSerializable");
            copyDataView(path, valueContainer);
        } else if (value instanceof CatalogType) {
            return set(path, ((CatalogType) value).getId());
        } else if (manager != null && manager.getTranslator(value.getClass()).isPresent()) {
            DataTranslator serializer = manager.getTranslator(value.getClass()).get();
            final DataContainer container = serializer.translate(value);
            checkArgument(!container.equals(this), "Cannot insert self-referencing Objects!");
            copyDataView(path, container);
        } else {
            List<String> parts = path.getParts();
            if (parts.size() > 1) {
                String subKey = parts.get(0);
                Optional<DataView> subViewOptional = this.getUnsafeView(subKey);
                DataView subView;
                if (!subViewOptional.isPresent()) {
                    this.createView(of(subKey));
                    subView = (DataView) this.map.get(subKey);
                } else {
                    subView = subViewOptional.get();
                }
                subView.set(path.popFirst(), value);
            } else {
                if (value instanceof Collection) {
                    setCollection(parts.get(0), (Collection) value);
                } else if (value instanceof Map) {
                    setMap(parts.get(0), (Map) value);
                } else {
                    this.map.put(parts.get(0), value);
                }
            }
        }
        return this;
    }

    @Override
    public <E> DataView set(Key<? extends BaseValue<E>> key, E value) {
        return set(checkNotNull(key, "Key was null!").getQuery(), value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void setCollection(String key, Collection<?> value) {
        ImmutableList.Builder<Object> builder = ImmutableList.builder();
        @Nullable DataManager manager;

        try {
            manager = Sponge.getDataManager();
        } catch (Exception e) {
            manager = null;
        }

        for (Object object : value) {
            if (object instanceof DataSerializable) {
                builder.add(((DataSerializable) object).toContainer());
            } else if (object instanceof DataView) {
                MemoryDataView view = new MemoryDataContainer();
                DataView internalView = (DataView) object;
                for (Map.Entry<DataQuery, Object> entry : internalView.getValues(false).entrySet()) {
                    view.set(entry.getKey(), entry.getValue());
                }
                builder.add(view);
            } else if (object instanceof Map) {
                builder.add(ensureSerialization((Map) object));
            } else if (object instanceof Collection) {
                builder.add(ensureSerialization((Collection) object));
            } else {
                if (manager != null) {
                    final Optional<? extends DataTranslator<?>> translatorOptional = manager.getTranslator(object.getClass());
                    if (translatorOptional.isPresent()) {
                        DataTranslator translator = translatorOptional.get();
                        final DataContainer container = translator.translate(value);
                        checkArgument(!container.equals(this), "Cannot insert self-referencing Objects!");
                        copyDataView(this.path, container);
                    } else {
                        builder.add(object);
                    }
                } else {
                    builder.add(object);
                }

            }
        }
        this.map.put(key, builder.build());
    }

    @SuppressWarnings("rawtypes")
    private ImmutableList<Object> ensureSerialization(Collection<?> collection) {
        ImmutableList.Builder<Object> objectBuilder = ImmutableList.builder();
        collection.forEach(element -> {
            if (element instanceof Collection) {
                objectBuilder.add(ensureSerialization((Collection) element));
            } else if (element instanceof DataSerializable) {
                objectBuilder.add(((DataSerializable) element).toContainer());
            } else {
                objectBuilder.add(element);
            }
        });
        return objectBuilder.build();

    }

    @SuppressWarnings("rawtypes")
    private ImmutableMap<?, ?> ensureSerialization(Map<?, ?> map) {
        ImmutableMap.Builder<Object, Object> builder = ImmutableMap.builder();
        map.entrySet().forEach(entry -> {
            if (entry.getValue() instanceof Map) {
                builder.put(entry.getKey(), ensureSerialization((Map) entry.getValue()));
            } else if (entry.getValue() instanceof DataSerializable) {
                builder.put(entry.getKey(), ((DataSerializable) entry.getValue()).toContainer());
            } else if (entry.getValue() instanceof Collection) {
                builder.put(entry.getKey(), ensureSerialization((Collection) entry.getValue()));
            } else {
                builder.put(entry.getKey(), entry.getValue());
            }
        });
        return builder.build();
    }

    private void setMap(String key, Map<?, ?> value) {
        DataView view = createView(of(key));
        for (Map.Entry<?, ?> entry : value.entrySet()) {
            view.set(of(entry.getKey().toString()), entry.getValue());
        }
    }

    private void copyDataView(DataQuery path, DataView value) {
        Collection<DataQuery> valueKeys = value.getKeys(true);
        for (DataQuery oldKey : valueKeys) {
            set(path.then(oldKey), value.get(oldKey).get());
        }
    }

    @Override
    public DataView remove(DataQuery path) {
        checkNotNull(path, "path");
        List<String> parts = path.getParts();
        if (parts.size() > 1) {
            String subKey = parts.get(0);
            Optional<DataView> subViewOptional = this.getUnsafeView(subKey);
            if (!subViewOptional.isPresent()) {
                return this;
            }
            DataView subView = subViewOptional.get();
            subView.remove(path.popFirst());
        } else {
            this.map.remove(parts.get(0));
        }
        return this;
    }

    @Override
    public DataView createView(DataQuery path) {
        checkNotNull(path, "path");
        List<String> queryParts = path.getParts();

        int sz = queryParts.size();

        checkArgument(sz != 0, "The size of the query must be at least 1");

        String key = queryParts.get(0);
        DataQuery keyQuery = of(key);
        
        if (sz == 1) {
            DataView result = new NonCloningDataView(this, keyQuery);
            this.map.put(key, result);
            return result;
        }
        DataQuery subQuery = path.popFirst();
        DataView subView = (DataView) this.map.get(key);
        if (subView == null) {
            subView = new NonCloningDataView(this.parent, keyQuery);
            this.map.put(key, subView);
        }
        return subView.createView(subQuery);
    }

    @Override
    public DataView createView(DataQuery path, Map<?, ?> map) {
        checkNotNull(path, "path");
        DataView section = createView(path);

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getValue() instanceof Map) {
                section.createView(of('.', entry.getKey().toString()), (Map<?, ?>) entry.getValue());
            } else {
                section.set(of('.', entry.getKey().toString()), entry.getValue());
            }
        }
        return section;
    }

    @Override
    public Optional<DataView> getView(DataQuery path) {
        return get(path).filter(obj -> obj instanceof DataView).map(obj -> (DataView) obj);
    }

    @Override
    public Optional<? extends Map<?, ?>> getMap(DataQuery path) {
        Optional<Object> val = get(path);
        if (val.isPresent()) {
            if (val.get() instanceof DataView) {
                ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
                for (Map.Entry<DataQuery, Object> entry : ((DataView) val.get()).getValues(false).entrySet()) {
                    builder.put(entry.getKey().asString('.'), viewsToMap(entry.getValue()));
                }
                return Optional.of(builder.build());
            } else if (val.get() instanceof Map) {
                return Optional.of((Map<?, ?>) val.get());
            }
        }
        return Optional.empty();
    }

    private Object viewsToMap(Object object) {
        if (object instanceof DataView) {
            final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
            for (Map.Entry<DataQuery, Object> entry : ((DataView) object).getValues(false).entrySet()) {
                builder.put(entry.getKey().asString('.'), viewsToMap(entry.getValue()));
            }
            return builder.build();
        }
        return object;
    }

    private Optional<DataView> getUnsafeView(String path) {
        final Object object = this.map.get(path);
        if(!(object instanceof DataView)) {
            return Optional.empty();
        }
        return Optional.of((DataView) object);
    }

    @Override
    public Optional<Boolean> getBoolean(DataQuery path) {
        return get(path).flatMap(Coerce::asBoolean);
    }

    @Override
    public Optional<Byte> getByte(DataQuery path) {
        return get(path).flatMap(Coerce::asByte);
    }

    @Override
    public Optional<Short> getShort(DataQuery path) {
        return get(path).flatMap(Coerce::asShort);
    }

    @Override
    public Optional<Integer> getInt(DataQuery path) {
        return get(path).flatMap(Coerce::asInteger);
    }

    @Override
    public Optional<Long> getLong(DataQuery path) {
        return get(path).flatMap(Coerce::asLong);
    }

    @Override
    public Optional<Float> getFloat(DataQuery path) {
        return get(path).flatMap(Coerce::asFloat);
    }

    @Override
    public Optional<Double> getDouble(DataQuery path) {
        return get(path).flatMap(Coerce::asDouble);
    }

    @Override
    public Optional<String> getString(DataQuery path) {
        return get(path).flatMap(Coerce::asString);
    }

    @Override
    public Optional<List<?>> getList(DataQuery path) {
        Optional<Object> val = get(path);
        if (val.isPresent()) {
            if (val.get() instanceof List<?>) {
                return Optional.<List<?>>of((List<?>) val.get());
            }
            if (val.get() instanceof Object[]) {
                return Optional.<List<?>>of(Lists.newArrayList((Object[]) val.get()));
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<String>> getStringList(DataQuery path) {
        return getUnsafeList(path).map(list ->
                list.stream()
                        .map(Coerce::asString)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList())
        );
    }

    private Optional<List<?>> getUnsafeList(DataQuery path) {
        return get(path)
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
    public Optional<List<Character>> getCharacterList(DataQuery path) {
        return getUnsafeList(path).map(list ->
                list.stream()
                        .map(Coerce::asChar)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public Optional<List<Boolean>> getBooleanList(DataQuery path) {
        return getUnsafeList(path).map(list ->
                list.stream()
                        .map(Coerce::asBoolean)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public Optional<List<Byte>> getByteList(DataQuery path) {
        return getUnsafeList(path).map(list ->
                list.stream()
                        .map(Coerce::asByte)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public Optional<List<Short>> getShortList(DataQuery path) {
        return getUnsafeList(path).map(list ->
                list.stream()
                        .map(Coerce::asShort)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public Optional<List<Integer>> getIntegerList(DataQuery path) {
        return getUnsafeList(path).map(list ->
                list.stream()
                        .map(Coerce::asInteger)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public Optional<List<Long>> getLongList(DataQuery path) {
        return getUnsafeList(path).map(list ->
                list.stream()
                        .map(Coerce::asLong)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public Optional<List<Float>> getFloatList(DataQuery path) {
        return getUnsafeList(path).map(list ->
                list.stream()
                        .map(Coerce::asFloat)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public Optional<List<Double>> getDoubleList(DataQuery path) {
        return getUnsafeList(path).map(list ->
                list.stream()
                        .map(Coerce::asDouble)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public Optional<List<Map<?, ?>>> getMapList(DataQuery path) {
        return getUnsafeList(path).<List<Map<?, ?>>>map(list ->
                list.stream()
                        .filter(obj -> obj instanceof Map<?, ?>)
                        .map(obj -> (Map<?, ?>) obj)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public Optional<List<DataView>> getViewList(DataQuery path) {
        return getUnsafeList(path).map(list ->
                list.stream()
                        .filter(obj -> obj instanceof DataView)
                        .map(obj -> (DataView) obj)
                        .collect(Collectors.toList())
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataSerializable> Optional<T> getSerializable(DataQuery path, Class<T> clazz) {
        checkNotNull(path, "path");
        checkNotNull(clazz, "clazz");
        if (clazz.isAssignableFrom(CatalogType.class)) {
            final Optional<T> catalog = (Optional<T>) getCatalogType(path, ((Class<? extends CatalogType>) clazz));
            if (catalog.isPresent()) {
                return catalog;
            }
        }
        
        Optional<Object> obj = get(path);
        if(!obj.isPresent()) {
            return Optional.empty();
        }
        DataView data = (DataView) obj.get();
        Optional<DataBuilder<T>> builder = Sponge.getDataManager().getBuilder(clazz);
        if(!builder.isPresent()) {
            return Optional.empty();
        }
        return builder.get().build(data);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataSerializable> Optional<List<T>> getSerializableList(DataQuery path, Class<T> clazz) {
        checkNotNull(path, "path");
        checkNotNull(clazz, "clazz");
        return Stream.<Supplier<Optional<List<T>>>>of(
                () -> {
                    if (clazz.isAssignableFrom(CatalogType.class)) {
                        return (Optional<List<T>>) (Optional<?>) getCatalogTypeList(path, (Class<? extends CatalogType>) clazz);
                    }
                    return Optional.empty();
                },
                () -> getViewList(path).flatMap(list ->
                        Sponge.getDataManager().getBuilder(clazz).map(builder ->
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
    public <T extends CatalogType> Optional<T> getCatalogType(DataQuery path, Class<T> catalogType) {
        checkNotNull(path, "path");
        checkNotNull(catalogType, "dummy type");
        return getString(path).flatMap(string -> Sponge.getRegistry().getType(catalogType, string));
    }

    @Override
    public <T extends CatalogType> Optional<List<T>> getCatalogTypeList(DataQuery path, Class<T> catalogType) {
        checkNotNull(path, "path");
        checkNotNull(catalogType, "catalogType");
        return getStringList(path).map(list ->
                list.stream()
                        .map(string -> Sponge.getRegistry().getType(catalogType, string))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public <T> Optional<T> getObject(DataQuery path, Class<T> objectClass) {
        return getView(path).flatMap(view ->
                Sponge.getDataManager().getTranslator(objectClass)
                        .flatMap(serializer -> Optional.of(serializer.translate(view)))
        );
    }

    @Override
    public <T> Optional<List<T>> getObjectList(DataQuery path, Class<T> objectClass) {
        return getViewList(path).flatMap(viewList ->
                Sponge.getDataManager().getTranslator(objectClass).map(serializer ->
                        viewList.stream()
                                .map(serializer::translate)
                                .collect(Collectors.toList())
                )
        );
    }

    @Override
    public DataContainer copy() {
        final DataContainer container = new MemoryDataContainer();
        getKeys(false).stream()
                .forEach(query ->
                        get(query).ifPresent(obj ->
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
        return Objects.hashCode(this.map, this.path);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final NonCloningDataView other = (NonCloningDataView) obj;

        return Objects.equal(this.map.entrySet(), other.map.entrySet())
               && Objects.equal(this.path, other.path);
    }

    @Override
    public String toString() {
        final Objects.ToStringHelper helper = Objects.toStringHelper(this);
        if (!this.path.toString().isEmpty()) {
            helper.add("path", this.path);
        }
        return helper.add("map", this.map).toString();
    }
}
