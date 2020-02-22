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

import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataProvider;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.data.provider.DataProviderRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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
    default <V extends Value<E>, E> DataProvider<V, E> getProviderFor(Key<V> key) {
        return DataProviderRegistry.get().getProvider(key, this.getClass());
    }

    /**
     * Attempts to get a {@link DataProvider} for the given {@link Key}.
     *
     * This method can be overridden to support customized data providers,
     * which add caching, etc.
     *
     * @return The data providers
     */
    default List<DataProvider<?, ?>> getAllProviders() {
        return DataProviderRegistry.get().getAllProviders(this.getClass());
    }

    @Override
    default boolean supports(Key<?> key) {
        return this.getProviderFor(key).isSupported(this);
    }

    @Override
    default <E> Optional<E> get(Key<? extends Value<E>> key) {
        return this.getProviderFor(key).get(this);
    }

    @Override
    default <E, V extends Value<E>> Optional<V> getValue(Key<V> key) {
        return this.getProviderFor(key).getValue(this);
    }

    default Map<Key<?>, Object> getMappedValues() {
        final Map<Key<?>, Object> map = new HashMap<>();
        for (final DataProvider<?, ?> provider : this.getAllProviders()) {
            provider.get(this).ifPresent(value -> map.put(provider.getKey(), value));
        }
        return map;
    }

    @Override
    default Set<Key<?>> getKeys() {
        return this.getAllProviders().stream()
                .map(provider -> provider.get(this).map(v -> provider.getKey()).orElse(null))
                .filter(Objects::nonNull)
                .collect(ImmutableSet.toImmutableSet());
    }

    @Override
    default Set<Value.Immutable<?>> getValues() {
        return this.getAllProviders().stream()
                .map(provider -> provider.getValue(this).map(Value::asImmutable).orElse(null))
                .filter(Objects::nonNull)
                .collect(ImmutableSet.toImmutableSet());
    }
}
