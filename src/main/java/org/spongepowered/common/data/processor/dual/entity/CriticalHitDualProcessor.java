package org.spongepowered.common.data.processor.dual.entity;

import net.minecraft.entity.projectile.EntityArrow;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableCriticalHitData;
import org.spongepowered.api.data.manipulator.mutable.entity.CriticalHitData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeCriticalHitData;
import org.spongepowered.common.data.processor.dual.common.AbstractSingleTargetDualProcessor;
import org.spongepowered.common.data.value.SpongeValueFactory;

import java.util.Optional;

public class CriticalHitDualProcessor
        extends AbstractSingleTargetDualProcessor<EntityArrow, Boolean, Value<Boolean>, CriticalHitData, ImmutableCriticalHitData> {

    public CriticalHitDualProcessor() {
        super(EntityArrow.class, Keys.CRITICAL_HIT);
    }

    @Override
    protected Value<Boolean> constructValue(Boolean actualValue) {
        return SpongeValueFactory.getInstance().createValue(Keys.CRITICAL_HIT, actualValue, false);
    }

    @Override
    protected boolean set(EntityArrow entity, Boolean value) {
        entity.setIsCritical(value);
        return true;
    }

    @Override
    protected Optional<Boolean> getVal(EntityArrow entity) {
        return Optional.of(entity.getIsCritical());
    }

    @Override
    protected ImmutableValue<Boolean> constructImmutableValue(Boolean value) {
        return constructValue(value).asImmutable();
    }

    @Override
    protected CriticalHitData createManipulator() {
        return new SpongeCriticalHitData();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
