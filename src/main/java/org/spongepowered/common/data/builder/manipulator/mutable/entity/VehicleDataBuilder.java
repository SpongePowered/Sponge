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
package org.spongepowered.common.data.builder.manipulator.mutable.entity;

import static org.spongepowered.common.data.util.DataUtil.getData;

import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableVehicleData;
import org.spongepowered.api.data.manipulator.mutable.entity.VehicleData;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeVehicleData;

import java.util.Optional;

public class VehicleDataBuilder implements DataManipulatorBuilder<VehicleData, ImmutableVehicleData> {

    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof net.minecraft.entity.Entity;
    }

    @Override
    public VehicleData create() {
        return new SpongeVehicleData();
    }

    @Override
    public Optional<VehicleData> createFrom(DataHolder dataHolder) {
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
                } else {
                    return Optional.empty();
                }
            } else {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<VehicleData> build(DataView container) throws InvalidDataException {
        if (container.contains(Keys.VEHICLE.getQuery())) {
            VehicleData vehicleData = create();
            vehicleData.set(Keys.BASE_VEHICLE, getData(container, Keys.BASE_VEHICLE));
            vehicleData.set(Keys.VEHICLE, getData(container, Keys.VEHICLE));
            return Optional.of(vehicleData);
        }
        return Optional.empty();
    }
}
