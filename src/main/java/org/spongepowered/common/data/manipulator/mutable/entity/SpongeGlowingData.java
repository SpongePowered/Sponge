package org.spongepowered.common.data.manipulator.mutable.entity;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableGlowingData;
import org.spongepowered.api.data.manipulator.mutable.entity.GlowingData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeGlowingData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractBooleanData;
import org.spongepowered.common.data.util.DataConstants;

public class SpongeGlowingData extends AbstractBooleanData<GlowingData, ImmutableGlowingData> implements GlowingData {

    public SpongeGlowingData(boolean value) {
        super(GlowingData.class, value, Keys.GLOWING, ImmutableSpongeGlowingData.class, DataConstants.DEFAULT_GLOWING);
    }

    public SpongeGlowingData() {
        this(DataConstants.DEFAULT_GLOWING);
    }

    @Override
    public Value<Boolean> glowing() {
        return this.getValueGetter();
    }
}
