package org.spongepowered.common.data.provider;

import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataProvider;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;

public abstract class AbstractDataProvider<V extends Value<E>, E> implements DataProvider<V, E> {

    private final Key<V> key;

    public AbstractDataProvider(Key<V> key) {
        this.key = key;
    }

    @Override
    public Key<V> getKey() {
        return this.key;
    }

    @Override
    public boolean allowsAsynchronousAccess(DataHolder dataHolder) {
        return false;
    }
}
