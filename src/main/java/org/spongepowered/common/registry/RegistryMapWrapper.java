package org.spongepowered.common.registry;

import org.spongepowered.api.CatalogKey;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public final class RegistryMapWrapper<V> implements Map<CatalogKey, V> {

    private final Map<CatalogKey, V> internal;

    public RegistryMapWrapper(Map<CatalogKey, V> internal) {
        this.internal = internal;
    }

    /**
     * Gets a value by its key.
     *
     * @param key The key
     * @return The optional value
     */
    public Optional<V> getOptional(final CatalogKey key) {
        return Optional.ofNullable(this.internal.get(key));
    }

    /**
     * Creates a copy of this map prepared for catalog field registration.
     *
     * @return A map of keys to values
     */
    public Map<String, V> forCatalogRegistration(Function<String, String> keyModifier) {
        final Map<String, V> map = new HashMap<>(this.size());
        for (final Map.Entry<CatalogKey, V> entry : this.entrySet()) {
            map.put(keyModifier.apply(entry.getKey().getValue()), entry.getValue());
        }
        return map;
    }

    @Override
    public Collection<V> values() {
        return Collections.unmodifiableCollection(this.internal.values());
    }

    @Override
    public int size() {
        return this.internal.size();
    }

    @Override
    public boolean isEmpty() {
        return this.internal.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.internal.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.internal.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return this.internal.get(key);
    }

    @Override
    public V put(CatalogKey key, V value) {
        return this.internal.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return this.internal.remove(key);
    }

    @Override
    public void putAll(Map<? extends CatalogKey, ? extends V> m) {
        this.internal.putAll(m);
    }

    @Override
    public void clear() {
        this.internal.clear();
    }

    @Override
    public Set<CatalogKey> keySet() {
        return this.internal.keySet();
    }

    @Override
    public Set<Entry<CatalogKey, V>> entrySet() {
        return this.internal.entrySet();
    }
}
