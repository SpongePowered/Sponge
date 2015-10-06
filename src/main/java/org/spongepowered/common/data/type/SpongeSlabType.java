package org.spongepowered.common.data.type;

import org.spongepowered.api.data.type.SlabType;

public class SpongeSlabType implements SlabType {

    private final String id;

    public SpongeSlabType(String id) {
        this.id = id;
    }

    public SpongeSlabType() {
        this("STONE");
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.id;
    }
}
