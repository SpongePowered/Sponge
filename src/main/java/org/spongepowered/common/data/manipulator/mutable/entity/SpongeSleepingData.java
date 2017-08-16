package org.spongepowered.common.data.manipulator.mutable.entity;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableSleepingData;
import org.spongepowered.api.data.manipulator.mutable.entity.SleepingData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractBooleanData;

public class SpongeSleepingData extends AbstractBooleanData<SleepingData, ImmutableSleepingData> implements SleepingData {

	public SpongeSleepingData(boolean value) {
        super(SleepingData.class, value, Keys.IS_SLEEPING, ImmutableSleepingData.class, false);
    }

	public SpongeSleepingData() {
		this(false);
	}

    @Override
    public Value<Boolean> sleeping() {
        return this.getValueGetter();
    }

}
