package org.spongepowered.common.data.manipulator.immutable.entity;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableCareerData;
import org.spongepowered.api.data.manipulator.mutable.entity.CareerData;
import org.spongepowered.api.data.type.Career;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableSingleCatalogData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeCareerData;

public class ImmutableSpongeCareerData extends AbstractImmutableSingleCatalogData<Career, ImmutableCareerData, CareerData> implements ImmutableCareerData {

    public ImmutableSpongeCareerData(Career value) {
        super(ImmutableCareerData.class, value, Keys.CAREER, SpongeCareerData.class);
    }
    @Override
    public ImmutableValue<Career> career() {
        return type();
    }
}
