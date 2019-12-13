package org.spongepowered.common.data.provider;

import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.BoundedValue;

@SuppressWarnings("unchecked")
public abstract class GenericMutableBoundedDataProvider<H, E> extends GenericMutableDataProviderBase<H, BoundedValue<E>, E> {

    public GenericMutableBoundedDataProvider(Key<? extends BoundedValue<E>> key) {
        super((Key<BoundedValue<E>>) key);
    }
}
