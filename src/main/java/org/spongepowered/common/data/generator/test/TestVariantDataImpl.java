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
package org.spongepowered.common.data.generator.test;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.InternalCopies;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeOptionalValue;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeOptionalValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

public class TestVariantDataImpl implements TestVariantData {

    // Static values will be injected through reflection
    // All fields are public to avoid synthetic bridges

    public static Key<Value<Boolean>> key$my_boolean;
    public static ImmutableSet<Key<?>> keys;

    public static boolean default_value$my_boolean;

    public boolean value$my_boolean = default_value$my_boolean;

    @Override
    public Optional<TestVariantData> fill(DataHolder dataHolder, MergeFunction overlap) {
        final Optional<TestData> optData = dataHolder.get(TestData.class);
        if (optData.isPresent()) {
            final TestVariantDataImpl data = (TestVariantDataImpl) overlap.merge(this, optData.get());
            this.value$my_boolean = data.value$my_boolean;
            return Optional.of(this);
        }
        return Optional.empty();
    }

    @Override
    public Optional<TestVariantData> from(DataContainer container) {
        if (container.contains(key$my_boolean.getQuery())) {
            this.value$my_boolean = container.getBoolean(key$my_boolean.getQuery()).get();
        }
        return Optional.of(this);
    }

    @Override
    public <E> TestVariantDataImpl set(Key<? extends BaseValue<E>> key, E value) {
        checkNotNull(key, "key");
        checkNotNull(value, "value");
        if ((Key) key == key$my_boolean) {
            this.value$my_boolean = (Boolean) value;
            return this;
        }
        return this;
    }

    @Override
    public <E> Optional<E> get(Key<? extends BaseValue<E>> key) {
        checkNotNull(key, "key");
        return Optional.empty();
    }

    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(Key<V> key) {
        checkNotNull(key, "key");
        return Optional.empty();
    }

    @Override
    public boolean supports(Key<?> key) {
        checkNotNull(key, "key");
        return keys.contains(key);
    }

    @Override
    public TestVariantDataImpl copy() {
        TestVariantDataImpl copy = new TestVariantDataImpl();
        return copy;
    }

    @Override
    public Set<Key<?>> getKeys() {
        return keys;
    }

    @Override
    public Set<ImmutableValue<?>> getValues() {
        ImmutableSet.Builder<ImmutableValue<?>> values = ImmutableSet.builder();
        return values.build();
    }

    @Override
    public Immutable asImmutable() {
        Immutable immutable = new Immutable();
        return immutable;
    }

    @Override
    public int getContentVersion() {
        return 5;
    }

    @Override
    public DataContainer toContainer() {
        DataContainer dataContainer = DataContainer.createNew();
        return dataContainer;
    }

    @Override
    public Value<Boolean> type() {
        return new SpongeValue<>(key$my_boolean, default_value$my_boolean, InternalCopies.mutableCopy(this.value$my_boolean));
    }

    public static class Immutable implements ImmutableTestVariantData {

        @Override
        public TestVariantDataImpl asMutable() {
            TestVariantDataImpl mutable = new TestVariantDataImpl();
            return mutable;
        }

        @Override
        public int getContentVersion() {
            return 1;
        }

        @Override
        public DataContainer toContainer() {
            return DataContainer.createNew();
        }

        @Override
        public <E> Optional<E> get(Key<? extends BaseValue<E>> key) {
            checkNotNull(key, "key");
            return Optional.empty();
        }

        @Override
        public <E, V extends BaseValue<E>> Optional<V> getValue(Key<V> key) {
            checkNotNull(key, "key");
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
            return values.build();
        }

        @Override
        public ImmutableValue<Boolean> type() {
            return new ImmutableSpongeValue<>(key$my_boolean, default_value$my_boolean, null);
        }
    }
}
