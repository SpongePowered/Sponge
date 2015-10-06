package org.spongepowered.common.data.type;

import org.spongepowered.api.data.type.StairShape;

public class SpongeStairShape implements StairShape {

    private final String id;

    public SpongeStairShape(String id) {
        this.id = id;
    }

    public SpongeStairShape() {
        this("STRAIGHT");
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
