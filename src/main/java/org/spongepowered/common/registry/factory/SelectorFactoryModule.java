package org.spongepowered.common.registry.factory;

import org.spongepowered.api.text.selector.SelectorFactory;
import org.spongepowered.api.text.selector.Selectors;
import org.spongepowered.common.registry.FactoryRegistry;
import org.spongepowered.common.text.selector.SpongeSelectorFactory;

public class SelectorFactoryModule implements FactoryRegistry<SelectorFactory, Selectors> {

    public static final SpongeSelectorFactory INSTANCE = new SpongeSelectorFactory();

    @Override
    public Class<Selectors> getFactoryOwner() {
        return Selectors.class;
    }

    @Override
    public SelectorFactory initialize() {
        return INSTANCE;
    }
}
