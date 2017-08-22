package org.spongepowered.common.data.processor.data.entity;

import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableIgnoreSleepingData;
import org.spongepowered.api.data.manipulator.mutable.entity.IgnoreSleepingData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeIgnoreSleepingData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.mixin.core.entity.player.MixinEntityPlayerMP;

import java.util.Optional;

public class IgnoreSleepingDataProcessor extends
        AbstractEntitySingleDataProcessor<EntityPlayerMP, Boolean, Value<Boolean>, IgnoreSleepingData, ImmutableIgnoreSleepingData> {

    public IgnoreSleepingDataProcessor() {
        super(EntityPlayerMP.class, Keys.IGNORE_SLEEPING);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected boolean set(EntityPlayerMP dataHolder, Boolean value) {
        ((MixinEntityPlayerMP) (Object) dataHolder).setSleepingIgnored(value);
        return true;
    }

    @Override
    protected Optional<Boolean> getVal(EntityPlayerMP dataHolder) {
        return Optional.of(((MixinEntityPlayerMP) (Object) dataHolder).isSleepingIgnored());
    }

    @Override
    protected ImmutableValue<Boolean> constructImmutableValue(Boolean value) {
        return ImmutableSpongeValue.cachedOf(Keys.IGNORE_SLEEPING, false, value);
    }

    @Override
    protected Value<Boolean> constructValue(Boolean actualValue) {
        return new SpongeValue<Boolean>(Keys.IGNORE_SLEEPING, false, actualValue);
    }

    @Override
    protected IgnoreSleepingData createManipulator() {
        return new SpongeIgnoreSleepingData();
    }

}
