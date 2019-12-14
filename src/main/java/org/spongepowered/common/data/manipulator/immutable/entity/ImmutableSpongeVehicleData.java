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
package org.spongepowered.common.data.manipulator.immutable.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableVehicleData;
import org.spongepowered.api.data.manipulator.mutable.entity.VehicleData;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.value.Value.Immutable;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeVehicleData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

public class ImmutableSpongeVehicleData extends AbstractImmutableData<ImmutableVehicleData, VehicleData> implements ImmutableVehicleData {

    private final EntitySnapshot vehicle;
    private final EntitySnapshot baseVehicle;

    private final Immutable<EntitySnapshot> vehicleValue;
    private final Immutable<EntitySnapshot> baseVehicleValue;

    public ImmutableSpongeVehicleData(EntitySnapshot vehicle, EntitySnapshot baseVehicle) {
        super(ImmutableVehicleData.class);
        this.vehicle = checkNotNull(vehicle, "vehicle");
        this.baseVehicle = checkNotNull(baseVehicle, "baseVehicle");

        this.vehicleValue = new ImmutableSpongeValue<>(Keys.VEHICLE, this.vehicle);
        this.baseVehicleValue = new ImmutableSpongeValue<>(Keys.BASE_VEHICLE, this.baseVehicle);
        this.registerGetters();
    }

    public ImmutableSpongeVehicleData(Entity vehicle, Entity baseVehicle) {
        this(vehicle.createSnapshot(), baseVehicle.createSnapshot());
    }

    @Override
    public Immutable<EntitySnapshot> vehicle() {
        return this.vehicleValue;
    }

    @Override
    public Immutable<EntitySnapshot> baseVehicle() {
        return this.baseVehicleValue;
    }

    @Override
    public VehicleData asMutable() {
        return new SpongeVehicleData(this.vehicle, this.baseVehicle);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.VEHICLE, this.vehicle)
                .set(Keys.BASE_VEHICLE, this.baseVehicle);
    }

    public EntitySnapshot getVehicle() {
        return this.vehicle;
    }

    public EntitySnapshot getBaseVehicle() {
        return this.baseVehicle;
    }

    @Override
    protected void registerGetters() {
        this.registerFieldGetter(Keys.VEHICLE, ImmutableSpongeVehicleData.this::getVehicle);
        this.registerKeyValue(Keys.VEHICLE, ImmutableSpongeVehicleData.this::vehicle);

        this.registerFieldGetter(Keys.BASE_VEHICLE, ImmutableSpongeVehicleData.this::getBaseVehicle);
        this.registerKeyValue(Keys.BASE_VEHICLE, ImmutableSpongeVehicleData.this::baseVehicle);
    }

}
