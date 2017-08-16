package org.spongepowered.common.data.manipulator.immutable.entity;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableSleepingData;
import org.spongepowered.api.data.manipulator.mutable.entity.SleepingData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableBooleanData;

public class ImmutableSpongeSleepingData extends AbstractImmutableBooleanData<ImmutableSleepingData, SleepingData> implements ImmutableSleepingData {
	public ImmutableSpongeSleepingData(boolean value) {
        super(ImmutableSleepingData.class, value, Keys.IS_SLEEPING, SleepingData.class, false);
    }

    @Override
    public ImmutableValue<Boolean> sleeping() {
        return this.getValueGetter();
    }
}