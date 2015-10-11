package org.spongepowered.common.data.manipulator.mutable.entity;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutablePlayingData;
import org.spongepowered.api.data.manipulator.mutable.entity.PlayingData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongePlayingData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractBooleanData;
import org.spongepowered.common.data.value.mutable.SpongeValue;

public class SpongePlayingData extends AbstractBooleanData<PlayingData, ImmutablePlayingData> implements PlayingData {

    public SpongePlayingData(boolean value) {
        super(PlayingData.class, value, Keys.IS_PLAYING, ImmutableSpongePlayingData.class);
    }

    public SpongePlayingData() {
        this(false);
    }

    @Override
    protected Value<?> getValueGetter() {
        return playing();
    }

    @Override
    public Value<Boolean> playing() {
        return new SpongeValue<>(Keys.IS_PLAYING, false, this.getValue());
    }

}
