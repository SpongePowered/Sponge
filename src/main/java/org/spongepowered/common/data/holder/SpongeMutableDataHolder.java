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

import static java.util.Objects.requireNonNull;

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
import org.spongepowered.common.data.util.EnsureMutable;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings({"unchecked", "rawtypes"})
public interface SpongeMutableDataHolder extends SpongeDataHolder, DataHolder.Mutable {

    @Override
    default <E> DataTransactionResult tryOffer(Key<? extends Value<E>> key, E value) {
        final DataTransactionResult result = this.offer(key, value);
        if (!result.isSuccessful()) {
            throw new IllegalArgumentException("Failed offer transaction!");
        }
        return result;
    }

    @Override
    default <E> DataTransactionResult offer(Key<? extends Value<E>> key, E value) {
        return this.getProviderFor(key).offer(this, value);
    }

    @Override
    default DataTransactionResult offer(Value<?> value) {
        return ((DataProvider) this.getProviderFor(value.getKey())).offerValue(this, value);
    }

    @Override
    default <E> DataTransactionResult offerSingle(Key<? extends CollectionValue<E, ?>> key, E element) {
        final SpongeKey<? extends CollectionValue<E, Collection<E>>, Collection<E>> key0 =
                (SpongeKey<? extends CollectionValue<E, Collection<E>>, Collection<E>>) key;
        final DataProvider<?, Collection<E>> provider = this.getProviderFor(key0);
        if (!provider.isSupported(this)) {
            return DataTransactionResult.failNoData();
        }
        final Collection<E> collection = provider.get(this)
                .map(EnsureMutable::ensureMutable)
                .orElseGet(key0.getDefaultValueSupplier());
        if (!collection.add(element)) {
            return DataTransactionResult.failNoData();
        }
        return provider.offer(this, collection);
    }

    @Override
    default <E> DataTransactionResult offerAll(Key<? extends CollectionValue<E, ?>> key, Collection<? extends E> elements) {
        final SpongeKey<? extends CollectionValue<E, Collection<E>>, Collection<E>> key0 =
                (SpongeKey<? extends CollectionValue<E, Collection<E>>, Collection<E>>) key;
        final DataProvider<?, Collection<E>> provider = this.getProviderFor(key0);
        if (!provider.isSupported(this)) {
            return DataTransactionResult.failNoData();
        }
        final Collection<E> collection = provider.get(this)
                .map(EnsureMutable::ensureMutable)
                .orElseGet(key0.getDefaultValueSupplier());
        if (!collection.addAll(elements)) {
            return DataTransactionResult.failNoData();
        }
        return provider.offer(this, collection);
    }

    @Override
    default DataTransactionResult offerAll(CollectionValue<?, ?> value) {
        return this.offerAll((Key<? extends CollectionValue<Object, ?>>) value.getKey(), value.get());
    }

    @Override
    default <K, V> DataTransactionResult offerSingle(Key<? extends MapValue<K, V>> key, K valueKey, V value) {
        final DataProvider<?, Map<K, V>> provider = this.getProviderFor(key);
        if (!provider.isSupported(this)) {
            return DataTransactionResult.failNoData();
        }
        final Map<K, V> copy = provider.get(this)
                .map(EnsureMutable::ensureMutable)
                .orElseGet(((SpongeKey) key).getDefaultValueSupplier());
        copy.put(valueKey, value);
        return provider.offer(this, copy);
    }

    @Override
    default <K, V> DataTransactionResult offerAll(Key<? extends MapValue<K, V>> key, Map<? extends K, ? extends V> values) {
        if (values.isEmpty()) {
            return DataTransactionResult.failNoData();
        }
        final DataProvider<?, Map<K, V>> provider = this.getProviderFor(key);
        if (!provider.isSupported(this)) {
            return DataTransactionResult.failNoData();
        }
        final Map<K, V> map = provider.get(this)
                .map(EnsureMutable::ensureMutable)
                .orElseGet(((SpongeKey) key).getDefaultValueSupplier());
        map.putAll(values);
        return provider.offer(this, map);
    }

    @Override
    default DataTransactionResult offerAll(MapValue<?, ?> value) {
        return this.offerAll((Key<? extends MapValue<Object, Object>>) value.getKey(), value.get());
    }

    @Override
    default <E> DataTransactionResult removeSingle(Key<? extends CollectionValue<E, ?>> key, E element) {
        final SpongeKey<? extends CollectionValue<E, Collection<E>>, Collection<E>> key0 =
                (SpongeKey<? extends CollectionValue<E, Collection<E>>, Collection<E>>) key;
        final DataProvider<?, Collection<E>> provider = this.getProviderFor(key0);
        if (!provider.isSupported(this)) {
            return DataTransactionResult.failNoData();
        }
        final Optional<Collection<E>> optCollection = provider.get(this)
                .map(EnsureMutable::ensureMutable);
        if (!optCollection.isPresent()) {
            return DataTransactionResult.failNoData();
        }
        final Collection<E> collection = optCollection.get();
        if (!collection.remove(element)) {
            return DataTransactionResult.failNoData();
        }
        return provider.offer(this, collection);
    }

    @Override
    default <E> DataTransactionResult removeAll(Key<? extends CollectionValue<E, ?>> key, Collection<? extends E> elements) {
        if (elements.isEmpty()) {
            return DataTransactionResult.failNoData();
        }
        final SpongeKey<? extends CollectionValue<E, Collection<E>>, Collection<E>> key0 =
                (SpongeKey<? extends CollectionValue<E, Collection<E>>, Collection<E>>) key;
        final DataProvider<?, Collection<E>> provider = this.getProviderFor(key0);
        if (!provider.isSupported(this)) {
            return DataTransactionResult.failNoData();
        }
        final Optional<Collection<E>> optCollection = provider.get(this)
                .map(EnsureMutable::ensureMutable);
        if (!optCollection.isPresent()) {
            return DataTransactionResult.failNoData();
        }
        final Collection<E> collection = optCollection.get();
        if (!collection.removeAll(elements)) {
            return DataTransactionResult.failNoData();
        }
        return provider.offer(this, collection);
    }

    @Override
    default DataTransactionResult removeAll(CollectionValue<?, ?> value) {
        return this.removeAll((Key<? extends CollectionValue<Object, ?>>) value.getKey(), value.get());
    }

    @Override
    default <K> DataTransactionResult removeKey(Key<? extends MapValue<K, ?>> key, K mapKey) {
        final SpongeKey<? extends MapValue<K, Object>, Map<K, Object>> key0 =
                (SpongeKey<? extends MapValue<K, Object>, Map<K, Object>>) key;
        final DataProvider<? extends MapValue<K, Object>, Map<K, Object>> provider = this.getProviderFor(key0);
        if (!provider.isSupported(this)) {
            return DataTransactionResult.failNoData();
        }
        final Optional<Map<K, Object>> optionalMap = provider.get(this);
        if (!optionalMap.isPresent() || !optionalMap.get().containsKey(mapKey)) {
            return DataTransactionResult.failNoData();
        }
        final Map<K, Object> map = EnsureMutable.ensureMutable(optionalMap.get());
        map.remove(mapKey);
        return provider.offer(this, map);
    }

    @Override
    default <K, V> DataTransactionResult removeAll(Key<? extends MapValue<K, V>> key, Map<? extends K, ? extends V> values) {
        if (values.isEmpty()) {
            return DataTransactionResult.failNoData();
        }
        final DataProvider<? extends MapValue<K, V>, Map<K, V>> provider = this.getProviderFor(key);
        if (!provider.isSupported(this)) {
            return DataTransactionResult.failNoData();
        }
        final Optional<Map<K, V>> optionalMap = provider.get(this)
                .map(EnsureMutable::ensureMutable);
        if (!optionalMap.isPresent()) {
            return DataTransactionResult.failNoData();
        }
        final Map<K, V> map = optionalMap.get();
        for (final Map.Entry<? extends K, ? extends V> entry : values.entrySet()) {
            map.remove(entry.getKey(), entry.getValue());
        }
        return provider.offer(this, map);
    }

    @Override
    default DataTransactionResult removeAll(MapValue<?, ?> value) {
        return this.removeAll((Key<? extends MapValue<Object, Object>>) value.getKey(), value.get());
    }

    @Override
    default DataTransactionResult remove(Key<?> key) {
        return this.getProviderFor(key).remove(this);
    }

    @Override
    default DataTransactionResult remove(Value<?> value) {
        final Key<Value<Object>> key = (Key<Value<Object>>) value.getKey();
        final DataProvider<?, Object> provider = this.getProviderFor(key);
        final Optional<Object> optionalElement = provider.get(this);
        if (!optionalElement.isPresent() || !optionalElement.get().equals(value.get())) {
            return DataTransactionResult.failNoData();
        }
        return provider.remove(this);
    }

    @Override
    default DataTransactionResult copyFrom(ValueContainer that, MergeFunction function) {
        requireNonNull(that, "that");
        requireNonNull(function, "function");
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
                final Key<Value<Object>> key = replacement.getKey();
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
                final Key<Value<Object>> key = replacement.getKey();
                @Nullable final Value original = this.getValue(key).map(Value::asImmutable).orElse(null);
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
        if (result.getReplacedData().isEmpty() && result.getSuccessfulData().isEmpty()) {
            return DataTransactionResult.successNoData();
        }
        final DataTransactionResult.Builder builder = DataTransactionResult.builder();
        for (final Value<?> value : result.getReplacedData()) {
            builder.absorbResult(this.offer(value));
        }
        for (final Value<?> value : result.getSuccessfulData()) {
            builder.absorbResult(this.remove(value));
        }
        return DataTransactionResult.failNoData();
    }
}
