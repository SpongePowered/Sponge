package org.spongepowered.common.data.manipulator.mutable.entity;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableRabbitData;
import org.spongepowered.api.data.manipulator.mutable.entity.RabbitData;
import org.spongepowered.api.data.type.RabbitType;
import org.spongepowered.api.data.type.RabbitTypes;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractSingleCatalogData;

public class SpongeRabbitData extends AbstractSingleCatalogData<RabbitType, RabbitData, ImmutableRabbitData> implements RabbitData {

    public SpongeRabbitData(RabbitType value) {
        super(RabbitData.class, value, Keys.RABBIT_TYPE, null);
    }

    public SpongeRabbitData() {
        this(RabbitTypes.WHITE);
    }

}
