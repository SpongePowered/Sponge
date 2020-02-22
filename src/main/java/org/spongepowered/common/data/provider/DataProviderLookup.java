package org.spongepowered.common.data.provider;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.DataProvider;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DataProviderLookup {

    private final Map<Key<?>, DataProvider<?,?>> providerMap = new ConcurrentHashMap<>();
    private final List<DataProvider<?,?>> providers;

    DataProviderLookup(Map<Key<?>, DataProvider<?, ?>> providerMap) {
        this.providerMap.putAll(providerMap);
        this.providers = ImmutableList.copyOf(providerMap.values());
    }

    /**
     * Gets all the non-empty delegate {@link DataProvider}s.
     *
     * @return The delegate data providers
     */
    public List<DataProvider<?,?>> getAllProviders() {
        return this.providers;
    }

    /**
     * Gets the delegate {@link DataProvider} for the given {@link Key}.
     *
     * @param key The key
     * @param <V> The value type
     * @param <E> The element type
     * @return The delegate provider
     */
    public <V extends Value<E>, E> DataProvider<V, E> getProvider(Key<V> key) {
        //noinspection unchecked
        return (DataProvider<V, E>) this.providerMap.computeIfAbsent(key, k -> new EmptyDataProvider<>(key));
    }
}
