package org.spongepowered.common.data.type;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.spongepowered.api.attribute.Operation;

public class SpongeOperation implements Operation {
    private final String id;
    private final boolean immediately;

    public SpongeOperation(String id, boolean immediately) {
        this.id = id;
        this.immediately = immediately;
    }

    public SpongeOperation() {
        this("ADD_AMOUNT", false);
    }

    @Override
    public double getIncrementation(double base, double modifier, double currentValue) {
        switch (this.id) {
            case "MULTIPLY_BASE":
                return base * modifier;
            case "MULTIPLY":
                return currentValue * modifier;
            case "ADD_AMOUNT":
            default:
                return currentValue + modifier;
        }
    }

    @Override
    public boolean changeValueImmediately() {
        return this.immediately;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.id;
    }

    @Override
    public int compareTo(Operation o) {
        return new CompareToBuilder()
                .append(this.id, o.getId())
                .build();
    }
}
