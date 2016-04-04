package org.spongepowered.common.data.processor.data.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.entity.Entity;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableGlowingData;
import org.spongepowered.api.data.manipulator.mutable.entity.GlowingData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeGlowingData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;

public class GlowingDataProcessor extends AbstractEntitySingleDataProcessor<Entity, Boolean, Value<Boolean>, GlowingData, ImmutableGlowingData> {

    public GlowingDataProcessor() {
        super(Entity.class, Keys.GLOWING);
    }

    @Override
    protected boolean set(Entity dataHolder, Boolean value) {
        dataHolder.setGlowing(checkNotNull(value));
        return true;
    }

    @Override
    protected Optional<Boolean> getVal(Entity dataHolder) {
        return Optional.of(dataHolder.isGlowing());
    }

    @Override
    protected ImmutableValue<Boolean> constructImmutableValue(Boolean value) {
        return ImmutableSpongeValue.cachedOf(Keys.GLOWING, value, DataConstants.DEFAULT_GLOWING);
    }

    @Override
    protected Value<Boolean> constructValue(Boolean actualValue) {
        return new SpongeValue<>(Keys.GLOWING, DataConstants.DEFAULT_GLOWING, actualValue);
    }

    @Override
    protected GlowingData createManipulator() {
        return new SpongeGlowingData();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
