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
package org.spongepowered.common.data.key;

import com.google.common.base.Preconditions;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeFactory;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.ListValue;
import org.spongepowered.api.data.value.MapValue;
import org.spongepowered.api.data.value.SetValue;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.WeightedCollectionValue;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.common.registry.provider.KeyProvider;
import org.spongepowered.common.util.AbstractResourceKeyedBuilder;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class SpongeKeyBuilder<E, V extends Value<E>> extends AbstractResourceKeyedBuilder<Key<V>, Key.Builder<E, V>> implements Key.Builder<E,
        V> {

    private @Nullable Type valueType;
    private @Nullable Type elementType;
    private @Nullable Comparator<? super E> comparator;
    private @Nullable BiPredicate<? super E, ? super E> includesTester;

    @Override
    public <T, B extends Value<T>> SpongeKeyBuilder<T, B> type(final TypeToken<B> token) {
        Objects.requireNonNull(token);
        this.valueType = token.getType();
        final Type valueTypeAsSuper = GenericTypeReflector.getExactSuperType(this.valueType, Value.class);
        if (!(valueTypeAsSuper instanceof ParameterizedType)) {
            throw new IllegalArgumentException("Raw type " + this.valueType + " provided when registering Key " + this.key);
        }
        this.elementType = ((ParameterizedType) valueTypeAsSuper).getActualTypeArguments()[0];
        return (SpongeKeyBuilder<T, B>) this;
    }

    @Override
    public <T> Key.Builder<T, Value<T>> elementType(final Class<T> type) {
        Objects.requireNonNull(type, "type");
        this.valueType = TypeFactory.parameterizedClass(Value.class, type);
        this.elementType = type;
        return (SpongeKeyBuilder<T, Value<T>>) this;
    }

    @Override
    public <T> Key.Builder<T, Value<T>> elementType(final TypeToken<T> type) {
        Objects.requireNonNull(type, "type");
        this.valueType = TypeFactory.parameterizedClass(Value.class, type.getType());
        this.elementType = type.getType();
        return (SpongeKeyBuilder<T, Value<T>>) this;
    }

    @Override
    public <T> Key.Builder<List<T>, ListValue<T>> listElementType(final Class<T> type) {
        Objects.requireNonNull(type, "type");
        this.valueType = TypeFactory.parameterizedClass(ListValue.class, type);
        this.elementType = TypeFactory.parameterizedClass(List.class, type);
        return (SpongeKeyBuilder<List<T>, ListValue<T>>) this;
    }

    @Override
    public <T> Key.Builder<List<T>, ListValue<T>> listElementType(final TypeToken<T> type) {
        Objects.requireNonNull(type, "type");
        this.valueType = TypeFactory.parameterizedClass(ListValue.class, type.getType());
        this.elementType = TypeFactory.parameterizedClass(List.class, type.getType());
        return (SpongeKeyBuilder<List<T>, ListValue<T>>) this;
    }

    @Override
    public <T> Key.Builder<Set<T>, SetValue<T>> setElementType(final Class<T> type) {
        Objects.requireNonNull(type, "type");
        this.valueType = TypeFactory.parameterizedClass(SetValue.class, type);
        this.elementType = TypeFactory.parameterizedClass(Set.class, type);
        return (SpongeKeyBuilder<Set<T>, SetValue<T>>) this;
    }

    @Override
    public <T> Key.Builder<Set<T>, SetValue<T>> setElementType(final TypeToken<T> type) {
        Objects.requireNonNull(type, "type");
        this.valueType = TypeFactory.parameterizedClass(SetValue.class, type.getType());
        this.elementType = TypeFactory.parameterizedClass(Set.class, type.getType());
        return (SpongeKeyBuilder<Set<T>, SetValue<T>>) this;
    }

    @Override
    public <K, V1> Key.Builder<Map<K, V1>, MapValue<K, V1>> mapElementType(final Class<K> keyType, final Class<V1> valueType) {
        Objects.requireNonNull(keyType, "keyType");
        Objects.requireNonNull(valueType, "valueType");
        this.valueType = TypeFactory.parameterizedClass(MapValue.class, keyType, valueType);
        this.elementType = TypeFactory.parameterizedClass(Map.class, keyType, valueType);
        return (SpongeKeyBuilder<Map<K, V1>, MapValue<K, V1>>) this;
    }

    @Override
    public <K, V1> Key.Builder<Map<K, V1>, MapValue<K, V1>> mapElementType(final TypeToken<K> keyType, final TypeToken<V1> valueType) {
        Objects.requireNonNull(keyType, "keyType");
        Objects.requireNonNull(valueType, "valueType");
        this.valueType = TypeFactory.parameterizedClass(MapValue.class, keyType.getType(), valueType.getType());
        this.elementType = TypeFactory.parameterizedClass(Map.class, keyType.getType(), valueType.getType());
        return (SpongeKeyBuilder<Map<K, V1>, MapValue<K, V1>>) this;
    }

    @Override
    public <T> Key.Builder<WeightedTable<T>, WeightedCollectionValue<T>> weightedCollectionElementType(final Class<T> type) {
        Objects.requireNonNull(type, "type");
        this.valueType = TypeFactory.parameterizedClass(WeightedCollectionValue.class, type);
        this.elementType = TypeFactory.parameterizedClass(WeightedTable.class, type);
        return (SpongeKeyBuilder<WeightedTable<T>, WeightedCollectionValue<T>>) this;
    }

    @Override
    public <T> Key.Builder<WeightedTable<T>, WeightedCollectionValue<T>> weightedCollectionElementType(final TypeToken<T> type) {
        Objects.requireNonNull(type, "type");
        this.valueType = TypeFactory.parameterizedClass(WeightedCollectionValue.class, type.getType());
        this.elementType = TypeFactory.parameterizedClass(WeightedTable.class, type.getType());
        return (SpongeKeyBuilder<WeightedTable<T>, WeightedCollectionValue<T>>) this;
    }

    @Override
    public SpongeKeyBuilder<E, V> comparator(final Comparator<? super E> comparator) {
        Preconditions.checkNotNull(comparator);
        this.comparator = comparator;
        return this;
    }

    @Override
    public SpongeKeyBuilder<E, V> includesTester(final BiPredicate<? super E, ? super E> predicate) {
        Preconditions.checkNotNull(predicate);
        this.includesTester = predicate;
        return this;
    }

    @Override
    public Key<V> build0() {
        Objects.requireNonNull(this.valueType, "The value type must be set");
        Objects.requireNonNull(this.elementType, "The element type must be set");

        BiPredicate<? super E, ? super E> includesTester = this.includesTester;
        if (includesTester == null) {
            includesTester = (e, e2) -> false;
        }

        Comparator<? super E> comparator = this.comparator;
        if (comparator == null) {
            if (Comparable.class.isAssignableFrom(GenericTypeReflector.erase(this.elementType))) {
                //noinspection unchecked
                comparator = Comparator.comparing(o -> ((Comparable) o));
            } else {
                comparator = (o1, o2) -> {
                    if (o1.equals(o2))
                        return 0;
                    // There could be collisions, but yeah, what can you do about that..
                    if (o1.hashCode() > o2.hashCode())
                        return 1;
                    return -1;
                };
            }
        }

        Supplier<E> defaultValueSupplier = () -> null;
        final Class<?> rawType = GenericTypeReflector.erase(this.valueType);
        if (ListValue.class.isAssignableFrom(rawType)) {
            defaultValueSupplier = () -> (E) new ArrayList();
        } else if (SetValue.class.isAssignableFrom(rawType)) {
            defaultValueSupplier = () -> (E) new HashSet();
        } else if (WeightedCollectionValue.class.isAssignableFrom(rawType)) {
            defaultValueSupplier = () -> (E) new WeightedTable();
        } else if (MapValue.class.isAssignableFrom(rawType)) {
            defaultValueSupplier = () -> (E) new HashMap<>();
        }
        final SpongeKey<Value<E>, E> key = new SpongeKey<>(this.key, this.valueType, this.elementType, comparator, includesTester, defaultValueSupplier);
        KeyProvider.INSTANCE.register(this.key, (Key<Value<?>>) (Object) key);
        return (Key<V>) key;
    }

    @Override
    public Key.Builder<E, V> reset() {
        this.valueType = null;
        this.includesTester = null;
        this.comparator = null;
        return this;
    }
}
