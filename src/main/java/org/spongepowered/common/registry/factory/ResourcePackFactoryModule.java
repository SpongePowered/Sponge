package org.spongepowered.common.registry.factory;

import org.spongepowered.api.resourcepack.ResourcePackFactory;
import org.spongepowered.api.resourcepack.ResourcePacks;
import org.spongepowered.common.registry.FactoryRegistry;
import org.spongepowered.common.resourcepack.SpongeResourcePackFactory;

public class ResourcePackFactoryModule implements FactoryRegistry<ResourcePackFactory, ResourcePacks> {

    @Override
    public Class<ResourcePacks> getFactoryOwner() {
        return ResourcePacks.class;
    }

    @Override
    public ResourcePackFactory initialize() {
        return new SpongeResourcePackFactory();
    }
}
