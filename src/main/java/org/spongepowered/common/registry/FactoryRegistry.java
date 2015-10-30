package org.spongepowered.common.registry;

public interface FactoryRegistry<T, TFactoryOwner> {

    Class<TFactoryOwner> getFactoryOwner();

    T initialize();

}
