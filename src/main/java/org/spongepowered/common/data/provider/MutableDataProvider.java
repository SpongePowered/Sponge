package org.spongepowered.common.data.provider;

import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataProvider;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;

import java.util.Optional;

public abstract class MutableDataProvider<V extends Value<E>, E> extends AbstractDataProvider<V, E> implements DataProvider<V, E> {

    public MutableDataProvider(Key<V> key) {
        super(key);
    }

    @Override
    public boolean allowsAsynchronousAccess(DataHolder dataHolder) {
        return false;
    }

    @Override
    public final <I extends DataHolder.Immutable<I>> Optional<I> with(I immutable, E element) {
        return Optional.empty();
    }

    @Override
    public final <I extends DataHolder.Immutable<I>> Optional<I> withValue(I immutable, V value) {
        return Optional.empty();
    }

    @Override
    public final <I extends DataHolder.Immutable<I>> Optional<I> without(I immutable) {
        return Optional.empty();
    }
}
