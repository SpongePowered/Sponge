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
package org.spongepowered.common.data.processor.multi.entity;

import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableVehicleData;
import org.spongepowered.api.data.manipulator.mutable.entity.VehicleData;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeVehicleData;
import org.spongepowered.common.data.processor.common.AbstractEntityDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import java.util.Map;
import java.util.Optional;

public class VehicleDataProcessor extends AbstractEntityDataProcessor<net.minecraft.entity.Entity, VehicleData, ImmutableVehicleData> {

    public VehicleDataProcessor() {
        super(net.minecraft.entity.Entity.class);
    }

    @Override
    protected boolean doesDataExist(final net.minecraft.entity.Entity entity) {
        return entity.func_184187_bx() != null;
    }

    @Override
    protected boolean set(final net.minecraft.entity.Entity entity, final Map<Key<?>, Object> keyValues) {
        return ((Entity) entity).setVehicle(((EntitySnapshot) keyValues.get(Keys.VEHICLE)).restore().orElse(null));

    }

    @Override
    protected Map<Key<?>, ?> getValues(final net.minecraft.entity.Entity entity) {
        return ImmutableMap.of(Keys.VEHICLE, ((Entity) entity.func_184187_bx()).createSnapshot(), Keys.BASE_VEHICLE, ((Entity) entity
                .func_184208_bv()).createSnapshot());
    }

    @Override
    public Optional<VehicleData> fill(final DataContainer container, final VehicleData vehicleData) {
        if (!container.contains(Keys.VEHICLE.getQuery(), Keys.BASE_VEHICLE.getQuery())) {
            return Optional.empty();
        }
        final EntitySnapshot vehicle = container.getSerializable(Keys.VEHICLE.getQuery(), EntitySnapshot.class).get();
        final EntitySnapshot baseVehicle = container.getSerializable(Keys.BASE_VEHICLE.getQuery(), EntitySnapshot.class).get();
        return Optional.of(vehicleData.set(Keys.VEHICLE, vehicle).set(Keys.BASE_VEHICLE, baseVehicle));
    }

    @Override
    public DataTransactionResult remove(final DataHolder dataHolder) {
        if (supports(dataHolder)) {
            final net.minecraft.entity.Entity entity = ((net.minecraft.entity.Entity) dataHolder);
            if (entity.func_184187_bx() != null) {
                final EntitySnapshot previousVehicle = ((Entity) entity.func_184187_bx()).createSnapshot();
                entity.func_184210_p();
                return DataTransactionResult.successResult(new ImmutableSpongeValue<>(Keys.VEHICLE, previousVehicle));
            }
            return DataTransactionResult.builder().result(DataTransactionResult.Type.SUCCESS).build();
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    protected VehicleData createManipulator() {
        return new SpongeVehicleData();
    }
}
