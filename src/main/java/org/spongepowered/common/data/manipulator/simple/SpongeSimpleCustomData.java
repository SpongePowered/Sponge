package org.spongepowered.common.data.manipulator.simple;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.KeyFactory;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.manipulator.SimpleCustomData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.DataContentUpdater;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.util.CollectionUtils;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.registry.type.data.SimpleDataRegistryModule;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SpongeSimpleCustomData<T> implements SimpleCustomData<T> {

    private static final int INTERNAL_CONTENT_VERSION = 1;

    private String name;
    private String id;
    private int version;
    private Set<DataContentUpdater> updaters;
    private Key<Value<T>> key;

    @SuppressWarnings("unchecked")
    public SpongeSimpleCustomData(SpongeSimpleCustomDataBuilder builder) {
        this.name = builder.name;
        this.id = builder.id;
        this.version = builder.version;
        this.updaters = builder.updaters;

        this.key = KeyFactory.makeSingleKey(new TypeToken<T>(getClass()) {}, new TypeToken<Value<T>>(getClass()) {},
                DataQuery.of(this.id, "value"), this.id, this.name);

        SimpleDataRegistryModule.getInstance().register(this);
    }

    @Override
    public Key<Value<T>> getKey() {
        return this.key;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getID() {
        return this.id;
    }

    @Override
    public int getContentVersion() {
        return this.version;
    }

    @Override
    public Set<DataContentUpdater> getContentUpdaters() {
        return this.updaters;
    }

    public static class Mutable implements DataManipulator<Mutable, Immutable> {

        private final Map<Key<?>, Object> valueMap;
        private final DataView leftover; // Preserves any unmatched data

        public Mutable() {
            this(Maps.newHashMap());
        }

        public Mutable(DataView leftover) {
            this(Maps.newHashMap(), leftover);
        }

        public Mutable(Map<Key<?>, Object> valueMap) {
            this(valueMap, new MemoryDataContainer());
        }

        public Mutable(Map<Key<?>, Object> valueMap, DataView leftover) {
            this.valueMap = Maps.newHashMap(valueMap);
            this.leftover = leftover;
        }

        @Override
        public int getContentVersion() {
            return INTERNAL_CONTENT_VERSION;
        }

        @Override
        public DataContainer toContainer() {
            return null; //TODO
        }

        @SuppressWarnings("unchecked")
        @Override
        public <E> Optional<E> get(Key<? extends BaseValue<E>> key) {
            return Optional.ofNullable((E) valueMap.get(key));
        }

        @Override
        public Optional<Mutable> fill(DataHolder dataHolder, MergeFunction overlap) {
            Mutable merged = overlap.merge(this, dataHolder.get(Mutable.class).orElse(null));
            merged.getValues()
                    .stream()
                    .map(ImmutableValue::asMutable)
                    .collect(Collectors.toMap(
                            BaseValue::getKey,
                            BaseValue::get,
                            null,
                            () -> this.valueMap
                    ));
            return Optional.of(this);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <E, V extends BaseValue<E>> Optional<V> getValue(Key<V> key) {
            return Optional.ofNullable((E) this.valueMap.get(key))
                    .map(value -> (V) new SpongeValue<>(key, value));
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean supports(Key<?> key) {
            return SimpleDataRegistryModule.getInstance().isRegistered(key);
        }

        @Override
        public Optional<Mutable> from(DataContainer container) {
            return null; // TODO
        }

        @SuppressWarnings("unchecked")
        @Override
        public <E> Mutable set(Key<? extends BaseValue<E>> key, E element) {
            checkState(supports(key), "Tried to set value of unsupported key");
            this.valueMap.put(key, element);
            return this;
        }

        @Override
        public Set<Key<?>> getKeys() {
            return this.valueMap.keySet();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Set<ImmutableValue<?>> getValues() {
            return this.valueMap.entrySet().stream()
                    .map((entry) -> new ImmutableSpongeValue(entry.getKey(), entry.getValue()))
                    .collect(CollectionUtils.immutableSetCollector());
        }

        @Override
        public Mutable copy() {
            return new Mutable(this.valueMap);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Immutable asImmutable() {
            Map<Key<?>, ImmutableValue<?>> immValues = this.valueMap.entrySet().stream()
                    .map((entry) -> new ImmutableSpongeValue(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toMap(
                            ImmutableValue::getKey,
                            Function.identity()
                    ));
            return new Immutable(immValues);
        }
    }

    public static class Immutable implements ImmutableDataManipulator<Immutable, Mutable> {

        private final ImmutableMap<Key<?>, ImmutableValue<?>> valueMap;
        private final DataView leftover; // Preserves any unmatched data

        public Immutable() {
            this(ImmutableMap.of());
        }

        public Immutable(DataView leftover) {
            this(ImmutableMap.of(), leftover);
        }

        public Immutable(Map<Key<?>, ImmutableValue<?>> valueMap) {
            this(valueMap, new MemoryDataContainer());
        }

        public Immutable(Map<Key<?>, ImmutableValue<?>> valueMap, DataView leftover) {
            this.valueMap = ImmutableMap.copyOf(valueMap);
            this.leftover = leftover;
        }

        @Override
        public int getContentVersion() {
            return INTERNAL_CONTENT_VERSION;
        }

        @Override
        public DataContainer toContainer() {
            return null; //TODO
        }

        @Override
        public <E> Optional<E> get(Key<? extends BaseValue<E>> key) {
            return Optional.ofNullable(valueMap.get(key)).map(value -> (E) value.get());
        }

        @Override
        public Mutable asMutable() {
            Map<Key<?>, Object> values = this.valueMap.values().stream()
                    .collect(Collectors.toMap(
                            ImmutableValue::getKey,
                            ImmutableValue::get
                    ));
            return new Mutable(values);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <E, V extends BaseValue<E>> Optional<V> getValue(Key<V> key) {
            return Optional.ofNullable((V) this.valueMap.get(key));
        }

        @Override
        public boolean supports(Key<?> key) {
            return SimpleDataRegistryModule.getInstance().isRegistered(key);
        }

        @Override
        public Set<Key<?>> getKeys() {
            return this.valueMap.keySet();
        }

        @Override
        public Set<ImmutableValue<?>> getValues() {
            return ImmutableSet.copyOf(this.valueMap.values());
        }
    }

    private static class DataBuilder implements DataManipulatorBuilder<Mutable, Immutable> {

        @Override
        public Mutable create() {
            return new Mutable();
        }

        @Override
        public Optional<Mutable> build(DataView container) throws InvalidDataException {
            return null;
        }

        @Override
        public Optional<Mutable> createFrom(DataHolder dataHolder) {
            return Optional.of(new Mutable());
        }
    }
}
