package org.spongepowered.common.data.manipulator.mutable.entity;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableCareerData;
import org.spongepowered.api.data.manipulator.mutable.entity.CareerData;
import org.spongepowered.api.data.type.Career;
import org.spongepowered.api.data.type.Careers;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeCareerData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractSingleCatalogData;

public class SpongeCareerData extends AbstractSingleCatalogData<Career, CareerData, ImmutableCareerData> implements CareerData {

    public SpongeCareerData(Career value) {
        super(CareerData.class, value, Keys.CAREER, ImmutableSpongeCareerData.class);
    }

    public SpongeCareerData() {
        this(Careers.ARMORER);
    }

    @Override
    public Value<Career> career() {
        return type();
    }
}
