package org.spongepowered.common.data.provider;

import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;

@SuppressWarnings("unchecked")
public abstract class GenericImmutableDataProvider<H, E> extends GenericImmutableDataProviderBase<H, Value<E>, E> {

    public GenericImmutableDataProvider(Key<? extends Value<E>> key) {
        super((Key<Value<E>>) key);
    }
}
