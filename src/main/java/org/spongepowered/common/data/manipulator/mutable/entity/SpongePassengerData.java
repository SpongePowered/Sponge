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

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutablePassengerData;
import org.spongepowered.api.data.manipulator.mutable.entity.PassengerData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongePassengerData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.common.data.util.ImplementationRequiredForTest;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.entity.SpongeEntitySnapshotBuilder;

@ImplementationRequiredForTest
public class SpongePassengerData extends AbstractSingleData<EntitySnapshot, PassengerData, ImmutablePassengerData> implements PassengerData {

    public SpongePassengerData() {
        this(new SpongeEntitySnapshotBuilder().type(EntityTypes.UNKNOWN).build());
    }

    public SpongePassengerData(EntitySnapshot passenger) {
        super(PassengerData.class, passenger, Keys.PASSENGER);
    }

    @Override
    public Value<EntitySnapshot> passenger() {
        return new SpongeValue<EntitySnapshot>(Keys.PASSENGER, this.getValue());
    }

    @Override
    protected Value<?> getValueGetter() {
        return this.passenger();
    }

    @Override
    public PassengerData copy() {
        return new SpongePassengerData(this.getValue());
    }

    @Override
    public ImmutablePassengerData asImmutable() {
        return new ImmutableSpongePassengerData(this.getValue());
    }

    @Override
    public int compareTo(PassengerData o) {
        return this.getValue().getUniqueId().get().compareTo(o.passenger().get().getUniqueId().get());
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.PASSENGER, this.getValue());
    }
}
