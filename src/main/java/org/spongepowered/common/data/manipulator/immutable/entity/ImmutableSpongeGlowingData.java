package org.spongepowered.common.data.manipulator.immutable.entity;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableGlowingData;
import org.spongepowered.api.data.manipulator.mutable.entity.GlowingData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableBooleanData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeGlowingData;
import org.spongepowered.common.data.util.DataConstants;

public class ImmutableSpongeGlowingData extends AbstractImmutableBooleanData<ImmutableGlowingData, GlowingData> implements ImmutableGlowingData {

    public ImmutableSpongeGlowingData(boolean value) {
        super(ImmutableGlowingData.class, value, Keys.GLOWING, SpongeGlowingData.class, DataConstants.DEFAULT_GLOWING);
    }

    @Override
    public ImmutableValue<Boolean> glowing() {
        return this.getValueGetter();
    }
}
