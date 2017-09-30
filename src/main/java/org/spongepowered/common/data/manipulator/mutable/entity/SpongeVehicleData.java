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
package org.spongepowered.common.data.manipulator.mutable.entity;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableVehicleData;
import org.spongepowered.api.data.manipulator.mutable.entity.VehicleData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeVehicleData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.util.ImplementationRequiredForTest;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import javax.annotation.Nullable;

@ImplementationRequiredForTest
public class SpongeVehicleData extends AbstractData<VehicleData, ImmutableVehicleData> implements VehicleData {

    private @Nullable EntitySnapshot vehicle;
    private @Nullable EntitySnapshot baseVehicle;

    public SpongeVehicleData() {
        this((EntitySnapshot) null, null);
    }

    public SpongeVehicleData(@Nullable EntitySnapshot vehicle, @Nullable EntitySnapshot baseVehicle) {
        super(VehicleData.class);
        this.vehicle = vehicle;
        this.baseVehicle = baseVehicle;
        registerGettersAndSetters();
    }

    public SpongeVehicleData(Entity vehicle, Entity baseVehicle) {
        this(checkNotNull(vehicle, "Vehicle").createSnapshot(), checkNotNull(baseVehicle, "Base vehicle").createSnapshot());
    }

    @Override
    public Value<EntitySnapshot> vehicle() {
        checkState(this.vehicle != null, "Vehicle cannot be null!");
        checkState(this.baseVehicle != null, "Base Vehicle cannot be null!");
        return new SpongeValue<>(Keys.VEHICLE, this.vehicle);
    }

    @Override
    public Value<EntitySnapshot> baseVehicle() {
        checkState(this.vehicle != null, "Vehicle cannot be null!");
        checkState(this.baseVehicle != null, "Base Vehicle cannot be null!");
        return new SpongeValue<>(Keys.BASE_VEHICLE, this.baseVehicle);
    }

    @Override
    public VehicleData copy() {
        checkState(this.vehicle != null, "Vehicle cannot be null!");
        checkState(this.baseVehicle != null, "Base Vehicle cannot be null!");
        return new SpongeVehicleData(this.vehicle, this.baseVehicle);
    }

    @Override
    public ImmutableVehicleData asImmutable() {
        checkState(this.vehicle != null, "Vehicle cannot be null!");
        checkState(this.baseVehicle != null, "Base Vehicle cannot be null!");
        return new ImmutableSpongeVehicleData(this.vehicle, this.baseVehicle);
    }

    @Override
    public DataContainer toContainer() {
        checkState(this.vehicle != null, "Vehicle cannot be null!");
        checkState(this.baseVehicle != null, "Base Vehicle cannot be null!");
        return super.toContainer()
            .set(Keys.VEHICLE, this.vehicle)
            .set(Keys.BASE_VEHICLE, this.baseVehicle);
    }

    public EntitySnapshot getVehicle() {
        return checkNotNull(this.vehicle);
    }

    public SpongeVehicleData setVehicle(EntitySnapshot value) {
        this.vehicle = checkNotNull(value, "Vehicle cannot be null!");
        return this;
    }

    public EntitySnapshot getBaseVehicle() {
        checkState(this.baseVehicle != null, "Base Vehicle cannot be null!");
        return this.baseVehicle;
    }

    public SpongeVehicleData setBaseVehicle(EntitySnapshot value) {
        this.baseVehicle = checkNotNull(value, "Vehicle cannot be null!");
        return this;
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.VEHICLE, SpongeVehicleData.this::getVehicle);
        registerFieldSetter(Keys.VEHICLE, SpongeVehicleData.this::setVehicle);
        registerKeyValue(Keys.VEHICLE, SpongeVehicleData.this::vehicle);

        registerFieldGetter(Keys.BASE_VEHICLE, SpongeVehicleData.this::getBaseVehicle);
        registerFieldSetter(Keys.BASE_VEHICLE, SpongeVehicleData.this::setBaseVehicle);
        registerKeyValue(Keys.BASE_VEHICLE, SpongeVehicleData.this::baseVehicle);
    }

}
