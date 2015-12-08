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

import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableVehicleData;
import org.spongepowered.api.data.manipulator.mutable.entity.VehicleData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeVehicleData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.util.ImplementationRequiredForTest;
import org.spongepowered.common.data.value.mutable.common.SpongeEntityValue;

import java.lang.ref.WeakReference;

@ImplementationRequiredForTest
public class SpongeVehicleData extends AbstractData<VehicleData, ImmutableVehicleData> implements VehicleData {

    private WeakReference<Entity> vehicle;
    private WeakReference<Entity> baseVehicle;

    public SpongeVehicleData(Entity vehicle, Entity baseVehicle) {
        super(VehicleData.class);
        if (vehicle != null) {
            this.vehicle = new WeakReference<>(vehicle);
        }
        if (baseVehicle != null) {
            this.baseVehicle = new WeakReference<>(baseVehicle);
        }
        registerGettersAndSetters();
    }

    public SpongeVehicleData() {
        this(null, null);
    }

    @Override
    public Value<Entity> vehicle() {
        checkState(this.vehicle.get() != null);
        return new SpongeEntityValue(Keys.VEHICLE, this.vehicle.get());
    }

    @Override
    public Value<Entity> baseVehicle() {
        checkState(this.vehicle.get() != null);
        checkState(this.baseVehicle.get() != null);
        return new SpongeEntityValue(Keys.BASE_VEHICLE, this.baseVehicle.get());
    }

    @Override
    public VehicleData copy() {
        checkState(this.vehicle.get() != null);
        checkState(this.baseVehicle.get() != null);
        return new SpongeVehicleData(this.vehicle.get(), this.baseVehicle.get());
    }

    @Override
    public ImmutableVehicleData asImmutable() {
        checkState(this.vehicle.get() != null);
        checkState(this.baseVehicle.get() != null);
        return new ImmutableSpongeVehicleData(this.vehicle.get(), this.baseVehicle.get());
    }

    @Override
    public DataContainer toContainer() {
        checkState(this.vehicle.get() != null);
        checkState(this.baseVehicle.get() != null);
        return new MemoryDataContainer()
            .set(Keys.VEHICLE.getQuery(), this.vehicle.get().getUniqueId())
            .set(Keys.BASE_VEHICLE.getQuery(), this.baseVehicle.get().getUniqueId());
    }

    public Entity getVehicle() {
        return checkNotNull(this.vehicle.get());
    }

    public SpongeVehicleData setVehicle(Entity value) {
        this.vehicle = new WeakReference<>(checkNotNull(value));
        return this;
    }

    public Entity getBaseVehicle() {
        checkState(this.baseVehicle.get() != null);
        return this.baseVehicle.get();
    }

    public SpongeVehicleData setBaseVehicle(Entity value) {
        this.baseVehicle = new WeakReference<>(checkNotNull(value));
        return this;
    }

    @Override
    public int compareTo(VehicleData o) {
        return ComparisonChain.start().compare(o.vehicle().get().getUniqueId(), this.vehicle.get().getUniqueId())
                .compare(o.baseVehicle().get().getUniqueId(), this.baseVehicle.get().getUniqueId()).result();
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
