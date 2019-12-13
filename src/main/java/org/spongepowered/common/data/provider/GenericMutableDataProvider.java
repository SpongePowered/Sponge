package org.spongepowered.common.data.provider;

import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;

@SuppressWarnings("unchecked")
public abstract class GenericMutableDataProvider<H, E> extends GenericMutableDataProviderBase<H, Value<E>, E> {

    public GenericMutableDataProvider(Key<? extends Value<E>> key) {
        super((Key<Value<E>>) key);
    }
}
