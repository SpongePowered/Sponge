package org.spongepowered.common.data.manipulator.immutable.entity;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutablePlayingData;
import org.spongepowered.api.data.manipulator.mutable.entity.PlayingData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableBooleanData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongePlayingData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

public class ImmutableSpongePlayingData extends AbstractImmutableBooleanData<ImmutablePlayingData, PlayingData> implements ImmutablePlayingData {

    public ImmutableSpongePlayingData(boolean value) {
        super(ImmutablePlayingData.class, value, Keys.IS_PLAYING, SpongePlayingData.class);
    }

    @Override
    protected ImmutableValue<?> getValueGetter() {
        return playing();
    }

    @Override
    public ImmutableValue<Boolean> playing() {
        return ImmutableSpongeValue.cachedOf(Keys.IS_PLAYING, false, this.value);
    }

}
