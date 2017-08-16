package org.spongepowered.common.data.processor.data.entity;

import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableSleepingData;
import org.spongepowered.api.data.manipulator.mutable.entity.SleepingData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeSleepingData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;

public class PlayerSleepingDataProcessor extends AbstractEntitySingleDataProcessor<EntityPlayerMP, Boolean, Value<Boolean>, SleepingData, ImmutableSleepingData> {

    public PlayerSleepingDataProcessor() {
		super(EntityPlayerMP.class, Keys.IS_SLEEPING);
	}

    @Override
    public boolean set(EntityPlayerMP dataHolder, Boolean value) {
    	dataHolder.sleeping = value;
		return true;
    }

    @Override
    public SleepingData createManipulator() {
		return new SpongeSleepingData();
    }

	@Override
	public DataTransactionResult removeFrom(ValueContainer<?> container) {
		return DataTransactionResult.failNoData();
	}

	@Override
	public Optional<Boolean> getVal(EntityPlayerMP dataHolder) {
		return Optional.of(dataHolder.isPlayerSleeping());
	}

	@Override
	public ImmutableValue<Boolean> constructImmutableValue(Boolean value) {
		return ImmutableSpongeValue.cachedOf(Keys.IS_SLEEPING, false, value);
	}

	@Override
	public Value<Boolean> constructValue(Boolean actualValue) {
		return new SpongeValue<>(this.key, false, actualValue);
	}

}
