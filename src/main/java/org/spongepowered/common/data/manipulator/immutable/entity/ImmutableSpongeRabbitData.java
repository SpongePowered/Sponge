package org.spongepowered.common.data.manipulator.immutable.entity;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableRabbitData;
import org.spongepowered.api.data.manipulator.mutable.entity.RabbitData;
import org.spongepowered.api.data.type.RabbitType;
import org.spongepowered.api.data.type.RabbitTypes;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableSingleCatalogData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeRabbitData;

public class ImmutableSpongeRabbitData extends AbstractImmutableSingleCatalogData<RabbitType, ImmutableRabbitData, RabbitData> implements ImmutableRabbitData {

    public ImmutableSpongeRabbitData(RabbitType type) {
        super(ImmutableRabbitData.class, type, RabbitTypes.WHITE, Keys.RABBIT_TYPE, SpongeRabbitData.class);
    }

}
