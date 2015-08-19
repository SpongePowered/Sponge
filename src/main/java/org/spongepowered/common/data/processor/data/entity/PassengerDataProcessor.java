/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.data.processor.data.entity;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.common.data.util.DataUtil.getData;

import com.google.common.base.Optional;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutablePassengerData;
import org.spongepowered.api.data.manipulator.mutable.entity.PassengerData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.DataProcessor;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongePassengerData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongePassengerData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

public class PassengerDataProcessor implements DataProcessor<PassengerData, ImmutablePassengerData> {

    @Override
    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof net.minecraft.entity.Entity;
    }

    @SuppressWarnings("unused")
    @Override
    public Optional<PassengerData> from(DataHolder dataHolder) {
        if (supports(dataHolder)) {
            final SpongePassengerData passengerData = new SpongePassengerData();
            final Entity vehicle = (Entity) ((net.minecraft.entity.Entity) dataHolder).ridingEntity;
            net.minecraft.entity.Entity currentEntity = (net.minecraft.entity.Entity) vehicle;
            while (currentEntity != null && currentEntity.isRiding()) {
                currentEntity = currentEntity.ridingEntity;
            }
            final Entity baseVehicle = (Entity) currentEntity;
            return Optional.<PassengerData>of(passengerData.setVehicle(vehicle).setBaseVehicle(baseVehicle));
        } else {
            return Optional.absent();
        }

    }

    @Override
    public Optional<PassengerData> fill(DataHolder dataHolder, PassengerData manipulator) {
        if (supports(dataHolder)) {
            manipulator.set(Keys.VEHICLE, (Entity) ((net.minecraft.entity.Entity) dataHolder).ridingEntity);
            net.minecraft.entity.Entity currentEntity = ((net.minecraft.entity.Entity) dataHolder).ridingEntity;
            while (currentEntity != null && currentEntity.isRiding()) {
                currentEntity = currentEntity.ridingEntity;
            }
            final Entity baseVehicle = (Entity) currentEntity;
            manipulator.set(Keys.BASE_VEHICLE, baseVehicle);
            return Optional.of(manipulator);
        }
        return Optional.absent();
    }

    @Override
    public Optional<PassengerData> fill(DataHolder dataHolder, PassengerData manipulator, MergeFunction overlap) {
        if (supports(dataHolder)) {
            final PassengerData merged = overlap.merge(checkNotNull(manipulator).copy(), from(dataHolder).get());
            manipulator.set(Keys.BASE_VEHICLE, merged.baseVehicle().get()).set(Keys.VEHICLE, merged.vehicle().get());
            return Optional.of(manipulator);
        }
        return Optional.absent();
    }

    @Override
    public Optional<PassengerData> fill(DataContainer container, PassengerData passengerData) {
        passengerData.set(Keys.BASE_VEHICLE, getData(container, Keys.BASE_VEHICLE));
        passengerData.set(Keys.VEHICLE, getData(container, Keys.VEHICLE));
        return Optional.of(passengerData);
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, PassengerData manipulator) {
        if (supports(dataHolder)) {
            DataTransactionBuilder builder = DataTransactionBuilder.builder();
            final Entity prevVehicle = (Entity) ((net.minecraft.entity.Entity) dataHolder).riddenByEntity;
            net.minecraft.entity.Entity currentEntity = ((net.minecraft.entity.Entity) dataHolder).ridingEntity;
            while (currentEntity != null && currentEntity.isRiding()) {
                currentEntity = currentEntity.ridingEntity;
            }
            final Entity prevBaseVehicle = (Entity) currentEntity;
            final net.minecraft.entity.Entity entity = (net.minecraft.entity.Entity) dataHolder;
            final Entity newBaseVehicle = manipulator.baseVehicle().get();
            final Entity newVehicle = manipulator.vehicle().get();
            try {
                builder.replace(new ImmutableSpongeValue<Entity>(Keys.VEHICLE, prevVehicle), new ImmutableSpongeValue<Entity>(Keys.BASE_VEHICLE,
                        prevBaseVehicle));
                entity.ridingEntity = (net.minecraft.entity.Entity) newVehicle;
                builder.success(new ImmutableSpongeValue<Entity>(Keys.BASE_VEHICLE, newBaseVehicle),
                        new ImmutableSpongeValue<Entity>(Keys.VEHICLE, newVehicle)).result(DataTransactionResult.Type.SUCCESS);
                return builder.build();
            } catch (Exception e) {
                entity.riddenByEntity = (net.minecraft.entity.Entity) prevVehicle;
                builder.reject(new ImmutableSpongeValue<Entity>(Keys.VEHICLE, newVehicle),
                        new ImmutableSpongeValue<Entity>(Keys.BASE_VEHICLE, newBaseVehicle)).result(DataTransactionResult.Type.ERROR);
                return builder.build();
            }
        }
        return DataTransactionBuilder.failResult(manipulator.getValues());
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, PassengerData manipulator, MergeFunction function) {
        // TODO
        return null;
    }

    @Override
    public Optional<ImmutablePassengerData> with(Key<? extends BaseValue<?>> key, Object value, ImmutablePassengerData immutable) {
        // TODO: Should not return Optional.absent()
        return Optional.absent();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        if (supports(dataHolder)) {
            net.minecraft.entity.Entity entity = ((net.minecraft.entity.Entity) dataHolder);
            if (entity.isRiding()) {
                entity.mountEntity(null);
                return DataTransactionBuilder.builder().result(DataTransactionResult.Type.SUCCESS).build();

            } else {
                return DataTransactionBuilder.builder().result(DataTransactionResult.Type.FAILURE).build();
            }
        } else {
            return DataTransactionBuilder.builder().result(DataTransactionResult.Type.FAILURE).build();
        }
    }

    @Override
    public PassengerData create() {
        return new SpongePassengerData();
    }

    @Override
    public ImmutablePassengerData createImmutable() {
        return new ImmutableSpongePassengerData(null, null);
    }

    @Override
    public Optional<PassengerData> createFrom(DataHolder dataHolder) {
        if (supports(dataHolder)) {
            final Entity vehicle = (Entity) ((net.minecraft.entity.Entity) dataHolder).riddenByEntity;
            net.minecraft.entity.Entity currentEntity = ((net.minecraft.entity.Entity) dataHolder).ridingEntity;
            while (currentEntity != null && currentEntity.isRiding()) {
                currentEntity = currentEntity.ridingEntity;
            }
            final Entity baseVehicle = (Entity) currentEntity;
            return Optional.<PassengerData>of(new SpongePassengerData(vehicle, baseVehicle));
        }
        return Optional.absent();
    }

    @Override
    public Optional<PassengerData> build(DataView container) throws InvalidDataException {
        PassengerData passengerData = create();
        passengerData.set(Keys.BASE_VEHICLE, getData(container, Keys.BASE_VEHICLE));
        passengerData.set(Keys.VEHICLE, getData(container, Keys.VEHICLE));
        return Optional.of(passengerData);
    }
}
