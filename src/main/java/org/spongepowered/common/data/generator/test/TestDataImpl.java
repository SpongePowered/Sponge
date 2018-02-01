package org.spongepowered.common.data.generator.test;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.generator.InternalCopies;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;
import java.util.Set;

public class TestDataImpl implements TestData {

    static Key<Value<String>> key$my_string;
    static Key<Value<Integer>> key$my_int;
    static ImmutableSet<Key<?>> keys;

    // TODO: Inline strings?
    static String default_value$my_string;
    // TODO: Inline primitives
    static int default_value$my_int;

    private String value$my_string;
    private int value$my_int;

    @Override
    public Optional<TestData> fill(DataHolder dataHolder, MergeFunction overlap) {
        final Optional<TestData> optData = dataHolder.get(TestData.class);
        if (optData.isPresent()) {
            final TestDataImpl data = (TestDataImpl) overlap.merge(this, optData.get());
            this.value$my_string = data.value$my_string;
            this.value$my_int = data.value$my_int;
        }
        return Optional.of(this);
    }

    @Override
    public Optional<TestData> from(DataContainer container) {
        if (container.contains(key$my_string.getQuery())) {
            this.value$my_string = container.getString(key$my_string.getQuery()).get();
        }
        if (container.contains(key$my_int.getQuery())) {
            this.value$my_int = container.getInt(key$my_int.getQuery()).get();
        }
        return Optional.of(this);
    }

    @Override
    public <E> TestData set(Key<? extends BaseValue<E>> key, E value) {
        checkNotNull(key, "key");
        checkNotNull(value, "value");
        if (key == key$my_string) {
            this.value$my_string = (String) InternalCopies.copy(value);
            return this;
        }
        if (key == key$my_int) {
            this.value$my_int = (int) value; // No internal copy for primitives
            return this;
        }
        return this;
    }

    @Override
    public <E> Optional<E> get(Key<? extends BaseValue<E>> key) {
        checkNotNull(key, "key");
        if (key == key$my_string) {
            return Optional.of((E) InternalCopies.copy(this.value$my_string));
        }
        if (key == key$my_int) {
            return Optional.of((E) (Object) this.value$my_int); // No internal copy for primitives
        }
        return Optional.empty();
    }

    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(Key<V> key) {
        checkNotNull(key, "key");
        if (key == key$my_string) {
            return Optional.of((V) new SpongeValue<>(key$my_string, default_value$my_string, this.value$my_string));
        }
        if (key == key$my_int) {
            return Optional.of((V) new SpongeValue<>(key$my_int, default_value$my_int, this.value$my_int));
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(Key<?> key) {
        checkNotNull(key, "key");
        return keys.contains(key);
    }

    @Override
    public TestData copy() {
        TestDataImpl copy = new TestDataImpl();
        copy.value$my_int = this.value$my_int;
        copy.value$my_string = this.value$my_string;
        return copy;
    }

    @Override
    public Set<Key<?>> getKeys() {
        return keys;
    }

    @Override
    public Set<ImmutableValue<?>> getValues() {
        ImmutableSet.Builder<ImmutableValue<?>> values = ImmutableSet.builder();
        values.add(new ImmutableSpongeValue<>(key$my_string, default_value$my_string, this.value$my_string));
        values.add(new ImmutableSpongeValue<>(key$my_int, default_value$my_int, this.value$my_int));
        return values.build();
    }

    @Override
    public ImmutableTestData asImmutable() {
        Immutable immutable = new Immutable();
        immutable.value$my_string = this.value$my_string;
        immutable.value$my_int = this.value$my_int;
        return immutable;
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew()
                .set(key$my_string, this.value$my_string)
                .set(key$my_int, this.value$my_int);
    }

    @Override
    public String getString() {
        return this.value$my_string;
    }

    @Override
    public int getInt() {
        return this.value$my_int;
    }

    public static class Immutable implements ImmutableTestData {

        private String value$my_string;
        private int value$my_int;

        @Override
        public TestData asMutable() {
            TestDataImpl mutable = new TestDataImpl();
            mutable.value$my_string = this.value$my_string;
            mutable.value$my_int = this.value$my_int;
            return mutable;
        }

        @Override
        public int getContentVersion() {
            return 1;
        }

        @Override
        public DataContainer toContainer() {
            return DataContainer.createNew()
                    .set(key$my_string, this.value$my_string)
                    .set(key$my_int, this.value$my_int);
        }

        @Override
        public <E> Optional<E> get(Key<? extends BaseValue<E>> key) {
            checkNotNull(key, "key");
            if (key == key$my_string) {
                return Optional.of((E) InternalCopies.copy(this.value$my_string));
            }
            if (key == key$my_int) {
                return Optional.of((E) (Object) this.value$my_int);
            }
            return Optional.empty();
        }

        @Override
        public <E, V extends BaseValue<E>> Optional<V> getValue(Key<V> key) {
            checkNotNull(key, "key");
            if (key == key$my_string) {
                return Optional.of((V) new ImmutableSpongeValue<>(key$my_string, default_value$my_string, this.value$my_string));
            }
            if (key == key$my_int) {
                return Optional.of((V) new ImmutableSpongeValue<>(key$my_int, default_value$my_int, this.value$my_int));
            }
            return Optional.empty();
        }

        @Override
        public boolean supports(Key<?> key) {
            checkNotNull(key, "key");
            return keys.contains(key);
        }

        @Override
        public Set<Key<?>> getKeys() {
            return keys;
        }

        @Override
        public Set<ImmutableValue<?>> getValues() {
            ImmutableSet.Builder<ImmutableValue<?>> values = ImmutableSet.builder();
            values.add(new ImmutableSpongeValue<>(key$my_string, default_value$my_string, this.value$my_string));
            values.add(new ImmutableSpongeValue<>(key$my_int, default_value$my_int, this.value$my_int));
            return values.build();
        }
    }
}
