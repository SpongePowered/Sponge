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

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableVehicleData;
import org.spongepowered.api.data.manipulator.mutable.entity.VehicleData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeVehicleData;
import org.spongepowered.common.data.processor.common.AbstractSpongeDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import java.util.Optional;

public class VehicleDataProcessor extends AbstractSpongeDataProcessor<VehicleData, ImmutableVehicleData> {

    @Override
    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof net.minecraft.entity.Entity;
    }

    @Override
    public Optional<VehicleData> from(DataHolder dataHolder) {
        if (supports(dataHolder)) {
            final SpongeVehicleData VehicleData = new SpongeVehicleData();
            final Entity vehicle = (Entity) ((net.minecraft.entity.Entity) dataHolder).ridingEntity;
            if (vehicle != null) {
                net.minecraft.entity.Entity currentEntity = (net.minecraft.entity.Entity) vehicle;
                while (currentEntity != null && currentEntity.isRiding()) {
                    currentEntity = currentEntity.ridingEntity;
                }
                if (currentEntity != null) {
                    final Entity baseVehicle = (Entity) currentEntity;
                    return Optional.<VehicleData>of(VehicleData.setVehicle(vehicle).setBaseVehicle(baseVehicle));
                }
                return Optional.empty();
            }
            return Optional.empty();
        }
        return Optional.empty();
    }

    @Override
    public Optional<VehicleData> fill(DataHolder dataHolder, VehicleData manipulator, MergeFunction overlap) {
        if (supports(dataHolder)) {
            final VehicleData merged = checkNotNull(overlap).merge(checkNotNull(manipulator).copy(), from(dataHolder).get());
            manipulator.set(Keys.BASE_VEHICLE, merged.baseVehicle().get()).set(Keys.VEHICLE, merged.vehicle().get());
            return Optional.of(manipulator);
        }
        return Optional.empty();
    }

    @Override
    public Optional<VehicleData> fill(DataContainer container, VehicleData VehicleData) {
        VehicleData.set(Keys.BASE_VEHICLE, getData(container, Keys.BASE_VEHICLE));
        VehicleData.set(Keys.VEHICLE, getData(container, Keys.VEHICLE));
        return Optional.of(VehicleData);
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, VehicleData manipulator, MergeFunction function) {
        // TODO
        return null;
    }

    @Override
    public Optional<ImmutableVehicleData> with(Key<? extends BaseValue<?>> key, Object value, ImmutableVehicleData immutable) {
        // TODO: Should not return Optional.empty()
        return Optional.empty();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        if (supports(dataHolder)) {
            net.minecraft.entity.Entity entity = ((net.minecraft.entity.Entity) dataHolder);
            if (entity.isRiding()) {
                Entity ridingEntity = (Entity) entity.ridingEntity;
                entity.mountEntity(null);
                return DataTransactionBuilder.successResult(new ImmutableSpongeValue<Entity>(Keys.VEHICLE, ridingEntity));
            }
            return DataTransactionBuilder.builder().result(DataTransactionResult.Type.SUCCESS).build();
        } else {
            return DataTransactionBuilder.builder().result(DataTransactionResult.Type.FAILURE).build();
        }
    }

    @Override
    public Optional<VehicleData> createFrom(DataHolder dataHolder) {
        if (supports(dataHolder)) {
            final Entity vehicle = (Entity) ((net.minecraft.entity.Entity) dataHolder).riddenByEntity;
            net.minecraft.entity.Entity currentEntity = ((net.minecraft.entity.Entity) dataHolder).ridingEntity;
            while (currentEntity != null && currentEntity.isRiding()) {
                currentEntity = currentEntity.ridingEntity;
            }
            final Entity baseVehicle = (Entity) currentEntity;
            return Optional.<VehicleData>of(new SpongeVehicleData(vehicle, baseVehicle));
        }
        return Optional.empty();
    }
}
