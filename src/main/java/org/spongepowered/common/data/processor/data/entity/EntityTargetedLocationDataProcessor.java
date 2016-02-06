package org.spongepowered.common.data.processor.data.entity;

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.entity.Entity;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableTargetedLocationData;
import org.spongepowered.api.data.manipulator.mutable.TargetedLocationData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.mutable.SpongeTargetedLocationData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.interfaces.ITargetedLocation;

import java.util.Optional;

public final class EntityTargetedLocationDataProcessor extends AbstractEntitySingleDataProcessor<Entity, Vector3d, Value<Vector3d>,
        TargetedLocationData, ImmutableTargetedLocationData> {

    public EntityTargetedLocationDataProcessor() {
        super(Entity.class, Keys.TARGETED_LOCATION);
    }

    @Override
    protected boolean set(Entity entity, Vector3d value) {
        if (entity instanceof ITargetedLocation) {
            ((ITargetedLocation) entity).setTargetedLocation(value);
            return true;
        }

        return false;
    }

    @Override
    protected Optional<Vector3d> getVal(Entity entity) {
        if (entity instanceof ITargetedLocation) {
            return Optional.of(((ITargetedLocation) entity).getTargetedLocation());
        }

        return Optional.empty();
    }

    @Override
    protected ImmutableValue<Vector3d> constructImmutableValue(Vector3d value) {
        return new ImmutableSpongeValue<>(this.key, Vector3d.ZERO, value);
    }

    @Override
    protected Value<Vector3d> constructValue(Vector3d actualValue) {
        return new SpongeValue<>(this.key, Vector3d.ZERO, actualValue);
    }

    @Override
    protected TargetedLocationData createManipulator() {
        return new SpongeTargetedLocationData();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected boolean supports(Entity entity) {
        return entity instanceof ITargetedLocation;
    }

}
