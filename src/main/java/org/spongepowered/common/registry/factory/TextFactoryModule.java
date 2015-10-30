package org.spongepowered.common.registry.factory;

import org.spongepowered.api.text.TextFactory;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.registry.FactoryRegistry;
import org.spongepowered.common.text.SpongeTextFactory;

public class TextFactoryModule implements FactoryRegistry<TextFactory, Texts> {

    @Override
    public Class<Texts> getFactoryOwner() {
        return Texts.class;
    }

    @Override
    public TextFactory initialize() {
        return new SpongeTextFactory();
    }
}
