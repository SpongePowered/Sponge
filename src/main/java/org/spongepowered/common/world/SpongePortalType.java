package org.spongepowered.common.world;

import org.spongepowered.api.world.PortalType;
import org.spongepowered.common.SpongeCatalogType;

public class SpongePortalType extends SpongeCatalogType implements PortalType {

    private final String name;

    public SpongePortalType(String id, String name) {
        super(id);
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
