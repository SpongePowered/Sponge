package org.spongepowered.common.data.builder.manipulator.mutable.entity;

import com.google.common.base.Optional;
import net.minecraft.entity.passive.EntityTameable;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableTameableData;
import org.spongepowered.api.data.manipulator.mutable.entity.TameableData;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeTameableData;
import org.spongepowered.common.data.processor.data.entity.TameableDataProcessor;

import java.util.UUID;

public class TameableDataBuilder implements DataManipulatorBuilder<TameableData, ImmutableTameableData> {
    @Override
    public TameableData create() {
        return new SpongeTameableData();
    }

    @Override
    public Optional<TameableData> createFrom(DataHolder dataHolder) {
        if(dataHolder instanceof EntityTameable) {
            Optional<UUID> dhAsTameable = TameableDataProcessor.getTamer((EntityTameable) dataHolder);
            return Optional.<TameableData>of(new SpongeTameableData(dhAsTameable.orNull()));
        } else {
            return Optional.absent();
        }
    }

    @Override
    public Optional<TameableData> build(DataView container) throws InvalidDataException {
        return null;
    }
}
