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
package org.spongepowered.common.data.holder;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataProvider;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.CollectionValue;
import org.spongepowered.api.data.value.MapValue;
import org.spongepowered.api.data.value.MergeFunction;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.common.data.key.SpongeKey;
import org.spongepowered.common.util.DataUtil;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings({"unchecked", "rawtypes"})
public interface SpongeMutableDataHolder extends SpongeDataHolder, DataHolder.Mutable {

    // Implementation Utility

    default List<DataHolder.Mutable> impl$mutableDelegateDataHolder() {
        return this.impl$delegateDataHolder().stream()
                .filter(dh -> dh instanceof DataHolder.Mutable)
                .map(DataHolder.Mutable.class::cast)
                .collect(Collectors.toList());
    }

    default <E, V extends Value<E>> DataTransactionResult impl$applyTransaction(Key<V> key, BiFunction<DataProvider<V, E>, Mutable, DataTransactionResult> function, Supplier<DataTransactionResult> defaultResult) {
        for (Mutable dataHolder : this.impl$mutableDelegateDataHolder()) {
            // Offer to the first available mutable data holder
            final DataProvider<V, E> dataProvider = this.impl$getProviderFor(key, dataHolder);
            if (dataProvider.isSupported(dataHolder)) {
                return function.apply(dataProvider, dataHolder);
            }
        }
        return defaultResult.get();
    }

    // Mutable Implementation

    @Override
    default <E> DataTransactionResult offer(Key<? extends Value<E>> key, E value) {
        return this.impl$applyTransaction(key, (p, m) -> p.offer(m, value),
                () -> DataTransactionResult.failResult(Value.immutableOf(key, value)));
    }

    @Override
    default DataTransactionResult offer(Value<?> value) {
        return this.impl$applyTransaction(value.key(), (p, m) -> ((DataProvider<Value<?>, ?>) p).offerValue(m, value),
                () -> DataTransactionResult.failResult(value.asImmutable()));
    }

    @Override
    default <E> DataTransactionResult offerSingle(Key<? extends CollectionValue<E, ?>> key, E element) {
        final SpongeKey<? extends CollectionValue<E, Collection<E>>, Collection<E>> key0 =
                (SpongeKey<? extends CollectionValue<E, Collection<E>>, Collection<E>>) key;
        return this.impl$applyTransaction(key0, (p, m) -> {
                    final Collection<E> collection = p.get(m)
                            .map(DataUtil::ensureMutable)
                            .orElseGet(key0.getDefaultValueSupplier());
                    if (!collection.add(element)) {
                        return DataTransactionResult.failNoData();
                    }
                    return p.offer(m, collection);
                },
                DataTransactionResult::failNoData);
    }

    @Override
    default <E> DataTransactionResult offerAll(Key<? extends CollectionValue<E, ?>> key, Collection<? extends E> elements) {
        final SpongeKey<? extends CollectionValue<E, Collection<E>>, Collection<E>> key0 =
                (SpongeKey<? extends CollectionValue<E, Collection<E>>, Collection<E>>) key;
        return this.impl$applyTransaction(key0, (p, m) -> {
                    final Collection<E> collection = p.get(m)
                            .map(DataUtil::ensureMutable)
                            .orElseGet(key0.getDefaultValueSupplier());
                    if (!collection.addAll(elements)) {
                        return DataTransactionResult.failNoData();
                    }
                    return p.offer(m, collection);
                },
                DataTransactionResult::failNoData);
    }

    @Override
    default <K, V> DataTransactionResult offerSingle(Key<? extends MapValue<K, V>> key, K valueKey, V value) {
        return this.impl$applyTransaction(key, (p, m) -> {
                    final Map<K, V> kvMap = p.get(m).map(DataUtil::ensureMutable).orElseGet(((SpongeKey) key).getDefaultValueSupplier());
                    kvMap.put(valueKey, value);
                    return p.offer(m, kvMap);
                },
                DataTransactionResult::failNoData);
    }

    @Override
    default <K, V> DataTransactionResult offerAll(Key<? extends MapValue<K, V>> key, Map<? extends K, ? extends V> values) {
        if (values.isEmpty()) {
            return DataTransactionResult.failNoData();
        }
        return this.impl$applyTransaction(key, (p, m) -> {
                    final Map<K, V> kvMap = p.get(m).map(DataUtil::ensureMutable).orElseGet(((SpongeKey) key).getDefaultValueSupplier());
                    kvMap.putAll(values);
                    return p.offer(m, kvMap);
                },
                DataTransactionResult::failNoData);
    }

    @Override
    default <E> DataTransactionResult removeSingle(Key<? extends CollectionValue<E, ?>> key, E element) {
        final SpongeKey<? extends CollectionValue<E, Collection<E>>, Collection<E>> key0 =
                (SpongeKey<? extends CollectionValue<E, Collection<E>>, Collection<E>>) key;
        return this.impl$applyTransaction(key0, (p, m) -> {
            final Optional<Collection<E>> optCollection = p.get(m).map(DataUtil::ensureMutable);
            if (!optCollection.isPresent()) {
                return DataTransactionResult.failNoData();
            }
            final Collection<E> collection = optCollection.get();
            if (!collection.remove(element)) {
                return DataTransactionResult.failNoData();
            }
            return p.offer(m, collection);
        }, DataTransactionResult::failNoData);
    }

    @Override
    default <E> DataTransactionResult removeAll(Key<? extends CollectionValue<E, ?>> key, Collection<? extends E> elements) {
        if (elements.isEmpty()) {
            return DataTransactionResult.failNoData();
        }
        final SpongeKey<? extends CollectionValue<E, Collection<E>>, Collection<E>> key0 =
                (SpongeKey<? extends CollectionValue<E, Collection<E>>, Collection<E>>) key;
        return this.impl$applyTransaction(key0, (p, m) -> {
            final Optional<Collection<E>> optCollection = p.get(m).map(DataUtil::ensureMutable);
            if (!optCollection.isPresent()) {
                return DataTransactionResult.failNoData();
            }
            final Collection<E> collection = optCollection.get();
            if (!collection.removeAll(elements)) {
                return DataTransactionResult.failNoData();
            }
            return p.offer(m, collection);
        }, DataTransactionResult::failNoData);
    }


    @Override
    default <K> DataTransactionResult removeKey(Key<? extends MapValue<K, ?>> key, K mapKey) {
        final SpongeKey<? extends MapValue<K, Object>, Map<K, Object>> key0 =
                (SpongeKey<? extends MapValue<K, Object>, Map<K, Object>>) key;
        return this.impl$applyTransaction(key0, (p, m) -> {
            final Optional<? extends Map<K, ?>> optMap = p.get(m).map(DataUtil::ensureMutable);
            if (!optMap.isPresent() || !optMap.get().containsKey(mapKey)) {
                return DataTransactionResult.failNoData();
            }
            final Map<K, ?> map = optMap.get();
            map.remove(mapKey);
            return ((DataProvider) p).offer(m, map);
        }, DataTransactionResult::failNoData);
    }

    @Override
    default <K, V> DataTransactionResult removeAll(Key<? extends MapValue<K, V>> key, Map<? extends K, ? extends V> values) {
        if (values.isEmpty()) {
            return DataTransactionResult.failNoData();
        }
        return this.impl$applyTransaction(key, (p, m) -> {
            final Optional<? extends Map<K, ?>> optMap = p.get(m).map(DataUtil::ensureMutable);
            if (!optMap.isPresent()) {
                return DataTransactionResult.failNoData();
            }
            final Map<K, ?> map = optMap.get();
            for (final Map.Entry<? extends K, ? extends V> entry : values.entrySet()) {
                map.remove(entry.getKey(), entry.getValue());
            }
            return ((DataProvider) p).offer(m, map);
        }, DataTransactionResult::failNoData);
    }

    @Override
    default DataTransactionResult remove(Key<?> key) {
        return this.impl$applyTransaction((Key) key, DataProvider::remove, DataTransactionResult::failNoData);
    }

    @Override
    default DataTransactionResult remove(Value<?> value) {
        return this.impl$applyTransaction(value.key(), (p, m) -> {
            final Optional<?> opt = p.get(m);
            if (opt.isPresent() && opt.get().equals(value.get())) {
                return p.remove(m);
            }
            return DataTransactionResult.failNoData();
        }, DataTransactionResult::failNoData);
    }

    @Override
    default DataTransactionResult copyFrom(ValueContainer that, MergeFunction function) {
        Objects.requireNonNull(that, "that");
        Objects.requireNonNull(function, "function");
        final DataTransactionResult.Builder builder = DataTransactionResult.builder();
        boolean success = false;
        if (function == MergeFunction.REPLACEMENT_PREFERRED) {
            // Produce less garbage if we know we don't have to merge any values
            for (final Value<?> replacement : that.getValues()) {
                final DataTransactionResult result = this.offer(replacement);
                builder.absorbResult(result);
                if (result.isSuccessful()) {
                    success = true;
                }
            }
        } else if (function == MergeFunction.ORIGINAL_PREFERRED) {
            // Produce less garbage if we know we don't have to merge any values
            for (final Value replacement : that.getValues()) {
                final Key<Value<Object>> key = replacement.key();
                if (this.get(key).isPresent()) {
                    continue;
                }
                final Value merged = function.merge(null, replacement);
                final DataTransactionResult result = this.offer(merged);
                builder.absorbResult(result);
                if (result.isSuccessful()) {
                    success = true;
                }
            }
        } else {
            for (final Value replacement : that.getValues()) {
                final Key<Value<Object>> key = replacement.key();
                final @Nullable Value original = this.getValue(key).map(Value::asImmutable).orElse(null);
                final Value merged = function.merge(original, replacement);
                final DataTransactionResult result = this.offer(merged);
                builder.absorbResult(result);
                if (result.isSuccessful()) {
                    success = true;
                }
            }
        }
        if (success) {
            builder.result(DataTransactionResult.Type.SUCCESS);
        } else {
            builder.result(DataTransactionResult.Type.FAILURE);
        }
        return builder.build();
    }

    @Override
    default DataTransactionResult undo(DataTransactionResult result) {
        if (result.replacedData().isEmpty() && result.successfulData().isEmpty()) {
            return DataTransactionResult.successNoData();
        }
        final DataTransactionResult.Builder builder = DataTransactionResult.builder();
        for (final Value<?> value : result.replacedData()) {
            builder.absorbResult(this.offer(value));
        }
        for (final Value<?> value : result.successfulData()) {
            builder.absorbResult(this.remove(value));
        }
        return DataTransactionResult.failNoData();
    }

    // Delegated

    @Override
    default <E> DataTransactionResult tryOffer(Key<? extends Value<E>> key, E value) {
        final DataTransactionResult result = this.offer(key, value);
        if (!result.isSuccessful()) {
            throw new IllegalArgumentException("Failed offer transaction!");
        }
        return result;
    }

    @Override
    default DataTransactionResult offerAll(CollectionValue<?, ?> value) {
        return this.offerAll((Key<? extends CollectionValue<Object, ?>>) value.key(), value.get());
    }

    @Override
    default DataTransactionResult offerAll(MapValue<?, ?> value) {
        return this.offerAll((Key<? extends MapValue<Object, Object>>) value.key(), value.get());
    }

    @Override
    default DataTransactionResult removeAll(CollectionValue<?, ?> value) {
        return this.removeAll((Key<? extends CollectionValue<Object, ?>>) value.key(), value.get());
    }

    @Override
    default DataTransactionResult removeAll(MapValue<?, ?> value) {
        return this.removeAll((Key<? extends MapValue<Object, Object>>) value.key(), value.get());
    }

}
