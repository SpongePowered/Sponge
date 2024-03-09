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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataProvider;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.data.SpongeDataManager;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public interface SpongeDataHolder extends DataHolder {

    /**
     * Attempts to get a {@link DataProvider} for the given {@link Key}.
     *
     * This method can be overridden to support customized data providers,
     * which add caching, etc.
     *
     * @param key The key to get the provider for
     * @param <V> The value type
     * @param <E> The element type
     * @return The data provider
     */
    default <V extends Value<E>, E> DataProvider<V, E> impl$getProviderFor(final Key<V> key, final DataHolder dataHolder) {
        Objects.requireNonNull(key, "key");
        return SpongeDataManager.getProviderRegistry().getProvider(key, dataHolder.getClass());
    }

    /**
     * Override this to delegate the data holder
     *
     * @return the delegate data holder
     */
    default List<DataHolder> impl$delegateDataHolder() {
        return Collections.singletonList(this);
    }

    default Collection<DataProvider<?, ?>> impl$getAllProviders(final DataHolder dataHolder) {
        return SpongeDataManager.getProviderRegistry().getAllProviders(dataHolder.getClass());
    }

    default <T, E, V extends Value<E>> T impl$apply(final Key<V> key, final BiFunction<DataProvider, DataHolder, T> function, final Supplier<T> defaultResult) {
        for (final DataHolder dataHolder : this.impl$delegateDataHolder()) {
            final DataProvider<V, E> dataProvider = this.impl$getProviderFor(key, dataHolder);
            if (dataProvider.isSupported(dataHolder)) {
                return function.apply(dataProvider, dataHolder);
            }
        }
        return defaultResult.get();
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    default boolean supports(final Key<?> key) {
        // XX: JDT cannot infer this cast
        return (boolean) this.impl$apply((Key) key, (p, d) -> true, () -> false);
    }

    @Override
    default <E> Optional<E> get(final Key<? extends Value<E>> key) {
        return this.impl$apply(key, DataProvider::get, Optional::empty);
    }

    @Override
    default <E, V extends Value<E>> Optional<V> getValue(final Key<V> key) {
        return this.impl$apply(key, DataProvider::value, Optional::empty);
    }

    default Map<Key<?>, Object> impl$getMappedValues() {
        return this.impl$delegateDataHolder().stream()
                .flatMap(dh -> this.impl$getAllProviders(dh).stream()
                        .map(provider -> provider.value(dh).orElse(null))
                        .filter(Objects::nonNull)
                        .map(Value::asImmutable))
                .collect(ImmutableMap.toImmutableMap(Value::key, Value::get));
    }

    @Override
    default Set<Key<?>> getKeys() {
        return this.impl$delegateDataHolder().stream()
                .flatMap(dh -> this.impl$getAllProviders(dh).stream()
                        .filter(provider -> provider.get(dh).isPresent()).map(DataProvider::key))
                .collect(ImmutableSet.toImmutableSet());
    }

    @Override
    default Set<Value.Immutable<?>> getValues() {
        return this.impl$delegateDataHolder().stream()
                .flatMap(dh -> this.impl$getAllProviders(dh).stream()
                        .map(provider -> provider.value(dh).orElse(null))
                        .filter(Objects::nonNull)
                        .map(Value::asImmutable))
                .collect(ImmutableSet.toImmutableSet());
    }
}
