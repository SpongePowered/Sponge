package org.spongepowered.common.data.provider;

import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataProvider;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;

public abstract class ImmutableDataProvider<V extends Value<E>, E> extends AbstractDataProvider<V, E> implements DataProvider<V, E> {

    public ImmutableDataProvider(Key<V> key) {
        super(key);
    }

    @Override
    public boolean allowsAsynchronousAccess(DataHolder dataHolder) {
        return false;
    }

    @Override
    public DataTransactionResult offer(DataHolder.Mutable dataHolder, E element) {
        return DataTransactionResult.failResult(Value.immutableOf(this.getKey(), element));
    }

    @Override
    public DataTransactionResult remove(DataHolder.Mutable dataHolder) {
        return DataTransactionResult.failNoData();
    }
}
