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

import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutablePassengerData;
import org.spongepowered.api.data.manipulator.mutable.entity.PassengerData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongePassengerData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.util.GetterFunction;
import org.spongepowered.common.util.SetterFunction;

public class SpongePassengerData extends AbstractData<PassengerData, ImmutablePassengerData> implements PassengerData {

    private Entity vehicle;
    private Entity baseVehicle;

    public SpongePassengerData(Entity vehicle, Entity baseVehicle) {
        super(PassengerData.class);
        this.vehicle = vehicle;
        this.baseVehicle = baseVehicle;
        registerStuff();
    }

    public SpongePassengerData() {
        this(null, null);
    }

    @Override
    public Value<Entity> vehicle() {
        return new SpongeValue<Entity>(Keys.VEHICLE, vehicle);
    }

    @Override
    public Value<Entity> baseVehicle() {
        return new SpongeValue<Entity>(Keys.BASE_VEHICLE, baseVehicle);
    }

    @Override
    public PassengerData copy() {
        return new SpongePassengerData(this.vehicle, this.baseVehicle);
    }

    @Override
    public ImmutablePassengerData asImmutable() {
        return new ImmutableSpongePassengerData(this.vehicle, this.baseVehicle);
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(Keys.VEHICLE, this.vehicle).set(Keys.BASE_VEHICLE, this.baseVehicle);
    }

    public Entity getVehicle() {
        return checkNotNull(this.vehicle);
    }

    public SpongePassengerData setVehicle(Entity value) {
        this.vehicle = checkNotNull(value);
        return this;
    }

    public Entity getBaseVehicle() {
        return checkNotNull(this.baseVehicle);
    }

    public SpongePassengerData setBaseVehicle(Entity value) {
        this.baseVehicle = checkNotNull(value);
        return this;
    }

    @Override
    public int compareTo(PassengerData o) {
        return ComparisonChain.start().compare(o.vehicle().get().getType().getName(), this.vehicle.getType().getName())
                .compare(o.baseVehicle().get().getType().getName(), this.baseVehicle.getType().getName())
                .compare(o.vehicle().get().getUniqueId(), this.vehicle.getUniqueId())
                .compare(o.baseVehicle().get().getUniqueId(), this.baseVehicle.getUniqueId()).result();
    }

    private void registerStuff() {
        registerFieldGetter(Keys.VEHICLE, new GetterFunction<Object>() {

            @Override
            public Object get() {
                return getVehicle();
            }
        });
        registerFieldSetter(Keys.VEHICLE, new SetterFunction<Object>() {

            @Override
            public void set(Object value) {
                setVehicle(((Entity) value));
            }
        });
        registerKeyValue(Keys.VEHICLE, new GetterFunction<Value<?>>() {

            @Override
            public Value<?> get() {
                return vehicle();
            }
        });

        registerFieldGetter(Keys.BASE_VEHICLE, new GetterFunction<Object>() {

            @Override
            public Object get() {
                return getBaseVehicle();
            }
        });
        registerFieldSetter(Keys.BASE_VEHICLE, new SetterFunction<Object>() {

            @Override
            public void set(Object value) {
                setBaseVehicle(((Entity) value));
            }
        });
        registerKeyValue(Keys.BASE_VEHICLE, new GetterFunction<Value<?>>() {

            @Override
            public Value<?> get() {
                return baseVehicle();
            }
        });
    }
}
