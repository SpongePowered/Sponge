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
package org.spongepowered.common.data.provider;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.spongepowered.api.data.DataProvider;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.data.key.SpongeKey;
import org.spongepowered.common.data.provider.block.entity.TileEntityDataProviders;
import org.spongepowered.common.data.provider.block.location.LocationDataProviders;
import org.spongepowered.common.data.provider.block.state.BlockStateDataProviders;
import org.spongepowered.common.data.provider.entity.EntityDataProviders;
import org.spongepowered.common.data.provider.generic.GenericDataProviders;
import org.spongepowered.common.data.provider.inventory.InventoryDataProviders;
import org.spongepowered.common.data.provider.item.ItemDataProviders;
import org.spongepowered.common.data.provider.item.stack.ItemStackDataProviders;
import org.spongepowered.common.data.provider.nbt.NBTDataProviders;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DataProviderRegistry {

    private static class LookupKey {

        final Class<?> holderType;

        final Key<?> key;
        private LookupKey(final Class<?> holderType, final Key<?> key) {
            this.holderType = holderType;
            this.key = key;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final LookupKey lookupKey = (LookupKey) o;
            return this.holderType.equals(lookupKey.holderType) &&
                    this.key.equals(lookupKey.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.holderType, this.key);
        }

    }
    private final Multimap<Key<?>, DataProvider<?,?>> dataProviders = HashMultimap.create();
    private final Map<LookupKey, DataProvider<?,?>> dataProviderCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, DataProviderLookup> dataProviderLookupCache = new ConcurrentHashMap<>();

    private static boolean filterHolderType(final DataProvider<?,?> provider, final Class<?> holderType) {
        // Filter out data providers of which we know that they will never be relevant.
        if (provider instanceof AbstractDataProvider.KnownHolderType) {
            final Class<?> foundHolderType = ((AbstractDataProvider.KnownHolderType) provider).getHolderType();
            return foundHolderType.isAssignableFrom(holderType);
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private DataProvider<?,?> loadProvider(final LookupKey key) {
        return this.buildDelegate((Key<Value<Object>>) key.key, provider -> DataProviderRegistry.filterHolderType(provider, key.holderType));
    }

    @SuppressWarnings(value = {"unchecked", "rawtypes"})
    private DataProviderLookup loadProviderLookup(final Class<?> holderType) {
        final Stream<DataProvider> stream = this.dataProviders.keySet().stream()
                .map(key -> this.getProvider((Key) key, holderType))
                .filter(provider -> !(provider instanceof EmptyDataProvider));
        final Map<Key<?>, DataProvider<?, ?>> map = stream.collect(Collectors.toMap(p -> (Key<?>) p.getKey(), p -> (DataProvider<?, ?>) p));
        return new DataProviderLookup(map);
    }

    /**
     * Constructs a delegate {@link DataProvider} for the given list of {@link DataProvider}s.
     *
     * @param key The key
     * @param providers The providers
     * @param <V> The value type
     * @param <E> The element type
     * @return The delegate data provider
     */
    @SuppressWarnings("unchecked")
    private static <V extends Value<E>, E> DataProvider<V, E> buildDelegateProvider(final Key<V> key, final List<DataProvider<V, E>> providers) {
        if (providers.isEmpty()) {
            return ((SpongeKey<V, E>) key).getEmptyDataProvider();
        }
        if (providers.size() == 1) {
            return providers.get(0);
        }
        return new DelegateDataProvider<>(key, providers);
    }

    /**
     * Builds a data provider lookup for the specified data holder type.
     *
     * @param dataHolderType The data holder type
     * @return The built lookup
     */
    public DataProviderLookup getProviderLookup(final Class<?> dataHolderType) {
        return this.dataProviderLookupCache.computeIfAbsent(dataHolderType, this::loadProviderLookup);
    }

    /**
     * Builds a data provider lookup.
     *
     * @param predicate The predicate to filter data providers
     * @return The built lookup
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public DataProviderLookup buildLookup(final Predicate<DataProvider<?,?>> predicate) {
        final Stream<DataProvider> stream = this.dataProviders.keySet().stream()
                .map(key -> DataProviderRegistry.buildDelegateProvider((Key) key, (List) this.dataProviders.get(key).stream().filter(predicate)));
        final Map<Key<?>, DataProvider<?, ?>> map = stream.collect(Collectors.toMap(p -> (Key<?>) p.getKey(), p -> (DataProvider<?, ?>) p));
        return new DataProviderLookup(map);
    }

    /**
     * Builds a delegate {@link DataProvider} from the {@link DataProvider} that
     * are accepted by the given {@link Predicate}.
     *
     * @param key The key
     * @param predicate The predicate
     * @param <V> The value type
     * @param <E> The element type
     * @return The delegate data provider
     */
    private  <V extends Value<E>, E> DataProvider<V, E> buildDelegate(final Key<V> key, final Predicate<DataProvider<V, E>> predicate) {
        @SuppressWarnings({"unchecked", "rawtypes"})
        final Collection<DataProvider<V, E>> providers = (Collection) this.dataProviders.get(key);
        return DataProviderRegistry.buildDelegateProvider(key, providers.stream()
                .filter(predicate)
                .collect(Collectors.toList()));
    }

    /**
     * Gets a delegate data providers for all the keys
     * registered for the specified data holder type.
     *
     * @return The delegate data provider
     */
    public Collection<DataProvider<?,?>> getAllProviders(final Class<?> dataHolderType) {
        return this.getProviderLookup(dataHolderType).getAllProviders();
    }

    /**
     * Gets a delegate data provider for the given {@link Key} and data holder type.
     *
     * @param key The key
     * @param dataHolderType The data holder type
     * @param <V> The value type
     * @param <E> The element type of the value
     * @return The delegate data provider
     */
    @SuppressWarnings({"unchecked"})
    public <V extends Value<E>, E> DataProvider<V, E> getProvider(final Key<V> key, final Class<?> dataHolderType) {
        return (DataProvider<V, E>) this.dataProviderCache.computeIfAbsent(new LookupKey(dataHolderType, key), this::loadProvider);
    }

    /**
     * Registers a new {@link DataProvider}.
     *
     * @param provider The data provider
     */
    public void register(final DataProvider<?,?> provider) {
        this.dataProviders.put(provider.getKey(), provider);
        this.dataProviderCache.clear();
        this.dataProviderLookupCache.clear();
    }

    public void registerDefaultProviders() {
        this.registerDefaultProviders(
                new LocationDataProviders(),
                new BlockStateDataProviders(),
                new NBTDataProviders(),
                new ItemDataProviders(),
                new TileEntityDataProviders(),
                new GenericDataProviders(),
                new ItemStackDataProviders(),
                new InventoryDataProviders(),
                new EntityDataProviders()
        );
    }

    private void registerDefaultProviders(DataProviderRegistratorBuilder... dataProviderRegistratorBuilders) {
        for (DataProviderRegistratorBuilder dataProviderRegistratorBuilder : dataProviderRegistratorBuilders) {
            dataProviderRegistratorBuilder.register();
        }
    }



}
