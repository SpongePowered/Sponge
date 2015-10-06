package org.spongepowered.common.data.type;

import org.spongepowered.api.item.FireworkShape;

public class SpongeFireworkShape implements FireworkShape {

    private final String id;

    public SpongeFireworkShape() {
        this("BALL");
    }

    public SpongeFireworkShape(String id) {
        this.id = id;
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
